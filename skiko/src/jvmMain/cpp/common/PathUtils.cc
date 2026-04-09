#include <jni.h>
#include "SkPaint.h"
#include "SkPath.h"
#include "SkPathBuilder.h"
#include "SkPathUtils.h"
#include "interop.hh"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PathUtilsKt__1nFillPathWithPaint
  (JNIEnv* env, jclass jclass, jlong srcPtr, jlong paintPtr) {
    SkPath* src = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(srcPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    SkPath* dst = new SkPath(skpathutils::FillPathWithPaint(*src, *paint));
    return reinterpret_cast<jlong>(dst);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathUtilsKt__1nFillPathWithPaintBuilder
  (JNIEnv* env, jclass jclass, jlong srcPtr, jlong paintPtr, jlong dstPtr) {
    SkPath* src = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(srcPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    SkPathBuilder* dst = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(dstPtr));
    return skpathutils::FillPathWithPaint(*src, *paint, dst);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathUtilsKt__1nFillPathWithPaintMatrix
  (JNIEnv* env, jclass jclass, jlong srcPtr, jlong paintPtr, jlong dstPtr, jfloatArray matrixArr) {
    std::unique_ptr<SkMatrix> matrix = skMatrix(env, matrixArr);
    SkPath* src = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(srcPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    SkPathBuilder* dst = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(dstPtr));
    return skpathutils::FillPathWithPaint(*src, *paint, dst, nullptr, *matrix);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathUtilsKt__1nFillPathWithPaintCull
  (JNIEnv* env, jclass jclass, jlong srcPtr, jlong paintPtr, jlong dstPtr, jfloat left, jfloat top, jfloat right, jfloat bottom, jfloatArray matrixArr) {
    std::unique_ptr<SkMatrix> matrix = skMatrix(env, matrixArr);
    SkPath* src = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(srcPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    SkPathBuilder* dst = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(dstPtr));
    SkRect cull {left, top, right, bottom};
    return skpathutils::FillPathWithPaint(*src, *paint, dst, &cull, *matrix);
}
