
// This file has been auto generated.

#include "SkShaper.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_IcuBidiRunIterator__1nMake
  (KNativePointer textPtr, KInt bidiLevel) {
    SkString* text = reinterpret_cast<SkString*>((textPtr));
    std::unique_ptr<SkShaper::BiDiRunIterator> instance(SkShaper::MakeIcuBiDiRunIterator(text->c_str(), text->size(), bidiLevel & 0xFF));
    return reinterpret_cast<KNativePointer>(instance.release());
}

SKIKO_EXPORT KInt org_jetbrains_skia_shaper_IcuBidiRunIterator__1nGetCurrentLevel
  (KNativePointer ptr) {
    SkShaper::BiDiRunIterator* instance = reinterpret_cast<SkShaper::BiDiRunIterator*>((ptr));
    return instance->currentLevel();
}
