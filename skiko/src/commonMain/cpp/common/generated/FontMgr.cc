
// This file has been auto generated.

#include <iostream>
#include "SkData.h"
#include "SkTypeface.h"
#include "SkFontMgr.h"
#include "common.h"

SKIKO_EXPORT KInt org_jetbrains_skia_FontMgr__1nGetFamiliesCount
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkFontMgr* instance = reinterpret_cast<SkFontMgr*>((ptr));
    return instance->countFamilies();
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_FontMgr__1nGetFamilyName
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt index) {
    TODO("implement org_jetbrains_skia_FontMgr__1nGetFamilyName");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_FontMgr__1nGetFamilyName
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt index) {
    SkFontMgr* instance = reinterpret_cast<SkFontMgr*>((ptr));
    SkString familyName;
    instance->getFamilyName(index, &familyName);
    return javaString(env, familyName);
}
#endif


SKIKO_EXPORT KNativePointer org_jetbrains_skia_FontMgr__1nMakeStyleSet
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt index) {
    SkFontMgr* instance = reinterpret_cast<SkFontMgr*>((ptr));
    SkFontStyleSet* styleSet = instance->createStyleSet(index);
    return reinterpret_cast<KNativePointer>(styleSet);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_FontMgr__1nMatchFamily
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointer familyNameStr) {
    TODO("implement org_jetbrains_skia_FontMgr__1nMatchFamily");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_FontMgr__1nMatchFamily
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointer familyNameStr) {
    SkFontMgr* instance = reinterpret_cast<SkFontMgr*>((ptr));
    SkString familyName = skString(env, familyNameStr);
    SkFontStyleSet* styleSet = instance->matchFamily(familyName.c_str());
    return reinterpret_cast<KNativePointer>(styleSet);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_FontMgr__1nMatchFamilyStyle
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointer familyNameStr, KInt fontStyle) {
    TODO("implement org_jetbrains_skia_FontMgr__1nMatchFamilyStyle");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_FontMgr__1nMatchFamilyStyle
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointer familyNameStr, KInt fontStyle) {
    SkFontMgr* instance = reinterpret_cast<SkFontMgr*>((ptr));
    SkString familyName = skString(env, familyNameStr);
    SkTypeface* typeface = instance->matchFamilyStyle(familyName.c_str(), skija::FontStyle::fromJava(fontStyle));
    return reinterpret_cast<KNativePointer>(typeface);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_FontMgr__1nMatchFamilyStyleCharacter
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointer familyNameStr, KInt fontStyle, KInteropPointerArray bcp47Array, KInt character) {
    TODO("implement org_jetbrains_skia_FontMgr__1nMatchFamilyStyleCharacter");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_FontMgr__1nMatchFamilyStyleCharacter
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointer familyNameStr, KInt fontStyle, KInteropPointerArray bcp47Array, KInt character) {
    SkFontMgr* instance = reinterpret_cast<SkFontMgr*>((ptr));

    SkString familyName = skString(env, familyNameStr);
    
    std::vector<SkString> bcp47Strings = skStringVector(env, bcp47Array);
    std::vector<const char*> bcp47(bcp47Strings.size());
    for (int i = 0; i < bcp47.size(); ++i)
        bcp47[i] = bcp47Strings[i].c_str();
    
    SkTypeface* typeface = instance->matchFamilyStyleCharacter(familyName.c_str(), skija::FontStyle::fromJava(fontStyle), bcp47.data(), (int) bcp47.size(), character);
    
    return reinterpret_cast<KNativePointer>(typeface);
}
#endif


SKIKO_EXPORT KNativePointer org_jetbrains_skia_FontMgr__1nMakeFromData
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer dataPtr, KInt ttcIndex) {
    SkFontMgr* instance = reinterpret_cast<SkFontMgr*>((ptr));
    SkData* data = reinterpret_cast<SkData*>((dataPtr));
    SkTypeface* typeface = instance->makeFromData(sk_ref_sp(data), ttcIndex).release();
    return reinterpret_cast<KNativePointer>(typeface);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_FontMgr__1nDefault
  (KInteropPointer __Kinstance) {
    SkFontMgr* instance = SkFontMgr::RefDefault().release();
    return reinterpret_cast<KNativePointer>(instance);
}
