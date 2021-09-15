
// This file has been auto generated.

#include "SkPathMeasure.h"
#include "common.h"

static void deletePathMeasure(SkPathMeasure* instance) {
    delete instance;
}

extern "C" jlong org_jetbrains_skia_PathMeasure__1nGetFinalizer() {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deletePathMeasure));
}

extern "C" jlong org_jetbrains_skia_PathMeasure__1nMake
  () {
    return reinterpret_cast<jlong>(new SkPathMeasure());
}

extern "C" jlong org_jetbrains_skia_PathMeasure__1nMakePath
  (jlong pathPtr, jboolean forceClosed, jfloat resScale) {
    SkPath* path = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(pathPtr));
    return reinterpret_cast<jlong>(new SkPathMeasure(*path, forceClosed, resScale));
}

extern "C" void org_jetbrains_skia_PathMeasure__1nSetPath
  (jlong ptr, jlong pathPtr, jboolean forceClosed) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>(static_cast<uintptr_t>(ptr));
    SkPath* path = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(pathPtr));
    instance->setPath(path, forceClosed);
}

extern "C" jfloat org_jetbrains_skia_PathMeasure__1nGetLength
  (jlong ptr) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>(static_cast<uintptr_t>(ptr));
    return instance->getLength();
}


extern "C" jobject org_jetbrains_skia_PathMeasure__1nGetPosition
  (jlong ptr, jfloat distance) {
    TODO("implement org_jetbrains_skia_PathMeasure__1nGetPosition");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_PathMeasure__1nGetPosition
  (jlong ptr, jfloat distance) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>(static_cast<uintptr_t>(ptr));
    SkPoint position;
    if (instance->getPosTan(distance, &position, nullptr))
        return skija::Point::fromSkPoint(env, position);
    else
        return nullptr;
}
#endif



extern "C" jobject org_jetbrains_skia_PathMeasure__1nGetTangent
  (jlong ptr, jfloat distance) {
    TODO("implement org_jetbrains_skia_PathMeasure__1nGetTangent");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_PathMeasure__1nGetTangent
  (jlong ptr, jfloat distance) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>(static_cast<uintptr_t>(ptr));
    SkVector tangent;
    if (instance->getPosTan(distance, nullptr, &tangent))
        return skija::Point::fromSkPoint(env, tangent);
    else
        return nullptr;
}
#endif



extern "C" jobject org_jetbrains_skia_PathMeasure__1nGetRSXform
  (jlong ptr, jfloat distance) {
    TODO("implement org_jetbrains_skia_PathMeasure__1nGetRSXform");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_PathMeasure__1nGetRSXform
  (jlong ptr, jfloat distance) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>(static_cast<uintptr_t>(ptr));
    SkPoint position;
    SkVector tangent;
    if (instance->getPosTan(distance, &position, &tangent))
        return env->NewObject(skija::RSXform::cls, skija::RSXform::ctor, tangent.fX, tangent.fY, position.fX, position.fY);
    else
        return nullptr;
}
#endif



extern "C" jobject org_jetbrains_skia_PathMeasure__1nGetMatrix
  (jlong ptr, jfloat distance, jboolean getPosition, jboolean getTangent) {
    TODO("implement org_jetbrains_skia_PathMeasure__1nGetMatrix");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_PathMeasure__1nGetMatrix
  (jlong ptr, jfloat distance, jboolean getPosition, jboolean getTangent) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>(static_cast<uintptr_t>(ptr));
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


extern "C" jboolean org_jetbrains_skia_PathMeasure__1nGetSegment
  (jlong ptr, jfloat startD, jfloat endD, jlong dstPtr, jboolean startWithMoveTo) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>(static_cast<uintptr_t>(ptr));
    SkPath* dst = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(dstPtr));
    return instance->getSegment(startD, endD, dst, startWithMoveTo);
}

extern "C" jboolean org_jetbrains_skia_PathMeasure__1nIsClosed
  (jlong ptr) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>(static_cast<uintptr_t>(ptr));
    return instance->isClosed();
}

extern "C" jboolean org_jetbrains_skia_PathMeasure__1nNextContour
  (jlong ptr) {
    SkPathMeasure* instance = reinterpret_cast<SkPathMeasure*>(static_cast<uintptr_t>(ptr));
    return instance->nextContour();
}
