#include <iostream>
#include "SkPathEffect.h"
#include "Sk1DPathEffect.h"
#include "Sk2DPathEffect.h"
#include "SkCornerPathEffect.h"
#include "SkDashPathEffect.h"
#include "SkDiscretePathEffect.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathEffect__1nMakeSum
  (KNativePointer firstPtr, KNativePointer secondPtr) {
    SkPathEffect* first = reinterpret_cast<SkPathEffect*>((firstPtr));
    SkPathEffect* second = reinterpret_cast<SkPathEffect*>((secondPtr));
    SkPathEffect* ptr = SkPathEffect::MakeSum(sk_ref_sp(first), sk_ref_sp(second)).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathEffect__1nMakeCompose
  (KNativePointer outerPtr, KNativePointer innerPtr) {
    SkPathEffect* outer = reinterpret_cast<SkPathEffect*>((outerPtr));
    SkPathEffect* inner = reinterpret_cast<SkPathEffect*>((innerPtr));
    SkPathEffect* ptr = SkPathEffect::MakeCompose(sk_ref_sp(outer), sk_ref_sp(inner)).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathEffect__1nMakePath1D
  (KNativePointer pathPtr, KFloat advance, KFloat phase, KInt styleInt) {
    SkPath* path = reinterpret_cast<SkPath*>((pathPtr));
    SkPath1DPathEffect::Style style = static_cast<SkPath1DPathEffect::Style>(styleInt);
    SkPathEffect* ptr = SkPath1DPathEffect::Make(*path, advance, phase, style).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathEffect__1nMakePath2D
  (KFloat* matrixArr, KNativePointer pathPtr) {
    std::unique_ptr<SkMatrix> m = skMatrix(matrixArr);
    SkPath* path = reinterpret_cast<SkPath*>((pathPtr));
    SkPathEffect* ptr = SkPath2DPathEffect::Make(*m, *path).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathEffect__1nMakeLine2D
  (KFloat width, KFloat* matrixArr) {
    std::unique_ptr<SkMatrix> m = skMatrix(matrixArr);
    SkPathEffect* ptr = SkLine2DPathEffect::Make(width, *m).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathEffect__1nMakeCorner
  (KFloat radius) {
    SkPathEffect* ptr = SkCornerPathEffect::Make(radius).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathEffect__1nMakeDash
  (KFloat* intervalsArray, KInt count, KFloat phase) {
    SkPathEffect* ptr = SkDashPathEffect::Make(reinterpret_cast<float*>(intervalsArray), count, phase).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathEffect__1nMakeDiscrete
  (KFloat segLength, KFloat dev, KInt seed) {
    SkPathEffect* ptr = SkDiscretePathEffect::Make(segLength, dev, static_cast<uint32_t>(seed)).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
