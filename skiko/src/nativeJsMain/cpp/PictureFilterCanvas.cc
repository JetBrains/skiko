#include "SkNWayCanvas.h"
#include "common.h"

class SkikoPictureFilterCanvas : public SkNWayCanvas {
public:
    SkikoPictureFilterCanvas(SkCanvas* canvas) :
        SkNWayCanvas(canvas->imageInfo().width(), canvas->imageInfo().height()),
        _onDrawPicture(nullptr),
        _onDrawPicture_picture(nullptr),
        _onDrawPicture_matrix(nullptr),
        _onDrawPicture_paint(nullptr) {
        this->addCanvas(canvas);
    }

    KBooleanCallback _onDrawPicture;

    // TODO: Support callback with parameters properly
    const SkPicture* _onDrawPicture_picture;
    const SkMatrix* _onDrawPicture_matrix;
    const SkPaint* _onDrawPicture_paint;

protected:
    void onDrawPicture(const SkPicture* picture, const SkMatrix* matrix, const SkPaint* paint) override {
        _onDrawPicture_picture = picture;
        _onDrawPicture_matrix = matrix;
        _onDrawPicture_paint = paint;

        KBoolean handled = _onDrawPicture();

        _onDrawPicture_picture = nullptr;
        _onDrawPicture_matrix = nullptr;
        _onDrawPicture_paint = nullptr;

        if (!handled) {
            SkCanvas::onDrawPicture(picture, matrix, paint);
        }
    }
};

SKIKO_EXPORT KNativePointer org_jetbrains_skia_SkikoPictureFilterCanvas__1nMake
  (SkCanvas* canvas) {
    SkikoPictureFilterCanvas* filterCanvas = new SkikoPictureFilterCanvas(canvas);
    return reinterpret_cast<KNativePointer>(filterCanvas);
}

SKIKO_EXPORT void org_jetbrains_skia_SkikoPictureFilterCanvas__1nInit
  (KNativePointer canvasPtr, KInteropPointer onDrawPicture) {
    SkikoPictureFilterCanvas* canvas = reinterpret_cast<SkikoPictureFilterCanvas*>(canvasPtr);
    canvas->_onDrawPicture = KBooleanCallback(onDrawPicture);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_SkikoPictureFilterCanvas__1nGetOnDrawPicture_picture
  (KNativePointer canvasPtr) {
    SkikoPictureFilterCanvas* canvas = reinterpret_cast<SkikoPictureFilterCanvas*>(canvasPtr);
    return reinterpret_cast<KNativePointer>(const_cast<SkPicture *>(canvas->_onDrawPicture_picture));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_SkikoPictureFilterCanvas__1nGetOnDrawPicture_matrix
  (KNativePointer canvasPtr) {
    SkikoPictureFilterCanvas* canvas = reinterpret_cast<SkikoPictureFilterCanvas*>(canvasPtr);
    return reinterpret_cast<KNativePointer>(const_cast<SkMatrix *>(canvas->_onDrawPicture_matrix));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_SkikoPictureFilterCanvas__1nGetOnDrawPicture_paint
  (KNativePointer canvasPtr) {
    SkikoPictureFilterCanvas* canvas = reinterpret_cast<SkikoPictureFilterCanvas*>(canvasPtr);
    return reinterpret_cast<KNativePointer>(const_cast<SkPaint *>(canvas->_onDrawPicture_paint));
}
