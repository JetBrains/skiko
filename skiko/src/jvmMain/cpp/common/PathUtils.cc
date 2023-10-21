#include <jni.h>
#include "SkPathUtils.h"
#include "SkPath.h"
#include "SkPaint.h"
#include "interop.hh"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PathUtilsKt__1nFillPathWithPaint
  (JNIEnv* env, jclass jclass, jlong srcPtr, jlong paintPtr, jfloatArray matrixArr) {
    std::unique_ptr<SkMatrix> matrix = skMatrix(env, matrixArr);
    SkPath* src = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(srcPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    SkPath* dst = new SkPath();
    skpathutils::FillPathWithPaint(*src, *paint, dst, nullptr, *matrix);
    return reinterpret_cast<jlong>(dst);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PathUtilsKt__1nFillPathWithPaintCull
  (JNIEnv* env, jclass jclass, jlong srcPtr, jlong paintPtr, jfloat left, jfloat top, jfloat right, jfloat bottom, jfloatArray matrixArr) {
    std::unique_ptr<SkMatrix> matrix = skMatrix(env, matrixArr);
    SkPath* src = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(srcPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    SkPath* dst = new SkPath();
    SkRect cull {left, top, right, bottom};
    skpathutils::FillPathWithPaint(*src, *paint, dst, &cull, *matrix);
    return reinterpret_cast<jlong>(dst);
}
