
// This file has been auto generated.

#include <iostream>
#include "SkRefCnt.h"
#include "FontCollection.h"
using namespace std;
using namespace skia::textlayout;
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_FontCollection__1nMake
  () {
    FontCollection* ptr = new FontCollection();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_FontCollection__1nGetFontManagersCount
  (KNativePointer ptr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>((ptr));
    return instance->getFontManagersCount();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_FontCollection__1nSetAssetFontManager
  (KNativePointer ptr, KNativePointer fontManagerPtr, KInteropPointer defaultFamilyNameStr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>((ptr));
    SkFontMgr* fontManager = reinterpret_cast<SkFontMgr*>((fontManagerPtr));
    instance->setAssetFontManager(sk_ref_sp(fontManager));
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_FontCollection__1nSetDynamicFontManager
  (KNativePointer ptr, KNativePointer fontManagerPtr, KNativePointer defaultFamilyNameStr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>((ptr));
    SkFontMgr* fontManager = reinterpret_cast<SkFontMgr*>((fontManagerPtr));
    instance->setDynamicFontManager(sk_ref_sp(fontManager));
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_FontCollection__1nSetTestFontManager
  (KNativePointer ptr, KNativePointer fontManagerPtr, KNativePointer defaultFamilyNameStr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>((ptr));
    SkFontMgr* fontManager = reinterpret_cast<SkFontMgr*>((fontManagerPtr));
    instance->setTestFontManager(sk_ref_sp(fontManager));
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_FontCollection__1nSetDefaultFontManager
  (KNativePointer ptr, KNativePointer fontManagerPtr, KNativePointer defaultFamilyNameStr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>((ptr));
    SkFontMgr* fontManager = reinterpret_cast<SkFontMgr*>((fontManagerPtr));

    if (defaultFamilyNameStr == nullptr)
        instance->setDefaultFontManager(sk_ref_sp(fontManager));
    else {
        SkString defaultFamilyName = skString(defaultFamilyNameStr);
        instance->setDefaultFontManager(sk_ref_sp(fontManager), defaultFamilyName.c_str());
    }
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_FontCollection__1nGetFallbackManager
  (KNativePointer ptr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>((ptr));
    return reinterpret_cast<KNativePointer>(instance->getFallbackManager().release());
}


SKIKO_EXPORT KNativePointerArray org_jetbrains_skia_paragraph_FontCollection__1nFindTypefaces
  (KNativePointer ptr, KInteropPointerArray familyNamesArray, KInt fontStyle) {
    TODO("implement org_jetbrains_skia_paragraph_FontCollection__1nFindTypefaces");
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_FontCollection__1nDefaultFallbackChar
  (KNativePointer ptr, KInt unicode, KInt fontStyle, KInteropPointer locale) {
    TODO("implement org_jetbrains_skia_paragraph_FontCollection__1nDefaultFallbackChar");
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_FontCollection__1nDefaultFallback
  (KNativePointer ptr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>((ptr));
    return reinterpret_cast<KNativePointer>(instance->defaultFallback().release());
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_FontCollection__1nSetEnableFallback
  (KNativePointer ptr, KBoolean value) {
    FontCollection* instance = reinterpret_cast<FontCollection*>((ptr));
    if (value)
        instance->enableFontFallback();
    else
        instance->disableFontFallback();
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_FontCollection__1nGetParagraphCache
  (KNativePointer ptr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>((ptr));
    return reinterpret_cast<KNativePointer>(instance->getParagraphCache());
}
