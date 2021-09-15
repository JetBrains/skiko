
// This file has been auto generated.

#include "SkShaper.h"
#include "common.h"

extern "C" jlong org_jetbrains_skia_shaper_IcuBidiRunIterator__1nMake
  (jlong textPtr, jint bidiLevel) {
    SkString* text = reinterpret_cast<SkString*>(static_cast<uintptr_t>(textPtr));
    std::unique_ptr<SkShaper::BiDiRunIterator> instance(SkShaper::MakeIcuBiDiRunIterator(text->c_str(), text->size(), bidiLevel & 0xFF));
    return reinterpret_cast<jlong>(instance.release());
}

extern "C" jint org_jetbrains_skia_shaper_IcuBidiRunIterator__1nGetCurrentLevel
  (jlong ptr) {
    SkShaper::BiDiRunIterator* instance = reinterpret_cast<SkShaper::BiDiRunIterator*>(static_cast<uintptr_t>(ptr));
    return instance->currentLevel();
}
