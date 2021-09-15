
// This file has been auto generated.

#include "SkShaper.h"
#include "common.h"

extern "C" jlong org_jetbrains_skia_shaper_HbIcuScriptRunIterator__1nMake
  (jlong textPtr) {
    SkString* text = reinterpret_cast<SkString*>(static_cast<uintptr_t>(textPtr));
    std::unique_ptr<SkShaper::ScriptRunIterator> instance(SkShaper::MakeHbIcuScriptRunIterator(text->c_str(), text->size()));
    return reinterpret_cast<jlong>(instance.release());
}

extern "C" jint org_jetbrains_skia_shaper_HbIcuScriptRunIterator__1nGetCurrentScriptTag
  (jlong ptr) {
    SkShaper::ScriptRunIterator* instance = reinterpret_cast<SkShaper::ScriptRunIterator*>(static_cast<uintptr_t>(ptr));
    return instance->currentScript();
}
