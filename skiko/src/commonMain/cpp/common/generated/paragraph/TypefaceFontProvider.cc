
// This file has been auto generated.

#include <iostream>
#include "TypefaceFontProvider.h"
#include "SkTypeface.h"
using namespace skia::textlayout;
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_TypefaceFontProvider__1nMake
  (KInteropPointer __Kinstance) {
    TypefaceFontProvider* instance = new TypefaceFontProvider();
    return reinterpret_cast<KNativePointer>(instance);
}


SKIKO_EXPORT void org_jetbrains_skia_paragraph_TypefaceFontProvider__1nRegisterTypeface
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer typefacePtr, KInteropPointer aliasStr) {
    TODO("implement org_jetbrains_skia_paragraph_TypefaceFontProvider__1nRegisterTypeface");
}
     
#if 0 
SKIKO_EXPORT void org_jetbrains_skia_paragraph_TypefaceFontProvider__1nRegisterTypeface
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer typefacePtr, KInteropPointer aliasStr) {
    TypefaceFontProvider* instance = reinterpret_cast<TypefaceFontProvider*>((ptr));
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>((typefacePtr));
    if (aliasStr == nullptr)
        instance->registerTypeface(sk_ref_sp(typeface));
    else
        instance->registerTypeface(sk_ref_sp(typeface), skString(env, aliasStr));
}
#endif

