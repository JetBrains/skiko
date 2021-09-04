
// This file has been auto generated.

#include <iostream>
#include "SkRegion.h"
#include "common.h"

static void deleteRegion(SkRegion* region) {
    // std::cout << "Deleting [SkRegion " << Region << "]" << std::endl;
    delete region;
}

extern "C" jlong org_jetbrains_skia_Region__1nMake(kref __Kinstance) {
    SkRegion* obj = new SkRegion();
    return reinterpret_cast<jlong>(obj);
}

extern "C" jlong org_jetbrains_skia_Region__1nGetFinalizer(kref __Kinstance) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteRegion));
}

extern "C" jboolean org_jetbrains_skia_Region__1nSet(kref __Kinstance, jlong ptr, jlong regionPtr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    SkRegion* other = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(regionPtr));
    return instance->set(*other);
}

extern "C" jboolean org_jetbrains_skia_Region__1nIsEmpty(kref __Kinstance, jlong ptr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    return instance->isEmpty();
}

extern "C" jboolean org_jetbrains_skia_Region__1nIsRect(kref __Kinstance, jlong ptr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    return instance->isRect();
}

extern "C" jboolean org_jetbrains_skia_Region__1nIsComplex(kref __Kinstance, jlong ptr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    return instance->isComplex();
}


extern "C" jobject org_jetbrains_skia_Region__1nGetBounds(kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_Region__1nGetBounds(kref");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Region__1nGetBounds(kref __Kinstance, jlong ptr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    return skija::IRect::fromSkIRect(env, instance->getBounds());
}
#endif


extern "C" jint org_jetbrains_skia_Region__1nComputeRegionComplexity(kref __Kinstance, jlong ptr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    return instance->computeRegionComplexity();
}

extern "C" jboolean org_jetbrains_skia_Region__1nGetBoundaryPath(kref __Kinstance, jlong ptr, jlong pathPtr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    SkPath* path = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(pathPtr));
    return instance->getBoundaryPath(path);
}

extern "C" jboolean org_jetbrains_skia_Region__1nSetEmpty(kref __Kinstance, jlong ptr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    return instance->setEmpty();
}

extern "C" jboolean org_jetbrains_skia_Region__1nSetRect(kref __Kinstance, jlong ptr, jint left, jint top, jint right, jint bottom) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    return instance->setRect({left, top, right, bottom});
}


extern "C" jboolean org_jetbrains_skia_Region__1nSetRects(kref __Kinstance, jlong ptr, jintArray coords) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_Region__1nSetRects(kref");
}
     
#if 0 
extern "C" jboolean org_jetbrains_skia_Region__1nSetRects(kref __Kinstance, jlong ptr, jintArray coords) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    int len = env->GetArrayLength(coords);
    std::vector<SkIRect> rects(len / 4);
    jint* arr = env->GetIntArrayElements(coords, 0);
    for (int i = 0; i < len; i += 4)
        rects[i / 4] = {arr[i], arr[i+1], arr[i+2], arr[i+3]};
    env->ReleaseIntArrayElements(coords, arr, 0);
    return instance->setRects(rects.data(), len / 4);
}
#endif


extern "C" jboolean org_jetbrains_skia_Region__1nSetRegion(kref __Kinstance, jlong ptr, jlong regionPtr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    SkRegion* region = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(regionPtr));
    return instance->setRegion(*region);
}

extern "C" jboolean org_jetbrains_skia_Region__1nSetPath(kref __Kinstance, jlong ptr, jlong pathPtr, jlong regionPtr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    SkPath* path = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(pathPtr));
    SkRegion* region = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(regionPtr));
    return instance->setPath(*path, *region);
}

extern "C" jboolean org_jetbrains_skia_Region__1nIntersectsIRect(kref __Kinstance, jlong ptr, jint left, jint top, jint right, jint bottom) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    return instance->intersects({left, top, right, bottom});
}

extern "C" jboolean org_jetbrains_skia_Region__1nIntersectsRegion(kref __Kinstance, jlong ptr, jlong regionPtr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    SkRegion* region = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(regionPtr));
    return instance->intersects(*region);
}

extern "C" jboolean org_jetbrains_skia_Region__1nContainsIPoint(kref __Kinstance, jlong ptr, jint x, jint y) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    return instance->contains(x, y);
}

extern "C" jboolean org_jetbrains_skia_Region__1nContainsIRect(kref __Kinstance, jlong ptr, jint left, jint top, jint right, jint bottom) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    return instance->contains({left, top, right, bottom});
}

extern "C" jboolean org_jetbrains_skia_Region__1nContainsRegion(kref __Kinstance, jlong ptr, jlong regionPtr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    SkRegion* region = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(regionPtr));
    return instance->contains(*region);
}

extern "C" jboolean org_jetbrains_skia_Region__1nQuickContains(kref __Kinstance, jlong ptr, jint left, jint top, jint right, jint bottom) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    return instance->quickContains({left, top, right, bottom});
}

extern "C" jboolean org_jetbrains_skia_Region__1nQuickRejectIRect(kref __Kinstance, jlong ptr, jint left, jint top, jint right, jint bottom) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    return instance->quickReject({left, top, right, bottom});
}

extern "C" jboolean org_jetbrains_skia_Region__1nQuickRejectRegion(kref __Kinstance, jlong ptr, jlong regionPtr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    SkRegion* region = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(regionPtr));
    return instance->contains(*region);
}

extern "C" void org_jetbrains_skia_Region__1nTranslate(kref __Kinstance, jlong ptr, jint dx, jint dy) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    instance->translate(dx, dy);
}

extern "C" jboolean org_jetbrains_skia_Region__1nOpIRect(kref __Kinstance, jlong ptr, jint left, jint top, jint right, jint bottom, jint op) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    return instance->op({left, top, right, bottom}, static_cast<SkRegion::Op>(op));
}

extern "C" jboolean org_jetbrains_skia_Region__1nOpRegion(kref __Kinstance, jlong ptr, jlong regionPtr, jint op) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    SkRegion* region = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(regionPtr));
    return instance->op(*region, static_cast<SkRegion::Op>(op));
}

extern "C" jboolean org_jetbrains_skia_Region__1nOpIRectRegion(kref __Kinstance, jlong ptr, jint left, jint top, jint right, jint bottom, jlong regionPtr, jint op) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    SkRegion* region = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(regionPtr));
    return instance->op({left, top, right, bottom}, *region, static_cast<SkRegion::Op>(op));
}

extern "C" jboolean org_jetbrains_skia_Region__1nOpRegionIRect(kref __Kinstance, jlong ptr, jlong regionPtr, jint left, jint top, jint right, jint bottom, jint op) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    SkRegion* region = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(regionPtr));
    return instance->op(*region, {left, top, right, bottom}, static_cast<SkRegion::Op>(op));
}

extern "C" jboolean org_jetbrains_skia_Region__1nOpRegionRegion(kref __Kinstance, jlong ptr, jlong regionPtrA, jlong regionPtrB, jint op) {
    SkRegion* instance = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(ptr));
    SkRegion* regionA = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(regionPtrA));
    SkRegion* regionB = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(regionPtrB));
    return instance->op(*regionA, *regionB, static_cast<SkRegion::Op>(op));
}
