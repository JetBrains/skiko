#include <iostream>
#include <jni.h>
#include "interop.hh"
#include "SkDrawable.h"
#include "SkPicture.h"
#include "SkPictureRecorder.h"

static void deletePictureRecorder(SkPictureRecorder* pr) {
    // std::cout << "Deleting [SkPictureRecorder " << PictureRecorder << "]" << std::endl;
    delete pr;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PictureRecorderExternalKt_PictureRecorder_1nMake
  (JNIEnv* env, jclass jclass) {
    SkPictureRecorder* instance = new SkPictureRecorder();
    return reinterpret_cast<jlong>(instance);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PictureRecorderExternalKt_PictureRecorder_1nGetFinalizer
  (JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deletePictureRecorder));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PictureRecorderExternalKt_PictureRecorder_1nBeginRecording
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat left, jfloat top, jfloat right, jfloat bottom, jlong bbh) {
    SkPictureRecorder* instance = reinterpret_cast<SkPictureRecorder*>(static_cast<uintptr_t>(ptr));
    SkBBHFactory* factory = reinterpret_cast<SkBBHFactory*>(static_cast<uintptr_t>(bbh));
    SkCanvas* canvas = instance->beginRecording(SkRect::MakeLTRB(left, top, right, bottom), factory);
    return reinterpret_cast<jlong>(canvas);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PictureRecorderExternalKt_PictureRecorder_1nGetRecordingCanvas
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkPictureRecorder* instance = reinterpret_cast<SkPictureRecorder*>(static_cast<uintptr_t>(ptr));
    SkCanvas* canvas = instance->getRecordingCanvas();
    return reinterpret_cast<jlong>(canvas);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PictureRecorderExternalKt_PictureRecorder_1nFinishRecordingAsPicture
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkPictureRecorder* instance = reinterpret_cast<SkPictureRecorder*>(static_cast<uintptr_t>(ptr));
    SkPicture* picture = instance->finishRecordingAsPicture().release();
    return reinterpret_cast<jlong>(picture);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PictureRecorderExternalKt_PictureRecorder_1nFinishRecordingAsPictureWithCull
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat left, jfloat top, jfloat right, jfloat bottom) {
    SkPictureRecorder* instance = reinterpret_cast<SkPictureRecorder*>(static_cast<uintptr_t>(ptr));
    SkPicture* picture = instance->finishRecordingAsPictureWithCull(SkRect::MakeLTRB(left, top, right, bottom)).release();
    return reinterpret_cast<jlong>(picture);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PictureRecorderExternalKt_PictureRecorder_1nFinishRecordingAsDrawable
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkPictureRecorder* instance = reinterpret_cast<SkPictureRecorder*>(static_cast<uintptr_t>(ptr));
    SkDrawable* drawable = instance->finishRecordingAsDrawable().release();
    return reinterpret_cast<jlong>(drawable);
}