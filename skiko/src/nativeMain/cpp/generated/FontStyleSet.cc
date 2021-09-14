
// This file has been auto generated.

#include <iostream>
#include "SkTypeface.h"
#include "SkFontMgr.h"
#include "common.h"

extern "C" jlong org_jetbrains_skia_FontStyleSet__1nMakeEmpty
  () {
    SkFontStyleSet* instance = SkFontStyleSet::CreateEmpty();
    return reinterpret_cast<jlong>(instance);
}

extern "C" jint org_jetbrains_skia_FontStyleSet__1nCount
  (jlong ptr) {
    SkFontStyleSet* instance = reinterpret_cast<SkFontStyleSet*>(static_cast<uintptr_t>(ptr));
    return instance->count();
}

extern "C" jint org_jetbrains_skia_FontStyleSet__1nGetStyle
  (jlong ptr, jint index) {
    SkFontStyleSet* instance = reinterpret_cast<SkFontStyleSet*>(static_cast<uintptr_t>(ptr));
    SkFontStyle fontStyle;
    instance->getStyle(index, &fontStyle, nullptr);
    return fontStyle.weight() + (fontStyle.width() << 16) + (fontStyle.slant() << 24);
}


extern "C" jstring org_jetbrains_skia_FontStyleSet__1nGetStyleName
  (jlong ptr, jint index) {
    TODO("implement org_jetbrains_skia_FontStyleSet__1nGetStyleName");
}
     
#if 0 
extern "C" jstring org_jetbrains_skia_FontStyleSet__1nGetStyleName
  (jlong ptr, jint index) {
    SkFontStyleSet* instance = reinterpret_cast<SkFontStyleSet*>(static_cast<uintptr_t>(ptr));
    SkString style;
    instance->getStyle(index, nullptr, &style);
    return javaString(env, style);
}
#endif


extern "C" jlong org_jetbrains_skia_FontStyleSet__1nGetTypeface
  (jlong ptr, jint index) {
    SkFontStyleSet* instance = reinterpret_cast<SkFontStyleSet*>(static_cast<uintptr_t>(ptr));
    SkTypeface* typeface = instance->createTypeface(index);
    return reinterpret_cast<jlong>(typeface);
}


extern "C" jlong org_jetbrains_skia_FontStyleSet__1nMatchStyle
  (jlong ptr, jint fontStyle) {
    TODO("implement org_jetbrains_skia_FontStyleSet__1nMatchStyle");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_FontStyleSet__1nMatchStyle
  (jlong ptr, jint fontStyle) {
    SkFontStyleSet* instance = reinterpret_cast<SkFontStyleSet*>(static_cast<uintptr_t>(ptr));
    SkTypeface* typeface = instance->matchStyle(skija::FontStyle::fromJava(fontStyle));
    return reinterpret_cast<jlong>(typeface);
}
#endif

