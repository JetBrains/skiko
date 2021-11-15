
// This file has been auto generated.

#include "Skottie.h"
using namespace skottie;
#include "common.h"

class SkikoLogger: public Logger {
public:
    SkikoLogger() : _log(nullptr) {}

    void init(KInteropPointer onLog) {
        _log = KVoidCallback(onLog);
    }

    SkString* logMessage() {
        return &_logMessage;
    }

    SkString* logJson() {
        return _hasLogJson ? &_logJson : nullptr;
    }

    Level logLevel() const {
        return _logLevel;
    }

    void log(Level level, const char message[], const char* json = nullptr) override {
        _logLevel = level;
        _logMessage = message;
        _hasLogJson = json != nullptr;
        _logJson = json;
        _log();
        _logLevel = Level::kError;
        _logMessage = nullptr;
        _logJson = nullptr;
        _hasLogJson = false;
    }

private:
    SkString _logMessage;
    SkString _logJson;
    KVoidCallback _log;
    Level _logLevel;
    bool _hasLogJson;
};

SKIKO_EXPORT KNativePointer org_jetbrains_skia_skottie_Logger__1nMake
  () {
    SkikoLogger* instance = sk_sp<SkikoLogger>(new SkikoLogger()).release();
    return reinterpret_cast<KNativePointer>(instance);
}

SKIKO_EXPORT void org_jetbrains_skia_skottie_Logger__1nInit
  (KNativePointer ptr, KInteropPointer onLog) {
    SkikoLogger* instance = reinterpret_cast<SkikoLogger*>((ptr));
    instance->init(onLog);
}

SKIKO_EXPORT KInteropPointer org_jetbrains_skia_skottie_Logger__1nGetLogMessage
  (KNativePointer ptr) {
    SkikoLogger* instance = reinterpret_cast<SkikoLogger*>((ptr));
    return reinterpret_cast<KInteropPointer>(instance->logMessage());
}

SKIKO_EXPORT KInteropPointer org_jetbrains_skia_skottie_Logger__1nGetLogJson
  (KNativePointer ptr) {
    SkikoLogger* instance = reinterpret_cast<SkikoLogger*>((ptr));
    return reinterpret_cast<KInteropPointer>(instance->logJson());
}

SKIKO_EXPORT KInt org_jetbrains_skia_skottie_Logger__1nGetLogLevel
  (KNativePointer ptr) {
    SkikoLogger* instance = reinterpret_cast<SkikoLogger*>((ptr));
    return static_cast<KInt>(instance->logLevel());
}
