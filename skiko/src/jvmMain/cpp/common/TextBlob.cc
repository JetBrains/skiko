#include <cstring>
#include <iostream>
#include <jni.h>
#include "SkData.h"
#include "SkSerialProcs.h"
#include "SkTextBlob.h"
#include "interop.hh"
#include "mppinterop.h"
#include "RunRecordClone.hh"

static void unrefTextBlob(SkTextBlob* ptr) {
    ptr->unref();
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_TextBlobKt_TextBlob_1nGetFinalizer
  (JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&unrefTextBlob));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_TextBlobKt__1nBounds
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray resultRect) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
    SkRect bounds = instance->bounds();

    jfloat* floats = env->GetFloatArrayElements(resultRect, 0);
    skikoMpp::skrect::serializeAs4Floats(bounds, floats);
    env->ReleaseFloatArrayElements(resultRect, floats, 0);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_TextBlobKt_TextBlob_1nGetUniqueId
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
    return instance->uniqueID();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_TextBlobKt__1nGetInterceptsLength
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat lower, jfloat upper, jlong paintPtr) {
      SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
      std::vector<float> bounds {lower, upper};
      SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
      int len = instance->getIntercepts(bounds.data(), nullptr, paint);
      return len;
}
extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_TextBlobKt__1nGetIntercepts
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat lower, jfloat upper, jlong paintPtr, jfloatArray resultArray) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    jfloat* floats = env->GetFloatArrayElements(resultArray, 0);
    std::vector<float> bounds {lower, upper};
    instance->getIntercepts(bounds.data(), floats, paint);
    env->ReleaseFloatArrayElements(resultArray, floats, 0);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_TextBlobKt__1nMakeFromPosH
  (JNIEnv* env, jclass jclass, jshortArray glyphsArr, jint glyphsLen, jfloatArray xposArr, jfloat ypos, jlong fontPtr) {
    jshort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);
    jfloat* xpos = env->GetFloatArrayElements(xposArr, nullptr);
    SkFont* font = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(fontPtr));

    SkTextBlob* instance = SkTextBlob::MakeFromPosTextH(glyphs, glyphsLen * sizeof(jshort), xpos, ypos, *font, SkTextEncoding::kGlyphID).release();

    env->ReleaseShortArrayElements(glyphsArr, glyphs, 0);
    env->ReleaseFloatArrayElements(xposArr, xpos, 0);

    return reinterpret_cast<jlong>(instance);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_TextBlobKt__1nMakeFromPos
  (JNIEnv* env, jclass jclass, jshortArray glyphsArr, jint glyphsLen, jfloatArray posArr, jlong fontPtr ) {
    jshort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);
    jfloat* pos = env->GetFloatArrayElements(posArr, nullptr);
    SkFont* font = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(fontPtr));

    SkTextBlob* instance = SkTextBlob::MakeFromPosText(
        glyphs,
        glyphsLen * sizeof(jshort),
        reinterpret_cast<SkPoint*>(pos),
        *font,
        SkTextEncoding::kGlyphID
    ).release();

    env->ReleaseShortArrayElements(glyphsArr, glyphs, 0);
    env->ReleaseFloatArrayElements(posArr, pos, 0);

    return reinterpret_cast<jlong>(instance);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_TextBlobKt__1nMakeFromRSXform
  (JNIEnv* env, jclass jclass, jshortArray glyphsArr, jint glyphsLen, jfloatArray xformArr, jlong fontPtr ) {
    jshort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);
    jfloat* xform = env->GetFloatArrayElements(xformArr, nullptr);
    SkFont* font = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(fontPtr));

    SkTextBlob* instance = SkTextBlob::MakeFromRSXform(glyphs, glyphsLen * sizeof(jshort), reinterpret_cast<SkRSXform*>(xform), *font, SkTextEncoding::kGlyphID).release();

    env->ReleaseShortArrayElements(glyphsArr, glyphs, 0);
    env->ReleaseFloatArrayElements(xformArr, xform, 0);

    return reinterpret_cast<jlong>(instance);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_TextBlobKt_TextBlob_1nSerializeToData
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
    SkData* data = instance->serialize({}).release();
    return reinterpret_cast<jlong>(data);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_TextBlobKt_TextBlob_1nMakeFromData
  (JNIEnv* env, jclass jclass, jlong dataPtr) {
    SkData* data = reinterpret_cast<SkData*>(static_cast<uintptr_t>(dataPtr));
    SkTextBlob* instance = SkTextBlob::Deserialize(data->data(), data->size(), {}).release();
    return reinterpret_cast<jlong>(instance);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_TextBlobKt__1nGetGlyphsLength
  (JNIEnv* env, jclass jclass, jlong ptr) {
  SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
  return skikoMpp::textblob::getGlyphsLength(instance);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_TextBlobKt__1nGetGlyphs
  (JNIEnv* env, jclass jclass, jlong ptr, jshortArray resultArray) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
    jshort * shorts = env->GetShortArrayElements(resultArray, nullptr);
    skikoMpp::textblob::getGlyphs(instance, reinterpret_cast<short*>(shorts));
    env->ReleaseShortArrayElements(resultArray, shorts, 0);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_TextBlobKt__1nGetPositionsLength
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
    return skikoMpp::textblob::getPositionsLength(instance);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_TextBlobKt__1nGetPositions
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray resultArray) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));

    jfloat* positions = env->GetFloatArrayElements(resultArray, 0);
    skikoMpp::textblob::getPositions(instance, reinterpret_cast<float*>(positions));
    env->ReleaseFloatArrayElements(resultArray, positions, 0);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_TextBlobKt__1nGetClustersLength
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
    return skikoMpp::textblob::getClustersLength(instance);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_TextBlobKt__1nGetClusters
  (JNIEnv* env, jclass jclass, jlong ptr, jintArray resultArray) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));

    jint* clusters = env->GetIntArrayElements(resultArray, 0);
    auto hasValue = skikoMpp::textblob::getClusters(instance, reinterpret_cast<int*>(clusters));
    env->ReleaseIntArrayElements(resultArray, clusters, 0);

    return hasValue;
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_TextBlobKt__1nGetTightBounds
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray resultArray) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));

    auto bounds = skikoMpp::textblob::getTightBounds(instance);
    if (!bounds) return false;

    jfloat* floats = env->GetFloatArrayElements(resultArray, 0);
    skikoMpp::skrect::serializeAs4Floats(*bounds, floats);
    env->ReleaseFloatArrayElements(resultArray, floats, 0);

    return true;
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_TextBlobKt__1nGetBlockBounds
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray resultArray) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));

    std::unique_ptr<SkRect> bounds = skikoMpp::textblob::getBlockBounds(instance);
    if (!bounds) return false;

    jfloat* floats = env->GetFloatArrayElements(resultArray, 0);
    skikoMpp::skrect::serializeAs4Floats(*bounds, floats);
    env->ReleaseFloatArrayElements(resultArray, floats, 0);
    return true;
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_TextBlobKt__1nGetFirstBaseline
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray resultArray) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
    jfloat* floats = env->GetFloatArrayElements(resultArray, 0);
    auto hasValue = skikoMpp::textblob::getFirstBaseline(instance, reinterpret_cast<float*>(floats));
    env->ReleaseFloatArrayElements(resultArray, floats, 0);
    return hasValue;
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_TextBlobKt__1nGetLastBaseline
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray resultArray) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
    jfloat* floats = env->GetFloatArrayElements(resultArray, 0);
    auto hasValue = skikoMpp::textblob::getLastBaseline(instance, reinterpret_cast<float*>(floats));
    env->ReleaseFloatArrayElements(resultArray, floats, 0);
    return hasValue;
}
