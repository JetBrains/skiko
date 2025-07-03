#include <iostream>
#include <jni.h>
#include "SkCanvas.h"
#include "SkRRect.h"
#include "SkTextBlob.h"
#include "SkVertices.h"
#include "hb.h"
#include "interop.hh"

static void deleteCanvas(SkCanvas* canvas) {
    // std::cout << "Deleting [SkCanvas " << canvas << "]" << std::endl;
    delete canvas;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nGetFinalizer(JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteCanvas));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nMakeFromBitmap
  (JNIEnv* env, jclass jclass, jlong bitmapPtr, jint flags, jint pixelGeometry) {
    SkBitmap* bitmap = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(bitmapPtr));
    SkCanvas* canvas = new SkCanvas(*bitmap, {static_cast<uint32_t>(flags), static_cast<SkPixelGeometry>(pixelGeometry)});
    return reinterpret_cast<jlong>(canvas);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nDrawPoint
  (JNIEnv* env, jclass jclass, jlong canvasPtr, jfloat x, jfloat y, jlong paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    canvas->drawPoint(x, y, *paint);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nDrawPoints
  (JNIEnv* env, jclass jclass, jlong canvasPtr, int mode, jint _coordsCount, jfloatArray coords, jlong paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    SkCanvas::PointMode skMode = static_cast<SkCanvas::PointMode>(mode);
    jsize len = env->GetArrayLength(coords);
    jfloat* arr = static_cast<jfloat*>(env->GetPrimitiveArrayCritical(coords, 0));
    canvas->drawPoints(skMode, len / 2, reinterpret_cast<SkPoint*>(arr), *paint);
    env->ReleasePrimitiveArrayCritical(coords, arr, 0);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nDrawLine
  (JNIEnv* env, jclass jclass, jlong canvasPtr, jfloat x0, jfloat y0, jfloat x1, jfloat y1, jlong paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    canvas->drawLine(x0, y0, x1, y1, *paint);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nDrawArc
  (JNIEnv* env, jclass jclass, jlong canvasPtr, jfloat left, jfloat top, jfloat right, jfloat bottom, jfloat startAngle, jfloat sweepAngle, jboolean includeCenter, jlong paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    canvas->drawArc({left, top, right, bottom}, startAngle, sweepAngle, includeCenter, *paint);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nDrawRect
  (JNIEnv* env, jclass jclass, jlong canvasPtr, jfloat left, jfloat top, jfloat right, jfloat bottom, jlong paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    canvas->drawRect({left, top, right, bottom}, *paint);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nDrawOval
  (JNIEnv* env, jclass jclass, jlong canvasPtr, jfloat left, jfloat top, jfloat right, jfloat bottom, jlong paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    canvas->drawOval({left, top, right, bottom}, *paint);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nDrawRRect
  (JNIEnv* env, jclass jclass, jlong canvasPtr, jfloat left, jfloat top, jfloat right, jfloat bottom, jfloatArray jradii, jint jradiiSize, jlong paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    canvas->drawRRect(skija::RRect::toSkRRect(env, left, top, right, bottom, jradii), *paint);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nDrawDRRect
  (JNIEnv* env, jclass jclass, jlong canvasPtr,
   jfloat ol, jfloat ot, jfloat oright, jfloat ob, jfloatArray ojradii, jint ojradiiSize,
   jfloat il, jfloat it, jfloat ir, jfloat ib, jfloatArray ijradii, jint ijradiiSize,
   jlong paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    canvas->drawDRRect(skija::RRect::toSkRRect(env, ol, ot, oright, ob, ojradii),
        skija::RRect::toSkRRect(env, il, it, ir, ib, ijradii), *paint);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nDrawPath
  (JNIEnv* env, jclass jclass, jlong canvasPtr, jlong pathPtr, jlong paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    SkPath* path = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(pathPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    canvas->drawPath(*path, *paint);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nDrawImageRect
  (JNIEnv* env, jclass jclass, jlong canvasPtr, jlong imagePtr, jfloat sl, jfloat st, jfloat sr, jfloat sb, jfloat dl, jfloat dt, jfloat dr, jfloat db, jint samplingModeVal1, jint samplingModeVal2, jlong paintPtr, jboolean strict) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    SkImage* image = reinterpret_cast<SkImage*>(static_cast<uintptr_t>(imagePtr));
    SkRect src {sl, st, sr, sb};
    SkRect dst {dl, dt, dr, db};
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    SkCanvas::SrcRectConstraint constraint = strict ? SkCanvas::SrcRectConstraint::kStrict_SrcRectConstraint : SkCanvas::SrcRectConstraint::kFast_SrcRectConstraint;
    canvas->drawImageRect(image, src, dst, skija::SamplingMode::unpackFrom2Ints(env, samplingModeVal1, samplingModeVal2), paint, constraint);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nDrawImageNine
  (JNIEnv* env, jclass jclass, jlong canvasPtr, jlong imagePtr, jint cl, jint ct, jint cr, jint cb, jfloat dl, jfloat dt, jfloat dr, jfloat db, jint filterMode, jlong paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    SkImage* image = reinterpret_cast<SkImage*>(static_cast<uintptr_t>(imagePtr));
    SkIRect center {cl, ct, cr, cb};
    SkRect dst {dl, dt, dr, db};
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    canvas->drawImageNine(image, center, dst, static_cast<SkFilterMode>(filterMode), paint);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nDrawRegion
  (JNIEnv* env, jclass jclass, jlong canvasPtr, jlong regionPtr, jlong paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    SkRegion* region = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(regionPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    canvas->drawRegion(*region, *paint);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nDrawString
  (JNIEnv* env, jclass jclass, jlong canvasPtr, jstring stringObj, jfloat x, jfloat y, jlong skFontPtr, jlong paintPtr) {
    SkCanvas* canvas    = reinterpret_cast<SkCanvas*>   (static_cast<uintptr_t>(canvasPtr));
    SkString string     = skString(env, stringObj);
    SkFont* font        = reinterpret_cast<SkFont*>     (static_cast<uintptr_t>(skFontPtr));
    SkPaint* paint      = reinterpret_cast<SkPaint*>    (static_cast<uintptr_t>(paintPtr));

    canvas->drawString(string, x, y, *font, *paint);
}


extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nDrawTextBlob
  (JNIEnv* env, jclass jclass, jlong canvasPtr, jlong blobPtr, jfloat x, jfloat y, jlong paintPtr) {
    SkCanvas* canvas    = reinterpret_cast<SkCanvas*>   (static_cast<uintptr_t>(canvasPtr));
    SkTextBlob* blob    = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(blobPtr));
    SkPaint* paint      = reinterpret_cast<SkPaint*>    (static_cast<uintptr_t>(paintPtr));

    canvas->drawTextBlob(blob, x, y, *paint);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nDrawPicture
  (JNIEnv* env, jclass jclass, jlong ptr, jlong picturePtr, jfloatArray matrixArr, jlong paintPtr) {
    SkCanvas* canvas   = reinterpret_cast<SkCanvas*>   (static_cast<uintptr_t>(ptr));
    SkPicture* picture = reinterpret_cast<SkPicture*>(static_cast<uintptr_t>(picturePtr));
    std::unique_ptr<SkMatrix> matrix = skMatrix(env, matrixArr);
    SkPaint* paint     = reinterpret_cast<SkPaint*>    (static_cast<uintptr_t>(paintPtr));
    canvas->drawPicture(picture, matrix.get(), paint);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nDrawVertices
  (JNIEnv* env, jclass jclass, jlong ptr, jint verticesMode, jint _vertexCount, jfloatArray positionsArr, jintArray colorsArr, jfloatArray texCoordsArr, jint _indexCount, jshortArray indexArr, jint blendMode, jlong paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>   (static_cast<uintptr_t>(ptr));
    int indexCount = indexArr == nullptr ? 0 : env->GetArrayLength(indexArr);
    jfloat* positions = env->GetFloatArrayElements(positionsArr, 0);
    jint*   colors    = colorsArr == nullptr ? nullptr : env->GetIntArrayElements(colorsArr, 0);
    jfloat* texCoords = texCoordsArr == nullptr ? nullptr : env->GetFloatArrayElements(texCoordsArr, 0);
    const jshort* indices = indexArr == nullptr ? nullptr : env->GetShortArrayElements(indexArr, 0);
    sk_sp<SkVertices> vertices = SkVertices::MakeCopy(
        static_cast<SkVertices::VertexMode>(verticesMode),
        env->GetArrayLength(positionsArr) / 2,
        reinterpret_cast<SkPoint*>(positions),
        reinterpret_cast<SkPoint*>(texCoords),
        reinterpret_cast<SkColor*>(colors),
        indexCount,
        reinterpret_cast<const uint16_t *>(indices));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));

    canvas->drawVertices(vertices, static_cast<SkBlendMode>(blendMode), *paint);

    if (texCoords != nullptr)
        env->ReleaseFloatArrayElements(texCoordsArr, texCoords, 0);
    if (colors != nullptr)
        env->ReleaseIntArrayElements(colorsArr, colors, 0);
    env->ReleaseFloatArrayElements(positionsArr, positions, 0);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nDrawPatch
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray cubicsArr, jintArray colorsArr, jfloatArray texCoordsArr, jint blendMode, jlong paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>   (static_cast<uintptr_t>(ptr));
    jfloat* cubics    = env->GetFloatArrayElements(cubicsArr, 0);
    jint*   colors    = env->GetIntArrayElements(colorsArr, 0);
    jfloat* texCoords = texCoordsArr == nullptr ? nullptr : env->GetFloatArrayElements(texCoordsArr, 0);
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));

    canvas->drawPatch(reinterpret_cast<SkPoint*>(cubics), reinterpret_cast<SkColor*>(colors), reinterpret_cast<SkPoint*>(texCoords), static_cast<SkBlendMode>(blendMode), *paint);

    if (texCoords != nullptr)
        env->ReleaseFloatArrayElements(texCoordsArr, texCoords, 0);
    env->ReleaseIntArrayElements(colorsArr, colors, 0);
    env->ReleaseFloatArrayElements(cubicsArr, cubics, 0);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nDrawDrawable
  (JNIEnv* env, jclass jclass, jlong ptr, jlong drawablePtr, jfloatArray matrixArr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(ptr));
    SkDrawable* drawable = reinterpret_cast<SkDrawable*>(static_cast<uintptr_t>(drawablePtr));
    std::unique_ptr<SkMatrix> matrix = skMatrix(env, matrixArr);
    canvas->drawDrawable(drawable, matrix.get());
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nClear(JNIEnv* env, jclass jclass, jlong ptr, jint color) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(ptr));
    canvas->clear(color);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nDrawPaint
  (JNIEnv* env, jclass jclass, jlong canvasPtr, jlong paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    canvas->drawPaint(*paint);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nSetMatrix
  (JNIEnv* env, jclass jclass, jlong canvasPtr, jfloatArray matrixArr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    std::unique_ptr<SkMatrix> matrix = skMatrix(env, matrixArr);
    canvas->setMatrix(*matrix);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nResetMatrix
  (JNIEnv* env, jclass jclass, jlong canvasPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    canvas->resetMatrix();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nGetLocalToDevice
(JNIEnv* env, jclass jclass, jlong canvasPtr, jfloatArray resultingMatrix) {
  jfloat *floats = env->GetFloatArrayElements(resultingMatrix, NULL);
  SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
  SkM44 matrix = canvas->getLocalToDevice();
  matrix.getRowMajor(floats);
  env->ReleaseFloatArrayElements(resultingMatrix, floats, 0);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nClipRect
  (JNIEnv* env, jclass jclass, jlong canvasPtr, jfloat left, jfloat top, jfloat right, jfloat bottom, jint mode, jboolean antiAlias) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    canvas->clipRect({left, top, right, bottom}, static_cast<SkClipOp>(mode), antiAlias);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nClipRRect
  (JNIEnv* env, jclass jclass, jlong canvasPtr, jfloat left, jfloat top, jfloat right, jfloat bottom, jfloatArray jradii, jint jradiiSize, jint mode, jboolean antiAlias) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    canvas->clipRRect(skija::RRect::toSkRRect(env, left, top, right, bottom, jradii), static_cast<SkClipOp>(mode), antiAlias);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nClipPath
  (JNIEnv* env, jclass jclass, jlong canvasPtr, jlong pathPtr, jint mode, jboolean antiAlias) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    SkPath* path = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(pathPtr));
    canvas->clipPath(*path, static_cast<SkClipOp>(mode), antiAlias);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nClipRegion
  (JNIEnv* env, jclass jclass, jlong canvasPtr, jlong regionPtr, jint mode) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    SkRegion* region = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(regionPtr));
    canvas->clipRegion(*region, static_cast<SkClipOp>(mode));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nConcat
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray matrixArr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(ptr));
    std::unique_ptr<SkMatrix> m = skMatrix(env, matrixArr);
    canvas->concat(*m);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nConcat44
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray matrixArr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(ptr));
    std::unique_ptr<SkM44> m = skM44(env, matrixArr);
    canvas->concat(*m);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nTranslate
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat dx, jfloat dy) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(ptr));
    canvas->translate(dx, dy);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nScale
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat sx, jfloat sy) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(ptr));
    canvas->scale(sx, sy);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nRotate
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat deg, jfloat x, jfloat y) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(ptr));
    canvas->rotate(deg, x, y);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nSkew
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat sx, jfloat sy) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(ptr));
    canvas->skew(sx, sy);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nReadPixels
  (JNIEnv* env, jclass jclass, jlong ptr, jlong bitmapPtr, jint srcX, jint srcY) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(ptr));
    SkBitmap* bitmap = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(bitmapPtr));
    return canvas->readPixels(*bitmap, srcX, srcY);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nWritePixels
  (JNIEnv* env, jclass jclass, jlong ptr, jlong bitmapPtr, jint x, jint y) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(ptr));
    SkBitmap* bitmap = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(bitmapPtr));
    return canvas->writePixels(*bitmap, x, y);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nSave(JNIEnv* env, jclass jclass, jlong ptr) {
    return reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(ptr))->save();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nSaveLayer
  (JNIEnv* env, jclass jclass, jlong ptr, jlong paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(ptr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    return canvas->saveLayer(nullptr, paint);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nSaveLayerRect
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat left, jfloat top, jfloat right, jfloat bottom, jlong paintPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(ptr));
    SkRect bounds {left, top, right, bottom};
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    return canvas->saveLayer(&bounds, paint);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nSaveLayerSaveLayerRec
  (JNIEnv* env, jclass jclass, jlong ptr, jlong paintPtr, jlong backdropImageFilterPtr, jlong colorSpacePtr, jint saveLayerFlags) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(ptr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    SkImageFilter* backdrop = reinterpret_cast<SkImageFilter*>(backdropImageFilterPtr);
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(colorSpacePtr);

    return canvas->saveLayer(SkCanvas::SaveLayerRec(nullptr, paint, backdrop, colorSpace, saveLayerFlags));
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nSaveLayerSaveLayerRecRect
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat left, jfloat top, jfloat right, jfloat bottom, jlong paintPtr, jlong backdropImageFilterPtr, jlong colorSpacePtr, jint saveLayerFlags) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(ptr));
    SkRect bounds {left, top, right, bottom};
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    SkImageFilter* backdrop = reinterpret_cast<SkImageFilter*>(backdropImageFilterPtr);
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(colorSpacePtr);

    return canvas->saveLayer(SkCanvas::SaveLayerRec(&bounds, paint, backdrop, colorSpace, saveLayerFlags));
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nGetSaveCount(JNIEnv* env, jclass jclass, jlong ptr) {
    return reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(ptr))->getSaveCount();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nRestore(JNIEnv* env, jclass jclass, jlong ptr) {
    reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(ptr))->restore();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CanvasExternalKt_Canvas_1nRestoreToCount(JNIEnv* env, jclass jclass, jlong ptr, jint saveCount) {
    reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(ptr))->restoreToCount(saveCount);
}
