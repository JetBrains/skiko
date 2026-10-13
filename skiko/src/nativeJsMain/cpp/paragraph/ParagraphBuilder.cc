#include <iostream>
#include <string>
#ifdef SKIKO_USE_ICU
#include "modules/skunicode/include/SkUnicode_icu.h"
#endif
#include "SkikoUnicodeHolder.hh"
#include "ParagraphBuilder.h"

using namespace std;
using namespace skia::textlayout;
#include "common.h"

static void deleteParagraphBuilder(ParagraphBuilder* instance) {
    delete instance;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_ParagraphBuilder__1nMake
  (KNativePointer paragraphStylePtr, KNativePointer fontCollectionPtr, KNativePointer unicodePtr) {
    ParagraphStyle* paragraphStyle = reinterpret_cast<ParagraphStyle*>((paragraphStylePtr));
    FontCollection* fontCollection = reinterpret_cast<FontCollection*>((fontCollectionPtr));
    SkikoUnicodeHolder* unicodeHolder = reinterpret_cast<SkikoUnicodeHolder*>((unicodePtr));
#ifdef SKIKO_USE_ICU
    sk_sp<SkUnicode> unicode =
        unicodeHolder != nullptr ? unicodeHolder->unicode : SkUnicodes::ICU::Make();
#else
    // ICU is excluded from this binary, so an SkUnicode must be supplied by the
    // caller (see SkUnicodeClient, backed by the browser Intl.Segmenter).
    sk_sp<SkUnicode> unicode = unicodeHolder != nullptr ? unicodeHolder->unicode : nullptr;
    if (unicode == nullptr) {
        return nullptr;
    }
#endif
    ParagraphBuilder* instance = ParagraphBuilder::make(
        *paragraphStyle,
        sk_ref_sp(fontCollection),
        unicode
    ).release();
    return reinterpret_cast<KNativePointer>(instance);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_ParagraphBuilder__1nGetFinalizer
  () {
    return reinterpret_cast<KNativePointer>((&deleteParagraphBuilder));
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphBuilder__1nPushStyle
  (KNativePointer ptr, KNativePointer textStylePtr) {
    ParagraphBuilder* instance = reinterpret_cast<ParagraphBuilder*>((ptr));
    TextStyle* textStyle = reinterpret_cast<TextStyle*>((textStylePtr));
    instance->pushStyle(*textStyle);
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphBuilder__1nPopStyle
  (KNativePointer ptr, KNativePointer textStylePtr) {
    ParagraphBuilder* instance = reinterpret_cast<ParagraphBuilder*>((ptr));
    instance->pop();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphBuilder__1nAddText
  (KNativePointer ptr, KInteropPointer textString) {
    ParagraphBuilder* instance = reinterpret_cast<ParagraphBuilder*>((ptr));
    SkString text = skString(textString);
    instance->addText(text.c_str(), text.size());
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphBuilder__1nAddPlaceholder
  (KNativePointer ptr, KFloat width, KFloat height, KInt alignment, KInt baselinePosition, KFloat baseline) {
    ParagraphBuilder* instance = reinterpret_cast<ParagraphBuilder*>((ptr));
    instance->addPlaceholder({width, height, static_cast<PlaceholderAlignment>(alignment), static_cast<TextBaseline>(baselinePosition), baseline});
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_ParagraphBuilder__1nBuild
  (KNativePointer ptr) {
    ParagraphBuilder* instance = reinterpret_cast<ParagraphBuilder*>((ptr));
    Paragraph* paragraph = instance->Build().release();
    return reinterpret_cast<KNativePointer>(paragraph);
}
