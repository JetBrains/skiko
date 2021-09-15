
// This file has been auto generated.

#include <algorithm>
#include <cfloat>
#include <iostream>
#include <vector>
#include "SkPath.h"
#include "SkPathOps.h"
#include "include/utils/SkParsePath.h"
#include "common.h"
#include "native_interop.h"

static void deletePath(SkPath* path) {
    // std::cout << "Deleting [SkPath " << path << "]" << std::endl;
    delete path;
}

extern "C" jlong org_jetbrains_skia_Path__1nGetFinalizer() {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deletePath));
}

extern "C" jlong org_jetbrains_skia_Path__1nMake() {
    SkPath* obj = new SkPath();
    return reinterpret_cast<jlong>(obj);
}


extern "C" jlong org_jetbrains_skia_Path__1nMakeFromSVGString
  (jstring d) {
    TODO("implement org_jetbrains_skia_Path__1nMakeFromSVGString");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Path__1nMakeFromSVGString
  (jstring d) {
    SkPath* obj = new SkPath();
    SkString s = skString(env, d);
    if (SkParsePath::FromSVGString(s.c_str(), obj))
        return reinterpret_cast<jlong>(obj);
    else {
        delete obj;
        return 0;
    }
}
#endif


extern "C" jboolean org_jetbrains_skia_Path__1nEquals(jlong aPtr, jlong bPtr) {
    SkPath* a = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(aPtr));
    SkPath* b = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(bPtr));
    return *a == *b;
}

extern "C" jboolean org_jetbrains_skia_Path__1nIsInterpolatable(jlong ptr, jlong comparePtr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPath* compare = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(comparePtr));
    return instance->isInterpolatable(*compare);
}

extern "C" jlong org_jetbrains_skia_Path__1nMakeLerp(jlong ptr, jlong endingPtr, jfloat weight) {
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

extern "C" jint org_jetbrains_skia_Path__1nGetFillMode(jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return static_cast<jint>(instance->getFillType());
}

extern "C" void org_jetbrains_skia_Path__1nSetFillMode(jlong ptr, jint fillMode) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->setFillType(static_cast<SkPathFillType>(fillMode));
}

extern "C" jboolean org_jetbrains_skia_Path__1nIsConvex(jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->isConvex();
}


extern "C" jobject org_jetbrains_skia_Path__1nIsOval(jlong ptr) {
    TODO("implement org_jetbrains_skia_Path__1nIsOval(ptr)");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Path__1nIsOval(jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkRect bounds;
    if (instance->isOval(&bounds))
        return skija::Rect::fromSkRect(env, bounds);
    else
        return nullptr;
}
#endif



extern "C" jobject org_jetbrains_skia_Path__1nIsRRect(jlong ptr) {
    TODO("implement org_jetbrains_skia_Path__1nIsRRect(ptr)");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Path__1nIsRRect(jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkRRect rrect;
    if (instance->isRRect(&rrect))
        return skija::RRect::fromSkRRect(env, rrect);
    else
        return nullptr;
}
#endif


extern "C" void org_jetbrains_skia_Path__1nReset(jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->reset();
}

extern "C" void org_jetbrains_skia_Path__1nRewind(jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->rewind();
}

extern "C" jboolean org_jetbrains_skia_Path__1nIsEmpty(jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->isEmpty();
}

extern "C" jboolean org_jetbrains_skia_Path__1nIsLastContourClosed(jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->isLastContourClosed();
}

extern "C" jboolean org_jetbrains_skia_Path__1nIsFinite(jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->isFinite();
}

extern "C" jboolean org_jetbrains_skia_Path__1nIsVolatile(jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->isVolatile();
}

extern "C" void org_jetbrains_skia_Path__1nSetVolatile(jlong ptr, jboolean isVolatile) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->setIsVolatile(isVolatile);
}

extern "C" jboolean org_jetbrains_skia_Path__1nIsLineDegenerate(jfloat x0, jfloat y0, jfloat x1, jfloat y1, jboolean exact) {
    return SkPath::IsLineDegenerate({x0, y0}, {x1, y1}, exact);
}

extern "C" jboolean org_jetbrains_skia_Path__1nIsQuadDegenerate(jfloat x0, jfloat y0, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jboolean exact) {
    return SkPath::IsQuadDegenerate({x0, y0}, {x1, y1}, {x2, y2}, exact);
}

extern "C" jboolean org_jetbrains_skia_Path__1nIsCubicDegenerate(jfloat x0, jfloat y0, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat x3, jfloat y3, jboolean exact) {
    return SkPath::IsCubicDegenerate({x0, y0}, {x1, y1}, {x2, y2}, {x3, y3}, exact);
}


extern "C" jobjectArray org_jetbrains_skia_Path__1nMaybeGetAsLine(jlong ptr) {
    TODO("implement org_jetbrains_skia_Path__1nMaybeGetAsLine(ptr)");
}
     
#if 0 
extern "C" jobjectArray org_jetbrains_skia_Path__1nMaybeGetAsLine(jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPoint line[2];
    if (instance->isLine(line)) {
        jobjectArray res = env->NewObjectArray(2, skija::Point::cls, nullptr);
        env->SetObjectArrayElement(res, 0, skija::Point::fromSkPoint(env, line[0]));
        env->SetObjectArrayElement(res, 1, skija::Point::fromSkPoint(env, line[1]));
        return res;
    } else
        return nullptr;
}
#endif


extern "C" jint org_jetbrains_skia_Path__1nGetPointsCount(jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->countPoints();
}


extern "C" jobject org_jetbrains_skia_Path__1nGetPoint(jlong ptr, jint index) {
    TODO("implement org_jetbrains_skia_Path__1nGetPoint(ptr, index)");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Path__1nGetPoint(jlong ptr, jint index) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPoint p = instance->getPoint(index);
    return skija::Point::fromSkPoint(env, p);
}
#endif



extern "C" jint org_jetbrains_skia_Path__1nGetPoints(jlong ptr, jobjectArray pointsArray, jint max) {
    TODO("implement org_jetbrains_skia_Path__1nGetPoints(ptr)");
}
     
#if 0 
extern "C" jint org_jetbrains_skia_Path__1nGetPoints(jlong ptr, jobjectArray pointsArray, jint max) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    std::vector<SkPoint> p(std::min<jint>(max, instance->countPoints()));
    int count = instance->getPoints(p.data(), max);
    for (int i = 0; i < max && i < count; ++ i)
        env->SetObjectArrayElement(pointsArray, i, skija::Point::fromSkPoint(env, p[i]));
    return count;
}
#endif


extern "C" jint org_jetbrains_skia_Path__1nCountVerbs(jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->countVerbs();
}


extern "C" jint org_jetbrains_skia_Path__1nGetVerbs(jlong ptr, jbyteArray verbsArray, jint max) {
    TODO("implement org_jetbrains_skia_Path__1nGetVerbs(ptr, verbsArray, max)");
}
     
#if 0 
extern "C" jint org_jetbrains_skia_Path__1nGetVerbs(jlong ptr, jbyteArray verbsArray, jint max) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    jbyte* verbs = verbsArray == nullptr ? nullptr : env->GetByteArrayElements(verbsArray, 0);
    int count = instance->getVerbs(reinterpret_cast<uint8_t *>(verbs), max);
    if (verbsArray != nullptr)
        env->ReleaseByteArrayElements(verbsArray, verbs, 0);
    return count;
}
#endif


extern "C" jint org_jetbrains_skia_Path__1nApproximateBytesUsed(jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return (jint) instance->approximateBytesUsed();
}

extern "C" void org_jetbrains_skia_Path__1nSwap(jlong ptr, jlong otherPtr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPath* other = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(otherPtr));
    instance->swap(*other);
}


extern "C" jobject org_jetbrains_skia_Path__1nGetBounds(jlong ptr) {
    TODO("implement org_jetbrains_skia_Path__1nGetBounds(ptr)");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Path__1nGetBounds(jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return skija::Rect::fromSkRect(env, instance->getBounds());
}
#endif


extern "C" void org_jetbrains_skia_Path__1nUpdateBoundsCache(jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->updateBoundsCache();
}


extern "C" jobject org_jetbrains_skia_Path__1nComputeTightBounds(jlong ptr) {
    TODO("implement org_jetbrains_skia_Path__1nComputeTightBounds(ptr)");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Path__1nComputeTightBounds(jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return skija::Rect::fromSkRect(env, instance->computeTightBounds());
}
#endif


extern "C" jboolean org_jetbrains_skia_Path__1nConservativelyContainsRect(jlong ptr, float l, float t, float r, float b) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkRect rect {l, t, r, b};
    return instance->conservativelyContainsRect(rect);
}

extern "C" void org_jetbrains_skia_Path__1nIncReserve(jlong ptr, int extraPtCount) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->incReserve(extraPtCount);
}

extern "C" void org_jetbrains_skia_Path__1nMoveTo(jlong ptr, jfloat x, jfloat y) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->moveTo(x, y);
}

extern "C" void org_jetbrains_skia_Path__1nRMoveTo(jlong ptr, jfloat dx, jfloat dy) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->rMoveTo(dx, dy);
}

extern "C" void org_jetbrains_skia_Path__1nLineTo(jlong ptr, jfloat x, jfloat y) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->lineTo(x, y);
}

extern "C" void org_jetbrains_skia_Path__1nRLineTo(jlong ptr, jfloat dx, jfloat dy) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->rLineTo(dx, dy);
}

extern "C" void org_jetbrains_skia_Path__1nQuadTo(jlong ptr, jfloat x1, jfloat y1, jfloat x2, jfloat y2) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->quadTo(x1, y1, x2, y2);
}

extern "C" void org_jetbrains_skia_Path__1nRQuadTo(jlong ptr, jfloat dx1, jfloat dy1, jfloat dx2, jfloat dy2) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->rQuadTo(dx1, dy1, dx2, dy2);
}

extern "C" void org_jetbrains_skia_Path__1nConicTo(jlong ptr, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat w) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->conicTo(x1, y1, x2, y2, w);
}

extern "C" void org_jetbrains_skia_Path__1nRConicTo(jlong ptr, jfloat dx1, jfloat dy1, jfloat dx2, jfloat dy2, jfloat w) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->rConicTo(dx1, dy1, dx2, dy2, w);
}

extern "C" void org_jetbrains_skia_Path__1nCubicTo(jlong ptr, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat x3, jfloat y3) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->cubicTo(x1, y1, x2, y2, x3, y3);
}

extern "C" void org_jetbrains_skia_Path__1nRCubicTo(jlong ptr, jfloat dx1, jfloat dy1, jfloat dx2, jfloat dy2, jfloat dx3, jfloat dy3) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->rCubicTo(dx1, dy1, dx2, dy2, dx3, dy3);
}

extern "C" void org_jetbrains_skia_Path__1nArcTo(jlong ptr, jfloat left, jfloat top, jfloat right, jfloat bottom, jfloat startAngle, jfloat sweepAngle, jboolean forceMoveTo) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->arcTo({left, top, right, bottom}, startAngle, sweepAngle, forceMoveTo);
}

extern "C" void org_jetbrains_skia_Path__1nTangentArcTo(jlong ptr, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat radius) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->arcTo(x1, y1, x2, y2, radius);
}

extern "C" void org_jetbrains_skia_Path__1nEllipticalArcTo(jlong ptr, jfloat rx, jfloat ry, jfloat xAxisRotate, jint size, jint direction, jfloat x, float y) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->arcTo(rx, ry, xAxisRotate, static_cast<SkPath::ArcSize>(size), static_cast<SkPathDirection>(direction), x, y);
}

extern "C" void org_jetbrains_skia_Path__1nREllipticalArcTo(jlong ptr, jfloat rx, jfloat ry, jfloat xAxisRotate, jint size, jint direction, jfloat dx, float dy) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->rArcTo(rx, ry, xAxisRotate, static_cast<SkPath::ArcSize>(size), static_cast<SkPathDirection>(direction), dx, dy);
}

extern "C" void org_jetbrains_skia_Path__1nClosePath(jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->close();
}


extern "C" jobjectArray org_jetbrains_skia_Path__1nConvertConicToQuads
  (jfloat x0, jfloat y0, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat w, jint pow2) {
    TODO("implement org_jetbrains_skia_Path__1nConvertConicToQuads");
}
     
#if 0 
extern "C" jobjectArray org_jetbrains_skia_Path__1nConvertConicToQuads
  (jfloat x0, jfloat y0, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat w, jint pow2) {
    std::vector<SkPoint> pts(1 + 2 * (1 << pow2));
    int count = SkPath::ConvertConicToQuads({x0, y0}, {x1, y1}, {x2, y2}, w, pts.data(), pow2);
    jobjectArray res = env->NewObjectArray(count, skija::Point::cls, nullptr);
    for (int i = 0; i < count; ++i) {
        env->SetObjectArrayElement(res, i, skija::Point::fromSkPoint(env, pts[i]));
    }
    return res;
}
#endif



extern "C" jobject org_jetbrains_skia_Path__1nIsRect
  (jlong ptr) {
    TODO("implement org_jetbrains_skia_Path__1nIsRect");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Path__1nIsRect
  (jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkRect rect;
    if (instance->isRect(&rect))
        return skija::Rect::fromSkRect(env, rect);
    else
        return nullptr;
}
#endif


extern "C" void org_jetbrains_skia_Path__1nAddRect
  (jlong ptr, jfloat l, jfloat t, jfloat r, jfloat b, jint dirInt, jint start) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPathDirection dir = static_cast<SkPathDirection>(dirInt);
    instance->addRect({l, t, r, b}, dir, start);
}

extern "C" void org_jetbrains_skia_Path__1nAddOval
  (jlong ptr, jfloat l, jfloat t, jfloat r, jfloat b, jint dirInt, jint start) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPathDirection dir = static_cast<SkPathDirection>(dirInt);
    instance->addOval({l, t, r, b}, dir, start);
}

extern "C" void org_jetbrains_skia_Path__1nAddCircle
  (jlong ptr, jfloat x, jfloat y, jfloat r, jint dirInt) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPathDirection dir = static_cast<SkPathDirection>(dirInt);
    instance->addCircle(x, y, r, dir);
}

extern "C" void org_jetbrains_skia_Path__1nAddArc
  (jlong ptr, jfloat l, jfloat t, jfloat r, jfloat b, jfloat startAngle, jfloat sweepAngle) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->addArc({l, t, r, b}, startAngle, sweepAngle);
}

extern "C" void org_jetbrains_skia_Path__1nAddRRect
  (jlong ptr, jfloat l, jfloat t, jfloat r, jfloat b, jfloatArray radii, jint radiiSize, jint dirInt, jint start) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkRRect rrect = skija::RRect::toSkRRect(l, t, r, b, radii, radiiSize);
    SkPathDirection dir = static_cast<SkPathDirection>(dirInt);
    instance->addRRect(rrect, dir, start);
}

extern "C" void org_jetbrains_skia_Path__1nAddPoly
  (jlong ptr, jfloatArray coords, jboolean close) {
    TODO("implement org_jetbrains_skia_Path__1nAddPoly");
}
     
#if 0 
extern "C" void org_jetbrains_skia_Path__1nAddPoly
  (jlong ptr, jfloatArray coords, jboolean close) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    jsize len = env->GetArrayLength(coords);
    jfloat* arr = env->GetFloatArrayElements(coords, 0);
    instance->addPoly(reinterpret_cast<SkPoint*>(arr), len / 2, close);
    env->ReleaseFloatArrayElements(coords, arr, 0);
}
#endif


extern "C" void org_jetbrains_skia_Path__1nAddPath
  (jlong ptr, jlong srcPtr, jboolean extend) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPath* src = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(srcPtr));
    SkPath::AddPathMode mode = extend ? SkPath::AddPathMode::kExtend_AddPathMode : SkPath::AddPathMode::kAppend_AddPathMode;
    instance->addPath(*src, mode);
}

extern "C" void org_jetbrains_skia_Path__1nAddPathOffset
  (jlong ptr, jlong srcPtr, jfloat dx, jfloat dy, jboolean extend) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPath* src = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(srcPtr));
    SkPath::AddPathMode mode = extend ? SkPath::AddPathMode::kExtend_AddPathMode : SkPath::AddPathMode::kAppend_AddPathMode;
    instance->addPath(*src, dx, dy, mode);
}


extern "C" void org_jetbrains_skia_Path__1nAddPathTransform
  (jlong ptr, jlong srcPtr, jfloatArray matrixArr, jboolean extend) {
    TODO("implement org_jetbrains_skia_Path__1nAddPathTransform");
}
     
#if 0 
extern "C" void org_jetbrains_skia_Path__1nAddPathTransform
  (jlong ptr, jlong srcPtr, jfloatArray matrixArr, jboolean extend) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPath* src = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(srcPtr));
    std::unique_ptr<SkMatrix> matrix = skMatrix(env, matrixArr);
    SkPath::AddPathMode mode = extend ? SkPath::AddPathMode::kExtend_AddPathMode : SkPath::AddPathMode::kAppend_AddPathMode;
    instance->addPath(*src, *matrix, mode);
}
#endif


extern "C" void org_jetbrains_skia_Path__1nReverseAddPath
  (jlong ptr, jlong srcPtr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPath* src = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(srcPtr));
    instance->reverseAddPath(*src);
}

extern "C" void org_jetbrains_skia_Path__1nOffset
  (jlong ptr, jfloat dx, jfloat dy, jlong dstPtr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPath* dst = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(dstPtr));
    instance->offset(dx, dy, dst);
}


extern "C" void org_jetbrains_skia_Path__1nTransform
  (jlong ptr, jfloatArray matrixArr, jlong dstPtr, jboolean pcBool) {
    TODO("implement org_jetbrains_skia_Path__1nTransform");
}
     
#if 0 
extern "C" void org_jetbrains_skia_Path__1nTransform
  (jlong ptr, jfloatArray matrixArr, jlong dstPtr, jboolean pcBool) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPath* dst = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(dstPtr));
    std::unique_ptr<SkMatrix> matrix = skMatrix(env, matrixArr);
    SkApplyPerspectiveClip pc = pcBool ? SkApplyPerspectiveClip::kYes : SkApplyPerspectiveClip::kNo;
    instance->transform(*matrix, dst, pc);
}
#endif



extern "C" jobject org_jetbrains_skia_Path__1nGetLastPt
  (jlong ptr) {
    TODO("implement org_jetbrains_skia_Path__1nGetLastPt");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Path__1nGetLastPt
  (jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPoint out;
    if (instance->getLastPt(&out))
        return skija::Point::fromSkPoint(env, out);
    else
        return nullptr;
}
#endif


extern "C" void org_jetbrains_skia_Path__1nSetLastPt
  (jlong ptr, jfloat x, jfloat y) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->setLastPt(x, y);
}

extern "C" jint org_jetbrains_skia_Path__1nGetSegmentMasks
  (jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->getSegmentMasks();
}

extern "C" jboolean org_jetbrains_skia_Path__1nContains
  (jlong ptr, jfloat x, jfloat y) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->contains(x, y);
}

extern "C" void org_jetbrains_skia_Path__1nDump
  (jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->dump();
}

extern "C" void org_jetbrains_skia_Path__1nDumpHex
  (jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->dumpHex();
}


extern "C" jbyteArray org_jetbrains_skia_Path__1nSerializeToBytes
  (jlong ptr) {
    TODO("implement org_jetbrains_skia_Path__1nSerializeToBytes");
}
     
#if 0 
extern "C" jbyteArray org_jetbrains_skia_Path__1nSerializeToBytes
  (jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    size_t count = instance->writeToMemory(nullptr);
    jbyteArray bytesArray = env->NewByteArray((jsize) count);
    jbyte* bytes = env->GetByteArrayElements(bytesArray, 0);
    instance->writeToMemory(bytes);
    env->ReleaseByteArrayElements(bytesArray, bytes, 0);
    return bytesArray;
}
#endif


extern "C" jlong org_jetbrains_skia_Path__1nMakeCombining
  (jlong aPtr, jlong bPtr, jint jop) {
    SkPath* a = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(aPtr));
    SkPath* b = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(bPtr));
    SkPathOp op = static_cast<SkPathOp>(jop);
    auto res = std::make_unique<SkPath>();
    if (Op(*a, *b, op, res.get()))
        return reinterpret_cast<jlong>(res.release());
    else
        return 0;
}


extern "C" jlong org_jetbrains_skia_Path__1nMakeFromBytes
  (jbyteArray bytesArray) {
    TODO("implement org_jetbrains_skia_Path__1nMakeFromBytes");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Path__1nMakeFromBytes
  (jbyteArray bytesArray) {
    SkPath* instance = new SkPath();
    int count = env->GetArrayLength(bytesArray);
    jbyte* bytes = env->GetByteArrayElements(bytesArray, 0);
    if (instance->readFromMemory(bytes, count)) {
        env->ReleaseByteArrayElements(bytesArray, bytes, 0);
        return reinterpret_cast<jlong>(instance);
    } else {
        env->ReleaseByteArrayElements(bytesArray, bytes, 0);
        delete instance;
        return 0;
    }
}
#endif


extern "C" jint org_jetbrains_skia_Path__1nGetGenerationId
  (jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->getGenerationID();
}

extern "C" jboolean org_jetbrains_skia_Path__1nIsValid
  (jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->isValid();
}
