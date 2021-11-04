
// This file has been auto generated.

#include <iostream>
#include "SkShaper.h"
#include "src/utils/SkUTF.h"
#include "unicode/ubidi.h"
#include "common.h"
#include "FontRunIterator.hh"
#include "src/utils/SkUTF.h"
#include "TextLineRunHandler.hh"

static void deleteShaper(SkShaper* instance) {
    // std::cout << "Deleting [SkShaper " << instance << "]" << std::endl;
    delete instance;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>((&deleteShaper));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nMakePrimitive
  () {
    return reinterpret_cast<KNativePointer>(SkShaper::MakePrimitive().release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nMakeShaperDrivenWrapper
  (KNativePointer fontMgrPtr) {
    SkFontMgr* fontMgr = reinterpret_cast<SkFontMgr*>((fontMgrPtr));
    return reinterpret_cast<KNativePointer>(SkShaper::MakeShaperDrivenWrapper(sk_ref_sp(fontMgr)).release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nMakeShapeThenWrap
  (KNativePointer fontMgrPtr) {
    SkFontMgr* fontMgr = reinterpret_cast<SkFontMgr*>((fontMgrPtr));
    return reinterpret_cast<KNativePointer>(SkShaper::MakeShapeThenWrap(sk_ref_sp(fontMgr)).release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nMakeShapeDontWrapOrReorder
  (KNativePointer fontMgrPtr) {
    SkFontMgr* fontMgr = reinterpret_cast<SkFontMgr*>((fontMgrPtr));
    return reinterpret_cast<KNativePointer>(SkShaper::MakeShapeDontWrapOrReorder(sk_ref_sp(fontMgr)).release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nMakeCoreText() {
    #ifdef SK_SHAPER_CORETEXT_AVAILABLE
        return reinterpret_cast<KNativePointer>(SkShaper::MakeCoreText().release());
    #else
        return 0;
    #endif
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nMake
  (KNativePointer fontMgrPtr) {
    SkFontMgr* fontMgr = reinterpret_cast<SkFontMgr*>((fontMgrPtr));
    return reinterpret_cast<KNativePointer>(SkShaper::Make(sk_ref_sp(fontMgr)).release());
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nShapeBlob
  (KNativePointer ptr, KInteropPointer textObj, KNativePointer fontPtr, KInteropPointer opts, KFloat width, KFloat offsetX, KFloat offsetY) {
    TODO("implement org_jetbrains_skia_shaper_Shaper__1nShapeBlob");
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nShapeLine
  (KNativePointer ptr, KInteropPointer textObj, KNativePointer fontPtr, KInt optsFeaturesLen, KInt* optsFeatures, KInt optsBooleanProps) {
    SkShaper* instance = reinterpret_cast<SkShaper*>(ptr);

    SkString text = skString(textObj);
    std::shared_ptr<UBreakIterator> graphemeIter = skija::shaper::graphemeBreakIterator(text);
    if (!graphemeIter) return 0;

    SkFont* font = reinterpret_cast<SkFont*>(fontPtr);
    std::vector<SkShaper::Feature> features = skija::shaper::ShapingOptions::getFeaturesFromIntsArray(optsFeatures, optsFeaturesLen);

    bool aproximatePunctuation = optsBooleanProps & 0x01;
    bool aproximateSpaces = optsBooleanProps & 0x02;
    bool isLeftToRight = optsBooleanProps & 0x04;

    std::unique_ptr<SkShaper::FontRunIterator> fontRunIter(new FontRunIterator(
        text.c_str(),
        text.size(),
        *font,
        SkFontMgr::RefDefault(),
        graphemeIter,
        aproximateSpaces,
        aproximatePunctuation));
    if (!fontRunIter) return 0;

    uint8_t defaultBiDiLevel = isLeftToRight ? UBIDI_DEFAULT_LTR : UBIDI_DEFAULT_RTL;
    std::unique_ptr<SkShaper::BiDiRunIterator> bidiRunIter(SkShaper::MakeBiDiRunIterator(text.c_str(), text.size(), defaultBiDiLevel));
    if (!bidiRunIter) return 0;

    std::unique_ptr<SkShaper::ScriptRunIterator> scriptRunIter(SkShaper::MakeHbIcuScriptRunIterator(text.c_str(), text.size()));
    if (!scriptRunIter) return 0;

    std::unique_ptr<SkShaper::LanguageRunIterator> languageRunIter(SkShaper::MakeStdLanguageRunIterator(text.c_str(), text.size()));
    if (!languageRunIter) return 0;

    TextLine* line;
//    if (text.size() == 0)
//        line = new TextLine(*font);
//    else {
//        TextLineRunHandler rh(text, graphemeIter);
//        instance->shape(text.c_str(), text.size(), *fontRunIter, *bidiRunIter, *scriptRunIter, *languageRunIter, features.data(), features.size(), std::numeric_limits<float>::infinity(), &rh);
//        line = rh.makeLine().release();
//    }
    return reinterpret_cast<KNativePointer>(line);
}
SKIKO_EXPORT void org_jetbrains_skia_shaper_Shaper__1nShape
  (KNativePointer ptr, KNativePointer textPtr, KInteropPointer fontRunIterObj, KInteropPointer bidiRunIterObj, KInteropPointer scriptRunIterObj, KInteropPointer languageRunIterObj, KInteropPointer opts, KFloat width, KInteropPointer runHandlerObj)
{
    TODO("implement org_jetbrains_skia_shaper_Shaper__1nShape");
}
