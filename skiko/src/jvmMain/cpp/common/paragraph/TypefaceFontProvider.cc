#include <iostream>
#include <jni.h>
#include "../interop.hh"
#include "TypefaceFontProvider.h"
#include "SkTypeface.h"
#include "FontMgrWithFallbackWrapper.hh"

using namespace skia::textlayout;

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_paragraph_TypefaceFontProviderKt_TypefaceFontProvider_1nMake
  (JNIEnv* env, jclass jclass) {
    TypefaceFontProvider* instance = new TypefaceFontProvider();
    return reinterpret_cast<jlong>(instance);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_paragraph_TypefaceFontProviderKt__1nRegisterTypeface
  (JNIEnv* env, jclass jclass, jlong ptr, jlong typefacePtr, jstring aliasStr) {
    TypefaceFontProvider* instance = reinterpret_cast<TypefaceFontProvider*>(static_cast<uintptr_t>(ptr));
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(typefacePtr));
    if (aliasStr == nullptr) {
        return instance->registerTypeface(sk_ref_sp(typeface));
    } else {
        return instance->registerTypeface(sk_ref_sp(typeface), skString(env, aliasStr));
    }
}


extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_paragraph_TypefaceFontProviderKt_TypefaceFontProvider_1nMakeAsFallbackProvider
  (JNIEnv* env, jclass jclass) {
    TypefaceFontProviderWithFallback* instance = new TypefaceFontProviderWithFallback();
    return reinterpret_cast<jlong>(instance);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_paragraph_TypefaceFontProviderKt__1nRegisterTypefaceForFallback
  (JNIEnv* env, jclass jclass, jlong ptr, jlong typefacePtr, jstring aliasStr) {
    TypefaceFontProviderWithFallback* instance = reinterpret_cast<TypefaceFontProviderWithFallback*>((ptr));
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>((typefacePtr));
    if (aliasStr == nullptr) {
        return instance->registerTypeface(sk_ref_sp(typeface));
    } else {
        return instance->registerTypeface(sk_ref_sp(typeface), skString(env, aliasStr));
    }
}