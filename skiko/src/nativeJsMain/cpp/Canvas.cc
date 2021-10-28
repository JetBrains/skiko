
// This file has been auto generated.

#include <iostream>
#include "SkCanvas.h"
#include "SkRRect.h"
#include "SkTextBlob.h"
#include "SkVertices.h"
#include "hb.h"
#include "common.h"

static void deleteCanvas(SkCanvas* canvas) {
    // std::cout << "Deleting [SkCanvas " << canvas << "]" << std::endl;
    delete canvas;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Canvas__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>((&deleteCanvas));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Canvas__1nMakeFromBitmap
  (KNativePointer bitmapPtr, KInt flags, KInt pixelGeometry) {
    SkBitmap* bitmap = reinterpret_cast<SkBitmap*>((bitmapPtr));
    SkCanvas* canvas = new SkCanvas(*bitmap, {static_cast<uint32_t>(flags), static_cast<SkPixelGeometry>(pixelGeometry)});
    return reinterpret_cast<KNativePointer>(canvas);
}

SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nDrawPoint
  (KNativePointer canvasPtr, KFloat x, KFloat y, KNativePointer paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    canvas->drawPoint(x, y, *paint);
}


SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nDrawPoints
  (KNativePointer canvasPtr, int mode, KInt coordsCount, KFloat* coords, KNativePointer paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    SkCanvas::PointMode skMode = static_cast<SkCanvas::PointMode>(mode);
    canvas->drawPoints(skMode, coordsCount / 2, reinterpret_cast<SkPoint*>(coords), *paint);
}

SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nDrawLine
  (KNativePointer canvasPtr, KFloat x0, KFloat y0, KFloat x1, KFloat y1, KNativePointer paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    canvas->drawLine(x0, y0, x1, y1, *paint);
}

SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nDrawArc
  (KNativePointer canvasPtr, KFloat left, KFloat top, KFloat right, KFloat bottom, KFloat startAngle, KFloat sweepAngle, KBoolean includeCenter, KNativePointer paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    canvas->drawArc({left, top, right, bottom}, startAngle, sweepAngle, includeCenter, *paint);
}

SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nDrawRect
  (KNativePointer canvasPtr, KFloat left, KFloat top, KFloat right, KFloat bottom, KNativePointer paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    canvas->drawRect({left, top, right, bottom}, *paint);
}

SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nDrawOval
  (KNativePointer canvasPtr, KFloat left, KFloat top, KFloat right, KFloat bottom, KNativePointer paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    canvas->drawOval({left, top, right, bottom}, *paint);
}

SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nDrawRRect
  (KNativePointer canvasPtr, KFloat left, KFloat top, KFloat right, KFloat bottom, KFloat* jradii, KInt jradiiSize, KNativePointer paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    canvas->drawRRect(skija::RRect::toSkRRect(left, top, right, bottom, jradii, jradiiSize), *paint);
}

SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nDrawDRRect
  (KNativePointer canvasPtr,
   KFloat ol, KFloat ot, KFloat oright, KFloat ob, KFloat* ojradii, KInt ojradiiSize,
   KFloat il, KFloat it, KFloat ir, KFloat ib, KFloat* ijradii, KInt ijradiiSize,
   KNativePointer paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    canvas->drawDRRect(skija::RRect::toSkRRect(ol, ot, oright, ob, ojradii, ojradiiSize),
        skija::RRect::toSkRRect(il, it, ir, ib, ijradii, ijradiiSize), *paint);
}

SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nDrawPath
  (KNativePointer canvasPtr, KNativePointer pathPtr, KNativePointer paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    SkPath* path = reinterpret_cast<SkPath*>((pathPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    canvas->drawPath(*path, *paint);
}

SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nDrawImageRect
  (KNativePointer canvasPtr, KNativePointer imagePtr, KFloat sl, KFloat st, KFloat sr, KFloat sb, KFloat dl, KFloat dt, KFloat dr, KFloat db, KInt samplingModeVal1, KInt samplingModeVal2, KNativePointer paintPtr, KBoolean strict) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    SkImage* image = reinterpret_cast<SkImage*>((imagePtr));
    SkRect src {sl, st, sr, sb};
    SkRect dst {dl, dt, dr, db};
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    SkCanvas::SrcRectConstraint constraint = strict ? SkCanvas::SrcRectConstraint::kStrict_SrcRectConstraint : SkCanvas::SrcRectConstraint::kFast_SrcRectConstraint;
    canvas->drawImageRect(image, src, dst, skija::SamplingMode::unpackFrom2Ints(samplingModeVal1, samplingModeVal2), paint, constraint);
}

SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nDrawImageNine
  (KNativePointer canvasPtr, KNativePointer imagePtr, KInt cl, KInt ct, KInt cr, KInt cb, KFloat dl, KFloat dt, KFloat dr, KFloat db, KInt filterMode, KNativePointer paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    SkImage* image = reinterpret_cast<SkImage*>((imagePtr));
    SkIRect center {cl, ct, cr, cb};
    SkRect dst {dl, dt, dr, db};
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    canvas->drawImageNine(image, center, dst, static_cast<SkFilterMode>(filterMode), paint);
}

SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nDrawRegion
  (KNativePointer canvasPtr, KNativePointer regionPtr, KNativePointer paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    SkRegion* region = reinterpret_cast<SkRegion*>((regionPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    canvas->drawRegion(*region, *paint);
}

SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nDrawString
  (KNativePointer canvasPtr, KInteropPointer stringObj, KFloat x, KFloat y, KNativePointer skFontPtr, KNativePointer paintPtr) {
    SkCanvas* canvas    = reinterpret_cast<SkCanvas*>(canvasPtr);
    SkString string     = skString(stringObj);
    SkFont* font        = reinterpret_cast<SkFont*>(skFontPtr);
    SkPaint* paint      = reinterpret_cast<SkPaint*>(paintPtr);

    canvas->drawString(string, x, y, *font, *paint);
}


SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nDrawTextBlob
  (KNativePointer canvasPtr, KNativePointer blobPtr, KFloat x, KFloat y, KNativePointer paintPtr) {
    SkCanvas* canvas    = reinterpret_cast<SkCanvas*>   ((canvasPtr));
    SkTextBlob* blob    = reinterpret_cast<SkTextBlob*>((blobPtr));
    SkPaint* paint      = reinterpret_cast<SkPaint*>    ((paintPtr));

    canvas->drawTextBlob(blob, x, y, *paint);
}


SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nDrawPicture
  (KNativePointer ptr, KNativePointer picturePtr, KFloat* matrixArr, KNativePointer paintPtr) {
    SkCanvas* canvas   = reinterpret_cast<SkCanvas*>   ((ptr));
    SkPicture* picture = reinterpret_cast<SkPicture*>((picturePtr));
    std::unique_ptr<SkMatrix> matrix = skMatrix(matrixArr);
    SkPaint* paint     = reinterpret_cast<SkPaint*>    ((paintPtr));
    canvas->drawPicture(picture, matrix.get(), paint);
}


SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nDrawVertices
  (KNativePointer ptr, KInt verticesMode, KInt vertexCount, KFloat* positionsArr, KInt* colorsArr, KFloat* texCoordsArr, KInt indexCount, KShort* indexArr, KInt blendMode, KNativePointer paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>   ((ptr));
    KFloat* positions = positionsArr;
    KInt*   colors    = colorsArr;
    KFloat* texCoords = texCoordsArr;
    KShort* indices = indexArr;
    sk_sp<SkVertices> vertices = SkVertices::MakeCopy(
        static_cast<SkVertices::VertexMode>(verticesMode),
        vertexCount,
        reinterpret_cast<SkPoint*>(positions),
        reinterpret_cast<SkPoint*>(texCoords),
        reinterpret_cast<SkColor*>(colors),
        indexCount,
        reinterpret_cast<const uint16_t *>(indices));
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));

    canvas->drawVertices(vertices, static_cast<SkBlendMode>(blendMode), *paint);
}



SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nDrawPatch
  (KNativePointer ptr, KFloat* cubicsArr, KInt* colorsArr, KFloat* texCoordsArr, KInt blendMode, KNativePointer paintPtr) {
    TODO("implement org_jetbrains_skia_Canvas__1nDrawPatch");
}

#if 0
SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nDrawPatch
  (KNativePointer ptr, KFloat* cubicsArr, KInt* colorsArr, KFloat* texCoordsArr, KInt blendMode, KNativePointer paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>   ((ptr));
    KFloat* cubics    = env->GetFloatArrayElements(cubicsArr, 0);
    KInt*   colors    = env->GetIntArrayElements(colorsArr, 0);
    KFloat* texCoords = texCoordsArr == nullptr ? nullptr : env->GetFloatArrayElements(texCoordsArr, 0);
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));

    canvas->drawPatch(reinterpret_cast<SkPoint*>(cubics), reinterpret_cast<SkColor*>(colors), reinterpret_cast<SkPoint*>(texCoords), static_cast<SkBlendMode>(blendMode), *paint);

    if (texCoords != nullptr)
        env->ReleaseFloatArrayElements(texCoordsArr, texCoords, 0);
    env->ReleaseIntArrayElements(colorsArr, colors, 0);
    env->ReleaseFloatArrayElements(cubicsArr, cubics, 0);
}
#endif



SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nDrawDrawable
  (KNativePointer ptr, KNativePointer drawablePtr, KFloat* matrixArr) {
    TODO("implement org_jetbrains_skia_Canvas__1nDrawDrawable");
}

#if 0
SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nDrawDrawable
  (KNativePointer ptr, KNativePointer drawablePtr, KFloat* matrixArr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((ptr));
    SkDrawable* drawable = reinterpret_cast<SkDrawable*>((drawablePtr));
    std::unique_ptr<SkMatrix> matrix = skMatrix(env, matrixArr);
    canvas->drawDrawable(drawable, matrix.get());
}
#endif


SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nClear(KNativePointer ptr, KInt color) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((ptr));
    canvas->clear(color);
}

SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nDrawPaint
  (KNativePointer canvasPtr, KNativePointer paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    canvas->drawPaint(*paint);
}


SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nSetMatrix
  (KNativePointer canvasPtr, KFloat* matrixArr) {
    TODO("implement org_jetbrains_skia_Canvas__1nSetMatrix");
}

#if 0
SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nSetMatrix
  (KNativePointer canvasPtr, KFloat* matrixArr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    std::unique_ptr<SkMatrix> matrix = skMatrix(env, matrixArr);
    canvas->setMatrix(*matrix);
}
#endif


SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nResetMatrix
  (KNativePointer canvasPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    canvas->resetMatrix();
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Canvas__1nGetLocalToDevice
(KNativePointer canvasPtr) {
    TODO("implement org_jetbrains_skia_Canvas__1nGetLocalToDevice");
}

#if 0
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Canvas__1nGetLocalToDevice
(KNativePointer canvasPtr) {
  SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
  SkM44 matrix = canvas->getLocalToDevice();
  std::vector<float> floats(16);
  matrix.getRowMajor(floats.data());
  return javaFloatArray(env, floats);
}
#endif


SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nClipRect
  (KNativePointer canvasPtr, KFloat left, KFloat top, KFloat right, KFloat bottom, KInt mode, KBoolean antiAlias) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    canvas->clipRect({left, top, right, bottom}, static_cast<SkClipOp>(mode), antiAlias);
}

SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nClipRRect
  (KNativePointer canvasPtr, KFloat left, KFloat top, KFloat right, KFloat bottom, KFloat* jradii, KInt jradiiSize, KInt mode, KBoolean antiAlias) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    canvas->clipRRect(skija::RRect::toSkRRect(left, top, right, bottom, jradii, jradiiSize), static_cast<SkClipOp>(mode), antiAlias);
}

SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nClipPath
  (KNativePointer canvasPtr, KNativePointer pathPtr, KInt mode, KBoolean antiAlias) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    SkPath* path = reinterpret_cast<SkPath*>((pathPtr));
    canvas->clipPath(*path, static_cast<SkClipOp>(mode), antiAlias);
}

SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nClipRegion
  (KNativePointer canvasPtr, KNativePointer regionPtr, KInt mode) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    SkRegion* region = reinterpret_cast<SkRegion*>((regionPtr));
    canvas->clipRegion(*region, static_cast<SkClipOp>(mode));
}


SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nConcat
  (KNativePointer ptr, KFloat* matrixArr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((ptr));
    std::unique_ptr<SkMatrix> m = skMatrix(matrixArr);
    canvas->concat(*m);
}


SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nConcat44
  (KNativePointer ptr, KFloat* matrixArr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((ptr));
    std::unique_ptr<SkM44> m = skM44(matrixArr);
    canvas->concat(*m);
}


SKIKO_EXPORT KInt org_jetbrains_skia_Canvas__1nReadPixels
  (KNativePointer ptr, KNativePointer bitmapPtr, KInt srcX, KInt srcY) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((ptr));
    SkBitmap* bitmap = reinterpret_cast<SkBitmap*>((bitmapPtr));
    return canvas->readPixels(*bitmap, srcX, srcY);
}

SKIKO_EXPORT KInt org_jetbrains_skia_Canvas__1nWritePixels
  (KNativePointer ptr, KNativePointer bitmapPtr, KInt x, KInt y) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((ptr));
    SkBitmap* bitmap = reinterpret_cast<SkBitmap*>((bitmapPtr));
    return canvas->writePixels(*bitmap, x, y);
}

SKIKO_EXPORT KInt org_jetbrains_skia_Canvas__1nSave(KNativePointer ptr) {
    return reinterpret_cast<SkCanvas*>((ptr))->save();
}

SKIKO_EXPORT KInt org_jetbrains_skia_Canvas__1nSaveLayer
  (KNativePointer ptr, KNativePointer paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((ptr));
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    return canvas->saveLayer(nullptr, paint);
}

SKIKO_EXPORT KInt org_jetbrains_skia_Canvas__1nSaveLayerRect
  (KNativePointer ptr, KFloat left, KFloat top, KFloat right, KFloat bottom, KNativePointer paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((ptr));
    SkRect bounds {left, top, right, bottom};
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    return canvas->saveLayer(&bounds, paint);
}

SKIKO_EXPORT KInt org_jetbrains_skia_Canvas__1nGetSaveCount(KNativePointer ptr) {
    return reinterpret_cast<SkCanvas*>((ptr))->getSaveCount();
}

SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nRestore(KNativePointer ptr) {
    reinterpret_cast<SkCanvas*>((ptr))->restore();
}

SKIKO_EXPORT void org_jetbrains_skia_Canvas__1nRestoreToCount(KNativePointer ptr, KInt saveCount) {
    reinterpret_cast<SkCanvas*>((ptr))->restoreToCount(saveCount);
}
