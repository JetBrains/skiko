
// This file has been auto generated.

#include "SkPath.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathSegmentIterator__1nMake
  (KNativePointer pathPtr, KBoolean forceClose) {
    SkPath* path = reinterpret_cast<SkPath*>((pathPtr));
    SkPath::Iter* iter = new SkPath::Iter(*path, forceClose);
    return reinterpret_cast<KNativePointer>(iter);
}

static void deletePathSegmentIterator(SkPath::Iter* iter) {
    // std::cout << "Deleting [SkPathSegmentIterator " << path << "]" << std::endl;
    delete iter;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathSegmentIterator__1nGetFinalizer
  () {
    return reinterpret_cast<KNativePointer>((&deletePathSegmentIterator));
}


SKIKO_EXPORT void org_jetbrains_skia_PathSegmentIterator__1nNext
  (KNativePointer ptr, float* points) {
    SkPath::Iter* instance = reinterpret_cast<SkPath::Iter*>((ptr));
    SkPoint pts[4];
    SkPath::Verb verb = instance->next(pts);
    KInteropPointer segment;

    points[0] = pts[0].fX;
    points[1] = pts[0].fY;

    points[2] = pts[1].fX;
    points[3] = pts[1].fY;

    points[4] = pts[2].fX;
    points[5] = pts[2].fY;

    points[6] = pts[3].fX;
    points[7] = pts[3].fY;

    points[8] = instance->conicWeight();

    std::bitset<8> context(verb);
    context.set(7, instance->isClosedContour());
    context.set(6, instance->isCloseLine());


    points[9] = (float) (int) context.to_ulong();
}

