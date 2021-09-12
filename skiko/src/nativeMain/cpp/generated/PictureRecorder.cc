
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
  (kref __Kinstance) {
    SkPictureRecorder* instance = new SkPictureRecorder();
    return reinterpret_cast<jlong>(instance);
}

extern "C" jlong org_jetbrains_skia_PictureRecorder__1nGetFinalizer
  (kref __Kinstance) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deletePictureRecorder));
}

extern "C" jlong org_jetbrains_skia_PictureRecorder__1nBeginRecording
  (kref __Kinstance, jlong ptr, jfloat left, jfloat top, jfloat right, jfloat bottom) {
    SkPictureRecorder* instance = reinterpret_cast<SkPictureRecorder*>(static_cast<uintptr_t>(ptr));
    SkCanvas* canvas = instance->beginRecording(SkRect::MakeLTRB(left, top, right, bottom), nullptr);
    return reinterpret_cast<jlong>(canvas);
}

extern "C" jlong org_jetbrains_skia_PictureRecorder__1nGetRecordingCanvas
  (kref __Kinstance, jlong ptr) {
    SkPictureRecorder* instance = reinterpret_cast<SkPictureRecorder*>(static_cast<uintptr_t>(ptr));
    SkCanvas* canvas = instance->getRecordingCanvas();
    return reinterpret_cast<jlong>(canvas);
}

extern "C" jlong org_jetbrains_skia_PictureRecorder__1nFinishRecordingAsPicture
  (kref __Kinstance, jlong ptr) {
    SkPictureRecorder* instance = reinterpret_cast<SkPictureRecorder*>(static_cast<uintptr_t>(ptr));
    SkPicture* picture = instance->finishRecordingAsPicture().release();
    return reinterpret_cast<jlong>(picture);
}

extern "C" jlong org_jetbrains_skia_PictureRecorder__1nFinishRecordingAsPictureWithCull
  (kref __Kinstance, jlong ptr, jfloat left, jfloat top, jfloat right, jfloat bottom) {
    SkPictureRecorder* instance = reinterpret_cast<SkPictureRecorder*>(static_cast<uintptr_t>(ptr));
    SkPicture* picture = instance->finishRecordingAsPictureWithCull(SkRect::MakeLTRB(left, top, right, bottom)).release();
    return reinterpret_cast<jlong>(picture);
}

extern "C" jlong org_jetbrains_skia_PictureRecorder__1nFinishRecordingAsDrawable
  (kref __Kinstance, jlong ptr) {
    SkPictureRecorder* instance = reinterpret_cast<SkPictureRecorder*>(static_cast<uintptr_t>(ptr));
    SkDrawable* drawable = instance->finishRecordingAsDrawable().release();
    return reinterpret_cast<jlong>(drawable);
}
