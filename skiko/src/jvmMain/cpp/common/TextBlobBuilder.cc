#include <iostream>
#include <jni.h>
#include "SkTextBlob.h"
#include "interop.hh"
#include "mppinterop.h"

static void deleteTextBlobBuilder(SkTextBlobBuilder* ptr) {
    delete ptr;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_TextBlobBuilderKt_TextBlobBuilder_1nGetFinalizer
  (JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteTextBlobBuilder));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_TextBlobBuilderKt_TextBlobBuilder_1nMake
  (JNIEnv* env, jclass jclass) {
    return reinterpret_cast<jlong>(new SkTextBlobBuilder());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_TextBlobBuilderKt__1nBuild
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkTextBlobBuilder* instance = reinterpret_cast<SkTextBlobBuilder*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(instance->make().release());
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_TextBlobBuilderKt__1nAppendRun
  (JNIEnv* env, jclass jclass, jlong ptr, jlong fontPtr, jshortArray glyphsArr, jint glyphsLen, jfloat x, jfloat y, jfloatArray rectFloats) {
    SkTextBlobBuilder* instance = reinterpret_cast<SkTextBlobBuilder*>(static_cast<uintptr_t>(ptr));
    SkFont* font = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(fontPtr));

    jfloat* skRectFloats = rectFloats != NULL ? env->GetFloatArrayElements(rectFloats, NULL) : NULL;
    std::unique_ptr<SkRect> bounds = skikoMpp::skrect::toSkRect(reinterpret_cast<float*>(skRectFloats));
    if (rectFloats != NULL) {
        env->ReleaseFloatArrayElements(rectFloats, skRectFloats, 0);
    }

    SkTextBlobBuilder::RunBuffer run = instance->allocRun(*font, glyphsLen, x, y, bounds.get());
    env->GetShortArrayRegion(glyphsArr, 0, glyphsLen, reinterpret_cast<jshort*>(run.glyphs));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_TextBlobBuilderKt__1nAppendRunPosH
  (JNIEnv* env, jclass jclass, jlong ptr, jlong fontPtr, jshortArray glyphsArr, jint glyphsLen, jfloatArray xsArr, jfloat y, jfloatArray rectFloats) {
    SkTextBlobBuilder* instance = reinterpret_cast<SkTextBlobBuilder*>(static_cast<uintptr_t>(ptr));
    SkFont* font = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(fontPtr));

    jfloat* skRectFloats = rectFloats != NULL ? env->GetFloatArrayElements(rectFloats, NULL) : NULL;
    std::unique_ptr<SkRect> bounds = skikoMpp::skrect::toSkRect(reinterpret_cast<float*>(skRectFloats));
    if (rectFloats != NULL) {
        env->ReleaseFloatArrayElements(rectFloats, skRectFloats, 0);
    }

    SkTextBlobBuilder::RunBuffer run = instance->allocRunPosH(*font, glyphsLen, y, bounds.get());
    env->GetShortArrayRegion(glyphsArr, 0, glyphsLen, reinterpret_cast<jshort*>(run.glyphs));
    env->GetFloatArrayRegion(xsArr, 0, glyphsLen, reinterpret_cast<jfloat*>(run.pos));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_TextBlobBuilderKt__1nAppendRunPos
  (JNIEnv* env, jclass jclass, jlong ptr, jlong fontPtr, jshortArray glyphsArr, jint glyphsLen, jfloatArray posArr, jfloatArray rectFloats) {
    SkTextBlobBuilder* instance = reinterpret_cast<SkTextBlobBuilder*>(static_cast<uintptr_t>(ptr));
    SkFont* font = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(fontPtr));

    jfloat* skRectFloats = rectFloats != NULL ? env->GetFloatArrayElements(rectFloats, NULL) : NULL;
    std::unique_ptr<SkRect> bounds = skikoMpp::skrect::toSkRect(reinterpret_cast<float*>(skRectFloats));
    if (rectFloats != NULL) {
        env->ReleaseFloatArrayElements(rectFloats, skRectFloats, 0);
    }

    SkTextBlobBuilder::RunBuffer run = instance->allocRunPos(*font, glyphsLen, bounds.get());
    env->GetShortArrayRegion(glyphsArr, 0, glyphsLen, reinterpret_cast<jshort*>(run.glyphs));
    env->GetFloatArrayRegion(posArr, 0, glyphsLen * 2, reinterpret_cast<jfloat*>(run.pos));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_TextBlobBuilderKt__1nAppendRunRSXform
  (JNIEnv* env, jclass jclass, jlong ptr, jlong fontPtr, jshortArray glyphsArr, jfloatArray xformArr) {
    SkTextBlobBuilder* instance = reinterpret_cast<SkTextBlobBuilder*>(static_cast<uintptr_t>(ptr));
    SkFont* font = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(fontPtr));
    jsize len = env->GetArrayLength(glyphsArr);
    SkTextBlobBuilder::RunBuffer run = instance->allocRunRSXform(*font, len);
    env->GetShortArrayRegion(glyphsArr, 0, len, reinterpret_cast<jshort*>(run.glyphs));
    env->GetFloatArrayRegion(xformArr, 0, len * 4, reinterpret_cast<jfloat*>(run.pos));
}
