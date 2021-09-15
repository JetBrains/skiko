
// This file has been auto generated.

#include <iostream>
#include "SkRegion.h"
#include "common.h"

static void deleteRegion(SkRegion* region) {
    // std::cout << "Deleting [SkRegion " << Region << "]" << std::endl;
    delete region;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Region__1nMake(KInteropPointer __Kinstance) {
    SkRegion* obj = new SkRegion();
    return reinterpret_cast<KNativePointer>(obj);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Region__1nGetFinalizer(KInteropPointer __Kinstance) {
    return reinterpret_cast<KNativePointer>((&deleteRegion));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nSet(KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer regionPtr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    SkRegion* other = reinterpret_cast<SkRegion*>((regionPtr));
    return instance->set(*other);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nIsEmpty(KInteropPointer __Kinstance, KNativePointer ptr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return instance->isEmpty();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nIsRect(KInteropPointer __Kinstance, KNativePointer ptr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return instance->isRect();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nIsComplex(KInteropPointer __Kinstance, KNativePointer ptr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return instance->isComplex();
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Region__1nGetBounds(KInteropPointer __Kinstance, KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Region__1nGetBounds(KInteropPointer");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Region__1nGetBounds(KInteropPointer __Kinstance, KNativePointer ptr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return skija::IRect::fromSkIRect(env, instance->getBounds());
}
#endif


SKIKO_EXPORT KInt org_jetbrains_skia_Region__1nComputeRegionComplexity(KInteropPointer __Kinstance, KNativePointer ptr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return instance->computeRegionComplexity();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nGetBoundaryPath(KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer pathPtr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    SkPath* path = reinterpret_cast<SkPath*>((pathPtr));
    return instance->getBoundaryPath(path);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nSetEmpty(KInteropPointer __Kinstance, KNativePointer ptr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return instance->setEmpty();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nSetRect(KInteropPointer __Kinstance, KNativePointer ptr, KInt left, KInt top, KInt right, KInt bottom) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return instance->setRect({left, top, right, bottom});
}


SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nSetRects(KInteropPointer __Kinstance, KNativePointer ptr, KInt* coords) {
    TODO("implement org_jetbrains_skia_Region__1nSetRects(KInteropPointer");
}
     
#if 0 
SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nSetRects(KInteropPointer __Kinstance, KNativePointer ptr, KInt* coords) {
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


SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nSetRegion(KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer regionPtr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    SkRegion* region = reinterpret_cast<SkRegion*>((regionPtr));
    return instance->setRegion(*region);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nSetPath(KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer pathPtr, KNativePointer regionPtr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    SkPath* path = reinterpret_cast<SkPath*>((pathPtr));
    SkRegion* region = reinterpret_cast<SkRegion*>((regionPtr));
    return instance->setPath(*path, *region);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nIntersectsIRect(KInteropPointer __Kinstance, KNativePointer ptr, KInt left, KInt top, KInt right, KInt bottom) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return instance->intersects({left, top, right, bottom});
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nIntersectsRegion(KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer regionPtr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    SkRegion* region = reinterpret_cast<SkRegion*>((regionPtr));
    return instance->intersects(*region);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nContainsIPoint(KInteropPointer __Kinstance, KNativePointer ptr, KInt x, KInt y) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return instance->contains(x, y);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nContainsIRect(KInteropPointer __Kinstance, KNativePointer ptr, KInt left, KInt top, KInt right, KInt bottom) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return instance->contains({left, top, right, bottom});
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nContainsRegion(KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer regionPtr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    SkRegion* region = reinterpret_cast<SkRegion*>((regionPtr));
    return instance->contains(*region);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nQuickContains(KInteropPointer __Kinstance, KNativePointer ptr, KInt left, KInt top, KInt right, KInt bottom) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return instance->quickContains({left, top, right, bottom});
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nQuickRejectIRect(KInteropPointer __Kinstance, KNativePointer ptr, KInt left, KInt top, KInt right, KInt bottom) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return instance->quickReject({left, top, right, bottom});
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nQuickRejectRegion(KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer regionPtr) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    SkRegion* region = reinterpret_cast<SkRegion*>((regionPtr));
    return instance->contains(*region);
}

SKIKO_EXPORT void org_jetbrains_skia_Region__1nTranslate(KInteropPointer __Kinstance, KNativePointer ptr, KInt dx, KInt dy) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    instance->translate(dx, dy);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nOpIRect(KInteropPointer __Kinstance, KNativePointer ptr, KInt left, KInt top, KInt right, KInt bottom, KInt op) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    return instance->op({left, top, right, bottom}, static_cast<SkRegion::Op>(op));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nOpRegion(KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer regionPtr, KInt op) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    SkRegion* region = reinterpret_cast<SkRegion*>((regionPtr));
    return instance->op(*region, static_cast<SkRegion::Op>(op));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nOpIRectRegion(KInteropPointer __Kinstance, KNativePointer ptr, KInt left, KInt top, KInt right, KInt bottom, KNativePointer regionPtr, KInt op) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    SkRegion* region = reinterpret_cast<SkRegion*>((regionPtr));
    return instance->op({left, top, right, bottom}, *region, static_cast<SkRegion::Op>(op));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nOpRegionIRect(KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer regionPtr, KInt left, KInt top, KInt right, KInt bottom, KInt op) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    SkRegion* region = reinterpret_cast<SkRegion*>((regionPtr));
    return instance->op(*region, {left, top, right, bottom}, static_cast<SkRegion::Op>(op));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Region__1nOpRegionRegion(KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer regionPtrA, KNativePointer regionPtrB, KInt op) {
    SkRegion* instance = reinterpret_cast<SkRegion*>((ptr));
    SkRegion* regionA = reinterpret_cast<SkRegion*>((regionPtrA));
    SkRegion* regionB = reinterpret_cast<SkRegion*>((regionPtrB));
    return instance->op(*regionA, *regionB, static_cast<SkRegion::Op>(op));
}
