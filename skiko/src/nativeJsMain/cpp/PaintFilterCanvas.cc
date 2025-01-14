#include "SkCanvas.h"
#include "SkDrawable.h"
#include "SkPaintFilterCanvas.h"
#include "common.h"

class SkikoPaintFilterCanvas : public SkPaintFilterCanvas {
public:
    SkikoPaintFilterCanvas(
        SkCanvas* canvas,
        bool unrollDrawable
    ) : SkPaintFilterCanvas(canvas), unrollDrawable(unrollDrawable), _onFilter(nullptr), _onFilter_paint(nullptr) {}

    SkPaint* onFilterPaint() const {
        return _onFilter_paint;
    }

    void init(KInteropPointer onFilter) {
        _onFilter = KBooleanCallback(onFilter);
    }

protected:
    bool onFilter(SkPaint& paint) const override {
        _onFilter_paint = &paint;
        KBoolean result = _onFilter();
        _onFilter_paint = nullptr;
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

    // TODO: Support callback with parameters properly
    mutable SkPaint* _onFilter_paint;

    bool unrollDrawable;
};

SKIKO_EXPORT void org_jetbrains_skia_PaintFilterCanvas__1nInit
  (KNativePointer canvasPtr, KInteropPointer onFilter) {
    SkikoPaintFilterCanvas* canvas = reinterpret_cast<SkikoPaintFilterCanvas*>((canvasPtr));
    canvas->init(onFilter);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PaintFilterCanvas__1nMake
  (KNativePointer canvasPtr, KBoolean unrollDrawable) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    SkikoPaintFilterCanvas* filterCanvas = new SkikoPaintFilterCanvas(canvas, unrollDrawable);
    return reinterpret_cast<KNativePointer>(filterCanvas);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PaintFilterCanvas__1nGetOnFilterPaint
  (KNativePointer canvasPtr) {
    SkikoPaintFilterCanvas* canvas = reinterpret_cast<SkikoPaintFilterCanvas*>((canvasPtr));
    return reinterpret_cast<KNativePointer>(canvas->onFilterPaint());
}

