#include <jni.h>
#include "../interop.hh"
#include "SkShaper.h"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_shaper_HbIcuScriptRunIteratorKt__1nMake
  (JNIEnv* env, jclass jclass, jlong textPtr) {
    SkString* text = reinterpret_cast<SkString*>(static_cast<uintptr_t>(textPtr));
    std::unique_ptr<SkShaper::ScriptRunIterator> instance(SkShaper::MakeHbIcuScriptRunIterator(text->c_str(), text->size()));
    return reinterpret_cast<jlong>(instance.release());
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_shaper_HbIcuScriptRunIteratorKt__1nGetCurrentScriptTag
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkShaper::ScriptRunIterator* instance = reinterpret_cast<SkShaper::ScriptRunIterator*>(static_cast<uintptr_t>(ptr));
    return instance->currentScript();
}
