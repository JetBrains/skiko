
// This file has been auto generated.

#include "SkPathMeasure.h"
#include "common.h"

static void deletePathMeasure(SkPathMeasure* instance) {
    delete instance;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathMeasure__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>((&deletePathMeasure));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathMeasure__1nMake
  () {
    return reinterpret_cast<KNativePointer>(new SkPathMeasure());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathMeasure__1nMakePath
  (KNativePointer pathPtr, KBoolean forceClosed, KFloat resScale) {
    SkPath* path = reinterpret_cast<SkPath*>((pathPtr));
    return reinterpret_cast<KNativePointer>(new SkPathMeasure(*path, forceClosed, resScale));
}

SKIKO_EXPORT void org_jetbrains_skia_PathMeasure__1nSetPath
  (KNativePointer ptr, KNativePointer pathPtr, KBoolean forceClosed) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>((ptr));
    SkPath* path = reinterpret_cast<SkPath*>((pathPtr));
    instance->setPath(path, forceClosed);
}

SKIKO_EXPORT KFloat org_jetbrains_skia_PathMeasure__1nGetLength
  (KNativePointer ptr) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>((ptr));
    return instance->getLength();
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_PathMeasure__1nGetPosition
  (KNativePointer ptr, KFloat distance) {
    TODO("implement org_jetbrains_skia_PathMeasure__1nGetPosition");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_PathMeasure__1nGetPosition
  (KNativePointer ptr, KFloat distance) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>((ptr));
    SkPoint position;
    if (instance->getPosTan(distance, &position, nullptr))
        return skija::Point::fromSkPoint(env, position);
    else
        return nullptr;
}
#endif



SKIKO_EXPORT KInteropPointer org_jetbrains_skia_PathMeasure__1nGetTangent
  (KNativePointer ptr, KFloat distance) {
    TODO("implement org_jetbrains_skia_PathMeasure__1nGetTangent");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_PathMeasure__1nGetTangent
  (KNativePointer ptr, KFloat distance) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>((ptr));
    SkVector tangent;
    if (instance->getPosTan(distance, nullptr, &tangent))
        return skija::Point::fromSkPoint(env, tangent);
    else
        return nullptr;
}
#endif


SKIKO_EXPORT KBoolean org_jetbrains_skia_PathMeasure__1nGetRSXform
  (KNativePointer ptr, KFloat distance, KInt* data) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>((ptr));
    SkPoint position;
    SkVector tangent;
    if (instance->getPosTan(distance, &position, &tangent)) {
        data[0] = rawBits(tangent.fX);
        data[1] = rawBits(tangent.fY);
        data[2] = rawBits(position.fX);
        data[3] = rawBits(position.fY);
        return true;
    }

    return false;
}
     
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_PathMeasure__1nGetMatrix
  (KNativePointer ptr, KFloat distance, KBoolean getPosition, KBoolean getTangent) {
    TODO("implement org_jetbrains_skia_PathMeasure__1nGetMatrix");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_PathMeasure__1nGetMatrix
  (KNativePointer ptr, KFloat distance, KBoolean getPosition, KBoolean getTangent) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>((ptr));
    SkMatrix matrix;
    int flags = 0;
    
    if (getPosition)
        flags |= SkPathMeasure::MatrixFlags::kGetPosition_MatrixFlag;
    if (getTangent)
        flags |= SkPathMeasure::MatrixFlags::kGetTangent_MatrixFlag;

    if (instance->getMatrix(distance, &matrix, static_cast<SkPathMeasure::MatrixFlags>(flags))) {
        std::vector<float> floats(9);
        matrix.get9(floats.data());
        return javaFloatArray(env, floats);
    } else
        return nullptr;
}
#endif


SKIKO_EXPORT KBoolean org_jetbrains_skia_PathMeasure__1nGetSegment
  (KNativePointer ptr, KFloat startD, KFloat endD, KNativePointer dstPtr, KBoolean startWithMoveTo) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>((ptr));
    SkPath* dst = reinterpret_cast<SkPath*>((dstPtr));
    return instance->getSegment(startD, endD, dst, startWithMoveTo);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_PathMeasure__1nIsClosed
  (KNativePointer ptr) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>((ptr));
    return instance->isClosed();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_PathMeasure__1nNextContour
  (KNativePointer ptr) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>((ptr));
    return instance->nextContour();
}
