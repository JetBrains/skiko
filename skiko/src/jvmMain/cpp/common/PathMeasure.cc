#include <jni.h>
#include "SkPathMeasure.h"
#include "interop.hh"

static void deletePathMeasure(SkPathMeasure* instance) {
    delete instance;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PathMeasureKt_PathMeasure_1nGetFinalizer(JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deletePathMeasure));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PathMeasureKt_PathMeasure_1nMake
  (JNIEnv* env, jclass jclass) {
    return reinterpret_cast<jlong>(new SkPathMeasure());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PathMeasureKt__1nMakePath
  (JNIEnv* env, jclass jclass, jlong pathPtr, jboolean forceClosed, jfloat resScale) {
    SkPath* path = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(pathPtr));
    return reinterpret_cast<jlong>(new SkPathMeasure(*path, forceClosed, resScale));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathMeasureKt__1nSetPath
  (JNIEnv* env, jclass jclass, jlong ptr, jlong pathPtr, jboolean forceClosed) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>(static_cast<uintptr_t>(ptr));
    SkPath* path = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(pathPtr));
    instance->setPath(path, forceClosed);
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skia_PathMeasureKt__1nGetLength
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>(static_cast<uintptr_t>(ptr));
    return instance->getLength();
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathMeasureKt__1nGetPosition
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat distance, jintArray data) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>(static_cast<uintptr_t>(ptr));
    SkPoint position;
    if (instance->getPosTan(distance, &position, nullptr)) {
        jint d[2] = { rawBits(position.fX), rawBits(position.fY) };
        env->SetIntArrayRegion(data, 0, 2, d);
        return true;
    }

    return false;
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathMeasureKt__1nGetTangent
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat distance, jintArray data) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>(static_cast<uintptr_t>(ptr));
    SkVector tangent;
    if (instance->getPosTan(distance, nullptr, &tangent)) {
        jint d[2] = { rawBits(tangent.fX), rawBits(tangent.fY) };
        env->SetIntArrayRegion(data, 0, 2, d);
        return true;
    }

    return false;
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathMeasureKt__1nGetRSXform
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat distance, jintArray data) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>(static_cast<uintptr_t>(ptr));
    SkPoint position;
    SkVector tangent;
    if (instance->getPosTan(distance, &position, &tangent)) {
        jint d[4] = {
            rawBits(tangent.fX), rawBits(tangent.fY), rawBits(position.fX), rawBits(position.fY)
        };
        env->SetIntArrayRegion(data, 0, 4, d);
        return true;
    }

    return false;
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathMeasureKt__1nGetMatrix
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat distance, jboolean getPosition, jboolean getTangent, jintArray data) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>(static_cast<uintptr_t>(ptr));
    SkMatrix matrix;
    int flags = 0;
    
    if (getPosition)
        flags |= SkPathMeasure::MatrixFlags::kGetPosition_MatrixFlag;
    if (getTangent)
        flags |= SkPathMeasure::MatrixFlags::kGetTangent_MatrixFlag;

    if (instance->getMatrix(distance, &matrix, static_cast<SkPathMeasure::MatrixFlags>(flags))) {
        float* floats;
        matrix.get9(floats);

        jint d[9] = {
          rawBits(floats[0]),
          rawBits(floats[1]),
          rawBits(floats[2]),
          rawBits(floats[3]),
          rawBits(floats[4]),
          rawBits(floats[5]),
          rawBits(floats[6]),
          rawBits(floats[7]),
          rawBits(floats[8])
        };

        env->SetIntArrayRegion(data, 0, 9, d);

        return true;
    }

    return false;
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathMeasureKt__1nGetSegment
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat startD, jfloat endD, jlong dstPtr, jboolean startWithMoveTo) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>(static_cast<uintptr_t>(ptr));
    SkPath* dst = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(dstPtr));
    return instance->getSegment(startD, endD, dst, startWithMoveTo);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathMeasureKt__1nIsClosed
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>(static_cast<uintptr_t>(ptr));
    return instance->isClosed();
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathMeasureKt__1nNextContour
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>(static_cast<uintptr_t>(ptr));
    return instance->nextContour();
}