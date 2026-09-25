#include "kotlin_app_host.h"

#include "skiko_host.h"

#include <algorithm>
#include <cmath>
#include <cstdlib>
#include <cstring>
#include <iostream>
#include <random>
#include <stdexcept>
#include <utility>

#include <wasmtime/anyref.h>
#include <wasmtime/arrayref.h>
#include <wasmtime/externref.h>
#include <wasmtime/global.h>
#include <wasmtime/tag.h>
#include <wasmtime/types/val.h>

namespace {

std::string Name(const wasm_name_t* name) {
    return name == nullptr ? std::string() : std::string(name->data, name->size);
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

void Check(wasmtime_error_t* error, const std::string& operation) {
    if (error != nullptr) {
        throw std::runtime_error(operation + ": " + ErrorMessage(error));
    }
}

enum class ObjectKind {
    Generic,
    Error,
    Promise,
    Document,
    Description,
    Canvas,
    Gl,
    GlContext,
    HtmlCanvasClass,
    WebGlClass,
    FinalizationRegistry,
    ContextAttributes,
};

} // namespace

struct KotlinAppHost::Callback {
    enum class Signature { JsToJs, JsToUnit, DoubleToUnit };
    Signature signature = Signature::JsToJs;
    wasmtime_anyref_t function{};

    ~Callback() {
        if (!wasmtime_anyref_is_null(&function)) {
            wasmtime_anyref_unroot(&function);
        }
    }
};

struct KotlinAppHost::HostValue {
    enum class Kind { String, Number, Boolean, Object, Callback };
    Kind kind = Kind::Object;
    std::string string;
    double number = 0;
    bool boolean = false;
    ObjectKind object = ObjectKind::Generic;
    int id = 0;
    std::shared_ptr<Callback> callback;

    static HostValue* String(std::string value) {
        auto* result = new HostValue;
        result->kind = Kind::String;
        result->string = std::move(value);
        return result;
    }
    static HostValue* Number(double value) {
        auto* result = new HostValue;
        result->kind = Kind::Number;
        result->number = value;
        return result;
    }
    static HostValue* Object(ObjectKind kind, int id = 0) {
        auto* result = new HostValue;
        result->object = kind;
        result->id = id;
        return result;
    }
    static HostValue* CallbackValue(std::shared_ptr<Callback> value) {
        auto* result = new HostValue;
        result->kind = Kind::Callback;
        result->callback = std::move(value);
        return result;
    }
};

struct KotlinAppHost::Binding {
    KotlinAppHost* host;
    std::string name;
};

KotlinAppHost::KotlinAppHost(wasmtime_context_t* context, SkikoHost* skiko)
    : context_(context), skiko_(skiko) {}

KotlinAppHost::~KotlinAppHost() = default;

void KotlinAppHost::DeleteHostValue(void* value) {
    delete static_cast<HostValue*>(value);
}

void KotlinAppHost::DeleteBinding(void* value) {
    delete static_cast<Binding*>(value);
}

void KotlinAppHost::SetExternResult(
    wasmtime_val_t* result,
    HostValue* value
) {
    result->kind = WASMTIME_EXTERNREF;
    if (!wasmtime_externref_new(context_, value, DeleteHostValue,
                                &result->of.externref)) {
        delete value;
        throw std::runtime_error("Could not allocate an externref");
    }
}

KotlinAppHost::HostValue* KotlinAppHost::GetHostValue(
    const wasmtime_val_t& value
) const {
    if (value.kind != WASMTIME_EXTERNREF ||
        wasmtime_externref_is_null(&value.of.externref)) {
        return nullptr;
    }
    return static_cast<HostValue*>(
        wasmtime_externref_data(context_, &value.of.externref));
}

std::string KotlinAppHost::GetString(const wasmtime_val_t& value) const {
    HostValue* host = GetHostValue(value);
    if (host == nullptr) return "null";
    if (host->kind == HostValue::Kind::String) return host->string;
    if (host->kind == HostValue::Kind::Number) {
        return std::to_string(host->number);
    }
    if (host->kind == HostValue::Kind::Boolean) {
        return host->boolean ? "true" : "false";
    }
    if (!host->string.empty()) return host->string;
    return "[object Object]";
}

wasm_trap_t* KotlinAppHost::Dispatch(
    void* environment,
    wasmtime_caller_t* caller,
    const wasmtime_val_t* args,
    size_t nargs,
    wasmtime_val_t* results,
    size_t nresults
) {
    auto* binding = static_cast<Binding*>(environment);
    if (std::getenv("SKIKO_WASMTIME_TRACE") != nullptr) {
        std::cerr << "[Kotlin host] " << binding->name << '\n';
    }
    try {
        return binding->host->CallHost(
            binding->name, caller, args, nargs, results, nresults);
    } catch (const std::exception& exception) {
        return SkikoHost::Trap(binding->name + ": " + exception.what());
    }
}

void KotlinAppHost::RegisterImports(
    wasmtime_linker_t* linker,
    const wasmtime_module_t* application_module
) {
    wasm_importtype_vec_t imports;
    wasmtime_module_imports(application_module, &imports);

    size_t functions = 0;
    size_t constants = 0;
    size_t tags = 0;
    for (size_t index = 0; index < imports.size; ++index) {
        const wasm_importtype_t* import = imports.data[index];
        const std::string module = Name(wasm_importtype_module(import));
        const std::string field = Name(wasm_importtype_name(import));
        const wasm_externtype_t* external = wasm_importtype_type(import);

        if (module == "./skiko.mjs") continue;

        if (module == "'") {
            const wasm_globaltype_t* type =
                wasm_externtype_as_globaltype_const(external);
            if (type == nullptr) {
                wasm_importtype_vec_delete(&imports);
                throw std::runtime_error("String constant import is not global");
            }
            wasmtime_val_t value{};
            SetExternResult(&value, HostValue::String(field));
            wasmtime_global_t global;
            Check(
                wasmtime_global_new(context_, type, &value, &global),
                "Creating string constant"
            );
            wasmtime_val_unroot(&value);
            wasmtime_extern_t item{};
            item.kind = WASMTIME_EXTERN_GLOBAL;
            item.of.global = global;
            Check(
                wasmtime_linker_define(
                    linker, context_, module.data(), module.size(),
                    field.data(), field.size(), &item),
                "Defining string constant"
            );
            ++constants;
            continue;
        }

        if (module == "intrinsics") {
            const wasm_tagtype_t* type = wasm_externtype_as_tagtype_const(external);
            if (type == nullptr) {
                wasm_importtype_vec_delete(&imports);
                throw std::runtime_error("intrinsics.tag is not a tag");
            }
            wasmtime_tag_t tag;
            Check(wasmtime_tag_new(context_, type, &tag), "Creating JS tag");
            wasmtime_extern_t item{};
            item.kind = WASMTIME_EXTERN_TAG;
            item.of.tag = tag;
            Check(
                wasmtime_linker_define(
                    linker, context_, module.data(), module.size(),
                    field.data(), field.size(), &item),
                "Defining JS tag"
            );
            ++tags;
            continue;
        }

        const wasm_functype_t* function =
            wasm_externtype_as_functype_const(external);
        if (function == nullptr) {
            wasm_importtype_vec_delete(&imports);
            throw std::runtime_error(
                "Unsupported application import " + module + "." + field);
        }
        auto* binding = new Binding{this, module + "." + field};
        wasmtime_error_t* error = wasmtime_linker_define_func(
            linker,
            module.data(), module.size(),
            field.data(), field.size(),
            function,
            Dispatch,
            binding,
            DeleteBinding
        );
        if (error != nullptr) {
            delete binding;
            wasm_importtype_vec_delete(&imports);
            throw std::runtime_error(ErrorMessage(error));
        }
        ++functions;
    }
    wasm_importtype_vec_delete(&imports);
    std::cout << "Registered " << functions << " Kotlin host functions, "
              << constants << " string constants, and " << tags << " tag\n";
}

void KotlinAppHost::AttachInstance(const wasmtime_instance_t& instance) {
    instance_ = instance;
    attached_ = true;
}

void KotlinAppHost::Start() {
    if (!attached_) throw std::runtime_error("Application instance is not attached");
    wasmtime_extern_t item;
    if (!wasmtime_instance_export_get(
            context_, &instance_, "_start", sizeof("_start") - 1, &item) ||
        item.kind != WASMTIME_EXTERN_FUNC) {
        throw std::runtime_error("Application does not export _start");
    }
    std::cout << "Kotlin host bridge revision 3\n";
    wasm_trap_t* trap = nullptr;
    wasmtime_error_t* error = wasmtime_func_call(
        context_, &item.of.func, nullptr, 0, nullptr, 0, &trap);
    if (error != nullptr) throw std::runtime_error(ErrorMessage(error));
    if (trap != nullptr) throw std::runtime_error(TrapMessage(trap));
    DrainReadyCallbacks();
}

void KotlinAppHost::Invoke(
    const std::shared_ptr<Callback>& callback,
    double timestamp
) {
    const char* export_name = nullptr;
    size_t argument_count = 1;
    if (callback->signature == Callback::Signature::JsToJs) {
        export_name = "__callFunction_((Js?)->Js?)";
        argument_count = 2;
    } else if (callback->signature == Callback::Signature::JsToUnit) {
        export_name = "__callFunction_((Js)->Unit)";
        argument_count = 2;
    } else {
        export_name = "__callFunction_((Double)->Unit)";
        argument_count = 2;
    }

    wasmtime_extern_t item;
    if (!wasmtime_instance_export_get(
            context_, &instance_, export_name, std::strlen(export_name), &item) ||
        item.kind != WASMTIME_EXTERN_FUNC) {
        throw std::runtime_error(std::string("Missing callback export ") + export_name);
    }

    wasmtime_val_t args[2]{};
    args[0].kind = WASMTIME_ANYREF;
    wasmtime_anyref_clone(&callback->function, &args[0].of.anyref);
    if (callback->signature == Callback::Signature::DoubleToUnit) {
        args[1].kind = WASMTIME_F64;
        args[1].of.f64 = timestamp;
    } else {
        // Promise.then receives the value with which awaitSkiko resolved.
        // The generated Kotlin callback models it as a non-null JsAny even
        // though it does not inspect the value.
        SetExternResult(&args[1], HostValue::Object(ObjectKind::Generic));
    }
    if (std::getenv("SKIKO_WASMTIME_TRACE") != nullptr) {
        std::cerr
            << "[Kotlin callback] export=" << export_name
            << " closure.store=" << args[0].of.anyref.store_id;
        if (args[1].kind == WASMTIME_EXTERNREF) {
            std::cerr << " value.store=" << args[1].of.externref.store_id;
        } else {
            std::cerr << " value.f64=" << args[1].of.f64;
        }
        std::cerr << '\n';
    }

    wasmtime_val_t result{};
    const size_t result_count =
        callback->signature == Callback::Signature::JsToJs ? 1 : 0;
    wasm_trap_t* trap = nullptr;
    wasmtime_error_t* error = wasmtime_func_call(
        context_, &item.of.func, args, argument_count,
        result_count ? &result : nullptr, result_count, &trap);
    wasmtime_val_unroot(&args[0]);
    if (callback->signature != Callback::Signature::DoubleToUnit) {
        wasmtime_val_unroot(&args[1]);
    }
    if (error != nullptr) throw std::runtime_error(ErrorMessage(error));
    if (trap != nullptr) throw std::runtime_error(TrapMessage(trap));
    if (result_count != 0) wasmtime_val_unroot(&result);
}

void KotlinAppHost::DrainReadyCallbacks() {
    while (!ready_callbacks_.empty()) {
        auto callback = ready_callbacks_.front();
        ready_callbacks_.pop_front();
        Invoke(callback, 0);
    }
}

void KotlinAppHost::RunContinuousFrame(double timestamp_millis) {
    // Honor callbacks explicitly scheduled since the previous frame. If the
    // application schedules only its initial frame (as this sample does), keep
    // driving that callback set to provide a native continuous animation loop.
    if (!animation_callbacks_.empty()) {
        continuous_callbacks_.assign(
            animation_callbacks_.begin(), animation_callbacks_.end());
        animation_callbacks_.clear();
    }
    for (const auto& callback : continuous_callbacks_) {
        Invoke(callback, timestamp_millis);
    }
}

std::vector<uint8_t> KotlinAppHost::CompositeFrame() {
    constexpr int output_width = 606;
    constexpr int output_height = 706;
    std::vector<uint8_t> output(
        static_cast<size_t>(output_width * output_height * 4), 255);

    struct Canvas { int id, width, height, x, y, display_width, display_height; };
    const Canvas canvases[] = {
        {1, 600, 600, 0, 0, 300, 300},
        {2, 600, 600, 306, 0, 300, 300},
        {3, 1212, 800, 0, 306, 606, 400},
    };
    const bool report_diagnostics = !diagnostics_reported_ &&
        std::getenv("SKIKO_WASMTIME_GL_DIAGNOSTICS") != nullptr;
    for (const Canvas& canvas : canvases) {
        std::vector<uint8_t> source;
        std::string error;
        if (!skiko_->ReadContextPixels(canvas.id, source, error)) {
            throw std::runtime_error(error);
        }
        if (report_diagnostics) {
            skiko_->PrintGLDiagnostics(canvas.id);
            size_t white_pixels = 0;
            size_t transparent_pixels = 0;
            size_t colored_pixels = 0;
            for (size_t offset = 0; offset < source.size(); offset += 4) {
                const bool white = source[offset] == 255 &&
                    source[offset + 1] == 255 && source[offset + 2] == 255;
                white_pixels += white ? 1 : 0;
                transparent_pixels += source[offset + 3] == 0 ? 1 : 0;
                colored_pixels += white ? 0 : 1;
            }
            std::cerr << "[canvas " << canvas.id << "] white=" << white_pixels
                      << " nonWhite=" << colored_pixels
                      << " alphaZero=" << transparent_pixels << "\n";
        }
        for (int y = 0; y < canvas.display_height; ++y) {
            for (int x = 0; x < canvas.display_width; ++x) {
                const bool border = y == 0 || x == 0 ||
                    y == canvas.display_height - 1 ||
                    x == canvas.display_width - 1;
                const size_t destination = static_cast<size_t>(
                    ((canvas.y + y) * output_width + canvas.x + x) * 4);
                if (border) {
                    output[destination] = output[destination + 1] =
                        output[destination + 2] = 0;
                    continue;
                }
                const int source_x = x * canvas.width / canvas.display_width;
                const int source_y = canvas.height - 1 -
                    y * canvas.height / canvas.display_height;
                const size_t source_offset = static_cast<size_t>(
                    (source_y * canvas.width + source_x) * 4);
                std::copy_n(source.data() + source_offset, 4,
                            output.data() + destination);
            }
        }
    }
    diagnostics_reported_ = diagnostics_reported_ || report_diagnostics;
    return output;
}

const std::string& KotlinAppHost::Title() const {
    return title_;
}

wasm_trap_t* KotlinAppHost::CallHost(
    const std::string& qualified_name,
    wasmtime_caller_t*,
    const wasmtime_val_t* args,
    size_t nargs,
    wasmtime_val_t* results,
    size_t nresults
) {
    const size_t dot = qualified_name.find('.');
    const std::string name = dot == std::string::npos
        ? qualified_name : qualified_name.substr(dot + 1);

    auto string_result = [&](std::string value) {
        SetExternResult(&results[0], HostValue::String(std::move(value)));
    };
    auto object_result = [&](ObjectKind kind, int id = 0) {
        SetExternResult(&results[0], HostValue::Object(kind, id));
    };
    auto null_result = [&] {
        results[0].kind = WASMTIME_EXTERNREF;
        wasmtime_externref_set_null(&results[0].of.externref);
    };

    if (qualified_name == "wasm:js-string.length") {
        const std::string value = GetString(args[0]);
        results[0].kind = WASMTIME_I32;
        results[0].of.i32 = static_cast<int32_t>(value.size());
        if (std::getenv("SKIKO_WASMTIME_TRACE") != nullptr &&
            value.find("[a-z0-9]") != std::string::npos) {
            std::cerr << "[target string] length=" << value.size()
                      << " value=" << value << '\n';
        }
    } else if (qualified_name == "wasm:js-string.concat") {
        string_result(GetString(args[0]) + GetString(args[1]));
    } else if (qualified_name == "wasm:js-string.charCodeAt") {
        const std::string value = GetString(args[0]);
        const size_t index = static_cast<uint32_t>(args[1].of.i32);
        results[0].kind = WASMTIME_I32;
        results[0].of.i32 = index < value.size()
            ? static_cast<unsigned char>(value[index]) : 0;
        if (std::getenv("SKIKO_WASMTIME_TRACE") != nullptr &&
            value.find("[a-z0-9]") != std::string::npos) {
            std::cerr << "[target string] charCodeAt(" << index << ")="
                      << results[0].of.i32 << '\n';
        }
    } else if (qualified_name == "wasm:js-string.substring") {
        const std::string value = GetString(args[0]);
        const size_t begin = std::min<size_t>(
            static_cast<uint32_t>(args[1].of.i32), value.size());
        const size_t end = std::min<size_t>(
            static_cast<uint32_t>(args[2].of.i32), value.size());
        string_result(value.substr(std::min(begin, end),
                                   std::max(begin, end) - std::min(begin, end)));
    } else if (qualified_name == "wasm:js-string.compare" ||
               qualified_name == "wasm:js-string.equals") {
        const std::string left = GetString(args[0]);
        const std::string right = GetString(args[1]);
        results[0].kind = WASMTIME_I32;
        results[0].of.i32 = qualified_name.ends_with("equals")
            ? (left == right)
            : (left == right ? 0 : (left < right ? -1 : 1));
    } else if (qualified_name == "wasm:js-string.fromCharCodeArray") {
        wasmtime_arrayref_t array;
        if (!wasmtime_anyref_as_array(context_, &args[0].of.anyref, &array)) {
            return SkikoHost::Trap("fromCharCodeArray expected an array");
        }
        std::string value;
        for (uint32_t index = static_cast<uint32_t>(args[1].of.i32);
             index < static_cast<uint32_t>(args[2].of.i32); ++index) {
            wasmtime_val_t element;
            if (wasmtime_error_t* error =
                    wasmtime_arrayref_get(context_, &array, index, &element)) {
                wasmtime_arrayref_unroot(&array);
                throw std::runtime_error(
                    "Reading UTF-16 character array: " + ErrorMessage(error));
            }
            value.push_back(static_cast<char>(element.of.i32));
            wasmtime_val_unroot(&element);
        }
        wasmtime_arrayref_unroot(&array);
        string_result(std::move(value));
    } else if (qualified_name == "wasm:js-string.intoCharCodeArray") {
        const std::string value = GetString(args[0]);
        wasmtime_arrayref_t array;
        if (!wasmtime_anyref_as_array(context_, &args[1].of.anyref, &array)) {
            return SkikoHost::Trap("intoCharCodeArray expected an array");
        }
        uint32_t index = static_cast<uint32_t>(args[2].of.i32);
        for (unsigned char character : value) {
            wasmtime_val_t element{};
            element.kind = WASMTIME_I32;
            element.of.i32 = character;
            if (wasmtime_error_t* error =
                    wasmtime_arrayref_set(context_, &array, index++, &element)) {
                wasmtime_arrayref_unroot(&array);
                throw std::runtime_error(
                    "Writing UTF-16 character array: " + ErrorMessage(error));
            }
        }
        wasmtime_arrayref_unroot(&array);
        results[0].kind = WASMTIME_I32;
        results[0].of.i32 = static_cast<int32_t>(value.size());
    } else if (name == "kotlin.wasm.internal.getJsEmptyString" ||
               name == "kotlin.js.stackPlaceHolder_js_code") {
        string_result("");
    } else if (name == "kotlin.wasm.internal.externrefToInt") {
        HostValue* value = GetHostValue(args[0]);
        results[0].kind = WASMTIME_I32;
        results[0].of.i32 = value && value->kind == HostValue::Kind::Number
            ? static_cast<int32_t>(value->number) : 0;
    } else if (name == "kotlin.wasm.internal.externrefToString" ||
               name == "kotlin.wasm.internal.itoa32_$external_fun" ||
               name == "kotlin.wasm.internal.utoa32_$external_fun") {
        if (args[0].kind == WASMTIME_I32) string_result(std::to_string(args[0].of.i32));
        else string_result(GetString(args[0]));
    } else if (name == "kotlin.wasm.internal.externrefEquals") {
        results[0].kind = WASMTIME_I32;
        results[0].of.i32 = GetString(args[0]) == GetString(args[1]);
    } else if (name == "kotlin.wasm.internal.externrefHashCode") {
        results[0].kind = WASMTIME_I32;
        uint32_t hash = 0;
        for (unsigned char c : GetString(args[0])) hash = hash * 31 + c;
        results[0].of.i32 = static_cast<int32_t>(hash);
    } else if (name == "kotlin.wasm.internal.isNullish") {
        results[0].kind = WASMTIME_I32;
        results[0].of.i32 = wasmtime_externref_is_null(&args[0].of.externref);
        if (std::getenv("SKIKO_WASMTIME_TRACE") != nullptr) {
            std::cerr
                << "[isNullish] kind=" << static_cast<int>(args[0].kind)
                << " store=" << args[0].of.externref.store_id
                << " result=" << results[0].of.i32 << '\n';
        }
    } else if (name == "kotlin.wasm.internal.kotlinUIntToJsNumberUnsafe") {
        SetExternResult(&results[0], HostValue::Number(
            static_cast<uint32_t>(args[0].of.i32)));
    } else if (name == "kotlin.wasm.internal.getCachedJsObject_$external_fun") {
        wasmtime_val_clone(&args[1], &results[0]);
    } else if (name == "kotlin.io.printlnImpl") {
        std::cout << GetString(args[0]) << '\n';
    } else if (name == "kotlin.random.initialSeed") {
        results[0].kind = WASMTIME_I32;
        results[0].of.i32 = 0x534b494b;
    } else if (name.find("__convertKotlinClosureToJsClosure_") != std::string::npos) {
        auto callback = std::make_shared<Callback>();
        if (name.find("Double") != std::string::npos) {
            callback->signature = Callback::Signature::DoubleToUnit;
        } else if (name.find("->Unit") != std::string::npos) {
            callback->signature = Callback::Signature::JsToUnit;
        }
        wasmtime_anyref_clone(&args[0].of.anyref, &callback->function);
        SetExternResult(&results[0], HostValue::CallbackValue(std::move(callback)));
    } else if (name == "kotlin.js.then_$external_fun") {
        HostValue* callback = nargs > 1 ? GetHostValue(args[1]) : nullptr;
        if (callback && callback->callback) ready_callbacks_.push_back(callback->callback);
        // JavaScript Promise.then always returns a new Promise. Kotlin models
        // that result as non-null even when the caller ignores it.
        object_result(ObjectKind::Promise);
    } else if (name == "org.jetbrains.skiko.wasm.awaitSkiko_$external_prop_getter") {
        object_result(ObjectKind::Promise);
    } else if (name == "kotlinx.browser.document_$external_prop_getter") {
        object_result(ObjectKind::Document);
    } else if (name == "org.w3c.dom.getElementById_$external_fun") {
        const std::string id = GetString(args[1]);
        if (id == "description") object_result(ObjectKind::Description);
        else if (id.size() == 2 && id[0] == 'c' && id[1] >= '1' && id[1] <= '3')
            object_result(ObjectKind::Canvas, id[1] - '0');
        else null_result();
    } else if (name == "org.w3c.dom.title_$external_prop_setter" ||
               name == "org.w3c.dom.innerHTML_$external_prop_setter") {
        title_ = GetString(args[1]);
    } else if (name == "org.w3c.dom.width_$external_prop_getter" ||
               name == "org.w3c.dom.height_$external_prop_getter") {
        HostValue* canvas = GetHostValue(args[0]);
        const bool width = name.find("width_") != std::string::npos;
        results[0].kind = WASMTIME_I32;
        if (!canvas || canvas->object != ObjectKind::Canvas) results[0].of.i32 = 0;
        else if (canvas->id == 3) results[0].of.i32 = width ? 1212 : 800;
        else results[0].of.i32 = 600;
    } else if (name == "org.w3c.dom.HTMLCanvasElement_$external_class_instanceof") {
        HostValue* value = GetHostValue(args[0]);
        results[0].kind = WASMTIME_I32;
        results[0].of.i32 = value && value->object == ObjectKind::Canvas;
    } else if (name == "org.w3c.dom.HTMLCanvasElement_$external_class_get") {
        object_result(ObjectKind::HtmlCanvasClass);
    } else if (name == "org.jetbrains.skiko.GL_$external_prop_getter") {
        object_result(ObjectKind::Gl);
    } else if (name == "org.jetbrains.skiko.createContext_$external_fun") {
        HostValue* canvas = GetHostValue(args[1]);
        if (!canvas || canvas->object != ObjectKind::Canvas) {
            return SkikoHost::Trap("createContext expected a canvas");
        }
        const int width = canvas->id == 3 ? 1212 : 600;
        const int height = canvas->id == 3 ? 800 : 600;
        std::string error;
        const int id = skiko_->CreateCanvasContext(width, height, error);
        if (id == 0) return SkikoHost::Trap(error);
        results[0].kind = WASMTIME_I32;
        results[0].of.i32 = id;
    } else if (name == "org.jetbrains.skiko.makeContextCurrent_$external_fun") {
        std::string error;
        results[0].kind = WASMTIME_I32;
        results[0].of.i32 = skiko_->MakeContextCurrent(args[1].of.i32, error) ? 1 : 0;
        if (!error.empty()) return SkikoHost::Trap(error);
    } else if (name == "org.jetbrains.skiko.currentGLContext") {
        object_result(ObjectKind::GlContext, skiko_->CurrentContextId());
    } else if (name == "org.khronos.webgl.SAMPLES_$external_prop_getter") {
        results[0].kind = WASMTIME_I32;
        results[0].of.i32 = 0x80A9;
    } else if (name == "org.khronos.webgl.getParameter_$external_fun") {
        SetExternResult(&results[0], HostValue::Number(0));
    } else if (name == "org.khronos.webgl.Companion_$external_object_getInstance") {
        object_result(ObjectKind::WebGlClass);
    } else if (name == "org.jetbrains.skiko.skikoRequestAnimationFrame") {
        HostValue* callback = GetHostValue(args[0]);
        if (!callback || !callback->callback) {
            return SkikoHost::Trap("requestAnimationFrame expected a callback");
        }
        animation_callbacks_.push_back(callback->callback);
        results[0].kind = WASMTIME_I32;
        results[0].of.i32 = next_animation_id_++;
    } else if (name == "org.jetbrains.skiko.devicePixelRatio") {
        results[0].kind = WASMTIME_F64;
        results[0].of.f64 = 1.0;
    } else if (name == "org.jetbrains.skiko.wasm.contextAttributesWithDefaults") {
        object_result(ObjectKind::ContextAttributes);
    } else if (name == "org.jetbrains.skia.impl.FinalizationRegistry_$external_fun") {
        object_result(ObjectKind::FinalizationRegistry);
    } else if (name == "org.jetbrains.skia.impl.unregister_$external_fun") {
        if (nresults) {
            results[0].kind = WASMTIME_I32;
            results[0].of.i32 = 1;
        }
    } else if (name == "org.jetbrains.skia.navigatorLanguage" ||
               name == "org.jetbrains.skiko.getNavigatorInfo" ||
               name == "org.jetbrains.skiko.getNavigatorUserAgent") {
        string_result(name.ends_with("navigatorLanguage") ? "en-US" : "macOS");
    } else if (name == "kotlin.wasm.internal.getJsClassName") {
        string_result("Object");
    } else if (name == "kotlin.wasm.internal.getConstructor") {
        object_result(ObjectKind::Generic);
    } else if (name == "kotlin.createJsError") {
        auto* error = HostValue::Object(ObjectKind::Error);
        error->string = nargs == 0 ? "Kotlin error" : GetString(args[0]);
        if (std::getenv("SKIKO_WASMTIME_TRACE") != nullptr) {
            std::cerr << "[createJsError] message=" << error->string << '\n';
        }
        SetExternResult(&results[0], error);
    } else if (name == "kotlin.js.JsError_$external_class_instanceof") {
        HostValue* value = GetHostValue(args[0]);
        results[0].kind = WASMTIME_I32;
        results[0].of.i32 = value && value->object == ObjectKind::Error;
    } else if (name == "kotlin.js.message_$external_prop_getter") {
        string_result(GetString(args[0]));
    } else if (name == "kotlin.js.kotlinException_$external_prop_getter") {
        null_result();
    } else if (name == "kotlin.wasm.internal.jsThrow") {
        return SkikoHost::Trap(
            "Kotlin JavaScript throw: " + GetString(args[0]));
    } else if (name == "org.jetbrains.skiko.wasm.patchWebGlContext" ||
               name == "org.jetbrains.skia.impl.register_$external_fun" ||
               name == "org.jetbrains.skia.impl._releaseLocalCallbackScope_$external_fun" ||
               name == "kotlin.js.name_$external_prop_setter" ||
               name == "kotlin.js.kotlinException_$external_prop_setter") {
        // Deliberate no-op host shims.
    } else {
        return SkikoHost::Trap("Unimplemented Kotlin host import: " + qualified_name);
    }
    return nullptr;
}
