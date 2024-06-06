#include <iostream>
#include "TypefaceFontProvider.h"
#include "SkTypeface.h"
#include "FontMgrWrapper.hh"
using namespace skia::textlayout;
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_TypefaceFontProvider__1nMake
  () {
    TypefaceFontProvider* instance = new TypefaceFontProvider();
    return reinterpret_cast<KNativePointer>(instance);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_TypefaceFontProvider__1nMakeExtended
  () {
    ExtendedTypefaceFontProvider* instance = new ExtendedTypefaceFontProvider();
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

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_TypefaceFontProvider__1nRegisterTypefaceExtended
  (KNativePointer ptr, KNativePointer typefacePtr, KInteropPointer aliasStr) {
    ExtendedTypefaceFontProvider* instance = reinterpret_cast<ExtendedTypefaceFontProvider*>((ptr));
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>((typefacePtr));
    if (aliasStr == nullptr) {
        return instance->registerTypeface(sk_ref_sp(typeface));
    } else {
        return instance->registerTypeface(sk_ref_sp(typeface), skString(aliasStr));
    }
}

