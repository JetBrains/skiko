#include <jni.h>
#include "interop.hh"
#include "Skottie.h"

using namespace skottie;

class SkikoLogger: public Logger {
public:
    SkikoLogger() {
    }

    ~SkikoLogger() {
        fEnv->DeleteGlobalRef(fObject);
    }

    void init(JNIEnv* e, jobject o) {
        fEnv = e;
        fObject = fEnv->NewGlobalRef(o);
    }

public:
    void log(Level level, const char message[], const char* json = nullptr) override {
        jobject levelObj;
        switch (level) {
            case Logger::Level::kWarning:
                levelObj = skija::skottie::LogLevel::WARNING;
                break;
            default:
                levelObj = skija::skottie::LogLevel::ERROR;
        }

        fEnv->CallVoidMethod(fObject, skija::skottie::Logger::log, levelObj, javaString(fEnv, message), javaString(fEnv, json));
    }

private:
    JNIEnv* fEnv;
    jobject fObject;
};

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_skottie_LoggerExternalKt_Logger_1nMake
  (JNIEnv* env, jclass jclass) {
    SkikoLogger* instance = new SkikoLogger();
    return reinterpret_cast<jlong>(instance);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_skottie_Logger_1jvmKt__1nInit
  (JNIEnv* env, jclass jclass, jobject jthis, jlong ptr) {
    SkikoLogger* instance = reinterpret_cast<SkikoLogger*>(static_cast<uintptr_t>(ptr));
    instance->init(env, jthis);
}
