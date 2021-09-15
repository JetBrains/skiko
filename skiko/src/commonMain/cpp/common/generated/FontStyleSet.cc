
// This file has been auto generated.

#include <iostream>
#include "SkTypeface.h"
#include "SkFontMgr.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_FontStyleSet__1nMakeEmpty
  (KInteropPointer __Kinstance) {
    SkFontStyleSet* instance = SkFontStyleSet::CreateEmpty();
    return reinterpret_cast<KNativePointer>(instance);
}

SKIKO_EXPORT KInt org_jetbrains_skia_FontStyleSet__1nCount
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkFontStyleSet* instance = reinterpret_cast<SkFontStyleSet*>((ptr));
    return instance->count();
}

SKIKO_EXPORT KInt org_jetbrains_skia_FontStyleSet__1nGetStyle
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt index) {
    SkFontStyleSet* instance = reinterpret_cast<SkFontStyleSet*>((ptr));
    SkFontStyle fontStyle;
    instance->getStyle(index, &fontStyle, nullptr);
    return fontStyle.weight() + (fontStyle.width() << 16) + (fontStyle.slant() << 24);
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_FontStyleSet__1nGetStyleName
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt index) {
    TODO("implement org_jetbrains_skia_FontStyleSet__1nGetStyleName");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_FontStyleSet__1nGetStyleName
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt index) {
    SkFontStyleSet* instance = reinterpret_cast<SkFontStyleSet*>((ptr));
    SkString style;
    instance->getStyle(index, nullptr, &style);
    return javaString(env, style);
}
#endif


SKIKO_EXPORT KNativePointer org_jetbrains_skia_FontStyleSet__1nGetTypeface
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt index) {
    SkFontStyleSet* instance = reinterpret_cast<SkFontStyleSet*>((ptr));
    SkTypeface* typeface = instance->createTypeface(index);
    return reinterpret_cast<KNativePointer>(typeface);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_FontStyleSet__1nMatchStyle
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt fontStyle) {
    TODO("implement org_jetbrains_skia_FontStyleSet__1nMatchStyle");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_FontStyleSet__1nMatchStyle
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt fontStyle) {
    SkFontStyleSet* instance = reinterpret_cast<SkFontStyleSet*>((ptr));
    SkTypeface* typeface = instance->matchStyle(skija::FontStyle::fromJava(fontStyle));
    return reinterpret_cast<KNativePointer>(typeface);
}
#endif

