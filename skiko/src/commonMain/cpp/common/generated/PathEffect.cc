
// This file has been auto generated.

#include <iostream>
#include "SkPathEffect.h"
#include "Sk1DPathEffect.h"
#include "Sk2DPathEffect.h"
#include "SkCornerPathEffect.h"
#include "SkDashPathEffect.h"
#include "SkDiscretePathEffect.h"
#include "common.h"

#error OK

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathEffect__1nMakeSum
  (KInteropPointer __Kinstance, KNativePointer firstPtr, KNativePointer secondPtr) {
    SkPathEffect* first = reinterpret_cast<SkPathEffect*>((firstPtr));
    SkPathEffect* second = reinterpret_cast<SkPathEffect*>((secondPtr));
    SkPathEffect* ptr = SkPathEffect::MakeSum(sk_ref_sp(first), sk_ref_sp(second)).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathEffect__1nMakeCompose
  (KInteropPointer __Kinstance, KNativePointer outerPtr, KNativePointer innerPtr) {
    SkPathEffect* outer = reinterpret_cast<SkPathEffect*>((outerPtr));
    SkPathEffect* inner = reinterpret_cast<SkPathEffect*>((innerPtr));
    SkPathEffect* ptr = SkPathEffect::MakeCompose(sk_ref_sp(outer), sk_ref_sp(inner)).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathEffect__1nMakePath1D
  (KInteropPointer __Kinstance, KNativePointer pathPtr, KFloat advance, KFloat phase, KInt styleInt) {
    SkPath* path = reinterpret_cast<SkPath*>((pathPtr));
    SkPath1DPathEffect::Style style = static_cast<SkPath1DPathEffect::Style>(styleInt);
    SkPathEffect* ptr = SkPath1DPathEffect::Make(*path, advance, phase, style).release();
    return reinterpret_cast<KNativePointer>(ptr);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathEffect__1nMakePath2D
  (KInteropPointer __Kinstance, KFloat* matrixArr, KNativePointer pathPtr) {
    TODO("implement org_jetbrains_skia_PathEffect__1nMakePath2D");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathEffect__1nMakePath2D
  (KInteropPointer __Kinstance, KFloat* matrixArr, KNativePointer pathPtr) {
    std::unique_ptr<SkMatrix> m = skMatrix(env, matrixArr);
    SkPath* path = reinterpret_cast<SkPath*>((pathPtr));
    SkPathEffect* ptr = SkPath2DPathEffect::Make(*m, *path).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathEffect__1nMakeLine2D
  (KInteropPointer __Kinstance, KFloat width, KFloat* matrixArr) {
    TODO("implement org_jetbrains_skia_PathEffect__1nMakeLine2D");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathEffect__1nMakeLine2D
  (KInteropPointer __Kinstance, KFloat width, KFloat* matrixArr) {
    std::unique_ptr<SkMatrix> m = skMatrix(env, matrixArr);
    SkPathEffect* ptr = SkLine2DPathEffect::Make(width, *m).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif


SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathEffect__1nMakeCorner
  (KInteropPointer __Kinstance, KFloat radius) {
    SkPathEffect* ptr = SkCornerPathEffect::Make(radius).release();
    return reinterpret_cast<KNativePointer>(ptr);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathEffect__1nMakeDash
  (KInteropPointer __Kinstance, KFloat* intervalsArray, KFloat phase) {
    TODO("implement org_jetbrains_skia_PathEffect__1nMakeDash");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathEffect__1nMakeDash
  (KInteropPointer __Kinstance, KFloat* intervalsArray, KFloat phase) {
    jsize len = env->GetArrayLength(intervalsArray);
    KFloat* intervals = env->GetFloatArrayElements(intervalsArray, 0);
    SkPathEffect* ptr = SkDashPathEffect::Make(intervals, len, phase).release();
    env->ReleaseFloatArrayElements(intervalsArray, intervals, 0);
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif


SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathEffect__1nMakeDiscrete
  (KInteropPointer __Kinstance, KFloat segLength, KFloat dev, KInt seed) {
    SkPathEffect* ptr = SkDiscretePathEffect::Make(segLength, dev, static_cast<uint32_t>(seed)).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
