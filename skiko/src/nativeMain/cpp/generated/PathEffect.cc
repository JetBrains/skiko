
// This file has been auto generated.

#include <iostream>
#include "SkPathEffect.h"
#include "Sk1DPathEffect.h"
#include "Sk2DPathEffect.h"
#include "SkCornerPathEffect.h"
#include "SkDashPathEffect.h"
#include "SkDiscretePathEffect.h"
#include "common.h"

extern "C" jlong org_jetbrains_skia_PathEffect__1nMakeSum
  (jlong firstPtr, jlong secondPtr) {
    SkPathEffect* first = reinterpret_cast<SkPathEffect*>(static_cast<uintptr_t>(firstPtr));
    SkPathEffect* second = reinterpret_cast<SkPathEffect*>(static_cast<uintptr_t>(secondPtr));
    SkPathEffect* ptr = SkPathEffect::MakeSum(sk_ref_sp(first), sk_ref_sp(second)).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" jlong org_jetbrains_skia_PathEffect__1nMakeCompose
  (jlong outerPtr, jlong innerPtr) {
    SkPathEffect* outer = reinterpret_cast<SkPathEffect*>(static_cast<uintptr_t>(outerPtr));
    SkPathEffect* inner = reinterpret_cast<SkPathEffect*>(static_cast<uintptr_t>(innerPtr));
    SkPathEffect* ptr = SkPathEffect::MakeCompose(sk_ref_sp(outer), sk_ref_sp(inner)).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" jlong org_jetbrains_skia_PathEffect__1nMakePath1D
  (jlong pathPtr, jfloat advance, jfloat phase, jint styleInt) {
    SkPath* path = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(pathPtr));
    SkPath1DPathEffect::Style style = static_cast<SkPath1DPathEffect::Style>(styleInt);
    SkPathEffect* ptr = SkPath1DPathEffect::Make(*path, advance, phase, style).release();
    return reinterpret_cast<jlong>(ptr);
}


extern "C" jlong org_jetbrains_skia_PathEffect__1nMakePath2D
  (jfloatArray matrixArr, jlong pathPtr) {
    TODO("implement org_jetbrains_skia_PathEffect__1nMakePath2D");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_PathEffect__1nMakePath2D
  (jfloatArray matrixArr, jlong pathPtr) {
    std::unique_ptr<SkMatrix> m = skMatrix(env, matrixArr);
    SkPath* path = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(pathPtr));
    SkPathEffect* ptr = SkPath2DPathEffect::Make(*m, *path).release();
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_PathEffect__1nMakeLine2D
  (jfloat width, jfloatArray matrixArr) {
    TODO("implement org_jetbrains_skia_PathEffect__1nMakeLine2D");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_PathEffect__1nMakeLine2D
  (jfloat width, jfloatArray matrixArr) {
    std::unique_ptr<SkMatrix> m = skMatrix(env, matrixArr);
    SkPathEffect* ptr = SkLine2DPathEffect::Make(width, *m).release();
    return reinterpret_cast<jlong>(ptr);
}
#endif


extern "C" jlong org_jetbrains_skia_PathEffect__1nMakeCorner
  (jfloat radius) {
    SkPathEffect* ptr = SkCornerPathEffect::Make(radius).release();
    return reinterpret_cast<jlong>(ptr);
}


extern "C" jlong org_jetbrains_skia_PathEffect__1nMakeDash
  (jfloatArray intervalsArray, jfloat phase) {
    TODO("implement org_jetbrains_skia_PathEffect__1nMakeDash");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_PathEffect__1nMakeDash
  (jfloatArray intervalsArray, jfloat phase) {
    jsize len = env->GetArrayLength(intervalsArray);
    jfloat* intervals = env->GetFloatArrayElements(intervalsArray, 0);
    SkPathEffect* ptr = SkDashPathEffect::Make(intervals, len, phase).release();
    env->ReleaseFloatArrayElements(intervalsArray, intervals, 0);
    return reinterpret_cast<jlong>(ptr);
}
#endif


extern "C" jlong org_jetbrains_skia_PathEffect__1nMakeDiscrete
  (jfloat segLength, jfloat dev, jint seed) {
    SkPathEffect* ptr = SkDiscretePathEffect::Make(segLength, dev, static_cast<uint32_t>(seed)).release();
    return reinterpret_cast<jlong>(ptr);
}
