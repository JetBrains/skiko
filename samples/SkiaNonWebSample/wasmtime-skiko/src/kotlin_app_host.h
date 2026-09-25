#pragma once

#include <cstdint>
#include <deque>
#include <memory>
#include <string>
#include <vector>

#include <wasmtime.h>

class SkikoHost;

class KotlinAppHost {
public:
    KotlinAppHost(wasmtime_context_t* context, SkikoHost* skiko);
    ~KotlinAppHost();

    KotlinAppHost(const KotlinAppHost&) = delete;
    KotlinAppHost& operator=(const KotlinAppHost&) = delete;

    void RegisterImports(
        wasmtime_linker_t* linker,
        const wasmtime_module_t* application_module
    );
    void AttachInstance(const wasmtime_instance_t& instance);
    void Start();
    void RunContinuousFrame(double timestamp_millis);
    std::vector<uint8_t> CompositeFrame();
    const std::string& Title() const;

private:
    struct Callback;
    struct Binding;
    struct HostValue;

    static void DeleteHostValue(void* value);
    static void DeleteBinding(void* value);

    static wasm_trap_t* Dispatch(
        void* environment,
        wasmtime_caller_t* caller,
        const wasmtime_val_t* args,
        size_t nargs,
        wasmtime_val_t* results,
        size_t nresults
    );
    wasm_trap_t* CallHost(
        const std::string& name,
        wasmtime_caller_t* caller,
        const wasmtime_val_t* args,
        size_t nargs,
        wasmtime_val_t* results,
        size_t nresults
    );
    void Invoke(const std::shared_ptr<Callback>& callback, double timestamp);
    void DrainReadyCallbacks();
    void SetExternResult(wasmtime_val_t* result, HostValue* value);
    HostValue* GetHostValue(const wasmtime_val_t& value) const;
    std::string GetString(const wasmtime_val_t& value) const;

    wasmtime_context_t* context_;
    SkikoHost* skiko_;
    wasmtime_instance_t instance_{};
    bool attached_ = false;
    std::deque<std::shared_ptr<Callback>> ready_callbacks_;
    std::deque<std::shared_ptr<Callback>> animation_callbacks_;
    std::vector<std::shared_ptr<Callback>> continuous_callbacks_;
    int next_animation_id_ = 1;
    std::string title_ = "Skiko WASM native OpenGL";
    bool diagnostics_reported_ = false;
};
