#include "SkPathUtils.h"
#include "SkPath.h"
#include "SkPaint.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathUtils__1nFillPathWithPaint
  (KNativePointer srcPtr, KNativePointer paintPtr, KFloat* matrixArr) {
    std::unique_ptr<SkMatrix> matrix = skMatrix(matrixArr);
    SkPath* src = reinterpret_cast<SkPath*>((srcPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    SkPath* dst = new SkPath();
    skpathutils::FillPathWithPaint(*src, *paint, dst, nullptr, *matrix);
    return reinterpret_cast<KNativePointer>(dst);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathUtils__1nFillPathWithPaintCull
  (KNativePointer srcPtr, KNativePointer paintPtr, KFloat left, KFloat top, KFloat right, KFloat bottom, KFloat* matrixArr) {
    std::unique_ptr<SkMatrix> matrix = skMatrix(matrixArr);
    SkPath* src = reinterpret_cast<SkPath*>((srcPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    SkPath* dst = new SkPath();
    SkRect cull {left, top, right, bottom};
    skpathutils::FillPathWithPaint(*src, *paint, dst, &cull, *matrix);
    return reinterpret_cast<KNativePointer>(dst);
}
