
// This file has been auto generated.

#include "SkFontMgr.h"
#include "SkShaper.h"
#include "common.h"
#include "FontRunIterator.hh"


SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_FontMgrRunIterator__1nMake
  (KNativePointer textPtr, KNativePointer fontPtr, KNativePointer fontMgrPtr, KInt optsBooleanProps) {
    SkString* text = reinterpret_cast<SkString*>(textPtr);
    SkFont* font = reinterpret_cast<SkFont*>(fontPtr);
    sk_sp<SkFontMgr> fontMgr = fontMgrPtr == nullptr
      ? SkFontMgr::RefDefault()
      : sk_ref_sp(reinterpret_cast<SkFontMgr*>(fontMgrPtr));
    std::shared_ptr<UBreakIterator> graphemeIter = skija::shaper::graphemeBreakIterator(*text);
    if (!graphemeIter) return nullptr;


    bool approximatePunctuation = (optsBooleanProps & 0x01) != 0;
    bool approximateSpaces = (optsBooleanProps & 0x02) != 0;

    auto instance = new FontRunIterator(
      text->c_str(),
      text->size(),
      *font,
      fontMgr,
      graphemeIter,
      approximateSpaces,
      approximatePunctuation
    );
    return reinterpret_cast<KNativePointer>(instance);
}
     
SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_FontMgrRunIterator__1nGetCurrentFont
  (KNativePointer ptr) {
    SkShaper::FontRunIterator* instance = reinterpret_cast<SkShaper::FontRunIterator*>(ptr);
    SkFont* font = new SkFont(instance->currentFont());
    return reinterpret_cast<KNativePointer>(font);
}
