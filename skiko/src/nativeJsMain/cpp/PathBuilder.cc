#include "SkPathBuilder.h"
#include "SkPath.h"
#include "SkRRect.h"
#include "SkMatrix.h"
#include "common.h"

static void deletePathBuilder(SkPathBuilder* builder) {
    delete builder;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathBuilder__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>((&deletePathBuilder));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathBuilder__1nMake() {
    SkPathBuilder* builder = new SkPathBuilder();
    return reinterpret_cast<KNativePointer>(builder);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathBuilder__1nMakeWithFillType(KInt fillType) {
    SkPathBuilder* builder = new SkPathBuilder(static_cast<SkPathFillType>(fillType));
    return reinterpret_cast<KNativePointer>(builder);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathBuilder__1nMakeFromPath(KNativePointer pathPtr) {
    SkPath* path = reinterpret_cast<SkPath*>((pathPtr));
    SkPathBuilder* builder = new SkPathBuilder(*path);
    return reinterpret_cast<KNativePointer>(builder);
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nReset(KNativePointer ptr) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    builder->reset();
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nSetFillType(KNativePointer ptr, KInt fillType) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    builder->setFillType(static_cast<SkPathFillType>(fillType));
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nIncReserve(KNativePointer ptr, KInt extraPtCount) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    builder->incReserve(extraPtCount);
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nMoveTo(KNativePointer ptr, KFloat x, KFloat y) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    builder->moveTo(x, y);
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nRMoveTo(KNativePointer ptr, KFloat dx, KFloat dy) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    builder->rMoveTo(dx, dy);
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nLineTo(KNativePointer ptr, KFloat x, KFloat y) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    builder->lineTo(x, y);
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nRLineTo(KNativePointer ptr, KFloat dx, KFloat dy) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    builder->rLineTo(dx, dy);
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nQuadTo(KNativePointer ptr, KFloat x1, KFloat y1, KFloat x2, KFloat y2) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    builder->quadTo(x1, y1, x2, y2);
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nRQuadTo(KNativePointer ptr, KFloat dx1, KFloat dy1, KFloat dx2, KFloat dy2) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    builder->rQuadTo(dx1, dy1, dx2, dy2);
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nConicTo(KNativePointer ptr, KFloat x1, KFloat y1, KFloat x2, KFloat y2, KFloat w) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    builder->conicTo(x1, y1, x2, y2, w);
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nRConicTo(KNativePointer ptr, KFloat dx1, KFloat dy1, KFloat dx2, KFloat dy2, KFloat w) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    builder->rConicTo(dx1, dy1, dx2, dy2, w);
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nCubicTo(KNativePointer ptr, KFloat x1, KFloat y1, KFloat x2, KFloat y2, KFloat x3, KFloat y3) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    builder->cubicTo(x1, y1, x2, y2, x3, y3);
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nRCubicTo(KNativePointer ptr, KFloat dx1, KFloat dy1, KFloat dx2, KFloat dy2, KFloat dx3, KFloat dy3) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    builder->rCubicTo(dx1, dy1, dx2, dy2, dx3, dy3);
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nArcTo(KNativePointer ptr, KFloat left, KFloat top, KFloat right, KFloat bottom, KFloat startAngle, KFloat sweepAngle, KBoolean forceMoveTo) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    builder->arcTo({left, top, right, bottom}, startAngle, sweepAngle, forceMoveTo);
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nTangentArcTo(KNativePointer ptr, KFloat x1, KFloat y1, KFloat x2, KFloat y2, KFloat radius) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    builder->arcTo({x1, y1}, {x2, y2}, radius);
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nEllipticalArcTo(KNativePointer ptr, KFloat rx, KFloat ry, KFloat xAxisRotate, KInt size, KInt direction, KFloat x, KFloat y) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    builder->arcTo({rx, ry}, xAxisRotate, static_cast<SkPathBuilder::ArcSize>(size), static_cast<SkPathDirection>(direction), {x, y});
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nREllipticalArcTo(KNativePointer ptr, KFloat rx, KFloat ry, KFloat xAxisRotate, KInt size, KInt direction, KFloat dx, KFloat dy) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    builder->rArcTo({rx, ry}, xAxisRotate, static_cast<SkPathBuilder::ArcSize>(size), static_cast<SkPathDirection>(direction), {dx, dy});
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nClosePath(KNativePointer ptr) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    builder->close();
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nAddRect(KNativePointer ptr, KFloat l, KFloat t, KFloat r, KFloat b, KInt dirInt, KInt start) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    SkPathDirection dir = static_cast<SkPathDirection>(dirInt);
    builder->addRect({l, t, r, b}, dir, start);
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nAddOval(KNativePointer ptr, KFloat l, KFloat t, KFloat r, KFloat b, KInt dirInt, KInt start) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    SkPathDirection dir = static_cast<SkPathDirection>(dirInt);
    builder->addOval({l, t, r, b}, dir, start);
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nAddCircle(KNativePointer ptr, KFloat x, KFloat y, KFloat r, KInt dirInt) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    SkPathDirection dir = static_cast<SkPathDirection>(dirInt);
    builder->addCircle(x, y, r, dir);
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nAddArc(KNativePointer ptr, KFloat l, KFloat t, KFloat r, KFloat b, KFloat startAngle, KFloat sweepAngle) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    builder->addArc({l, t, r, b}, startAngle, sweepAngle);
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nAddRRect(KNativePointer ptr, KFloat l, KFloat t, KFloat r, KFloat b, KInteropPointer radii, KInt radiiSize, KInt dirInt, KInt start) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    SkRRect rrect = skija::RRect::toSkRRect(l, t, r, b, reinterpret_cast<KFloat*>(radii), radiiSize);
    SkPathDirection dir = static_cast<SkPathDirection>(dirInt);
    builder->addRRect(rrect, dir, start);
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nAddPoly(KNativePointer ptr, KInteropPointer coords, KInt _count, KBoolean close) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    float* arr = reinterpret_cast<float*>(coords);
    builder->addPolygon({reinterpret_cast<SkPoint*>(arr), _count}, close);
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nAddPath(KNativePointer ptr, KNativePointer srcPtr, KInt mode) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    SkPath* src = reinterpret_cast<SkPath*>((srcPtr));
    builder->addPath(*src, static_cast<SkPath::AddPathMode>(mode));
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nAddPathOffset(KNativePointer ptr, KNativePointer srcPtr, KFloat dx, KFloat dy, KInt mode) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    SkPath* src = reinterpret_cast<SkPath*>((srcPtr));
    builder->addPath(*src, dx, dy, static_cast<SkPath::AddPathMode>(mode));
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nAddPathTransform(KNativePointer ptr, KNativePointer srcPtr, KInteropPointer matrixArr, KInt mode) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    SkPath* src = reinterpret_cast<SkPath*>((srcPtr));
    std::unique_ptr<SkMatrix> matrix = skMatrix(reinterpret_cast<KFloat*>(matrixArr));
    builder->addPath(*src, *matrix, static_cast<SkPath::AddPathMode>(mode));
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nSetLastPt(KNativePointer ptr, KFloat x, KFloat y) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    builder->setLastPt(x, y);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathBuilder__1nDetach(KNativePointer ptr) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    SkPath* path = new SkPath(builder->detach());
    return reinterpret_cast<KNativePointer>(path);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathBuilder__1nSnapshot(KNativePointer ptr) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    SkPath* path = new SkPath(builder->snapshot());
    return reinterpret_cast<KNativePointer>(path);
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nOffset(KNativePointer ptr, KFloat dx, KFloat dy) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    builder->offset(dx, dy);
}

SKIKO_EXPORT void org_jetbrains_skia_PathBuilder__1nTransform(KNativePointer ptr, KInteropPointer matrixArr) {
    SkPathBuilder* builder = reinterpret_cast<SkPathBuilder*>((ptr));
    std::unique_ptr<SkMatrix> matrix = skMatrix(reinterpret_cast<KFloat*>(matrixArr));
    builder->transform(*matrix);
}
