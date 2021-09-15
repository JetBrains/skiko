
// This file has been auto generated.

#include <iostream>
#include "SkRefCnt.h"
#include "FontCollection.h"
using namespace std;
using namespace skia::textlayout;
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_FontCollection__1nMake
  (KInteropPointer __Kinstance) {
    FontCollection* ptr = new FontCollection();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_FontCollection__1nGetFontManagersCount
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>((ptr));
    return instance->getFontManagersCount();
}


SKIKO_EXPORT void org_jetbrains_skia_paragraph_FontCollection__1nSetAssetFontManager
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer fontManagerPtr, KInteropPointer defaultFamilyNameStr) {
    TODO("implement org_jetbrains_skia_paragraph_FontCollection__1nSetAssetFontManager");
}
     
#if 0 
SKIKO_EXPORT void org_jetbrains_skia_paragraph_FontCollection__1nSetAssetFontManager
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer fontManagerPtr, KInteropPointer defaultFamilyNameStr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>((ptr));
    SkFontMgr* fontManager = reinterpret_cast<SkFontMgr*>((fontManagerPtr));
    instance->setAssetFontManager(sk_ref_sp(fontManager));
}
#endif



SKIKO_EXPORT void org_jetbrains_skia_paragraph_FontCollection__1nSetDynamicFontManager
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer fontManagerPtr, KInteropPointer defaultFamilyNameStr) {
    TODO("implement org_jetbrains_skia_paragraph_FontCollection__1nSetDynamicFontManager");
}
     
#if 0 
SKIKO_EXPORT void org_jetbrains_skia_paragraph_FontCollection__1nSetDynamicFontManager
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer fontManagerPtr, KInteropPointer defaultFamilyNameStr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>((ptr));
    SkFontMgr* fontManager = reinterpret_cast<SkFontMgr*>((fontManagerPtr));
    instance->setDynamicFontManager(sk_ref_sp(fontManager));
}
#endif



SKIKO_EXPORT void org_jetbrains_skia_paragraph_FontCollection__1nSetTestFontManager
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer fontManagerPtr, KInteropPointer defaultFamilyNameStr) {
    TODO("implement org_jetbrains_skia_paragraph_FontCollection__1nSetTestFontManager");
}
     
#if 0 
SKIKO_EXPORT void org_jetbrains_skia_paragraph_FontCollection__1nSetTestFontManager
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer fontManagerPtr, KInteropPointer defaultFamilyNameStr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>((ptr));
    SkFontMgr* fontManager = reinterpret_cast<SkFontMgr*>((fontManagerPtr));
    instance->setTestFontManager(sk_ref_sp(fontManager));
}
#endif



SKIKO_EXPORT void org_jetbrains_skia_paragraph_FontCollection__1nSetDefaultFontManager
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer fontManagerPtr, KInteropPointer defaultFamilyNameStr) {
    TODO("implement org_jetbrains_skia_paragraph_FontCollection__1nSetDefaultFontManager");
}
     
#if 0 
SKIKO_EXPORT void org_jetbrains_skia_paragraph_FontCollection__1nSetDefaultFontManager
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer fontManagerPtr, KInteropPointer defaultFamilyNameStr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>((ptr));
    SkFontMgr* fontManager = reinterpret_cast<SkFontMgr*>((fontManagerPtr));

    if (defaultFamilyNameStr == nullptr)
        instance->setDefaultFontManager(sk_ref_sp(fontManager));
    else {
        SkString defaultFamilyName = skString(env, defaultFamilyNameStr);
        instance->setDefaultFontManager(sk_ref_sp(fontManager), defaultFamilyName.c_str());
    }
}
#endif


SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_FontCollection__1nGetFallbackManager
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>((ptr));
    return reinterpret_cast<KNativePointer>(instance->getFallbackManager().release());
}


SKIKO_EXPORT KNativePointerArray org_jetbrains_skia_paragraph_FontCollection__1nFindTypefaces
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointerArray familyNamesArray, KInt fontStyle) {
    TODO("implement org_jetbrains_skia_paragraph_FontCollection__1nFindTypefaces");
}
     
#if 0 
SKIKO_EXPORT KNativePointerArray org_jetbrains_skia_paragraph_FontCollection__1nFindTypefaces
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointerArray familyNamesArray, KInt fontStyle) {
    FontCollection* instance = reinterpret_cast<FontCollection*>((ptr));

    jsize len = env->GetArrayLength(familyNamesArray);
    vector<SkString> familyNames(len);
    for (int i = 0; i < len; ++i) {
        KInteropPointer str = static_cast<KInteropPointer>(env->GetObjectArrayElement(familyNamesArray, i));
        familyNames.push_back(skString(env, str));
        env->DeleteLocalRef(str);
    }

    vector<sk_sp<SkTypeface>> found = instance->findTypefaces(familyNames, skija::FontStyle::fromJava(fontStyle));
    vector<KNativePointer> res(found.size());
    for (int i = 0; i < found.size(); ++i)
        res[i] = reinterpret_cast<KNativePointer>(found[i].release());

    KNativePointerArray resArray = env->NewLongArray((jsize) found.size());
    env->SetLongArrayRegion(resArray, 0, (jsize) found.size(), res.data());
    return resArray;
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_FontCollection__1nDefaultFallbackChar
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt unicode, KInt fontStyle, KInteropPointer locale) {
    TODO("implement org_jetbrains_skia_paragraph_FontCollection__1nDefaultFallbackChar");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_FontCollection__1nDefaultFallbackChar
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt unicode, KInt fontStyle, KInteropPointer locale) {
    FontCollection* instance = reinterpret_cast<FontCollection*>((ptr));
    return reinterpret_cast<KNativePointer>(instance->defaultFallback(unicode, skija::FontStyle::fromJava(fontStyle), skString(env, locale)).release());
}
#endif


SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_FontCollection__1nDefaultFallback
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>((ptr));
    return reinterpret_cast<KNativePointer>(instance->defaultFallback().release());
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_FontCollection__1nSetEnableFallback
  (KInteropPointer __Kinstance, KNativePointer ptr, KBoolean value) {
    FontCollection* instance = reinterpret_cast<FontCollection*>((ptr));
    if (value)
        instance->enableFontFallback();
    else
        instance->disableFontFallback();
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_FontCollection__1nGetParagraphCache
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>((ptr));
    return reinterpret_cast<KNativePointer>(instance->getParagraphCache());
}
