#include "skiko_host.h"
#include "kotlin_app_host.h"
#include "macos_window.h"

#include <chrono>
#include <fstream>
#include <iostream>
#include <memory>
#include <string>
#include <string_view>
#include <thread>
#include <vector>

#include <wasi.h>
#include <wasmtime.h>

namespace {

template <typename T, void (*Delete)(T*)>
struct HandleDeleter {
    void operator()(T* value) const {
        if (value != nullptr) {
            Delete(value);
        }
    }
};

template <typename T, void (*Delete)(T*)>
using Handle = std::unique_ptr<T, HandleDeleter<T, Delete>>;

using Engine = Handle<wasm_engine_t, wasm_engine_delete>;
using Store = Handle<wasmtime_store_t, wasmtime_store_delete>;
using Module = Handle<wasmtime_module_t, wasmtime_module_delete>;
using Linker = Handle<wasmtime_linker_t, wasmtime_linker_delete>;

std::string Name(const wasm_name_t* name) {
    if (name == nullptr) {
        return {};
    }
    return std::string(name->data, name->size);
}

std::vector<uint8_t> ReadFile(const std::string& path) {
    std::ifstream input(path, std::ios::binary | std::ios::ate);
    if (!input) {
        throw std::runtime_error("Cannot open " + path);
    }

    const std::streamsize size = input.tellg();
    input.seekg(0, std::ios::beg);
    std::vector<uint8_t> bytes(static_cast<size_t>(size));
    if (!input.read(
            reinterpret_cast<char*>(bytes.data()),
            size)) {
        throw std::runtime_error("Cannot read " + path);
    }
    return bytes;
}

std::string ErrorMessage(wasmtime_error_t* error) {
    wasm_byte_vec_t message;
    wasmtime_error_message(error, &message);
    std::string result(message.data, message.size);
    wasm_byte_vec_delete(&message);
    wasmtime_error_delete(error);
    return result;
}

std::string TrapMessage(wasm_trap_t* trap) {
    wasm_byte_vec_t message;
    wasm_trap_message(trap, &message);
    std::string result(message.data, message.size);
    wasm_byte_vec_delete(&message);
    wasm_trap_delete(trap);
    return result;
}

void Check(wasmtime_error_t* error, std::string_view operation) {
    if (error != nullptr) {
        throw std::runtime_error(
            std::string(operation) + ": " + ErrorMessage(error)
        );
    }
}

wasm_trap_t* UnsupportedImport(
    void* environment,
    wasmtime_caller_t*,
    wasmtime_val_raw_t*,
    size_t
) {
    const auto* name = static_cast<const std::string*>(environment);
    return SkikoHost::Trap(
        *name + " is not implemented by the standalone Wasmtime runner"
    );
}

wasm_trap_t* SetJmp(
    void*,
    wasmtime_caller_t*,
    wasmtime_val_raw_t* slots,
    size_t
) {
    slots[0].i32 = 0;
    return nullptr;
}

void DeleteCallbackName(void* value) {
    delete static_cast<std::string*>(value);
}

struct ImportRegistration {
    size_t ordinary_gl = 0;
    size_t special_gl = 0;
};

ImportRegistration RegisterEnvironmentImports(
    wasmtime_linker_t* linker,
    const wasmtime_module_t* module,
    SkikoHost* host
) {
    ImportRegistration registration;
    wasm_importtype_vec_t imports;
    wasmtime_module_imports(module, &imports);

    for (size_t index = 0; index < imports.size; ++index) {
        const wasm_importtype_t* import = imports.data[index];
        const std::string module_name = Name(wasm_importtype_module(import));
        const std::string field_name = Name(wasm_importtype_name(import));

        if (module_name != "env") {
            continue;
        }

        const wasm_externtype_t* external_type =
            wasm_importtype_type(import);
        const wasm_functype_t* function_type =
            wasm_externtype_as_functype_const(external_type);
        if (function_type == nullptr) {
            wasm_importtype_vec_delete(&imports);
            throw std::runtime_error(
                module_name + "." + field_name + " is not a function"
            );
        }

        GLCallback callback = nullptr;
        void* environment = host;
        void (*finalizer)(void*) = nullptr;

        if (const GLBinding* binding =
                FindGeneratedGLBinding(field_name)) {
            callback = binding->callback;
            ++registration.ordinary_gl;
        } else if (const GLBinding* binding =
                       FindSpecialGLBinding(field_name)) {
            callback = binding->callback;
            ++registration.special_gl;
        } else if (field_name == "setjmp") {
            callback = SetJmp;
            environment = nullptr;
        } else if (field_name.starts_with("gl")) {
            wasm_importtype_vec_delete(&imports);
            throw std::runtime_error(
                "No GL binding exists for env." + field_name
            );
        } else {
            auto* name = new std::string(module_name + "." + field_name);
            callback = UnsupportedImport;
            environment = name;
            finalizer = DeleteCallbackName;
        }

        wasmtime_error_t* error =
            wasmtime_linker_define_func_unchecked(
                linker,
                module_name.data(),
                module_name.size(),
                field_name.data(),
                field_name.size(),
                function_type,
                callback,
                environment,
                finalizer
            );
        if (error != nullptr) {
            if (finalizer != nullptr) {
                finalizer(environment);
            }
            wasm_importtype_vec_delete(&imports);
            throw std::runtime_error(
                "Defining " + module_name + "." + field_name + ": " +
                ErrorMessage(error)
            );
        }
    }

    wasm_importtype_vec_delete(&imports);
    return registration;
}

void PrintImports(const wasmtime_module_t* module) {
    wasm_importtype_vec_t imports;
    wasmtime_module_imports(module, &imports);
    for (size_t index = 0; index < imports.size; ++index) {
        const wasm_importtype_t* import = imports.data[index];
        std::cout << Name(wasm_importtype_module(import)) << "."
                  << Name(wasm_importtype_name(import)) << "\n";
    }
    std::cout << imports.size << " imports\n";
    wasm_importtype_vec_delete(&imports);
}

void CallInitializer(
    wasmtime_context_t* context,
    const wasmtime_instance_t& instance
) {
    constexpr const char* candidates[] = {
        "_initialize",
        "__wasm_call_ctors",
    };

    for (const char* name : candidates) {
        wasmtime_extern_t item;
        if (!wasmtime_instance_export_get(
                context,
                &instance,
                name,
                std::char_traits<char>::length(name),
                &item) ||
            item.kind != WASMTIME_EXTERN_FUNC) {
            continue;
        }

        wasm_trap_t* trap = nullptr;
        wasmtime_error_t* error = wasmtime_func_call(
            context,
            &item.of.func,
            nullptr,
            0,
            nullptr,
            0,
            &trap
        );
        if (error != nullptr) {
            throw std::runtime_error(
                std::string("Calling ") + name + ": " +
                ErrorMessage(error)
            );
        }
        if (trap != nullptr) {
            throw std::runtime_error(
                std::string("Calling ") + name + ": " +
                TrapMessage(trap)
            );
        }

        std::cout << "Called " << name << "\n";
        return;
    }

    std::cout << "No reactor initializer export found\n";
}

} // namespace

int main(int argc, char** argv) {
    if (argc < 2) {
        std::cerr
            << "usage: skiko-wasmtime <skiko.wasm> [application.wasm] "
               "[--list-imports]\n";
        return 2;
    }

    try {
        bool list_only = false;
        std::string application_path;
        for (int index = 2; index < argc; ++index) {
            const std::string_view argument(argv[index]);
            if (argument == "--list-imports") {
                list_only = true;
            } else if (application_path.empty()) {
                application_path = argument;
            } else {
                throw std::runtime_error("Unexpected argument: " + std::string(argument));
            }
        }
        const std::vector<uint8_t> wasm = ReadFile(argv[1]);

        wasm_config_t* config = wasm_config_new();
        if (config == nullptr) {
            throw std::runtime_error("wasm_config_new failed");
        }
        wasmtime_config_wasm_gc_set(config, true);
        wasmtime_config_wasm_function_references_set(config, true);
        wasmtime_config_wasm_exceptions_set(config, true);
        Engine engine(wasm_engine_new_with_config(config));
        if (!engine) {
            throw std::runtime_error("wasm_engine_new failed");
        }

        wasmtime_module_t* module_value = nullptr;
        Check(
            wasmtime_module_new(
                engine.get(),
                wasm.data(),
                wasm.size(),
                &module_value
            ),
            "Compiling skiko.wasm"
        );
        Module module(module_value);

        if (list_only) {
            PrintImports(module.get());
            return 0;
        }

        SkikoHost host;
        std::string gl_error;
        if (!host.CreateOffscreenContext(600, 600, gl_error)) {
            throw std::runtime_error(gl_error);
        }

        Store store(wasmtime_store_new(engine.get(), &host, nullptr));
        if (!store) {
            throw std::runtime_error("wasmtime_store_new failed");
        }
        wasmtime_context_t* context = wasmtime_store_context(store.get());

        wasi_config_t* wasi = wasi_config_new();
        if (wasi == nullptr) {
            throw std::runtime_error("wasi_config_new failed");
        }
        wasi_config_inherit_stdout(wasi);
        wasi_config_inherit_stderr(wasi);
        Check(wasmtime_context_set_wasi(context, wasi), "Configuring WASI");

        Linker linker(wasmtime_linker_new(engine.get()));
        if (!linker) {
            throw std::runtime_error("wasmtime_linker_new failed");
        }
        Check(wasmtime_linker_define_wasi(linker.get()), "Defining WASI");
        const ImportRegistration registration =
            RegisterEnvironmentImports(linker.get(), module.get(), &host);
        std::cout
            << "Registered " << registration.ordinary_gl
            << " ordinary and " << registration.special_gl
            << " special GL imports\n";

        wasmtime_instance_t instance;
        wasm_trap_t* trap = nullptr;
        wasmtime_error_t* instantiate_error =
            wasmtime_linker_instantiate(
                linker.get(),
                context,
                module.get(),
                &instance,
                &trap
            );
        if (instantiate_error != nullptr) {
            throw std::runtime_error(
                "Instantiating skiko.wasm: " +
                ErrorMessage(instantiate_error)
            );
        }
        if (trap != nullptr) {
            throw std::runtime_error(
                "Instantiating skiko.wasm: " + TrapMessage(trap)
            );
        }

        std::string runtime_error;
        if (!host.AttachRuntime(context, instance, runtime_error)) {
            throw std::runtime_error(runtime_error);
        }

        CallInitializer(context, instance);

        if (!application_path.empty()) {
            const std::vector<uint8_t> application_wasm =
                ReadFile(application_path);
            wasmtime_module_t* application_module_value = nullptr;
            Check(
                wasmtime_module_new(
                    engine.get(),
                    application_wasm.data(),
                    application_wasm.size(),
                    &application_module_value
                ),
                "Compiling Kotlin application"
            );
            Module application_module(application_module_value);

            Check(
                wasmtime_linker_define_instance(
                    linker.get(),
                    context,
                    "./skiko.mjs",
                    sizeof("./skiko.mjs") - 1,
                    &instance
                ),
                "Linking skiko.wasm exports to the Kotlin application"
            );

            KotlinAppHost application(context, &host);
            application.RegisterImports(linker.get(), application_module.get());

            wasmtime_instance_t application_instance;
            trap = nullptr;
            wasmtime_error_t* application_error =
                wasmtime_linker_instantiate(
                    linker.get(),
                    context,
                    application_module.get(),
                    &application_instance,
                    &trap
                );
            if (application_error != nullptr) {
                throw std::runtime_error(
                    "Instantiating Kotlin application: " +
                    ErrorMessage(application_error)
                );
            }
            if (trap != nullptr) {
                throw std::runtime_error(
                    "Instantiating Kotlin application: " + TrapMessage(trap)
                );
            }

            application.AttachInstance(application_instance);
            application.Start();

            constexpr int window_width = 606;
            constexpr int window_height = 706;
            MacOSWindow window(
                window_width, window_height, application.Title());
            std::cout << "Skiko Wasmtime demo running. "
                         "Close the window to exit.\n";

            const auto start = std::chrono::steady_clock::now();
            auto next_frame = start;
            while (window.IsOpen()) {
                window.PollEvents();
                if (!window.IsOpen()) break;

                const auto now = std::chrono::steady_clock::now();
                const double timestamp =
                    std::chrono::duration<double, std::milli>(now - start)
                        .count();
                application.RunContinuousFrame(timestamp);
                window.SetTitle(application.Title());
                window.Present(application.CompositeFrame());

                next_frame += std::chrono::microseconds(16667);
                if (next_frame > std::chrono::steady_clock::now()) {
                    std::this_thread::sleep_until(next_frame);
                } else {
                    next_frame = std::chrono::steady_clock::now();
                }
            }
        }

        std::cout
            << "skiko.wasm instantiated with Wasmtime; "
            << "the Node runner remains unchanged\n";
        return 0;
    } catch (const std::exception& exception) {
        std::cerr << "skiko-wasmtime: " << exception.what() << "\n";
        return 1;
    }
}
