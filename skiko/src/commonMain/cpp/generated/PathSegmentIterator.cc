
// This file has been auto generated.

#include "SkPath.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathSegmentIterator__1nMake
  (KNativePointer pathPtr, KBoolean forceClose) {
    SkPath* path = reinterpret_cast<SkPath*>(pathPtr);
    SkPath::Iter* iter = new SkPath::Iter(*path, forceClose);
    return reinterpret_cast<KNativePointer>(iter);
}

static void deletePathSegmentIterator(SkPath::Iter* iter) {
    // std::cout << "Deleting [SkPathSegmentIterator " << path << "]" << std::endl;
    delete iter;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_PathSegmentIterator__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>((&deletePathSegmentIterator));
}

SKIKO_EXPORT void org_jetbrains_skia_PathSegmentIterator__1nNext(KNativePointer ptr, KInt* data) {
    SkPath::Iter* instance = reinterpret_cast<SkPath::Iter*>(ptr);
    SkPoint pts[4];
    SkPath::Verb verb = instance->next(pts);

    data[0] = rawBits(pts[0].fX);
    data[1] = rawBits(pts[0].fY);

    data[2] = rawBits(pts[1].fX);
    data[3] = rawBits(pts[1].fY);

    data[4] = rawBits(pts[2].fX);
    data[5] = rawBits(pts[2].fY);

    data[6] = rawBits(pts[3].fX);
    data[7] = rawBits(pts[3].fY);

    // Otherwise it's null.
    if (verb == SkPath::Verb::kConic_Verb)
        data[8] = rawBits(instance->conicWeight());

    int context = verb;
    if (instance -> isClosedContour()) {
        context = context | (1 << 7);
    }
    if (instance -> isCloseLine()) {
        context = context | (1 << 6);
    }

    data[9] = context;
}

