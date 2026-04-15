#include "SkPaint.h"
#include "SkPath.h"
#include "SkPathBuilder.h"
#include "SkPathUtils.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathUtils__1nFillPathWithPaint
  (KNativePointer srcPtr, KNativePointer paintPtr) {
    SkPath* src = reinterpret_cast<SkPath*>((srcPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    return reinterpret_cast<KNativePointer>(new SkPath(skpathutils::FillPathWithPaint(*src, *paint)));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_PathUtils__1nFillPathWithPaintBuilder
  (KNativePointer srcPtr, KNativePointer paintPtr, KNativePointer dstPtr) {
    SkPath* src = reinterpret_cast<SkPath*>(srcPtr);
    SkPaint* paint = reinterpret_cast<SkPaint*>(paintPtr);
    SkPathBuilder* dst = reinterpret_cast<SkPathBuilder*>(dstPtr);
    return skpathutils::FillPathWithPaint(*src, *paint, dst);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_PathUtils__1nFillPathWithPaintMatrix
  (KNativePointer srcPtr, KNativePointer paintPtr, KNativePointer dstPtr, KFloat* matrixArr) {
    std::unique_ptr<SkMatrix> matrix = skMatrix(matrixArr);
    SkPath* src = reinterpret_cast<SkPath*>(srcPtr);
    SkPaint* paint = reinterpret_cast<SkPaint*>(paintPtr);
    SkPathBuilder* dst = reinterpret_cast<SkPathBuilder*>(dstPtr);
    return skpathutils::FillPathWithPaint(*src, *paint, dst, nullptr, *matrix);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_PathUtils__1nFillPathWithPaintCull
  (KNativePointer srcPtr, KNativePointer paintPtr, KNativePointer dstPtr, KFloat left, KFloat top, KFloat right, KFloat bottom, KFloat* matrixArr) {
    std::unique_ptr<SkMatrix> matrix = skMatrix(matrixArr);
    SkPath* src = reinterpret_cast<SkPath*>((srcPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    SkPathBuilder* dst = reinterpret_cast<SkPathBuilder*>(dstPtr);
    SkRect cull {left, top, right, bottom};
    return skpathutils::FillPathWithPaint(*src, *paint, dst, &cull, *matrix);
}
