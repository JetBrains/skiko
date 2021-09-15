
// This file has been auto generated.

#include <iostream>
#include "SkRegion.h"
#include "common.h"

static void deleteRegion(SkRegion* region) {
    // std::cout << "Deleting [SkRegion " << Region << "]" << std::endl;
    delete region;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Region__1nMake() {
    SkRegion* obj = new SkRegion();
    return reinterpret_cast<KNativePointer>(obj);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Region__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>((&deleteRegion));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nSet(KNativePointer ptr, KNativePointer regionPtr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    SkRegion* other = reinterpret_cast<SkRegion*>((regionPtr));
    return instance->set(*other);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nIsEmpty(KNativePointer ptr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return instance->isEmpty();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nIsRect(KNativePointer ptr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return instance->isRect();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nIsComplex(KNativePointer ptr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return instance->isComplex();
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Region__1nGetBounds(KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Region__1nGetBounds(KInteropPointer");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Region__1nGetBounds(KNativePointer ptr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return skija::IRect::fromSkIRect(env, instance->getBounds());
}
#endif


SKIKO_EXPORT KInt org_jetbrains_skia_Region__1nComputeRegionComplexity(KNativePointer ptr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return instance->computeRegionComplexity();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nGetBoundaryPath(KNativePointer ptr, KNativePointer pathPtr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    SkPath* path = reinterpret_cast<SkPath*>((pathPtr));
    return instance->getBoundaryPath(path);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nSetEmpty(KNativePointer ptr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return instance->setEmpty();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nSetRect(KNativePointer ptr, KInt left, KInt top, KInt right, KInt bottom) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return instance->setRect({left, top, right, bottom});
}


SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nSetRects(KNativePointer ptr, KInt* coords) {
    TODO("implement org_jetbrains_skia_Region__1nSetRects(KInteropPointer");
}
     
#if 0 
SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nSetRects(KNativePointer ptr, KInt* coords) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    int len = env->GetArrayLength(coords);
    std::vector<SkIRect> rects(len / 4);
    KInt* arr = env->GetIntArrayElements(coords, 0);
    for (int i = 0; i < len; i += 4)
        rects[i / 4] = {arr[i], arr[i+1], arr[i+2], arr[i+3]};
    env->ReleaseIntArrayElements(coords, arr, 0);
    return instance->setRects(rects.data(), len / 4);
}
#endif


SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nSetRegion(KNativePointer ptr, KNativePointer regionPtr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    SkRegion* region = reinterpret_cast<SkRegion*>((regionPtr));
    return instance->setRegion(*region);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nSetPath(KNativePointer ptr, KNativePointer pathPtr, KNativePointer regionPtr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    SkPath* path = reinterpret_cast<SkPath*>((pathPtr));
    SkRegion* region = reinterpret_cast<SkRegion*>((regionPtr));
    return instance->setPath(*path, *region);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nIntersectsIRect(KNativePointer ptr, KInt left, KInt top, KInt right, KInt bottom) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return instance->intersects({left, top, right, bottom});
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nIntersectsRegion(KNativePointer ptr, KNativePointer regionPtr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    SkRegion* region = reinterpret_cast<SkRegion*>((regionPtr));
    return instance->intersects(*region);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nContainsIPoint(KNativePointer ptr, KInt x, KInt y) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return instance->contains(x, y);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nContainsIRect(KNativePointer ptr, KInt left, KInt top, KInt right, KInt bottom) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return instance->contains({left, top, right, bottom});
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nContainsRegion(KNativePointer ptr, KNativePointer regionPtr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    SkRegion* region = reinterpret_cast<SkRegion*>((regionPtr));
    return instance->contains(*region);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nQuickContains(KNativePointer ptr, KInt left, KInt top, KInt right, KInt bottom) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return instance->quickContains({left, top, right, bottom});
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nQuickRejectIRect(KNativePointer ptr, KInt left, KInt top, KInt right, KInt bottom) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return instance->quickReject({left, top, right, bottom});
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nQuickRejectRegion(KNativePointer ptr, KNativePointer regionPtr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    SkRegion* region = reinterpret_cast<SkRegion*>((regionPtr));
    return instance->contains(*region);
}

SKIKO_EXPORT void org_jetbrains_skia_Region__1nTranslate(KNativePointer ptr, KInt dx, KInt dy) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    instance->translate(dx, dy);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nOpIRect(KNativePointer ptr, KInt left, KInt top, KInt right, KInt bottom, KInt op) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return instance->op({left, top, right, bottom}, static_cast<SkRegion::Op>(op));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nOpRegion(KNativePointer ptr, KNativePointer regionPtr, KInt op) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    SkRegion* region = reinterpret_cast<SkRegion*>((regionPtr));
    return instance->op(*region, static_cast<SkRegion::Op>(op));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nOpIRectRegion(KNativePointer ptr, KInt left, KInt top, KInt right, KInt bottom, KNativePointer regionPtr, KInt op) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    SkRegion* region = reinterpret_cast<SkRegion*>((regionPtr));
    return instance->op({left, top, right, bottom}, *region, static_cast<SkRegion::Op>(op));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nOpRegionIRect(KNativePointer ptr, KNativePointer regionPtr, KInt left, KInt top, KInt right, KInt bottom, KInt op) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    SkRegion* region = reinterpret_cast<SkRegion*>((regionPtr));
    return instance->op(*region, {left, top, right, bottom}, static_cast<SkRegion::Op>(op));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nOpRegionRegion(KNativePointer ptr, KNativePointer regionPtrA, KNativePointer regionPtrB, KInt op) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    SkRegion* regionA = reinterpret_cast<SkRegion*>((regionPtrA));
    SkRegion* regionB = reinterpret_cast<SkRegion*>((regionPtrB));
    return instance->op(*regionA, *regionB, static_cast<SkRegion::Op>(op));
}
