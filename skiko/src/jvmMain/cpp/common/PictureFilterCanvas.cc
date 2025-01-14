#include <jni.h>
#include "SkNWayCanvas.h"
#include "interop.hh"

class SkikoPictureFilterCanvas : public SkNWayCanvas {
public:
    SkikoPictureFilterCanvas(SkCanvas* canvas) :
        SkNWayCanvas(canvas->imageInfo().width(), canvas->imageInfo().height()),
        _jobject(nullptr) {
        this->addCanvas(canvas);
    }

    virtual ~SkikoPictureFilterCanvas() {
        skija::PictureFilterCanvas::detach(_jobject);
    }

    jobject _jobject;

protected:
    void onDrawPicture(const SkPicture* picture, const SkMatrix* matrix, const SkPaint* paint) override {
        jboolean handled = skija::PictureFilterCanvas::onDrawPicture(_jobject, picture, matrix, paint);
        if (!handled) {
            SkCanvas::onDrawPicture(picture, matrix, paint);
        }
    }
};

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PictureFilterCanvas_1jvmKt_PictureFilterCanvas_1nInit
  (JNIEnv* env, jclass jclass, jobject pictureFilterCanvas, jlong canvasPtr) {
    SkikoPictureFilterCanvas* canvas = reinterpret_cast<SkikoPictureFilterCanvas*>(static_cast<uintptr_t>(canvasPtr));
    canvas->_jobject = skija::PictureFilterCanvas::attach(env, pictureFilterCanvas);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PictureFilterCanvasKt_PictureFilterCanvas_1nMake
  (JNIEnv* env, jclass jclass, SkCanvas* canvas) {
    SkikoPictureFilterCanvas* filterCanvas = new SkikoPictureFilterCanvas(canvas);
    return reinterpret_cast<jlong>(filterCanvas);
}
