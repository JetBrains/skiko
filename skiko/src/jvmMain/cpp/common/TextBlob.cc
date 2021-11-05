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
  (JNIEnv* env, jclass jclass, jshortArray glyphsArr, jfloatArray xposArr, jfloat ypos, jlong fontPtr) {
    jsize len = env->GetArrayLength(glyphsArr);
    jshort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);
    jfloat* xpos = env->GetFloatArrayElements(xposArr, nullptr);
    SkFont* font = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(fontPtr));

    SkTextBlob* instance = SkTextBlob::MakeFromPosTextH(glyphs, len * sizeof(jshort), xpos, ypos, *font, SkTextEncoding::kGlyphID).release();

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
  (JNIEnv* env, jclass jclass, jshortArray glyphsArr, jfloatArray xformArr, jlong fontPtr ) {
    jsize len = env->GetArrayLength(glyphsArr);
    jshort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);
    jfloat* xform = env->GetFloatArrayElements(xformArr, nullptr);
    SkFont* font = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(fontPtr));

    SkTextBlob* instance = SkTextBlob::MakeFromRSXform(glyphs, len * sizeof(jshort), reinterpret_cast<SkRSXform*>(xform), *font, SkTextEncoding::kGlyphID).release();

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
    skikoMpp::textblob::getGlyphs(instance, shorts);
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
    skikoMpp::textblob::getPositions(instance, positions);
    env->ReleaseFloatArrayElements(resultArray, positions, 0);
}

extern "C" JNIEXPORT jintArray JNICALL Java_org_jetbrains_skia_TextBlobKt__1nGetClusters
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
    SkTextBlob::Iter iter(*instance);
    SkTextBlob::Iter::Run run;
    std::vector<jint> clusters;
    size_t stored = 0;
    // uint32_t cluster8 = 0;
    uint32_t runStart16 = 0;
    while (iter.next(&run)) {
        // run.fGlyphIndices points directly to runRecord.glyphBuffer(), which comes directly after RunRecord itself
        auto runRecord = reinterpret_cast<const RunRecordClone*>(run.fGlyphIndices) - 1;
        if (!runRecord->isExtended())
            return nullptr;

        skija::UtfIndicesConverter conv(runRecord->textBuffer(), runRecord->textSize());
        clusters.resize(stored + run.fGlyphCount);
        uint32_t* clusterBuffer = runRecord->clusterBuffer();
        for (int i = 0; i < run.fGlyphCount; ++i)
            clusters[stored + i] = runStart16 + conv.from8To16(clusterBuffer[i]);
        runStart16 += conv.from8To16(runRecord->textSize());
        // memcpy(&clusters[stored], runRecord->clusterBuffer(), run.fGlyphCount * sizeof(uint32_t));

        stored += run.fGlyphCount;
    }
    return javaIntArray(env, clusters);
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

extern "C" JNIEXPORT jobject JNICALL Java_org_jetbrains_skia_TextBlobKt__1nGetFirstBaseline
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
    SkTextBlob::Iter iter(*instance);
    SkTextBlob::Iter::Run run;
    while (iter.next(&run)) {
        // run.fGlyphIndices points directly to runRecord.glyphBuffer(), which comes directly after RunRecord itself
        auto runRecord = reinterpret_cast<const RunRecordClone*>(run.fGlyphIndices) - 1;
        if (runRecord->positioning() != 2) // kFull_Positioning
            return nullptr;

        return javaFloat(env, runRecord->posBuffer()[1]);
    }
    return nullptr;
}

extern "C" JNIEXPORT jobject JNICALL Java_org_jetbrains_skia_TextBlobKt__1nGetLastBaseline
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
    SkTextBlob::Iter iter(*instance);
    SkTextBlob::Iter::Run run;
    SkScalar baseline = 0;
    while (iter.next(&run)) {
        // run.fGlyphIndices points directly to runRecord.glyphBuffer(), which comes directly after RunRecord itself
        auto runRecord = reinterpret_cast<const RunRecordClone*>(run.fGlyphIndices) - 1;
        if (runRecord->positioning() != 2) // kFull_Positioning
            return nullptr;

        baseline = std::max(baseline, runRecord->posBuffer()[1]);
    }
    return javaFloat(env, baseline);
}
