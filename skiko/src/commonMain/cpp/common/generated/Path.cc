
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

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Path__1nGetFinalizer(KInteropPointer __Kinstance) {
    return reinterpret_cast<KNativePointer>((&deletePath));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Path__1nMake(KInteropPointer __Kinstance) {
    SkPath* obj = new SkPath();
    return reinterpret_cast<KNativePointer>(obj);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Path__1nMakeFromSVGString
  (KInteropPointer __Kinstance, KInteropPointer d) {
    TODO("implement org_jetbrains_skia_Path__1nMakeFromSVGString");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Path__1nMakeFromSVGString
  (KInteropPointer __Kinstance, KInteropPointer d) {
    SkPath* obj = new SkPath();
    SkString s = skString(env, d);
    if (SkParsePath::FromSVGString(s.c_str(), obj))
        return reinterpret_cast<KNativePointer>(obj);
    else {
        delete obj;
        return 0;
    }
}
#endif


SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nEquals(KInteropPointer __Kinstance, KNativePointer aPtr, KNativePointer bPtr) {
    SkPath* a = reinterpret_cast<SkPath*>((aPtr));
    SkPath* b = reinterpret_cast<SkPath*>((bPtr));
    return *a == *b;
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nIsInterpolatable(KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer comparePtr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPath* compare = reinterpret_cast<SkPath*>((comparePtr));
    return instance->isInterpolatable(*compare);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Path__1nMakeLerp(KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer endingPtr, KFloat weight) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPath* ending = reinterpret_cast<SkPath*>((endingPtr));
    SkPath* out = new SkPath();
    if (instance->interpolate(*ending, weight, out)) {
        return reinterpret_cast<KNativePointer>(out);
    } else {
        delete out;
        return 0;
    }
}

SKIKO_EXPORT KInt org_jetbrains_skia_Path__1nGetFillMode(KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return static_cast<KInt>(instance->getFillType());
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nSetFillMode(KInteropPointer __Kinstance, KNativePointer ptr, KInt fillMode) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->setFillType(static_cast<SkPathFillType>(fillMode));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nIsConvex(KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return instance->isConvex();
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Path__1nIsOval(KInteropPointer __Kinstance, KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Path__1nIsOval(KInteropPointer");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Path__1nIsOval(KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkRect bounds;
    if (instance->isOval(&bounds))
        return skija::Rect::fromSkRect(env, bounds);
    else
        return nullptr;
}
#endif



SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Path__1nIsRRect(KInteropPointer __Kinstance, KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Path__1nIsRRect(KInteropPointer");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Path__1nIsRRect(KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkRRect rrect;
    if (instance->isRRect(&rrect))
        return skija::RRect::fromSkRRect(env, rrect);
    else
        return nullptr;
}
#endif


SKIKO_EXPORT void org_jetbrains_skia_Path__1nReset(KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->reset();
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nRewind(KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->rewind();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nIsEmpty(KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return instance->isEmpty();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nIsLastContourClosed(KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return instance->isLastContourClosed();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nIsFinite(KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return instance->isFinite();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nIsVolatile(KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return instance->isVolatile();
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nSetVolatile(KInteropPointer __Kinstance, KNativePointer ptr, KBoolean isVolatile) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->setIsVolatile(isVolatile);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nIsLineDegenerate(KInteropPointer __Kinstance, KFloat x0, KFloat y0, KFloat x1, KFloat y1, KBoolean exact) {
    return SkPath::IsLineDegenerate({x0, y0}, {x1, y1}, exact);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nIsQuadDegenerate(KInteropPointer __Kinstance, KFloat x0, KFloat y0, KFloat x1, KFloat y1, KFloat x2, KFloat y2, KBoolean exact) {
    return SkPath::IsQuadDegenerate({x0, y0}, {x1, y1}, {x2, y2}, exact);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nIsCubicDegenerate(KInteropPointer __Kinstance, KFloat x0, KFloat y0, KFloat x1, KFloat y1, KFloat x2, KFloat y2, KFloat x3, KFloat y3, KBoolean exact) {
    return SkPath::IsCubicDegenerate({x0, y0}, {x1, y1}, {x2, y2}, {x3, y3}, exact);
}


SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_Path__1nMaybeGetAsLine(KInteropPointer __Kinstance, KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Path__1nMaybeGetAsLine(KInteropPointer");
}
     
#if 0 
SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_Path__1nMaybeGetAsLine(KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPoint line[2];
    if (instance->isLine(line)) {
        KInteropPointerArray res = env->NewObjectArray(2, skija::Point::cls, nullptr);
        env->SetObjectArrayElement(res, 0, skija::Point::fromSkPoint(env, line[0]));
        env->SetObjectArrayElement(res, 1, skija::Point::fromSkPoint(env, line[1]));
        return res;
    } else
        return nullptr;
}
#endif


SKIKO_EXPORT KInt org_jetbrains_skia_Path__1nGetPointsCount(KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return instance->countPoints();
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Path__1nGetPoint(KInteropPointer __Kinstance, KNativePointer ptr, KInt index) {
    TODO("implement org_jetbrains_skia_Path__1nGetPoint(KInteropPointer");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Path__1nGetPoint(KInteropPointer __Kinstance, KNativePointer ptr, KInt index) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPoint p = instance->getPoint(index);
    return skija::Point::fromSkPoint(env, p);
}
#endif



SKIKO_EXPORT KInt org_jetbrains_skia_Path__1nGetPoints(KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointerArray pointsArray, KInt max) {
    TODO("implement org_jetbrains_skia_Path__1nGetPoints(KInteropPointer");
}
     
#if 0 
SKIKO_EXPORT KInt org_jetbrains_skia_Path__1nGetPoints(KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointerArray pointsArray, KInt max) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    std::vector<SkPoint> p(std::min<KInt>(max, instance->countPoints()));
    int count = instance->getPoints(p.data(), max);
    for (int i = 0; i < max && i < count; ++ i)
        env->SetObjectArrayElement(pointsArray, i, skija::Point::fromSkPoint(env, p[i]));
    return count;
}
#endif


SKIKO_EXPORT KInt org_jetbrains_skia_Path__1nCountVerbs(KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return instance->countVerbs();
}


SKIKO_EXPORT KInt org_jetbrains_skia_Path__1nGetVerbs(KInteropPointer __Kinstance, KNativePointer ptr, jbyteArray verbsArray, KInt max) {
    TODO("implement org_jetbrains_skia_Path__1nGetVerbs(KInteropPointer");
}
     
#if 0 
SKIKO_EXPORT KInt org_jetbrains_skia_Path__1nGetVerbs(KInteropPointer __Kinstance, KNativePointer ptr, jbyteArray verbsArray, KInt max) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    jbyte* verbs = verbsArray == nullptr ? nullptr : env->GetByteArrayElements(verbsArray, 0);
    int count = instance->getVerbs(reinterpret_cast<uint8_t *>(verbs), max);
    if (verbsArray != nullptr)
        env->ReleaseByteArrayElements(verbsArray, verbs, 0);
    return count;
}
#endif


SKIKO_EXPORT KInt org_jetbrains_skia_Path__1nApproximateBytesUsed(KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return (KInt) instance->approximateBytesUsed();
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nSwap(KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer otherPtr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPath* other = reinterpret_cast<SkPath*>((otherPtr));
    instance->swap(*other);
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Path__1nGetBounds(KInteropPointer __Kinstance, KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Path__1nGetBounds(KInteropPointer");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Path__1nGetBounds(KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return skija::Rect::fromSkRect(env, instance->getBounds());
}
#endif


SKIKO_EXPORT void org_jetbrains_skia_Path__1nUpdateBoundsCache(KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->updateBoundsCache();
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Path__1nComputeTightBounds(KInteropPointer __Kinstance, KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Path__1nComputeTightBounds(KInteropPointer");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Path__1nComputeTightBounds(KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return skija::Rect::fromSkRect(env, instance->computeTightBounds());
}
#endif


SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nConservativelyContainsRect(KInteropPointer __Kinstance, KNativePointer ptr, float l, float t, float r, float b) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkRect rect {l, t, r, b};
    return instance->conservativelyContainsRect(rect);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nIncReserve(KInteropPointer __Kinstance, KNativePointer ptr, int extraPtCount) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->incReserve(extraPtCount);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nMoveTo(KInteropPointer __Kinstance, KNativePointer ptr, KFloat x, KFloat y) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->moveTo(x, y);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nRMoveTo(KInteropPointer __Kinstance, KNativePointer ptr, KFloat dx, KFloat dy) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->rMoveTo(dx, dy);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nLineTo(KInteropPointer __Kinstance, KNativePointer ptr, KFloat x, KFloat y) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->lineTo(x, y);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nRLineTo(KInteropPointer __Kinstance, KNativePointer ptr, KFloat dx, KFloat dy) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->rLineTo(dx, dy);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nQuadTo(KInteropPointer __Kinstance, KNativePointer ptr, KFloat x1, KFloat y1, KFloat x2, KFloat y2) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->quadTo(x1, y1, x2, y2);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nRQuadTo(KInteropPointer __Kinstance, KNativePointer ptr, KFloat dx1, KFloat dy1, KFloat dx2, KFloat dy2) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->rQuadTo(dx1, dy1, dx2, dy2);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nConicTo(KInteropPointer __Kinstance, KNativePointer ptr, KFloat x1, KFloat y1, KFloat x2, KFloat y2, KFloat w) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->conicTo(x1, y1, x2, y2, w);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nRConicTo(KInteropPointer __Kinstance, KNativePointer ptr, KFloat dx1, KFloat dy1, KFloat dx2, KFloat dy2, KFloat w) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->rConicTo(dx1, dy1, dx2, dy2, w);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nCubicTo(KInteropPointer __Kinstance, KNativePointer ptr, KFloat x1, KFloat y1, KFloat x2, KFloat y2, KFloat x3, KFloat y3) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->cubicTo(x1, y1, x2, y2, x3, y3);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nRCubicTo(KInteropPointer __Kinstance, KNativePointer ptr, KFloat dx1, KFloat dy1, KFloat dx2, KFloat dy2, KFloat dx3, KFloat dy3) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->rCubicTo(dx1, dy1, dx2, dy2, dx3, dy3);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nArcTo(KInteropPointer __Kinstance, KNativePointer ptr, KFloat left, KFloat top, KFloat right, KFloat bottom, KFloat startAngle, KFloat sweepAngle, KBoolean forceMoveTo) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->arcTo({left, top, right, bottom}, startAngle, sweepAngle, forceMoveTo);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nTangentArcTo(KInteropPointer __Kinstance, KNativePointer ptr, KFloat x1, KFloat y1, KFloat x2, KFloat y2, KFloat radius) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->arcTo(x1, y1, x2, y2, radius);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nEllipticalArcTo(KInteropPointer __Kinstance, KNativePointer ptr, KFloat rx, KFloat ry, KFloat xAxisRotate, KInt size, KInt direction, KFloat x, float y) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->arcTo(rx, ry, xAxisRotate, static_cast<SkPath::ArcSize>(size), static_cast<SkPathDirection>(direction), x, y);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nREllipticalArcTo(KInteropPointer __Kinstance, KNativePointer ptr, KFloat rx, KFloat ry, KFloat xAxisRotate, KInt size, KInt direction, KFloat dx, float dy) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->rArcTo(rx, ry, xAxisRotate, static_cast<SkPath::ArcSize>(size), static_cast<SkPathDirection>(direction), dx, dy);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nClosePath(KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->close();
}


SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_Path__1nConvertConicToQuads
  (KInteropPointer __Kinstance, KFloat x0, KFloat y0, KFloat x1, KFloat y1, KFloat x2, KFloat y2, KFloat w, KInt pow2) {
    TODO("implement org_jetbrains_skia_Path__1nConvertConicToQuads");
}
     
#if 0 
SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_Path__1nConvertConicToQuads
  (KInteropPointer __Kinstance, KFloat x0, KFloat y0, KFloat x1, KFloat y1, KFloat x2, KFloat y2, KFloat w, KInt pow2) {
    std::vector<SkPoint> pts(1 + 2 * (1 << pow2));
    int count = SkPath::ConvertConicToQuads({x0, y0}, {x1, y1}, {x2, y2}, w, pts.data(), pow2);
    KInteropPointerArray res = env->NewObjectArray(count, skija::Point::cls, nullptr);
    for (int i = 0; i < count; ++i) {
        env->SetObjectArrayElement(res, i, skija::Point::fromSkPoint(env, pts[i]));
    }
    return res;
}
#endif



SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Path__1nIsRect
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Path__1nIsRect");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Path__1nIsRect
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkRect rect;
    if (instance->isRect(&rect))
        return skija::Rect::fromSkRect(env, rect);
    else
        return nullptr;
}
#endif


SKIKO_EXPORT void org_jetbrains_skia_Path__1nAddRect
  (KInteropPointer __Kinstance, KNativePointer ptr, KFloat l, KFloat t, KFloat r, KFloat b, KInt dirInt, KInt start) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPathDirection dir = static_cast<SkPathDirection>(dirInt);
    instance->addRect({l, t, r, b}, dir, start);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nAddOval
  (KInteropPointer __Kinstance, KNativePointer ptr, KFloat l, KFloat t, KFloat r, KFloat b, KInt dirInt, KInt start) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPathDirection dir = static_cast<SkPathDirection>(dirInt);
    instance->addOval({l, t, r, b}, dir, start);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nAddCircle
  (KInteropPointer __Kinstance, KNativePointer ptr, KFloat x, KFloat y, KFloat r, KInt dirInt) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPathDirection dir = static_cast<SkPathDirection>(dirInt);
    instance->addCircle(x, y, r, dir);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nAddArc
  (KInteropPointer __Kinstance, KNativePointer ptr, KFloat l, KFloat t, KFloat r, KFloat b, KFloat startAngle, KFloat sweepAngle) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->addArc({l, t, r, b}, startAngle, sweepAngle);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nAddRRect
  (KInteropPointer __Kinstance, KNativePointer ptr, KFloat l, KFloat t, KFloat r, KFloat b, KFloat* radii, KInt radiiSize, KInt dirInt, KInt start) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkRRect rrect = skija::RRect::toSkRRect(l, t, r, b, radii, radiiSize);
    SkPathDirection dir = static_cast<SkPathDirection>(dirInt);
    instance->addRRect(rrect, dir, start);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nAddPoly
  (KInteropPointer __Kinstance, KNativePointer ptr, KFloat* coords, KBoolean close) {
    TODO("implement org_jetbrains_skia_Path__1nAddPoly");
}
     
#if 0 
SKIKO_EXPORT void org_jetbrains_skia_Path__1nAddPoly
  (KInteropPointer __Kinstance, KNativePointer ptr, KFloat* coords, KBoolean close) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    jsize len = env->GetArrayLength(coords);
    KFloat* arr = env->GetFloatArrayElements(coords, 0);
    instance->addPoly(reinterpret_cast<SkPoint*>(arr), len / 2, close);
    env->ReleaseFloatArrayElements(coords, arr, 0);
}
#endif


SKIKO_EXPORT void org_jetbrains_skia_Path__1nAddPath
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer srcPtr, KBoolean extend) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPath* src = reinterpret_cast<SkPath*>((srcPtr));
    SkPath::AddPathMode mode = extend ? SkPath::AddPathMode::kExtend_AddPathMode : SkPath::AddPathMode::kAppend_AddPathMode;
    instance->addPath(*src, mode);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nAddPathOffset
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer srcPtr, KFloat dx, KFloat dy, KBoolean extend) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPath* src = reinterpret_cast<SkPath*>((srcPtr));
    SkPath::AddPathMode mode = extend ? SkPath::AddPathMode::kExtend_AddPathMode : SkPath::AddPathMode::kAppend_AddPathMode;
    instance->addPath(*src, dx, dy, mode);
}


SKIKO_EXPORT void org_jetbrains_skia_Path__1nAddPathTransform
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer srcPtr, KFloat* matrixArr, KBoolean extend) {
    TODO("implement org_jetbrains_skia_Path__1nAddPathTransform");
}
     
#if 0 
SKIKO_EXPORT void org_jetbrains_skia_Path__1nAddPathTransform
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer srcPtr, KFloat* matrixArr, KBoolean extend) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPath* src = reinterpret_cast<SkPath*>((srcPtr));
    std::unique_ptr<SkMatrix> matrix = skMatrix(env, matrixArr);
    SkPath::AddPathMode mode = extend ? SkPath::AddPathMode::kExtend_AddPathMode : SkPath::AddPathMode::kAppend_AddPathMode;
    instance->addPath(*src, *matrix, mode);
}
#endif


SKIKO_EXPORT void org_jetbrains_skia_Path__1nReverseAddPath
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer srcPtr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPath* src = reinterpret_cast<SkPath*>((srcPtr));
    instance->reverseAddPath(*src);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nOffset
  (KInteropPointer __Kinstance, KNativePointer ptr, KFloat dx, KFloat dy, KNativePointer dstPtr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPath* dst = reinterpret_cast<SkPath*>((dstPtr));
    instance->offset(dx, dy, dst);
}


SKIKO_EXPORT void org_jetbrains_skia_Path__1nTransform
  (KInteropPointer __Kinstance, KNativePointer ptr, KFloat* matrixArr, KNativePointer dstPtr, KBoolean pcBool) {
    TODO("implement org_jetbrains_skia_Path__1nTransform");
}
     
#if 0 
SKIKO_EXPORT void org_jetbrains_skia_Path__1nTransform
  (KInteropPointer __Kinstance, KNativePointer ptr, KFloat* matrixArr, KNativePointer dstPtr, KBoolean pcBool) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPath* dst = reinterpret_cast<SkPath*>((dstPtr));
    std::unique_ptr<SkMatrix> matrix = skMatrix(env, matrixArr);
    SkApplyPerspectiveClip pc = pcBool ? SkApplyPerspectiveClip::kYes : SkApplyPerspectiveClip::kNo;
    instance->transform(*matrix, dst, pc);
}
#endif



SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Path__1nGetLastPt
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Path__1nGetLastPt");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Path__1nGetLastPt
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPoint out;
    if (instance->getLastPt(&out))
        return skija::Point::fromSkPoint(env, out);
    else
        return nullptr;
}
#endif


SKIKO_EXPORT void org_jetbrains_skia_Path__1nSetLastPt
  (KInteropPointer __Kinstance, KNativePointer ptr, KFloat x, KFloat y) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->setLastPt(x, y);
}

SKIKO_EXPORT KInt org_jetbrains_skia_Path__1nGetSegmentMasks
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return instance->getSegmentMasks();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nContains
  (KInteropPointer __Kinstance, KNativePointer ptr, KFloat x, KFloat y) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return instance->contains(x, y);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nDump
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->dump();
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nDumpHex
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->dumpHex();
}


SKIKO_EXPORT jbyteArray org_jetbrains_skia_Path__1nSerializeToBytes
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Path__1nSerializeToBytes");
}
     
#if 0 
SKIKO_EXPORT jbyteArray org_jetbrains_skia_Path__1nSerializeToBytes
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    size_t count = instance->writeToMemory(nullptr);
    jbyteArray bytesArray = env->NewByteArray((jsize) count);
    jbyte* bytes = env->GetByteArrayElements(bytesArray, 0);
    instance->writeToMemory(bytes);
    env->ReleaseByteArrayElements(bytesArray, bytes, 0);
    return bytesArray;
}
#endif


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Path__1nMakeCombining
  (KInteropPointer __Kinstance, KNativePointer aPtr, KNativePointer bPtr, KInt jop) {
    SkPath* a = reinterpret_cast<SkPath*>((aPtr));
    SkPath* b = reinterpret_cast<SkPath*>((bPtr));
    SkPathOp op = static_cast<SkPathOp>(jop);
    auto res = std::make_unique<SkPath>();
    if (Op(*a, *b, op, res.get()))
        return reinterpret_cast<KNativePointer>(res.release());
    else
        return 0;
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Path__1nMakeFromBytes
  (KInteropPointer __Kinstance, jbyteArray bytesArray) {
    TODO("implement org_jetbrains_skia_Path__1nMakeFromBytes");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Path__1nMakeFromBytes
  (KInteropPointer __Kinstance, jbyteArray bytesArray) {
    SkPath* instance = new SkPath();
    int count = env->GetArrayLength(bytesArray);
    jbyte* bytes = env->GetByteArrayElements(bytesArray, 0);
    if (instance->readFromMemory(bytes, count)) {
        env->ReleaseByteArrayElements(bytesArray, bytes, 0);
        return reinterpret_cast<KNativePointer>(instance);
    } else {
        env->ReleaseByteArrayElements(bytesArray, bytes, 0);
        delete instance;
        return 0;
    }
}
#endif


SKIKO_EXPORT KInt org_jetbrains_skia_Path__1nGetGenerationId
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return instance->getGenerationID();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nIsValid
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return instance->isValid();
}
