
// This file has been auto generated.

#include <iostream>
#include <string>
#include "ParagraphBuilder.h"
using namespace std;
using namespace skia::textlayout;
#include "common.h"

static void deleteParagraphBuilder(ParagraphBuilder* instance) {
    delete instance;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_ParagraphBuilder__1nMake
  (KInteropPointer __Kinstance, KNativePointer paragraphStylePtr, KNativePointer fontCollectionPtr) {
    ParagraphStyle* paragraphStyle = reinterpret_cast<ParagraphStyle*>((paragraphStylePtr));
    FontCollection* fontCollection = reinterpret_cast<FontCollection*>((fontCollectionPtr));
    ParagraphBuilder* instance = ParagraphBuilder::make(*paragraphStyle, sk_ref_sp(fontCollection)).release();
    return reinterpret_cast<KNativePointer>(instance);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_ParagraphBuilder__1nGetFinalizer
  (KInteropPointer __Kinstance) {
    return reinterpret_cast<KNativePointer>((&deleteParagraphBuilder));
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphBuilder__1nPushStyle
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer textStylePtr) {
    ParagraphBuilder* instance = reinterpret_cast<ParagraphBuilder*>((ptr));
    TextStyle* textStyle = reinterpret_cast<TextStyle*>((textStylePtr));
    instance->pushStyle(*textStyle);
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphBuilder__1nPopStyle
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer textStylePtr) {
    ParagraphBuilder* instance = reinterpret_cast<ParagraphBuilder*>((ptr));
    instance->pop();
}


SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphBuilder__1nAddText
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointer textString) {
    TODO("implement org_jetbrains_skia_paragraph_ParagraphBuilder__1nAddText");
}
     
#if 0 
SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphBuilder__1nAddText
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointer textString) {
    ParagraphBuilder* instance = reinterpret_cast<ParagraphBuilder*>((ptr));
    SkString text = skString(env, textString);
    instance->addText(text.c_str(), text.size());
}
#endif


SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphBuilder__1nAddPlaceholder
  (KInteropPointer __Kinstance, KNativePointer ptr, KFloat width, KFloat height, KInt alignment, KInt baselinePosition, KFloat baseline) {
    ParagraphBuilder* instance = reinterpret_cast<ParagraphBuilder*>((ptr));
    instance->addPlaceholder({width, height, static_cast<PlaceholderAlignment>(alignment), static_cast<TextBaseline>(baselinePosition), baseline});
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphBuilder__1nSetParagraphStyle
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer stylePtr) {
    ParagraphBuilder* instance = reinterpret_cast<ParagraphBuilder*>((ptr));
    ParagraphStyle* style = reinterpret_cast<ParagraphStyle*>((stylePtr));
    instance->setParagraphStyle(*style);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_ParagraphBuilder__1nBuild
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    ParagraphBuilder* instance = reinterpret_cast<ParagraphBuilder*>((ptr));
    Paragraph* paragraph = instance->Build().release();
    return reinterpret_cast<KNativePointer>(paragraph);
}
