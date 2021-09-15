
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
  (KInteropPointer __Kinstance) {
    return reinterpret_cast<KNativePointer>((&deletePathSegmentIterator));
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_PathSegmentIterator__1nNext
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_PathSegmentIterator__1nNext");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_PathSegmentIterator__1nNext
  (KNativePointer ptr) {
    SkPath::Iter* instance = reinterpret_cast<SkPath::Iter*>((ptr));
    SkPoint pts[4];
    SkPath::Verb verb = instance->next(pts);
    KInteropPointer segment;
    switch (verb) {
        case SkPath::Verb::kDone_Verb:
            segment = env->NewObject(skija::PathSegment::cls, skija::PathSegment::ctorDone);
            break;
        case SkPath::Verb::kMove_Verb:
        case SkPath::Verb::kClose_Verb:
            segment = env->NewObject(skija::PathSegment::cls, skija::PathSegment::ctorMoveClose, static_cast<KInt>(verb), pts[0].fX, pts[0].fY, instance->isClosedContour());
            break;
        case SkPath::Verb::kLine_Verb:
            segment = env->NewObject(skija::PathSegment::cls, skija::PathSegment::ctorLine, pts[0].fX, pts[0].fY, pts[1].fX, pts[1].fY, instance->isCloseLine(), instance->isClosedContour());
            break;
        case SkPath::Verb::kQuad_Verb:
            segment = env->NewObject(skija::PathSegment::cls, skija::PathSegment::ctorQuad, pts[0].fX, pts[0].fY, pts[1].fX, pts[1].fY, pts[2].fX, pts[2].fY, instance->isClosedContour());
            break;
        case SkPath::Verb::kConic_Verb:
            segment = env->NewObject(skija::PathSegment::cls, skija::PathSegment::ctorConic, pts[0].fX, pts[0].fY, pts[1].fX, pts[1].fY, pts[2].fX, pts[2].fY, instance->conicWeight(), instance->isClosedContour());
            break;
        case SkPath::Verb::kCubic_Verb:
            segment = env->NewObject(skija::PathSegment::cls, skija::PathSegment::ctorConic, pts[0].fX, pts[0].fY, pts[1].fX, pts[1].fY, pts[2].fX, pts[2].fY, pts[3].fX, pts[3].fY, instance->isClosedContour());
            break;
    }
    return segment;
}
#endif

