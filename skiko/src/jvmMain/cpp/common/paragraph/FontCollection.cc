#include <iostream>
#include <jni.h>
#include "../interop.hh"
#include "SkRefCnt.h"
#include "FontCollection.h"

using namespace std;
using namespace skia::textlayout;

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_paragraph_FontCollectionExternalKt_FontCollection_1nMake
  (JNIEnv* env, jclass jclass) {
    FontCollection* ptr = new FontCollection();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_paragraph_FontCollectionExternalKt_FontCollection_1nGetFontManagersCount
  (JNIEnv* env, jclass jclass, jlong ptr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>(static_cast<uintptr_t>(ptr));
    return instance->getFontManagersCount();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_paragraph_FontCollectionExternalKt_FontCollection_1nSetAssetFontManager
  (JNIEnv* env, jclass jclass, jlong ptr, jlong fontManagerPtr, jstring defaultFamilyNameStr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>(static_cast<uintptr_t>(ptr));
    SkFontMgr* fontManager = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(fontManagerPtr));
    instance->setAssetFontManager(sk_ref_sp(fontManager));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_paragraph_FontCollectionExternalKt_FontCollection_1nSetDynamicFontManager
  (JNIEnv* env, jclass jclass, jlong ptr, jlong fontManagerPtr, jstring defaultFamilyNameStr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>(static_cast<uintptr_t>(ptr));
    SkFontMgr* fontManager = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(fontManagerPtr));
    instance->setDynamicFontManager(sk_ref_sp(fontManager));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_paragraph_FontCollectionExternalKt_FontCollection_1nSetTestFontManager
  (JNIEnv* env, jclass jclass, jlong ptr, jlong fontManagerPtr, jstring defaultFamilyNameStr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>(static_cast<uintptr_t>(ptr));
    SkFontMgr* fontManager = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(fontManagerPtr));
    instance->setTestFontManager(sk_ref_sp(fontManager));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_paragraph_FontCollectionExternalKt_FontCollection_1nSetDefaultFontManager
  (JNIEnv* env, jclass jclass, jlong ptr, jlong fontManagerPtr, jstring defaultFamilyNameStr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>(static_cast<uintptr_t>(ptr));
    SkFontMgr* fontManager = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(fontManagerPtr));

    if (defaultFamilyNameStr == nullptr)
        instance->setDefaultFontManager(sk_ref_sp(fontManager));
    else {
        SkString defaultFamilyName = skString(env, defaultFamilyNameStr);
        instance->setDefaultFontManager(sk_ref_sp(fontManager), defaultFamilyName.c_str());
    }
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_paragraph_FontCollectionExternalKt_FontCollection_1nGetFallbackManager
  (JNIEnv* env, jclass jclass, jlong ptr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(instance->getFallbackManager().release());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_paragraph_FontCollectionExternalKt_FontCollection_1nFindTypefaces
  (JNIEnv* env, jclass jclass, jlong ptr, jobjectArray familyNamesArray, jsize len, jint fontStyle) {
    FontCollection* instance = reinterpret_cast<FontCollection*>(static_cast<uintptr_t>(ptr));

    vector<sk_sp<SkTypeface>> found = instance->findTypefaces(skStringVector(env, familyNamesArray), skija::FontStyle::fromJava(fontStyle));

    std::vector<jlong>* res = new std::vector<jlong>();
    for (auto& f : found)
        res->push_back(reinterpret_cast<jlong>(f.release()));

    return reinterpret_cast<jlong>(res);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_paragraph_FontCollectionExternalKt_FontCollection_1nDefaultFallbackChar
  (JNIEnv* env, jclass jclass, jlong ptr, jint unicode, jint fontStyle, jstring locale) {
    FontCollection* instance = reinterpret_cast<FontCollection*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(instance->defaultFallback(unicode, skija::FontStyle::fromJava(fontStyle), skString(env, locale)).release());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_paragraph_FontCollectionExternalKt_FontCollection_1nDefaultFallback
  (JNIEnv* env, jclass jclass, jlong ptr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(instance->defaultFallback().release());
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_paragraph_FontCollectionExternalKt_FontCollection_1nSetEnableFallback
  (JNIEnv* env, jclass jclass, jlong ptr, jboolean value) {
    FontCollection* instance = reinterpret_cast<FontCollection*>(static_cast<uintptr_t>(ptr));
    if (value)
        instance->enableFontFallback();
    else
        instance->disableFontFallback();
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_paragraph_FontCollectionExternalKt_FontCollection_1nGetParagraphCache
  (JNIEnv* env, jclass jclass, jlong ptr) {
    FontCollection* instance = reinterpret_cast<FontCollection*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(instance->getParagraphCache());
}