
// This file has been auto generated.

#include <iostream>
#include "SkDrawable.h"
#include "SkPictureRecorder.h"
#include "common.h"

static void deletePictureRecorder(SkPictureRecorder* pr) {
    // std::cout << "Deleting [SkPictureRecorder " << PictureRecorder << "]" << std::endl;
    delete pr;
}

extern "C" jlong org_jetbrains_skia_PictureRecorder__1nMake
  () {
    SkPictureRecorder* instance = new SkPictureRecorder();
    return reinterpret_cast<jlong>(instance);
}

extern "C" jlong org_jetbrains_skia_PictureRecorder__1nGetFinalizer
  () {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deletePictureRecorder));
}

extern "C" jlong org_jetbrains_skia_PictureRecorder__1nBeginRecording
  (jlong ptr, jfloat left, jfloat top, jfloat right, jfloat bottom) {
    SkPictureRecorder* instance = reinterpret_cast<SkPictureRecorder*>(static_cast<uintptr_t>(ptr));
    SkCanvas* canvas = instance->beginRecording(SkRect::MakeLTRB(left, top, right, bottom), nullptr);
    return reinterpret_cast<jlong>(canvas);
}

extern "C" jlong org_jetbrains_skia_PictureRecorder__1nGetRecordingCanvas
  (jlong ptr) {
    SkPictureRecorder* instance = reinterpret_cast<SkPictureRecorder*>(static_cast<uintptr_t>(ptr));
    SkCanvas* canvas = instance->getRecordingCanvas();
    return reinterpret_cast<jlong>(canvas);
}

extern "C" jlong org_jetbrains_skia_PictureRecorder__1nFinishRecordingAsPicture
  (jlong ptr) {
    SkPictureRecorder* instance = reinterpret_cast<SkPictureRecorder*>(static_cast<uintptr_t>(ptr));
    SkPicture* picture = instance->finishRecordingAsPicture().release();
    return reinterpret_cast<jlong>(picture);
}

extern "C" jlong org_jetbrains_skia_PictureRecorder__1nFinishRecordingAsPictureWithCull
  (jlong ptr, jfloat left, jfloat top, jfloat right, jfloat bottom) {
    SkPictureRecorder* instance = reinterpret_cast<SkPictureRecorder*>(static_cast<uintptr_t>(ptr));
    SkPicture* picture = instance->finishRecordingAsPictureWithCull(SkRect::MakeLTRB(left, top, right, bottom)).release();
    return reinterpret_cast<jlong>(picture);
}

extern "C" jlong org_jetbrains_skia_PictureRecorder__1nFinishRecordingAsDrawable
  (jlong ptr) {
    SkPictureRecorder* instance = reinterpret_cast<SkPictureRecorder*>(static_cast<uintptr_t>(ptr));
    SkDrawable* drawable = instance->finishRecordingAsDrawable().release();
    return reinterpret_cast<jlong>(drawable);
}
