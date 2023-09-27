#include <iostream>
#include "SkPicture.h"
#include "SkDrawable.h"
#include "common.h"

class SkikoDrawable : public SkDrawable {
public:
    SkikoDrawable(KInteropPointer onDraw, KInteropPointer onGetBounds) : _onDraw(onDraw), _onGetBounds(onGetBounds) {};

    void init(KInteropPointer onDraw, KInteropPointer onGetBounds) {
        _onDraw = KVoidCallback(onDraw);
        _onGetBounds = KVoidCallback(onGetBounds);
    }

    SkRect& bounds() { return _bounds; }
    SkCanvas* onDrawCanvas() { return _onDrawCanvas; }
protected:
    void onDraw(SkCanvas* canvas) override {
        _onDrawCanvas = canvas;
        _onDraw();
        _onDrawCanvas = nullptr;
    };
    SkRect onGetBounds() override {
        _onGetBounds();
        return _bounds;
    };
private:
    SkRect _bounds;
    SkCanvas* _onDrawCanvas;
    KVoidCallback _onDraw;
    KVoidCallback _onGetBounds;
};

static void deleteSkikoDrawable(SkikoDrawable* drawable) {
    delete drawable;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Drawable__1nGetFinalizer
  () {
    return reinterpret_cast<KNativePointer>(&deleteSkikoDrawable);
}

SKIKO_EXPORT void org_jetbrains_skia_Drawable__1nSetBounds
  (KNativePointer ptr, KFloat left, KFloat top, KFloat right, KFloat bottom) {
    SkikoDrawable* instance = reinterpret_cast<SkikoDrawable*>(ptr);
    instance->bounds().setLTRB(left, top, right, bottom);
}

SKIKO_EXPORT void org_jetbrains_skia_Drawable__1nGetBounds
  (KNativePointer ptr, KInteropPointer result) {
    SkikoDrawable* instance = reinterpret_cast<SkikoDrawable*>(ptr);
    skija::Rect::copyToInterop(instance->getBounds(), result);
}

SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Drawable__1nGetOnDrawCanvas
  (KNativePointer ptr) {
    SkikoDrawable* instance = reinterpret_cast<SkikoDrawable*>(ptr);
    return reinterpret_cast<KInteropPointer>(instance->onDrawCanvas());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Drawable__1nMake
  (){
    SkikoDrawable* instance = new SkikoDrawable(nullptr, nullptr);
    return reinterpret_cast<KNativePointer>(instance);
}

SKIKO_EXPORT void org_jetbrains_skia_Drawable__1nInit
  (KNativePointer ptr, KInteropPointer onGetBounds, KInteropPointer onDraw) {
    SkikoDrawable* instance = reinterpret_cast<SkikoDrawable*>((ptr));
    instance->init(onDraw, onGetBounds);
}

SKIKO_EXPORT void org_jetbrains_skia_Drawable__1nDraw
  (KNativePointer ptr, KNativePointer canvasPtr, KFloat* matrixArr) {
    SkikoDrawable* instance = reinterpret_cast<SkikoDrawable*>((ptr));
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    std::unique_ptr<SkMatrix> matrix = skMatrix(matrixArr);
    instance->draw(canvas, matrix.get());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Drawable__1nMakePictureSnapshot
  (KNativePointer ptr) {
    SkikoDrawable* instance = reinterpret_cast<SkikoDrawable*>((ptr));
    return reinterpret_cast<KNativePointer>(instance->makePictureSnapshot().release());
}

SKIKO_EXPORT KInt org_jetbrains_skia_Drawable__1nGetGenerationId
  (KNativePointer ptr) {
    SkikoDrawable* instance = reinterpret_cast<SkikoDrawable*>((ptr));
    return instance->getGenerationID();
}

SKIKO_EXPORT void org_jetbrains_skia_Drawable__1nNotifyDrawingChanged
  (KNativePointer ptr) {
    SkikoDrawable* instance = reinterpret_cast<SkikoDrawable*>((ptr));
    return instance->notifyDrawingChanged();
}

