
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

static SkFontStyle fromKotlin(KInt style) {
   return SkFontStyle(style & 0xFFFF, (style >> 16) & 0xFF, static_cast<SkFontStyle::Slant>((style >> 24) & 0xFF));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_FontCollection__1nFindTypefaces
  (KNativePointer ptr, KInteropPointerArray familyNamesArray, KInt len, KInt fontStyle) {
    FontCollection* instance = reinterpret_cast<FontCollection*>(ptr);

    vector<sk_sp<SkTypeface>> found = instance->findTypefaces(skStringVector(familyNamesArray, len), fromKotlin(fontStyle));

    std::vector<KNativePointer>* res = new std::vector<KNativePointer>();
    for (auto& f : found)
        res->push_back(reinterpret_cast<KNativePointer>(f.release()));

    return reinterpret_cast<KNativePointer>(res);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_FontCollection__1nDefaultFallbackChar
  (KNativePointer ptr, KInt unicode, KInt fontStyle, KInteropPointer locale) {
    FontCollection* instance = reinterpret_cast<FontCollection*>(ptr);
    return reinterpret_cast<KNativePointer>(instance->defaultFallback(unicode, skija::FontStyle::fromKotlin(fontStyle), skString(locale)).release());
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
