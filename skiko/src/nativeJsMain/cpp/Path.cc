
// This file has been auto generated.

#include <algorithm>
#include <cfloat>
#include <iostream>
#include <vector>
#include <limits>
#include "SkPath.h"
#include "SkPathOps.h"
#include "include/utils/SkParsePath.h"
#include "common.h"

static void deletePath(SkPath* path) {
    // std::cout << "Deleting [SkPath " << path << "]" << std::endl;
    delete path;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Path__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>((&deletePath));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Path__1nMake() {
    SkPath* obj = new SkPath();
    return reinterpret_cast<KNativePointer>(obj);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Path__1nMakeFromSVGString
  (KInteropPointer d) {
    SkPath* obj = new SkPath();
    SkString s = skString(d);
    if (SkParsePath::FromSVGString(s.c_str(), obj))
        return reinterpret_cast<KNativePointer>(obj);
    else {
        delete obj;
        return 0;
    }
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nEquals(KNativePointer aPtr, KNativePointer bPtr) {
    SkPath* a = reinterpret_cast<SkPath*>((aPtr));
    SkPath* b = reinterpret_cast<SkPath*>((bPtr));
    return *a == *b;
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nIsInterpolatable(KNativePointer ptr, KNativePointer comparePtr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPath* compare = reinterpret_cast<SkPath*>((comparePtr));
    return instance->isInterpolatable(*compare);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Path__1nMakeLerp(KNativePointer ptr, KNativePointer endingPtr, KFloat weight) {
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

SKIKO_EXPORT KInt org_jetbrains_skia_Path__1nGetFillMode(KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return static_cast<KInt>(instance->getFillType());
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nSetFillMode(KNativePointer ptr, KInt fillMode) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->setFillType(static_cast<SkPathFillType>(fillMode));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nIsConvex(KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return instance->isConvex();
}


SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nIsOval(KNativePointer ptr, KInteropPointer resultArray) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkRect bounds;
    if (instance->isOval(&bounds)) {
        skija::Rect::copyToInterop(bounds, resultArray);
        return true;
    } else {
        return false;
    }
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nIsRRect(KNativePointer ptr, KInteropPointer resultArray) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkRRect rrect;
    if (instance->isRRect(&rrect)) {
        skija::RRect::copyToInterop(rrect, resultArray);
        return true;
    } else {
        return false;
    }
}


SKIKO_EXPORT void org_jetbrains_skia_Path__1nReset(KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->reset();
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nRewind(KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->rewind();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nIsEmpty(KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return instance->isEmpty();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nIsLastContourClosed(KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return instance->isLastContourClosed();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nIsFinite(KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return instance->isFinite();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nIsVolatile(KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return instance->isVolatile();
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nSetVolatile(KNativePointer ptr, KBoolean isVolatile) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->setIsVolatile(isVolatile);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nIsLineDegenerate(KFloat x0, KFloat y0, KFloat x1, KFloat y1, KBoolean exact) {
    return SkPath::IsLineDegenerate({x0, y0}, {x1, y1}, exact);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nIsQuadDegenerate(KFloat x0, KFloat y0, KFloat x1, KFloat y1, KFloat x2, KFloat y2, KBoolean exact) {
    return SkPath::IsQuadDegenerate({x0, y0}, {x1, y1}, {x2, y2}, exact);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nIsCubicDegenerate(KFloat x0, KFloat y0, KFloat x1, KFloat y1, KFloat x2, KFloat y2, KFloat x3, KFloat y3, KBoolean exact) {
    return SkPath::IsCubicDegenerate({x0, y0}, {x1, y1}, {x2, y2}, {x3, y3}, exact);
}


SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nMaybeGetAsLine(KNativePointer ptr, KInteropPointer resultArray) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPoint line[2];
    if (instance->isLine(line)) {
        SkRect rect = SkRect::MakeLTRB(line[0].x(), line[0].y(), line[1].x(), line[1].y());
        skija::Rect::copyToInterop(rect, resultArray);
        return true;
    } else {
        return false;
    }
}


SKIKO_EXPORT KInt org_jetbrains_skia_Path__1nGetPointsCount(KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return instance->countPoints();
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nGetPoint(KNativePointer ptr, KInt index, KInteropPointer resultArray) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    skija::Point::copyToInterop(instance->getPoint(index), resultArray);
}

SKIKO_EXPORT KInt org_jetbrains_skia_Path__1nGetPoints(KNativePointer ptr, KInteropPointer pointsArray, KInt max) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPoint* points = reinterpret_cast<SkPoint*>(pointsArray);
    return instance->getPoints(points, max);
}

SKIKO_EXPORT KInt org_jetbrains_skia_Path__1nCountVerbs(KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return instance->countVerbs();
}

SKIKO_EXPORT KInt org_jetbrains_skia_Path__1nGetVerbs(KNativePointer ptr, KByte* verbsArray, KInt max) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return instance->getVerbs(reinterpret_cast<uint8_t *>(verbsArray), max);
}


SKIKO_EXPORT KInt org_jetbrains_skia_Path__1nApproximateBytesUsed(KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return (KInt) instance->approximateBytesUsed();
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nSwap(KNativePointer ptr, KNativePointer otherPtr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPath* other = reinterpret_cast<SkPath*>((otherPtr));
    instance->swap(*other);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nGetBounds(KNativePointer ptr, KInteropPointer resultArray) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkRect bounds = instance->getBounds();
    skija::Rect::copyToInterop(bounds, resultArray);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nUpdateBoundsCache(KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->updateBoundsCache();
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nComputeTightBounds(KNativePointer ptr, KInteropPointer resultArray) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkRect bounds = instance->computeTightBounds();
    skija::Rect::copyToInterop(bounds, resultArray);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nConservativelyContainsRect(KNativePointer ptr, float l, float t, float r, float b) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkRect rect {l, t, r, b};
    return instance->conservativelyContainsRect(rect);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nIncReserve(KNativePointer ptr, int extraPtCount) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->incReserve(extraPtCount);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nMoveTo(KNativePointer ptr, KFloat x, KFloat y) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->moveTo(x, y);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nRMoveTo(KNativePointer ptr, KFloat dx, KFloat dy) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->rMoveTo(dx, dy);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nLineTo(KNativePointer ptr, KFloat x, KFloat y) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->lineTo(x, y);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nRLineTo(KNativePointer ptr, KFloat dx, KFloat dy) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->rLineTo(dx, dy);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nQuadTo(KNativePointer ptr, KFloat x1, KFloat y1, KFloat x2, KFloat y2) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->quadTo(x1, y1, x2, y2);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nRQuadTo(KNativePointer ptr, KFloat dx1, KFloat dy1, KFloat dx2, KFloat dy2) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->rQuadTo(dx1, dy1, dx2, dy2);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nConicTo(KNativePointer ptr, KFloat x1, KFloat y1, KFloat x2, KFloat y2, KFloat w) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->conicTo(x1, y1, x2, y2, w);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nRConicTo(KNativePointer ptr, KFloat dx1, KFloat dy1, KFloat dx2, KFloat dy2, KFloat w) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->rConicTo(dx1, dy1, dx2, dy2, w);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nCubicTo(KNativePointer ptr, KFloat x1, KFloat y1, KFloat x2, KFloat y2, KFloat x3, KFloat y3) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->cubicTo(x1, y1, x2, y2, x3, y3);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nRCubicTo(KNativePointer ptr, KFloat dx1, KFloat dy1, KFloat dx2, KFloat dy2, KFloat dx3, KFloat dy3) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->rCubicTo(dx1, dy1, dx2, dy2, dx3, dy3);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nArcTo(KNativePointer ptr, KFloat left, KFloat top, KFloat right, KFloat bottom, KFloat startAngle, KFloat sweepAngle, KBoolean forceMoveTo) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->arcTo({left, top, right, bottom}, startAngle, sweepAngle, forceMoveTo);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nTangentArcTo(KNativePointer ptr, KFloat x1, KFloat y1, KFloat x2, KFloat y2, KFloat radius) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->arcTo(x1, y1, x2, y2, radius);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nEllipticalArcTo(KNativePointer ptr, KFloat rx, KFloat ry, KFloat xAxisRotate, KInt size, KInt direction, KFloat x, float y) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->arcTo(rx, ry, xAxisRotate, static_cast<SkPath::ArcSize>(size), static_cast<SkPathDirection>(direction), x, y);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nREllipticalArcTo(KNativePointer ptr, KFloat rx, KFloat ry, KFloat xAxisRotate, KInt size, KInt direction, KFloat dx, float dy) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->rArcTo(rx, ry, xAxisRotate, static_cast<SkPath::ArcSize>(size), static_cast<SkPathDirection>(direction), dx, dy);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nClosePath(KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->close();
}


SKIKO_EXPORT KInt org_jetbrains_skia_Path__1nConvertConicToQuads
  (KFloat x0, KFloat y0, KFloat x1, KFloat y1, KFloat x2, KFloat y2, KFloat w, KInt pow2, KInteropPointer resultArray) {
    SkPoint* result = reinterpret_cast<SkPoint*>(resultArray);
    return SkPath::ConvertConicToQuads({x0, y0}, {x1, y1}, {x2, y2}, w, result, pow2);
}


SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nIsRect
  (KNativePointer ptr, KInteropPointer resultArray) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkRect rect;
    if (instance->isRect(&rect)) {
        skija::Rect::copyToInterop(rect, resultArray);
        return true;
    } else {
        return false;
    }
}


SKIKO_EXPORT void org_jetbrains_skia_Path__1nAddRect
  (KNativePointer ptr, KFloat l, KFloat t, KFloat r, KFloat b, KInt dirInt, KInt start) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPathDirection dir = static_cast<SkPathDirection>(dirInt);
    instance->addRect({l, t, r, b}, dir, start);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nAddOval
  (KNativePointer ptr, KFloat l, KFloat t, KFloat r, KFloat b, KInt dirInt, KInt start) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPathDirection dir = static_cast<SkPathDirection>(dirInt);
    instance->addOval({l, t, r, b}, dir, start);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nAddCircle
  (KNativePointer ptr, KFloat x, KFloat y, KFloat r, KInt dirInt) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPathDirection dir = static_cast<SkPathDirection>(dirInt);
    instance->addCircle(x, y, r, dir);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nAddArc
  (KNativePointer ptr, KFloat l, KFloat t, KFloat r, KFloat b, KFloat startAngle, KFloat sweepAngle) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->addArc({l, t, r, b}, startAngle, sweepAngle);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nAddRRect
  (KNativePointer ptr, KFloat l, KFloat t, KFloat r, KFloat b, KFloat* radii, KInt radiiSize, KInt dirInt, KInt start) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkRRect rrect = skija::RRect::toSkRRect(l, t, r, b, radii, radiiSize);
    SkPathDirection dir = static_cast<SkPathDirection>(dirInt);
    instance->addRRect(rrect, dir, start);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nAddPoly
  (KNativePointer ptr, KFloat* coords, KInt count, KBoolean close) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->addPoly(reinterpret_cast<SkPoint*>(coords), count, close);
}


SKIKO_EXPORT void org_jetbrains_skia_Path__1nAddPath
  (KNativePointer ptr, KNativePointer srcPtr, KBoolean extend) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPath* src = reinterpret_cast<SkPath*>((srcPtr));
    SkPath::AddPathMode mode = extend ? SkPath::AddPathMode::kExtend_AddPathMode : SkPath::AddPathMode::kAppend_AddPathMode;
    instance->addPath(*src, mode);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nAddPathOffset
  (KNativePointer ptr, KNativePointer srcPtr, KFloat dx, KFloat dy, KBoolean extend) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPath* src = reinterpret_cast<SkPath*>((srcPtr));
    SkPath::AddPathMode mode = extend ? SkPath::AddPathMode::kExtend_AddPathMode : SkPath::AddPathMode::kAppend_AddPathMode;
    instance->addPath(*src, dx, dy, mode);
}


SKIKO_EXPORT void org_jetbrains_skia_Path__1nAddPathTransform
  (KNativePointer ptr, KNativePointer srcPtr, KFloat* matrixArr, KBoolean extend) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPath* src = reinterpret_cast<SkPath*>((srcPtr));
    std::unique_ptr<SkMatrix> matrix = skMatrix(matrixArr);
    SkPath::AddPathMode mode = extend ? SkPath::AddPathMode::kExtend_AddPathMode : SkPath::AddPathMode::kAppend_AddPathMode;
    instance->addPath(*src, *matrix, mode);
}


SKIKO_EXPORT void org_jetbrains_skia_Path__1nReverseAddPath
  (KNativePointer ptr, KNativePointer srcPtr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPath* src = reinterpret_cast<SkPath*>((srcPtr));
    instance->reverseAddPath(*src);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nOffset
  (KNativePointer ptr, KFloat dx, KFloat dy, KNativePointer dstPtr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPath* dst = reinterpret_cast<SkPath*>((dstPtr));
    instance->offset(dx, dy, dst);
}


SKIKO_EXPORT void org_jetbrains_skia_Path__1nTransform
  (KNativePointer ptr, KFloat* matrixArr, KNativePointer dstPtr, KBoolean pcBool) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPath* dst = reinterpret_cast<SkPath*>((dstPtr));
    std::unique_ptr<SkMatrix> matrix = skMatrix(matrixArr);
    SkApplyPerspectiveClip pc = pcBool ? SkApplyPerspectiveClip::kYes : SkApplyPerspectiveClip::kNo;
    instance->transform(*matrix, dst, pc);
}


SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nGetLastPt
  (KNativePointer ptr, KInteropPointer resultArray) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    SkPoint out;
    if (instance->getLastPt(&out)) {
        skija::Point::copyToInterop(out, resultArray);
        return true;
    } else {
        return false;
    }
}


SKIKO_EXPORT void org_jetbrains_skia_Path__1nSetLastPt
  (KNativePointer ptr, KFloat x, KFloat y) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->setLastPt(x, y);
}

SKIKO_EXPORT KInt org_jetbrains_skia_Path__1nGetSegmentMasks
  (KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return instance->getSegmentMasks();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nContains
  (KNativePointer ptr, KFloat x, KFloat y) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return instance->contains(x, y);
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nDump
  (KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->dump();
}

SKIKO_EXPORT void org_jetbrains_skia_Path__1nDumpHex
  (KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    instance->dumpHex();
}


SKIKO_EXPORT KInt org_jetbrains_skia_Path__1nSerializeToBytes
  (KNativePointer ptr, KInteropPointer dst) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    size_t count = instance->writeToMemory(reinterpret_cast<void*>(dst));
    if (count > std::numeric_limits<KInt>::max()) {
        return -1;
    } else {
        return count;
    }
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Path__1nMakeCombining
  (KNativePointer aPtr, KNativePointer bPtr, KInt jop) {
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
  (KByte* bytesArray, KInt size) {
    SkPath* instance = new SkPath();
    void* bytes = reinterpret_cast<void*>(bytesArray);
    if (instance->readFromMemory(bytes, size)) {
        return reinterpret_cast<KNativePointer>(instance);
    } else {
        delete instance;
        return 0;
    }
}


SKIKO_EXPORT KInt org_jetbrains_skia_Path__1nGetGenerationId
  (KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return instance->getGenerationID();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Path__1nIsValid
  (KNativePointer ptr) {
    SkPath* instance = reinterpret_cast<SkPath*>((ptr));
    return instance->isValid();
}
