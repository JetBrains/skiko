#include <jni.h>
#include "SkPathBuilder.h"
#include "SkPath.h"
#include "SkRRect.h"
#include "SkMatrix.h"
#include "interop.hh"

static void deletePathBuilder(SkPathBuilder* builder) {
    delete builder;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nGetFinalizer
  (JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deletePathBuilder));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nMake
  (JNIEnv* env, jclass jclass) {
    SkPathBuilder* builder = new SkPathBuilder();
    return reinterpret_cast<jlong>(builder);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nMakeWithFillType
  (JNIEnv* env, jclass jclass, jint fillType) {
    SkPathBuilder* builder = new SkPathBuilder(static_cast<SkPathFillType>(fillType));
    return reinterpret_cast<jlong>(builder);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nMakeFromPath
  (JNIEnv* env, jclass jclass, jlong pathPtr) {
    SkPath* path = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(pathPtr));
    SkPathBuilder* builder = new SkPathBuilder(*path);
    return reinterpret_cast<jlong>(builder);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nReset
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    builder->reset();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nSetFillType
  (JNIEnv* env, jclass jclass, jlong ptr, jint fillType) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    builder->setFillType(static_cast<SkPathFillType>(fillType));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nIncReserve
  (JNIEnv* env, jclass jclass, jlong ptr, jint extraPtCount) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    builder->incReserve(extraPtCount);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nMoveTo
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat x, jfloat y) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    builder->moveTo(x, y);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nRMoveTo
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat dx, jfloat dy) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    builder->rMoveTo(dx, dy);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nLineTo
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat x, jfloat y) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    builder->lineTo(x, y);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nRLineTo
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat dx, jfloat dy) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    builder->rLineTo(dx, dy);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nQuadTo
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat x1, jfloat y1, jfloat x2, jfloat y2) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    builder->quadTo(x1, y1, x2, y2);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nRQuadTo
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat dx1, jfloat dy1, jfloat dx2, jfloat dy2) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    builder->rQuadTo(dx1, dy1, dx2, dy2);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nConicTo
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat w) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    builder->conicTo(x1, y1, x2, y2, w);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nRConicTo
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat dx1, jfloat dy1, jfloat dx2, jfloat dy2, jfloat w) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    builder->rConicTo(dx1, dy1, dx2, dy2, w);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nCubicTo
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat x3, jfloat y3) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    builder->cubicTo(x1, y1, x2, y2, x3, y3);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nRCubicTo
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat dx1, jfloat dy1, jfloat dx2, jfloat dy2, jfloat dx3, jfloat dy3) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    builder->rCubicTo(dx1, dy1, dx2, dy2, dx3, dy3);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nArcTo
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat left, jfloat top, jfloat right, jfloat bottom, jfloat startAngle, jfloat sweepAngle, jboolean forceMoveTo) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    builder->arcTo({left, top, right, bottom}, startAngle, sweepAngle, forceMoveTo);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nTangentArcTo
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat radius) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    builder->arcTo({x1, y1}, {x2, y2}, radius);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nEllipticalArcTo
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat rx, jfloat ry, jfloat xAxisRotate, jint size, jint direction, jfloat x, float y) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    builder->arcTo({rx, ry}, xAxisRotate, static_cast<SkPathBuilder::ArcSize>(size), static_cast<SkPathDirection>(direction), {x, y});
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nREllipticalArcTo
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat rx, jfloat ry, jfloat xAxisRotate, jint size, jint direction, jfloat dx, float dy) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    builder->rArcTo({rx, ry}, xAxisRotate, static_cast<SkPathBuilder::ArcSize>(size), static_cast<SkPathDirection>(direction), {dx, dy});
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nClosePath
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    builder->close();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nAddRect
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat l, jfloat t, jfloat r, jfloat b, jint dirInt, jint start) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    SkPathDirection dir = static_cast<SkPathDirection>(dirInt);
    builder->addRect({l, t, r, b}, dir, start);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nAddOval
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat l, jfloat t, jfloat r, jfloat b, jint dirInt, jint start) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    SkPathDirection dir = static_cast<SkPathDirection>(dirInt);
    builder->addOval({l, t, r, b}, dir, start);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nAddCircle
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat x, jfloat y, jfloat r, jint dirInt) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    SkPathDirection dir = static_cast<SkPathDirection>(dirInt);
    builder->addCircle(x, y, r, dir);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nAddArc
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat l, jfloat t, jfloat r, jfloat b, jfloat startAngle, jfloat sweepAngle) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    builder->addArc({l, t, r, b}, startAngle, sweepAngle);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nAddRRect
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat l, jfloat t, jfloat r, jfloat b, jfloatArray radii, jint radiiSize, jint dirInt, jint start) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    SkRRect rrect = skija::RRect::toSkRRect(env, l, t, r, b, radii);
    SkPathDirection dir = static_cast<SkPathDirection>(dirInt);
    builder->addRRect(rrect, dir, start);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nAddPoly
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray coords, jint _count, jboolean close) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    jsize len = env->GetArrayLength(coords);
    jfloat* arr = env->GetFloatArrayElements(coords, 0);
    builder->addPolygon({reinterpret_cast<SkPoint*>(arr), len / 2}, close);
    env->ReleaseFloatArrayElements(coords, arr, 0);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nAddPath
  (JNIEnv* env, jclass jclass, jlong ptr, jlong srcPtr, jint mode) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    SkPath* src = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(srcPtr));
    builder->addPath(*src, static_cast<SkPath::AddPathMode>(mode));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nAddPathOffset
  (JNIEnv* env, jclass jclass, jlong ptr, jlong srcPtr, jfloat dx, jfloat dy, jint mode) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    SkPath* src = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(srcPtr));
    builder->addPath(*src, dx, dy, static_cast<SkPath::AddPathMode>(mode));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nAddPathTransform
  (JNIEnv* env, jclass jclass, jlong ptr, jlong srcPtr, jfloatArray matrixArr, jint mode) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    SkPath* src = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(srcPtr));
    std::unique_ptr<SkMatrix> matrix = skMatrix(env, matrixArr);
    builder->addPath(*src, *matrix, static_cast<SkPath::AddPathMode>(mode));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nSetLastPt
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat x, jfloat y) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    builder->setLastPt(x, y);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nDetach
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    SkPath* path = new SkPath(builder->detach());
    return reinterpret_cast<jlong>(path);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nSnapshot
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    SkPath* path = new SkPath(builder->snapshot());
    return reinterpret_cast<jlong>(path);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nOffset
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat dx, jfloat dy) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    builder->offset(dx, dy);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathBuilderKt_PathBuilder_1nTransform
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray matrixArr) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>(static_cast<uintptr_t>(ptr));
    std::unique_ptr<SkMatrix> matrix = skMatrix(env, matrixArr);
    builder->transform(*matrix);
}
