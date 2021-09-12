
// This file has been auto generated.

#include <iostream>
#include "SkData.h"
#include "SkTypeface.h"
#include "SkFontMgr.h"
#include "common.h"

extern "C" jint org_jetbrains_skia_FontMgr__1nGetFamiliesCount
  (kref __Kinstance, jlong ptr) {
    SkFontMgr* instance = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(ptr));
    return instance->countFamilies();
}


extern "C" jstring org_jetbrains_skia_FontMgr__1nGetFamilyName
  (kref __Kinstance, jlong ptr, jint index) {
    TODO("implement org_jetbrains_skia_FontMgr__1nGetFamilyName");
}
     
#if 0 
extern "C" jstring org_jetbrains_skia_FontMgr__1nGetFamilyName
  (kref __Kinstance, jlong ptr, jint index) {
    SkFontMgr* instance = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(ptr));
    SkString familyName;
    instance->getFamilyName(index, &familyName);
    return javaString(env, familyName);
}
#endif


extern "C" jlong org_jetbrains_skia_FontMgr__1nMakeStyleSet
  (kref __Kinstance, jlong ptr, jint index) {
    SkFontMgr* instance = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(ptr));
    SkFontStyleSet* styleSet = instance->createStyleSet(index);
    return reinterpret_cast<jlong>(styleSet);
}


extern "C" jlong org_jetbrains_skia_FontMgr__1nMatchFamily
  (kref __Kinstance, jlong ptr, jstring familyNameStr) {
    TODO("implement org_jetbrains_skia_FontMgr__1nMatchFamily");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_FontMgr__1nMatchFamily
  (kref __Kinstance, jlong ptr, jstring familyNameStr) {
    SkFontMgr* instance = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(ptr));
    SkString familyName = skString(env, familyNameStr);
    SkFontStyleSet* styleSet = instance->matchFamily(familyName.c_str());
    return reinterpret_cast<jlong>(styleSet);
}
#endif



extern "C" jlong org_jetbrains_skia_FontMgr__1nMatchFamilyStyle
  (kref __Kinstance, jlong ptr, jstring familyNameStr, jint fontStyle) {
    TODO("implement org_jetbrains_skia_FontMgr__1nMatchFamilyStyle");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_FontMgr__1nMatchFamilyStyle
  (kref __Kinstance, jlong ptr, jstring familyNameStr, jint fontStyle) {
    SkFontMgr* instance = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(ptr));
    SkString familyName = skString(env, familyNameStr);
    SkTypeface* typeface = instance->matchFamilyStyle(familyName.c_str(), skija::FontStyle::fromJava(fontStyle));
    return reinterpret_cast<jlong>(typeface);
}
#endif



extern "C" jlong org_jetbrains_skia_FontMgr__1nMatchFamilyStyleCharacter
  (kref __Kinstance, jlong ptr, jstring familyNameStr, jint fontStyle, jobjectArray bcp47Array, jint character) {
    TODO("implement org_jetbrains_skia_FontMgr__1nMatchFamilyStyleCharacter");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_FontMgr__1nMatchFamilyStyleCharacter
  (kref __Kinstance, jlong ptr, jstring familyNameStr, jint fontStyle, jobjectArray bcp47Array, jint character) {
    SkFontMgr* instance = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(ptr));

    SkString familyName = skString(env, familyNameStr);
    
    std::vector<SkString> bcp47Strings = skStringVector(env, bcp47Array);
    std::vector<const char*> bcp47(bcp47Strings.size());
    for (int i = 0; i < bcp47.size(); ++i)
        bcp47[i] = bcp47Strings[i].c_str();
    
    SkTypeface* typeface = instance->matchFamilyStyleCharacter(familyName.c_str(), skija::FontStyle::fromJava(fontStyle), bcp47.data(), (int) bcp47.size(), character);
    
    return reinterpret_cast<jlong>(typeface);
}
#endif


extern "C" jlong org_jetbrains_skia_FontMgr__1nMakeFromData
  (kref __Kinstance, jlong ptr, jlong dataPtr, jint ttcIndex) {
    SkFontMgr* instance = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(ptr));
    SkData* data = reinterpret_cast<SkData*>(static_cast<uintptr_t>(dataPtr));
    SkTypeface* typeface = instance->makeFromData(sk_ref_sp(data), ttcIndex).release();
    return reinterpret_cast<jlong>(typeface);
}

extern "C" jlong org_jetbrains_skia_FontMgr__1nDefault
  (kref __Kinstance) {
    SkFontMgr* instance = SkFontMgr::RefDefault().release();
    return reinterpret_cast<jlong>(instance);
}
