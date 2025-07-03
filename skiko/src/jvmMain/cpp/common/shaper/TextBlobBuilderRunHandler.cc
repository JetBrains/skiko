#include <jni.h>
#include "../interop.hh"
#include "SkShaper.h"

static void deleteTextBlobBuilderRunHandler(SkTextBlobBuilderRunHandler* instance) {
    // std::cout << "Deleting [SkTextBlobBuilderRunHandler " << instance << "]" << std::endl;
    delete instance;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_shaper_TextBlobBuilderRunHandlerExternalKt_TextBlobBuilderRunHandler_1nGetFinalizer
  (JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteTextBlobBuilderRunHandler));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_shaper_TextBlobBuilderRunHandlerExternalKt_TextBlobBuilderRunHandler_1nMake
  (JNIEnv* env, jclass jclass, jlong textPtr, jfloat offsetX, jfloat offsetY) {
    SkString* text = reinterpret_cast<SkString*>(static_cast<uintptr_t>(textPtr));
    auto instance = new SkTextBlobBuilderRunHandler(text->c_str(), {offsetX, offsetY});
    return reinterpret_cast<jlong>(instance);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_shaper_TextBlobBuilderRunHandlerExternalKt_TextBlobBuilderRunHandler_1nMakeBlob
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkTextBlobBuilderRunHandler* instance = reinterpret_cast<SkTextBlobBuilderRunHandler*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(instance->makeBlob().release());
}
