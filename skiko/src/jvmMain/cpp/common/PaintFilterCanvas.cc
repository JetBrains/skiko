#include <iostream>
#include <jni.h>
#include "SkCanvas.h"
#include "SkDrawable.h"
#include "SkPaintFilterCanvas.h"
#include "interop.hh"

class SkikoPaintFilterCanvas : public SkPaintFilterCanvas {
public:
    SkikoPaintFilterCanvas(
        SkCanvas* canvas,
        bool unrollDrawable
    ) : SkPaintFilterCanvas(canvas), unrollDrawable(unrollDrawable) {}

    virtual ~SkikoPaintFilterCanvas() {
        skija::PaintFilterCanvas::detach(jobj);
    }

    jobject jobj;

protected:
    bool onFilter(SkPaint& paint) const override {
        return skija::PaintFilterCanvas::onFilter(jobj, paint);
    }

    void onDrawDrawable(SkDrawable* drawable, const SkMatrix* matrix) override {
        if (unrollDrawable) {
            drawable->draw(this, matrix);
        } else {
            SkPaintFilterCanvas::onDrawDrawable(drawable, matrix);
        }
    }

private:
    bool unrollDrawable;
};

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PaintFilterCanvas_1jvmKt_PaintFilterCanvas_1nInit
  (JNIEnv* env, jclass jclass, jobject jobj, jlong canvasPtr) {
    SkikoPaintFilterCanvas* canvas = reinterpret_cast<SkikoPaintFilterCanvas*>(static_cast<uintptr_t>(canvasPtr));
    canvas->jobj = skija::PaintFilterCanvas::attach(env, jobj);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PaintFilterCanvasKt_PaintFilterCanvas_1nMake
  (JNIEnv* env, jclass jclass, jlong canvasPtr, jboolean unrollDrawable) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    SkikoPaintFilterCanvas* filterCanvas = new SkikoPaintFilterCanvas(canvas, unrollDrawable);
    return reinterpret_cast<jlong>(filterCanvas);
}