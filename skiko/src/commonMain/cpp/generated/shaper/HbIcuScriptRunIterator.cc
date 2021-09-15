
// This file has been auto generated.

#include "SkShaper.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_HbIcuScriptRunIterator__1nMake
  (KNativePointer textPtr) {
    SkString* text = reinterpret_cast<SkString*>((textPtr));
    std::unique_ptr<SkShaper::ScriptRunIterator> instance(SkShaper::MakeHbIcuScriptRunIterator(text->c_str(), text->size()));
    return reinterpret_cast<KNativePointer>(instance.release());
}

SKIKO_EXPORT KInt org_jetbrains_skia_shaper_HbIcuScriptRunIterator__1nGetCurrentScriptTag
  (KNativePointer ptr) {
    SkShaper::ScriptRunIterator* instance = reinterpret_cast<SkShaper::ScriptRunIterator*>((ptr));
    return instance->currentScript();
}
