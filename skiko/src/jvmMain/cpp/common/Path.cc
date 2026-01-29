#include <algorithm>
#include <cfloat>
#include <iostream>
#include <vector>
#include <limits>
#include <jni.h>
#include "SkPath.h"
#include "SkPathOps.h"
#include "interop.hh"
#include "include/utils/SkParsePath.h"

static void deletePath(SkPath* path) {
    // std::cout << "Deleting [SkPath " << path << "]" << std::endl;
    delete path;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PathKt_Path_1nGetFinalizer(JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deletePath));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PathKt_Path_1nMake(JNIEnv* env, jclass jclass) {
    SkPath* obj = new SkPath();
    return reinterpret_cast<jlong>(obj);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PathKt__1nMakeFromSVGString
  (JNIEnv* env, jclass jclass, jstring d) {
    SkPath* obj = new SkPath();
    SkString s = skString(env, d);
    if (SkParsePath::FromSVGString(s.c_str(), obj))
        return reinterpret_cast<jlong>(obj);
    else {
        delete obj;
        return 0;
    }
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathKt_Path_1nEquals(JNIEnv* env, jclass jclass, jlong aPtr, jlong bPtr) {
    SkPath* a = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(aPtr));
    SkPath* b = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(bPtr));
    return *a == *b;
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathKt__1nIsInterpolatable(JNIEnv* env, jclass jclass, jlong ptr, jlong comparePtr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPath* compare = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(comparePtr));
    return instance->isInterpolatable(*compare);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PathKt__1nMakeLerp(JNIEnv* env, jclass jclass, jlong ptr, jlong endingPtr, jfloat weight) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPath* ending = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(endingPtr));
    SkPath* out = new SkPath();
    if (instance->interpolate(*ending, weight, out)) {
        return reinterpret_cast<jlong>(out);
    } else {
        delete out;
        return 0;
    }
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_PathKt__1nGetFillMode(JNIEnv* env, jclass jclass, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return static_cast<jint>(instance->getFillType());
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathKt__1nSetFillMode(JNIEnv* env, jclass jclass, jlong ptr, jint fillMode) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->setFillType(static_cast<SkPathFillType>(fillMode));
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathKt__1nIsConvex(JNIEnv* env, jclass jclass, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->isConvex();
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathKt__1nIsOval(JNIEnv* env, jclass jclass, jlong ptr, jfloatArray resultArray) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkRect bounds;
    if (instance->isOval(&bounds)) {
        skija::Rect::copyToInterop(env, bounds, resultArray);
        return true;
    } else {
        return false;
    }
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathKt__1nIsRRect(JNIEnv* env, jclass jclass, jlong ptr, jfloatArray resultArray) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkRRect rrect;
    if (instance->isRRect(&rrect)) {
        skija::RRect::copyToInterop(env, rrect, resultArray);
        return true;
    } else {
        return false;
    }
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathKt__1nIsEmpty(JNIEnv* env, jclass jclass, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->isEmpty();
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathKt__1nIsLastContourClosed(JNIEnv* env, jclass jclass, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->isLastContourClosed();
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathKt__1nIsFinite(JNIEnv* env, jclass jclass, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->isFinite();
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathKt_Path_1nIsVolatile(JNIEnv* env, jclass jclass, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->isVolatile();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathKt_Path_1nSetVolatile(JNIEnv* env, jclass jclass, jlong ptr, jboolean isVolatile) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->setIsVolatile(isVolatile);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathKt_Path_1nSwap(JNIEnv* env, jclass jclass, jlong ptr, jlong otherPtr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPath* other = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(otherPtr));
    instance->swap(*other);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathKt__1nIsLineDegenerate(JNIEnv* env, jclass jclass, jfloat x0, jfloat y0, jfloat x1, jfloat y1, jboolean exact) {
    return SkPath::IsLineDegenerate({x0, y0}, {x1, y1}, exact);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathKt__1nIsQuadDegenerate(JNIEnv* env, jclass jclass, jfloat x0, jfloat y0, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jboolean exact) {
    return SkPath::IsQuadDegenerate({x0, y0}, {x1, y1}, {x2, y2}, exact);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathKt__1nIsCubicDegenerate(JNIEnv* env, jclass jclass, jfloat x0, jfloat y0, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat x3, jfloat y3, jboolean exact) {
    return SkPath::IsCubicDegenerate({x0, y0}, {x1, y1}, {x2, y2}, {x3, y3}, exact);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathKt__1nMaybeGetAsLine(JNIEnv* env, jclass jclass, jlong ptr, jfloatArray resultArray) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPoint line[2];
    if (instance->isLine(line)) {
        SkRect rect = SkRect::MakeLTRB(line[0].x(), line[0].y(), line[1].x(), line[1].y());
        skija::Rect::copyToInterop(env, rect, resultArray);
        return true;
    } else {
        return false;
    }
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_PathKt__1nGetPointsCount(JNIEnv* env, jclass jclass, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->countPoints();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathKt__1nGetPoint(JNIEnv* env, jclass jclass, jlong ptr, jint index, jfloatArray resultArray) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    skija::Point::copyToInterop(env, instance->getPoint(index), resultArray);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_PathKt__1nGetPoints(JNIEnv* env, jclass jclass, jlong ptr, jfloatArray pointsArray, jint max) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    jfloat* points = pointsArray == nullptr ? nullptr : env->GetFloatArrayElements(pointsArray, 0);
    int count = instance->getPoints({reinterpret_cast<SkPoint*>(points), max});
    if (pointsArray != nullptr) {
        env->ReleaseFloatArrayElements(pointsArray, points, 0);
    }
    return count;
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_PathKt__1nCountVerbs(JNIEnv* env, jclass jclass, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->countVerbs();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_PathKt__1nGetVerbs(JNIEnv* env, jclass jclass, jlong ptr, jbyteArray verbsArray, jint max) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    jbyte* verbs = verbsArray == nullptr ? nullptr : env->GetByteArrayElements(verbsArray, 0);
    int count = instance->getVerbs({reinterpret_cast<uint8_t *>(verbs), max});
    if (verbsArray != nullptr)
        env->ReleaseByteArrayElements(verbsArray, verbs, 0);
    return count;
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_PathKt__1nApproximateBytesUsed(JNIEnv* env, jclass jclass, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return (jint) instance->approximateBytesUsed();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathKt__1nGetBounds(JNIEnv* env, jclass jclass, jlong ptr, jfloatArray resultArray) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    skija::Rect::copyToInterop(env, instance->getBounds(), resultArray);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathKt__1nComputeTightBounds(JNIEnv* env, jclass jclass, jlong ptr, jfloatArray resultArray) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    skija::Rect::copyToInterop(env, instance->computeTightBounds(), resultArray);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathKt__1nConservativelyContainsRect(JNIEnv* env, jclass jclass, jlong ptr, float l, float t, float r, float b) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkRect rect {l, t, r, b};
    return instance->conservativelyContainsRect(rect);
}

extern "C" JNIEXPORT jint Java_org_jetbrains_skia_PathKt__1nConvertConicToQuads
  (JNIEnv* env, jclass jclass, jfloat x0, jfloat y0, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat w, jint pow2, jfloatArray resultArray) {
    jfloat* points = env->GetFloatArrayElements(resultArray, 0);
    int count = SkPath::ConvertConicToQuads({x0, y0}, {x1, y1}, {x2, y2}, w, reinterpret_cast<SkPoint*>(points), pow2);
    env->ReleaseFloatArrayElements(resultArray, points, 0);
    return count;
}

extern "C" JNIEXPORT jboolean Java_org_jetbrains_skia_PathKt__1nIsRect
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray resultArray) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkRect rect;
    if (instance->isRect(&rect)) {
        skija::Rect::copyToInterop(env, rect, resultArray);
        return true;
    } else {
        return false;
    }
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathKt__1nOffset
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat dx, jfloat dy, jlong dstPtr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPath* dst = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(dstPtr));
    *dst = instance->makeOffset(dx, dy);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathKt__1nTransform
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray matrixArr, jlong dstPtr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPath* dst = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(dstPtr));
    std::unique_ptr<SkMatrix> matrix = skMatrix(env, matrixArr);
    *dst = instance->makeTransform(*matrix);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathKt__1nGetLastPt
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray resultArray) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPoint out;
    if (instance->getLastPt(&out)) {
        skija::Point::copyToInterop(env, out, resultArray);
        return true;
    } else {
        return false;
    }
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_PathKt__1nGetSegmentMasks
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->getSegmentMasks();
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathKt__1nContains
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat x, jfloat y) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->contains(x, y);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathKt__1nDump
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->dump();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathKt__1nDumpHex
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->dumpHex();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_PathKt__1nSerializeToBytes
  (JNIEnv* env, jclass jclass, jlong ptr, jbyteArray dstArray) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    jbyte* dst = dstArray == nullptr ? nullptr : env->GetByteArrayElements(dstArray, 0);
    size_t count = instance->writeToMemory(reinterpret_cast<void*>(dst));
    if (dst != nullptr) {
        env->ReleaseByteArrayElements(dstArray, dst, 0);
    }

    if (count > std::numeric_limits<jint>::max()) {
        return -1;
    } else {
        return count;
    }
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PathKt__1nMakeCombining
  (JNIEnv* env, jclass jclass, jlong aPtr, jlong bPtr, jint jop) {
    SkPath* a = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(aPtr));
    SkPath* b = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(bPtr));
    SkPathOp op = static_cast<SkPathOp>(jop);
    auto res = std::make_unique<SkPath>();
    if (Op(*a, *b, op, res.get()))
        return reinterpret_cast<jlong>(res.release());
    else
        return 0;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PathKt__1nMakeFromBytes
  (JNIEnv* env, jclass jclass, jbyteArray bytesArray, jint _size) {
    int count = env->GetArrayLength(bytesArray);
    jbyte* bytes = env->GetByteArrayElements(bytesArray, 0);
    std::optional<SkPath> pathOpt = SkPath::ReadFromMemory(bytes, count);
    env->ReleaseByteArrayElements(bytesArray, bytes, 0);
    if (pathOpt.has_value()) {
        SkPath* instance = new SkPath(pathOpt.value());
        return reinterpret_cast<jlong>(instance);
    } else {
        return 0;
    }
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_PathKt_Path_1nGetGenerationId
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->getGenerationID();
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PathKt__1nIsValid
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->isValid();
}