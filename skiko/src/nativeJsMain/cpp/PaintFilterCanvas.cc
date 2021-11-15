
// This file has been auto generated.

#include <iostream>
#include "SkCanvas.h"
#include "SkDrawable.h"
#include "SkPaintFilterCanvas.h"
#include "common.h"

class SkijaPaintFilterCanvas : public SkPaintFilterCanvas {
public:
    SkijaPaintFilterCanvas(
        SkCanvas* canvas,
        bool unrollDrawable
    ) : SkPaintFilterCanvas(canvas), unrollDrawable(unrollDrawable), _onFilter(nullptr), _onFilterPaint(nullptr) {}

    SkPaint* onFilterPaint() const {
        return _onFilterPaint;
    }

    void init(KInteropPointer onFilter) {
        _onFilter = KBooleanCallback(onFilter);
    }
protected:
    bool onFilter(SkPaint& paint) const override {
        _onFilterPaint = &paint;
        KBoolean result = _onFilter();
        _onFilterPaint = nullptr;
        return static_cast<bool>(result);
    }

    void onDrawDrawable(SkDrawable* drawable, const SkMatrix* matrix) override {
        if (unrollDrawable) {
            drawable->draw(this, matrix);
        } else {
            SkPaintFilterCanvas::onDrawDrawable(drawable, matrix);
        }
    }

private:
    KBooleanCallback _onFilter;
    mutable SkPaint* _onFilterPaint;
    bool unrollDrawable;
};

SKIKO_EXPORT void org_jetbrains_skia_PaintFilterCanvas__1nInit
  (KNativePointer canvasPtr, KInteropPointer onFilter) {
    SkijaPaintFilterCanvas* canvas = reinterpret_cast<SkijaPaintFilterCanvas*>((canvasPtr));
    canvas->init(onFilter);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PaintFilterCanvas__1nMake
  (KNativePointer canvasPtr, KBoolean unrollDrawable) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    SkijaPaintFilterCanvas* filterCanvas = new SkijaPaintFilterCanvas(canvas, unrollDrawable);
    return reinterpret_cast<KNativePointer>(filterCanvas);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PaintFilterCanvas__1nGetOnFilterPaint
  (KNativePointer canvasPtr) {
    SkijaPaintFilterCanvas* canvas = reinterpret_cast<SkijaPaintFilterCanvas*>((canvasPtr));
    return reinterpret_cast<KNativePointer>(canvas->onFilterPaint());
}

