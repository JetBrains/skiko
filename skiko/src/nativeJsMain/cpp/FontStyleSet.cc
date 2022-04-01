#include <iostream>
#include "SkTypeface.h"
#include "SkFontMgr.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_FontStyleSet__1nMakeEmpty
  () {
    SkFontStyleSet* instance = SkFontStyleSet::CreateEmpty();
    return reinterpret_cast<KNativePointer>(instance);
}

SKIKO_EXPORT KInt org_jetbrains_skia_FontStyleSet__1nCount
  (KNativePointer ptr) {
    SkFontStyleSet* instance = reinterpret_cast<SkFontStyleSet*>(ptr);
    return instance->count();
}

SKIKO_EXPORT KInt org_jetbrains_skia_FontStyleSet__1nGetStyle
  (KNativePointer ptr, KInt index) {
    SkFontStyleSet* instance = reinterpret_cast<SkFontStyleSet*>((ptr));
    SkFontStyle fontStyle;
    instance->getStyle(index, &fontStyle, nullptr);
    return fontStyle.weight() + (fontStyle.width() << 16) + (fontStyle.slant() << 24);
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_FontStyleSet__1nGetStyleName
  (KNativePointer ptr, KInt index) {
    SkFontStyleSet* instance = reinterpret_cast<SkFontStyleSet*>((ptr));
    SkString style;
    instance->getStyle(index, nullptr, &style);
    return new SkString(style);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_FontStyleSet__1nGetTypeface
  (KNativePointer ptr, KInt index) {
    SkFontStyleSet* instance = reinterpret_cast<SkFontStyleSet*>((ptr));
    SkTypeface* typeface = instance->createTypeface(index);
    return reinterpret_cast<KNativePointer>(typeface);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_FontStyleSet__1nMatchStyle
  (KNativePointer ptr, KInt fontStyle) {
    SkFontStyleSet* instance = reinterpret_cast<SkFontStyleSet*>((ptr));
    SkTypeface* typeface = instance->matchStyle(skija::FontStyle::fromKotlin(fontStyle));
    return reinterpret_cast<KNativePointer>(typeface);
}
