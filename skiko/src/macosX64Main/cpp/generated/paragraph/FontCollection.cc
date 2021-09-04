
// This file has been auto generated.

#include <iostream>
#include "SkRefCnt.h"
#include "FontCollection.h"
using namespace std;
using namespace skia::textlayout;
#include "common.h"

extern "C" jlong org_jetbrains_skia_paragraph_FontCollection__1nMake
  (kref __Kinstance) {
    FontCollection* ptr = new FontCollection();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" jlong org_jetbrains_skia_paragraph_FontCollection__1nGetFontManagersCount
  (kref __Kinstance, jlong ptr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>(static_cast<uintptr_t>(ptr));
    return instance->getFontManagersCount();
}


extern "C" void org_jetbrains_skia_paragraph_FontCollection__1nSetAssetFontManager
  (kref __Kinstance, jlong ptr, jlong fontManagerPtr, jstring defaultFamilyNameStr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_paragraph_FontCollection__1nSetAssetFontManager");
}
     
#if 0 
extern "C" void org_jetbrains_skia_paragraph_FontCollection__1nSetAssetFontManager
  (kref __Kinstance, jlong ptr, jlong fontManagerPtr, jstring defaultFamilyNameStr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>(static_cast<uintptr_t>(ptr));
    SkFontMgr* fontManager = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(fontManagerPtr));
    instance->setAssetFontManager(sk_ref_sp(fontManager));
}
#endif



extern "C" void org_jetbrains_skia_paragraph_FontCollection__1nSetDynamicFontManager
  (kref __Kinstance, jlong ptr, jlong fontManagerPtr, jstring defaultFamilyNameStr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_paragraph_FontCollection__1nSetDynamicFontManager");
}
     
#if 0 
extern "C" void org_jetbrains_skia_paragraph_FontCollection__1nSetDynamicFontManager
  (kref __Kinstance, jlong ptr, jlong fontManagerPtr, jstring defaultFamilyNameStr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>(static_cast<uintptr_t>(ptr));
    SkFontMgr* fontManager = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(fontManagerPtr));
    instance->setDynamicFontManager(sk_ref_sp(fontManager));
}
#endif



extern "C" void org_jetbrains_skia_paragraph_FontCollection__1nSetTestFontManager
  (kref __Kinstance, jlong ptr, jlong fontManagerPtr, jstring defaultFamilyNameStr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_paragraph_FontCollection__1nSetTestFontManager");
}
     
#if 0 
extern "C" void org_jetbrains_skia_paragraph_FontCollection__1nSetTestFontManager
  (kref __Kinstance, jlong ptr, jlong fontManagerPtr, jstring defaultFamilyNameStr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>(static_cast<uintptr_t>(ptr));
    SkFontMgr* fontManager = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(fontManagerPtr));
    instance->setTestFontManager(sk_ref_sp(fontManager));
}
#endif



extern "C" void org_jetbrains_skia_paragraph_FontCollection__1nSetDefaultFontManager
  (kref __Kinstance, jlong ptr, jlong fontManagerPtr, jstring defaultFamilyNameStr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_paragraph_FontCollection__1nSetDefaultFontManager");
}
     
#if 0 
extern "C" void org_jetbrains_skia_paragraph_FontCollection__1nSetDefaultFontManager
  (kref __Kinstance, jlong ptr, jlong fontManagerPtr, jstring defaultFamilyNameStr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>(static_cast<uintptr_t>(ptr));
    SkFontMgr* fontManager = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(fontManagerPtr));

    if (defaultFamilyNameStr == nullptr)
        instance->setDefaultFontManager(sk_ref_sp(fontManager));
    else {
        SkString defaultFamilyName = skString(env, defaultFamilyNameStr);
        instance->setDefaultFontManager(sk_ref_sp(fontManager), defaultFamilyName.c_str());
    }
}
#endif


extern "C" jlong org_jetbrains_skia_paragraph_FontCollection__1nGetFallbackManager
  (kref __Kinstance, jlong ptr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(instance->getFallbackManager().release());
}


extern "C" jlongArray org_jetbrains_skia_paragraph_FontCollection__1nFindTypefaces
  (kref __Kinstance, jlong ptr, jobjectArray familyNamesArray, jint fontStyle) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_paragraph_FontCollection__1nFindTypefaces");
}
     
#if 0 
extern "C" jlongArray org_jetbrains_skia_paragraph_FontCollection__1nFindTypefaces
  (kref __Kinstance, jlong ptr, jobjectArray familyNamesArray, jint fontStyle) {
    FontCollection* instance = reinterpret_cast<FontCollection*>(static_cast<uintptr_t>(ptr));

    jsize len = env->GetArrayLength(familyNamesArray);
    vector<SkString> familyNames(len);
    for (int i = 0; i < len; ++i) {
        jstring str = static_cast<jstring>(env->GetObjectArrayElement(familyNamesArray, i));
        familyNames.push_back(skString(env, str));
        env->DeleteLocalRef(str);
    }

    vector<sk_sp<SkTypeface>> found = instance->findTypefaces(familyNames, skija::FontStyle::fromJava(fontStyle));
    vector<jlong> res(found.size());
    for (int i = 0; i < found.size(); ++i)
        res[i] = reinterpret_cast<jlong>(found[i].release());

    jlongArray resArray = env->NewLongArray((jsize) found.size());
    env->SetLongArrayRegion(resArray, 0, (jsize) found.size(), res.data());
    return resArray;
}
#endif



extern "C" jlong org_jetbrains_skia_paragraph_FontCollection__1nDefaultFallbackChar
  (kref __Kinstance, jlong ptr, jint unicode, jint fontStyle, jstring locale) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_paragraph_FontCollection__1nDefaultFallbackChar");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_paragraph_FontCollection__1nDefaultFallbackChar
  (kref __Kinstance, jlong ptr, jint unicode, jint fontStyle, jstring locale) {
    FontCollection* instance = reinterpret_cast<FontCollection*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(instance->defaultFallback(unicode, skija::FontStyle::fromJava(fontStyle), skString(env, locale)).release());
}
#endif


extern "C" jlong org_jetbrains_skia_paragraph_FontCollection__1nDefaultFallback
  (kref __Kinstance, jlong ptr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(instance->defaultFallback().release());
}

extern "C" void org_jetbrains_skia_paragraph_FontCollection__1nSetEnableFallback
  (kref __Kinstance, jlong ptr, jboolean value) {
    FontCollection* instance = reinterpret_cast<FontCollection*>(static_cast<uintptr_t>(ptr));
    if (value)
        instance->enableFontFallback();
    else
        instance->disableFontFallback();
}

extern "C" jlong org_jetbrains_skia_paragraph_FontCollection__1nGetParagraphCache
  (kref __Kinstance, jlong ptr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(instance->getParagraphCache());
}
