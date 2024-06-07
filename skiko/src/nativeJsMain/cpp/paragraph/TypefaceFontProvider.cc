#include <iostream>
#include "TypefaceFontProvider.h"
#include "SkTypeface.h"
#include "FontMgrWithFallbackWrapper.hh"
using namespace skia::textlayout;
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_TypefaceFontProvider__1nMake
  () {
    TypefaceFontProvider* instance = new TypefaceFontProvider();
    return reinterpret_cast<KNativePointer>(instance);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_TypefaceFontProvider__1nMakeAsFallbackProvider
  () {
    TypefaceFontProviderWithFallback* instance = new TypefaceFontProviderWithFallback();
    return reinterpret_cast<KNativePointer>(instance);
}

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_TypefaceFontProvider__1nRegisterTypeface
  (KNativePointer ptr, KNativePointer typefacePtr, KInteropPointer aliasStr) {
    TypefaceFontProvider* instance = reinterpret_cast<TypefaceFontProvider*>((ptr));
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>((typefacePtr));
    if (aliasStr == nullptr) {
        return instance->registerTypeface(sk_ref_sp(typeface));
    } else {
        return instance->registerTypeface(sk_ref_sp(typeface), skString(aliasStr));
    }
}

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_TypefaceFontProvider__1nRegisterTypefaceForFallback
  (KNativePointer ptr, KNativePointer typefacePtr, KInteropPointer aliasStr) {
    TypefaceFontProviderWithFallback* instance = reinterpret_cast<TypefaceFontProviderWithFallback*>((ptr));
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>((typefacePtr));
    if (aliasStr == nullptr) {
        return instance->registerTypeface(sk_ref_sp(typeface));
    } else {
        return instance->registerTypeface(sk_ref_sp(typeface), skString(aliasStr));
    }
}

