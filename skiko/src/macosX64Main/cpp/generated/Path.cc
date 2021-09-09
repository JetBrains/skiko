
// This file has been auto generated.

#include <algorithm>
#include <cfloat>
#include <iostream>
#include <vector>
#include "SkPath.h"
#include "SkPathOps.h"
#include "include/utils/SkParsePath.h"
#include "common.h"

static void deletePath(SkPath* path) {
    // std::cout << "Deleting [SkPath " << path << "]" << std::endl;
    delete path;
}

extern "C" jlong org_jetbrains_skia_Path__1nGetFinalizer(kref __Kinstance) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deletePath));
}

extern "C" jlong org_jetbrains_skia_Path__1nMake(kref __Kinstance) {
    SkPath* obj = new SkPath();
    return reinterpret_cast<jlong>(obj);
}


extern "C" jlong org_jetbrains_skia_Path__1nMakeFromSVGString
  (kref __Kinstance, jstring d) {
    TODO("implement org_jetbrains_skia_Path__1nMakeFromSVGString");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Path__1nMakeFromSVGString
  (kref __Kinstance, jstring d) {
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


extern "C" jboolean org_jetbrains_skia_Path__1nEquals(kref __Kinstance, jlong aPtr, jlong bPtr) {
    SkPath* a = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(aPtr));
    SkPath* b = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(bPtr));
    return *a == *b;
}

extern "C" jboolean org_jetbrains_skia_Path__1nIsInterpolatable(kref __Kinstance, jlong ptr, jlong comparePtr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPath* compare = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(comparePtr));
    return instance->isInterpolatable(*compare);
}

extern "C" jlong org_jetbrains_skia_Path__1nMakeLerp(kref __Kinstance, jlong ptr, jlong endingPtr, jfloat weight) {
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

extern "C" jint org_jetbrains_skia_Path__1nGetFillMode(kref __Kinstance, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return static_cast<jint>(instance->getFillType());
}

extern "C" void org_jetbrains_skia_Path__1nSetFillMode(kref __Kinstance, jlong ptr, jint fillMode) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->setFillType(static_cast<SkPathFillType>(fillMode));
}

extern "C" jboolean org_jetbrains_skia_Path__1nIsConvex(kref __Kinstance, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->isConvex();
}


extern "C" jobject org_jetbrains_skia_Path__1nIsOval(kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_Path__1nIsOval(kref");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Path__1nIsOval(kref __Kinstance, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkRect bounds;
    if (instance->isOval(&bounds))
        return skija::Rect::fromSkRect(env, bounds);
    else
        return nullptr;
}
#endif



extern "C" jobject org_jetbrains_skia_Path__1nIsRRect(kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_Path__1nIsRRect(kref");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Path__1nIsRRect(kref __Kinstance, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkRRect rrect;
    if (instance->isRRect(&rrect))
        return skija::RRect::fromSkRRect(env, rrect);
    else
        return nullptr;
}
#endif


extern "C" void org_jetbrains_skia_Path__1nReset(kref __Kinstance, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->reset();
}

extern "C" void org_jetbrains_skia_Path__1nRewind(kref __Kinstance, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->rewind();
}

extern "C" jboolean org_jetbrains_skia_Path__1nIsEmpty(kref __Kinstance, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->isEmpty();
}

extern "C" jboolean org_jetbrains_skia_Path__1nIsLastContourClosed(kref __Kinstance, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->isLastContourClosed();
}

extern "C" jboolean org_jetbrains_skia_Path__1nIsFinite(kref __Kinstance, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->isFinite();
}

extern "C" jboolean org_jetbrains_skia_Path__1nIsVolatile(kref __Kinstance, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->isVolatile();
}

extern "C" void org_jetbrains_skia_Path__1nSetVolatile(kref __Kinstance, jlong ptr, jboolean isVolatile) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->setIsVolatile(isVolatile);
}

extern "C" jboolean org_jetbrains_skia_Path__1nIsLineDegenerate(kref __Kinstance, jfloat x0, jfloat y0, jfloat x1, jfloat y1, jboolean exact) {
    return SkPath::IsLineDegenerate({x0, y0}, {x1, y1}, exact);
}

extern "C" jboolean org_jetbrains_skia_Path__1nIsQuadDegenerate(kref __Kinstance, jfloat x0, jfloat y0, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jboolean exact) {
    return SkPath::IsQuadDegenerate({x0, y0}, {x1, y1}, {x2, y2}, exact);
}

extern "C" jboolean org_jetbrains_skia_Path__1nIsCubicDegenerate(kref __Kinstance, jfloat x0, jfloat y0, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat x3, jfloat y3, jboolean exact) {
    return SkPath::IsCubicDegenerate({x0, y0}, {x1, y1}, {x2, y2}, {x3, y3}, exact);
}


extern "C" jobjectArray org_jetbrains_skia_Path__1nMaybeGetAsLine(kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_Path__1nMaybeGetAsLine(kref");
}
     
#if 0 
extern "C" jobjectArray org_jetbrains_skia_Path__1nMaybeGetAsLine(kref __Kinstance, jlong ptr) {
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


extern "C" jint org_jetbrains_skia_Path__1nGetPointsCount(kref __Kinstance, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->countPoints();
}


extern "C" jobject org_jetbrains_skia_Path__1nGetPoint(kref __Kinstance, jlong ptr, jint index) {
    TODO("implement org_jetbrains_skia_Path__1nGetPoint(kref");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Path__1nGetPoint(kref __Kinstance, jlong ptr, jint index) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPoint p = instance->getPoint(index);
    return skija::Point::fromSkPoint(env, p);
}
#endif



extern "C" jint org_jetbrains_skia_Path__1nGetPoints(kref __Kinstance, jlong ptr, jobjectArray pointsArray, jint max) {
    TODO("implement org_jetbrains_skia_Path__1nGetPoints(kref");
}
     
#if 0 
extern "C" jint org_jetbrains_skia_Path__1nGetPoints(kref __Kinstance, jlong ptr, jobjectArray pointsArray, jint max) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    std::vector<SkPoint> p(std::min<jint>(max, instance->countPoints()));
    int count = instance->getPoints(p.data(), max);
    for (int i = 0; i < max && i < count; ++ i)
        env->SetObjectArrayElement(pointsArray, i, skija::Point::fromSkPoint(env, p[i]));
    return count;
}
#endif


extern "C" jint org_jetbrains_skia_Path__1nCountVerbs(kref __Kinstance, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->countVerbs();
}


extern "C" jint org_jetbrains_skia_Path__1nGetVerbs(kref __Kinstance, jlong ptr, jbyteArray verbsArray, jint max) {
    TODO("implement org_jetbrains_skia_Path__1nGetVerbs(kref");
}
     
#if 0 
extern "C" jint org_jetbrains_skia_Path__1nGetVerbs(kref __Kinstance, jlong ptr, jbyteArray verbsArray, jint max) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    jbyte* verbs = verbsArray == nullptr ? nullptr : env->GetByteArrayElements(verbsArray, 0);
    int count = instance->getVerbs(reinterpret_cast<uint8_t *>(verbs), max);
    if (verbsArray != nullptr)
        env->ReleaseByteArrayElements(verbsArray, verbs, 0);
    return count;
}
#endif


extern "C" jint org_jetbrains_skia_Path__1nApproximateBytesUsed(kref __Kinstance, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return (jint) instance->approximateBytesUsed();
}

extern "C" void org_jetbrains_skia_Path__1nSwap(kref __Kinstance, jlong ptr, jlong otherPtr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPath* other = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(otherPtr));
    instance->swap(*other);
}


extern "C" jobject org_jetbrains_skia_Path__1nGetBounds(kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_Path__1nGetBounds(kref");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Path__1nGetBounds(kref __Kinstance, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return skija::Rect::fromSkRect(env, instance->getBounds());
}
#endif


extern "C" void org_jetbrains_skia_Path__1nUpdateBoundsCache(kref __Kinstance, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->updateBoundsCache();
}


extern "C" jobject org_jetbrains_skia_Path__1nComputeTightBounds(kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_Path__1nComputeTightBounds(kref");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Path__1nComputeTightBounds(kref __Kinstance, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return skija::Rect::fromSkRect(env, instance->computeTightBounds());
}
#endif


extern "C" jboolean org_jetbrains_skia_Path__1nConservativelyContainsRect(kref __Kinstance, jlong ptr, float l, float t, float r, float b) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkRect rect {l, t, r, b};
    return instance->conservativelyContainsRect(rect);
}

extern "C" void org_jetbrains_skia_Path__1nIncReserve(kref __Kinstance, jlong ptr, int extraPtCount) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->incReserve(extraPtCount);
}

extern "C" void org_jetbrains_skia_Path__1nMoveTo(kref __Kinstance, jlong ptr, jfloat x, jfloat y) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->moveTo(x, y);
}

extern "C" void org_jetbrains_skia_Path__1nRMoveTo(kref __Kinstance, jlong ptr, jfloat dx, jfloat dy) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->rMoveTo(dx, dy);
}

extern "C" void org_jetbrains_skia_Path__1nLineTo(kref __Kinstance, jlong ptr, jfloat x, jfloat y) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->lineTo(x, y);
}

extern "C" void org_jetbrains_skia_Path__1nRLineTo(kref __Kinstance, jlong ptr, jfloat dx, jfloat dy) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->rLineTo(dx, dy);
}

extern "C" void org_jetbrains_skia_Path__1nQuadTo(kref __Kinstance, jlong ptr, jfloat x1, jfloat y1, jfloat x2, jfloat y2) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->quadTo(x1, y1, x2, y2);
}

extern "C" void org_jetbrains_skia_Path__1nRQuadTo(kref __Kinstance, jlong ptr, jfloat dx1, jfloat dy1, jfloat dx2, jfloat dy2) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->rQuadTo(dx1, dy1, dx2, dy2);
}

extern "C" void org_jetbrains_skia_Path__1nConicTo(kref __Kinstance, jlong ptr, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat w) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->conicTo(x1, y1, x2, y2, w);
}

extern "C" void org_jetbrains_skia_Path__1nRConicTo(kref __Kinstance, jlong ptr, jfloat dx1, jfloat dy1, jfloat dx2, jfloat dy2, jfloat w) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->rConicTo(dx1, dy1, dx2, dy2, w);
}

extern "C" void org_jetbrains_skia_Path__1nCubicTo(kref __Kinstance, jlong ptr, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat x3, jfloat y3) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->cubicTo(x1, y1, x2, y2, x3, y3);
}

extern "C" void org_jetbrains_skia_Path__1nRCubicTo(kref __Kinstance, jlong ptr, jfloat dx1, jfloat dy1, jfloat dx2, jfloat dy2, jfloat dx3, jfloat dy3) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->rCubicTo(dx1, dy1, dx2, dy2, dx3, dy3);
}

extern "C" void org_jetbrains_skia_Path__1nArcTo(kref __Kinstance, jlong ptr, jfloat left, jfloat top, jfloat right, jfloat bottom, jfloat startAngle, jfloat sweepAngle, jboolean forceMoveTo) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->arcTo({left, top, right, bottom}, startAngle, sweepAngle, forceMoveTo);
}

extern "C" void org_jetbrains_skia_Path__1nTangentArcTo(kref __Kinstance, jlong ptr, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat radius) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->arcTo(x1, y1, x2, y2, radius);
}

extern "C" void org_jetbrains_skia_Path__1nEllipticalArcTo(kref __Kinstance, jlong ptr, jfloat rx, jfloat ry, jfloat xAxisRotate, jint size, jint direction, jfloat x, float y) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->arcTo(rx, ry, xAxisRotate, static_cast<SkPath::ArcSize>(size), static_cast<SkPathDirection>(direction), x, y);
}

extern "C" void org_jetbrains_skia_Path__1nREllipticalArcTo(kref __Kinstance, jlong ptr, jfloat rx, jfloat ry, jfloat xAxisRotate, jint size, jint direction, jfloat dx, float dy) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->rArcTo(rx, ry, xAxisRotate, static_cast<SkPath::ArcSize>(size), static_cast<SkPathDirection>(direction), dx, dy);
}

extern "C" void org_jetbrains_skia_Path__1nClosePath(kref __Kinstance, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->close();
}


extern "C" jobjectArray org_jetbrains_skia_Path__1nConvertConicToQuads
  (kref __Kinstance, jfloat x0, jfloat y0, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat w, jint pow2) {
    TODO("implement org_jetbrains_skia_Path__1nConvertConicToQuads");
}
     
#if 0 
extern "C" jobjectArray org_jetbrains_skia_Path__1nConvertConicToQuads
  (kref __Kinstance, jfloat x0, jfloat y0, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat w, jint pow2) {
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
  (kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_Path__1nIsRect");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Path__1nIsRect
  (kref __Kinstance, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkRect rect;
    if (instance->isRect(&rect))
        return skija::Rect::fromSkRect(env, rect);
    else
        return nullptr;
}
#endif


extern "C" void org_jetbrains_skia_Path__1nAddRect
  (kref __Kinstance, jlong ptr, jfloat l, jfloat t, jfloat r, jfloat b, jint dirInt, jint start) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPathDirection dir = static_cast<SkPathDirection>(dirInt);
    instance->addRect({l, t, r, b}, dir, start);
}

extern "C" void org_jetbrains_skia_Path__1nAddOval
  (kref __Kinstance, jlong ptr, jfloat l, jfloat t, jfloat r, jfloat b, jint dirInt, jint start) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPathDirection dir = static_cast<SkPathDirection>(dirInt);
    instance->addOval({l, t, r, b}, dir, start);
}

extern "C" void org_jetbrains_skia_Path__1nAddCircle
  (kref __Kinstance, jlong ptr, jfloat x, jfloat y, jfloat r, jint dirInt) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPathDirection dir = static_cast<SkPathDirection>(dirInt);
    instance->addCircle(x, y, r, dir);
}

extern "C" void org_jetbrains_skia_Path__1nAddArc
  (kref __Kinstance, jlong ptr, jfloat l, jfloat t, jfloat r, jfloat b, jfloat startAngle, jfloat sweepAngle) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->addArc({l, t, r, b}, startAngle, sweepAngle);
}


extern "C" void org_jetbrains_skia_Path__1nAddRRect
  (kref __Kinstance, jlong ptr, jfloat l, jfloat t, jfloat r, jfloat b, jfloatArray radii, jint dirInt, jint start) {
    TODO("implement org_jetbrains_skia_Path__1nAddRRect");
}
     
#if 0 
extern "C" void org_jetbrains_skia_Path__1nAddRRect
  (kref __Kinstance, jlong ptr, jfloat l, jfloat t, jfloat r, jfloat b, jfloatArray radii, jint dirInt, jint start) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkRRect rrect = skija::RRect::toSkRRect(env, l, t, r, b, radii);
    SkPathDirection dir = static_cast<SkPathDirection>(dirInt);
    instance->addRRect(rrect, dir, start);
}
#endif



extern "C" void org_jetbrains_skia_Path__1nAddPoly
  (kref __Kinstance, jlong ptr, jfloatArray coords, jboolean close) {
    TODO("implement org_jetbrains_skia_Path__1nAddPoly");
}
     
#if 0 
extern "C" void org_jetbrains_skia_Path__1nAddPoly
  (kref __Kinstance, jlong ptr, jfloatArray coords, jboolean close) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    jsize len = env->GetArrayLength(coords);
    jfloat* arr = env->GetFloatArrayElements(coords, 0);
    instance->addPoly(reinterpret_cast<SkPoint*>(arr), len / 2, close);
    env->ReleaseFloatArrayElements(coords, arr, 0);
}
#endif


extern "C" void org_jetbrains_skia_Path__1nAddPath
  (kref __Kinstance, jlong ptr, jlong srcPtr, jboolean extend) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPath* src = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(srcPtr));
    SkPath::AddPathMode mode = extend ? SkPath::AddPathMode::kExtend_AddPathMode : SkPath::AddPathMode::kAppend_AddPathMode;
    instance->addPath(*src, mode);
}

extern "C" void org_jetbrains_skia_Path__1nAddPathOffset
  (kref __Kinstance, jlong ptr, jlong srcPtr, jfloat dx, jfloat dy, jboolean extend) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPath* src = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(srcPtr));
    SkPath::AddPathMode mode = extend ? SkPath::AddPathMode::kExtend_AddPathMode : SkPath::AddPathMode::kAppend_AddPathMode;
    instance->addPath(*src, dx, dy, mode);
}


extern "C" void org_jetbrains_skia_Path__1nAddPathTransform
  (kref __Kinstance, jlong ptr, jlong srcPtr, jfloatArray matrixArr, jboolean extend) {
    TODO("implement org_jetbrains_skia_Path__1nAddPathTransform");
}
     
#if 0 
extern "C" void org_jetbrains_skia_Path__1nAddPathTransform
  (kref __Kinstance, jlong ptr, jlong srcPtr, jfloatArray matrixArr, jboolean extend) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPath* src = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(srcPtr));
    std::unique_ptr<SkMatrix> matrix = skMatrix(env, matrixArr);
    SkPath::AddPathMode mode = extend ? SkPath::AddPathMode::kExtend_AddPathMode : SkPath::AddPathMode::kAppend_AddPathMode;
    instance->addPath(*src, *matrix, mode);
}
#endif


extern "C" void org_jetbrains_skia_Path__1nReverseAddPath
  (kref __Kinstance, jlong ptr, jlong srcPtr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPath* src = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(srcPtr));
    instance->reverseAddPath(*src);
}

extern "C" void org_jetbrains_skia_Path__1nOffset
  (kref __Kinstance, jlong ptr, jfloat dx, jfloat dy, jlong dstPtr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPath* dst = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(dstPtr));
    instance->offset(dx, dy, dst);
}


extern "C" void org_jetbrains_skia_Path__1nTransform
  (kref __Kinstance, jlong ptr, jfloatArray matrixArr, jlong dstPtr, jboolean pcBool) {
    TODO("implement org_jetbrains_skia_Path__1nTransform");
}
     
#if 0 
extern "C" void org_jetbrains_skia_Path__1nTransform
  (kref __Kinstance, jlong ptr, jfloatArray matrixArr, jlong dstPtr, jboolean pcBool) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPath* dst = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(dstPtr));
    std::unique_ptr<SkMatrix> matrix = skMatrix(env, matrixArr);
    SkApplyPerspectiveClip pc = pcBool ? SkApplyPerspectiveClip::kYes : SkApplyPerspectiveClip::kNo;
    instance->transform(*matrix, dst, pc);
}
#endif



extern "C" jobject org_jetbrains_skia_Path__1nGetLastPt
  (kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_Path__1nGetLastPt");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Path__1nGetLastPt
  (kref __Kinstance, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    SkPoint out;
    if (instance->getLastPt(&out))
        return skija::Point::fromSkPoint(env, out);
    else
        return nullptr;
}
#endif


extern "C" void org_jetbrains_skia_Path__1nSetLastPt
  (kref __Kinstance, jlong ptr, jfloat x, jfloat y) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->setLastPt(x, y);
}

extern "C" jint org_jetbrains_skia_Path__1nGetSegmentMasks
  (kref __Kinstance, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->getSegmentMasks();
}

extern "C" jboolean org_jetbrains_skia_Path__1nContains
  (kref __Kinstance, jlong ptr, jfloat x, jfloat y) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->contains(x, y);
}

extern "C" void org_jetbrains_skia_Path__1nDump
  (kref __Kinstance, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->dump();
}

extern "C" void org_jetbrains_skia_Path__1nDumpHex
  (kref __Kinstance, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    instance->dumpHex();
}


extern "C" jbyteArray org_jetbrains_skia_Path__1nSerializeToBytes
  (kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_Path__1nSerializeToBytes");
}
     
#if 0 
extern "C" jbyteArray org_jetbrains_skia_Path__1nSerializeToBytes
  (kref __Kinstance, jlong ptr) {
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
  (kref __Kinstance, jlong aPtr, jlong bPtr, jint jop) {
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
  (kref __Kinstance, jbyteArray bytesArray) {
    TODO("implement org_jetbrains_skia_Path__1nMakeFromBytes");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Path__1nMakeFromBytes
  (kref __Kinstance, jbyteArray bytesArray) {
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
  (kref __Kinstance, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->getGenerationID();
}

extern "C" jboolean org_jetbrains_skia_Path__1nIsValid
  (kref __Kinstance, jlong ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(ptr));
    return instance->isValid();
}
