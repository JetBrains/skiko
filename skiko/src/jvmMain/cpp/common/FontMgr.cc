#include <iostream>
#include <jni.h>
#include "interop.hh"
#include "SkData.h"
#include "SkTypeface.h"
#include "SkFontMgr.h"
#include "FontMgrWithFallbackWrapper.hh"

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_FontMgrKt__1nGetFamiliesCount
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkFontMgr* instance = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(ptr));
    return instance->countFamilies();
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_FontMgrKt__1nGetFamilyName
  (JNIEnv* env, jclass jclass, jlong ptr, jint index) {
    SkFontMgr* instance = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(ptr));
    SkString familyName;
    instance->getFamilyName(index, &familyName);
    return reinterpret_cast<jlong>(new SkString(familyName));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_FontMgrKt__1nMakeStyleSet
  (JNIEnv* env, jclass jclass, jlong ptr, jint index) {
    SkFontMgr* instance = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(ptr));
    SkFontStyleSet* styleSet = instance->createStyleSet(index).release();
    return reinterpret_cast<jlong>(styleSet);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_FontMgrKt__1nMatchFamily
  (JNIEnv* env, jclass jclass, jlong ptr, jstring familyNameStr) {
    SkFontMgr* instance = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(ptr));
    SkString familyName = skString(env, familyNameStr);
    SkFontStyleSet* styleSet = instance->matchFamily(familyName.c_str()).release();
    return reinterpret_cast<jlong>(styleSet);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_FontMgrKt__1nMatchFamilyStyle
  (JNIEnv* env, jclass jclass, jlong ptr, jstring familyNameStr, jint fontStyle) {
    SkFontMgr* instance = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(ptr));
    SkString familyName = skString(env, familyNameStr);
    SkTypeface* typeface = instance->matchFamilyStyle(familyName.c_str(), skija::FontStyle::fromJava(fontStyle)).release();
    return reinterpret_cast<jlong>(typeface);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_FontMgrKt__1nMatchFamilyStyleCharacter
  (JNIEnv* env, jclass jclass, jlong ptr, jstring familyNameStr, jint fontStyle, jobjectArray bcp47Array, jint bcp47size, jint character) {
    SkFontMgr* instance = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(ptr));

    SkString familyName = skString(env, familyNameStr);
    
    std::vector<SkString> bcp47Strings = skStringVector(env, bcp47Array);
    std::vector<const char*> bcp47(bcp47Strings.size());
    for (int i = 0; i < bcp47.size(); ++i)
        bcp47[i] = bcp47Strings[i].c_str();
    
    SkTypeface* typeface = instance->matchFamilyStyleCharacter(familyName.c_str(), skija::FontStyle::fromJava(fontStyle), bcp47.data(), (int) bcp47.size(), character).release();
    
    return reinterpret_cast<jlong>(typeface);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_FontMgrKt__1nMakeFromData
  (JNIEnv* env, jclass jclass, jlong ptr, jlong dataPtr, jint ttcIndex) {
    SkFontMgr* instance = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(ptr));
    SkData* data = reinterpret_cast<SkData*>(static_cast<uintptr_t>(dataPtr));
    SkTypeface* typeface = instance->makeFromData(sk_ref_sp(data), ttcIndex).release();
    return reinterpret_cast<jlong>(typeface);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_FontMgrKt__1nDefault
  (JNIEnv* env, jclass jclass) {
    SkFontMgr* instance = SkFontMgr::RefDefault().release();
    return reinterpret_cast<jlong>(instance);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_FontMgrKt__1nDefaultWithFallbackFontProvider
(JNIEnv* env, jclass jclass, jlong fallbackPtr) {
    TypefaceFontProviderWithFallback* fallback = reinterpret_cast<TypefaceFontProviderWithFallback*>((fallbackPtr));
    FontMgrWithFallbackWrapper* instance = new FontMgrWithFallbackWrapper(sk_ref_sp(fallback));
    return reinterpret_cast<jlong>(instance);
}