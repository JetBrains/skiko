
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


SKIKO_EXPORT KBoolean org_jetbrains_skia_PathMeasure__1nGetPosition
  (KNativePointer ptr, KFloat distance, KInt* data) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>((ptr));
    SkPoint position;
    if (instance->getPosTan(distance, &position, nullptr)) {
        data[0] = rawBits(position.fX);
        data[1] = rawBits(position.fY);
        return true;
    }

    return false;
}
     
SKIKO_EXPORT KBoolean org_jetbrains_skia_PathMeasure__1nGetTangent
  (KNativePointer ptr, KFloat distance, KInt* data) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>((ptr));
    SkVector tangent;

    if (instance->getPosTan(distance, nullptr, &tangent)) {
        data[0] = rawBits(tangent.fX);
        data[1] = rawBits(tangent.fY);
        return true;
    }

    return false;
}
     
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
     
SKIKO_EXPORT KBoolean org_jetbrains_skia_PathMeasure__1nGetMatrix
  (KNativePointer ptr, KFloat distance, KBoolean getPosition, KBoolean getTangent, KInt* data) {
  SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>((ptr));
  SkMatrix matrix;
  int flags = 0;

  if (getPosition)
      flags |= SkPathMeasure::MatrixFlags::kGetPosition_MatrixFlag;
  if (getTangent)
      flags |= SkPathMeasure::MatrixFlags::kGetTangent_MatrixFlag;

  if (instance->getMatrix(distance, &matrix, static_cast<SkPathMeasure::MatrixFlags>(flags))) {
      float* f;
      matrix.get9(f);

      data[0] = rawBits(data[0]);
      data[1] = rawBits(data[1]);
      data[2] = rawBits(data[2]);
      data[3] = rawBits(data[3]);
      data[4] = rawBits(data[4]);
      data[5] = rawBits(data[5]);
      data[6] = rawBits(data[6]);
      data[7] = rawBits(data[7]);
      data[8] = rawBits(data[8]);

      return true;
  }

  return false;
}
     
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
