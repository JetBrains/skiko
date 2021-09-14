
// This file has been auto generated.

#include <iostream>
#include "TypefaceFontProvider.h"
#include "SkTypeface.h"
using namespace skia::textlayout;
#include "common.h"

extern "C" jlong org_jetbrains_skia_paragraph_TypefaceFontProvider__1nMake
  () {
    TypefaceFontProvider* instance = new TypefaceFontProvider();
    return reinterpret_cast<jlong>(instance);
}


extern "C" void org_jetbrains_skia_paragraph_TypefaceFontProvider__1nRegisterTypeface
  (jlong ptr, jlong typefacePtr, jstring aliasStr) {
    TODO("implement org_jetbrains_skia_paragraph_TypefaceFontProvider__1nRegisterTypeface");
}
     
#if 0 
extern "C" void org_jetbrains_skia_paragraph_TypefaceFontProvider__1nRegisterTypeface
  (jlong ptr, jlong typefacePtr, jstring aliasStr) {
    TypefaceFontProvider* instance = reinterpret_cast<TypefaceFontProvider*>(static_cast<uintptr_t>(ptr));
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(typefacePtr));
    if (aliasStr == nullptr)
        instance->registerTypeface(sk_ref_sp(typeface));
    else
        instance->registerTypeface(sk_ref_sp(typeface), skString(env, aliasStr));
}
#endif

