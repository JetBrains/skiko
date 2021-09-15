
// This file has been auto generated.

#include <iostream>
#include "SkDrawable.h"
#include "SkPictureRecorder.h"
#include "common.h"

static void deletePictureRecorder(SkPictureRecorder* pr) {
    // std::cout << "Deleting [SkPictureRecorder " << PictureRecorder << "]" << std::endl;
    delete pr;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PictureRecorder__1nMake
  () {
    SkPictureRecorder* instance = new SkPictureRecorder();
    return reinterpret_cast<KNativePointer>(instance);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PictureRecorder__1nGetFinalizer
  () {
    return reinterpret_cast<KNativePointer>((&deletePictureRecorder));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PictureRecorder__1nBeginRecording
  (KNativePointer ptr, KFloat left, KFloat top, KFloat right, KFloat bottom) {
    printf("begin %p\n", ptr);
    SkPictureRecorder* instance = reinterpret_cast<SkPictureRecorder*>((ptr));
    SkCanvas* canvas = instance->beginRecording(SkRect::MakeLTRB(left, top, right, bottom), nullptr);
    return reinterpret_cast<KNativePointer>(canvas);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PictureRecorder__1nGetRecordingCanvas
  (KNativePointer ptr) {
    SkPictureRecorder* instance = reinterpret_cast<SkPictureRecorder*>((ptr));
    SkCanvas* canvas = instance->getRecordingCanvas();
    return reinterpret_cast<KNativePointer>(canvas);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PictureRecorder__1nFinishRecordingAsPicture
  (KNativePointer ptr) {
    SkPictureRecorder* instance = reinterpret_cast<SkPictureRecorder*>((ptr));
    SkPicture* picture = instance->finishRecordingAsPicture().release();
    return reinterpret_cast<KNativePointer>(picture);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PictureRecorder__1nFinishRecordingAsPictureWithCull
  (KNativePointer ptr, KFloat left, KFloat top, KFloat right, KFloat bottom) {
    SkPictureRecorder* instance = reinterpret_cast<SkPictureRecorder*>((ptr));
    SkPicture* picture = instance->finishRecordingAsPictureWithCull(SkRect::MakeLTRB(left, top, right, bottom)).release();
    return reinterpret_cast<KNativePointer>(picture);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PictureRecorder__1nFinishRecordingAsDrawable
  (KNativePointer ptr) {
    SkPictureRecorder* instance = reinterpret_cast<SkPictureRecorder*>((ptr));
    SkDrawable* drawable = instance->finishRecordingAsDrawable().release();
    return reinterpret_cast<KNativePointer>(drawable);
}
