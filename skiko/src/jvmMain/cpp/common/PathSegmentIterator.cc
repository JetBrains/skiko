#include <jni.h>
#include "SkPath.h"
#include "interop.hh"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PathSegmentIteratorExternalKt_PathSegmentIterator_1nMake
  (JNIEnv* env, jclass jclass, jlong pathPtr, jboolean forceClose) {
    SkPath* path = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(pathPtr));
    SkPath::Iter* iter = new SkPath::Iter(*path, forceClose);
    return reinterpret_cast<jlong>(iter);
}

static void deletePathSegmentIterator(SkPath::Iter* iter) {
    // std::cout << "Deleting [SkPathSegmentIterator " << path << "]" << std::endl;
    delete iter;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PathSegmentIteratorExternalKt_PathSegmentIterator_1nGetFinalizer
  (JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deletePathSegmentIterator));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PathSegmentIteratorExternalKt_PathSegmentIterator_1nNext
  (JNIEnv* env, jclass jclass, jlong ptr, jintArray result) {
    SkPath::Iter* instance = reinterpret_cast<SkPath::Iter*>(static_cast<uintptr_t>(ptr));
    SkPoint pts[4];
    SkPath::Verb verb = instance->next(pts);

    int context = verb;
    if (instance -> isClosedContour()) {
        context = context | (1 << 7);
    }
    if (instance -> isCloseLine()) {
        context = context | (1 << 6);
    }

    switch (verb) {
        case SkPath::Verb::kDone_Verb: {
            jint d[10] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, context};
            env->SetIntArrayRegion(result, 0, 10, d);
            break;
        }
        case SkPath::Verb::kMove_Verb:
        case SkPath::Verb::kClose_Verb: {
            jint d[10] = { rawBits(pts[0].fX), rawBits(pts[0].fY), 0, 0, 0, 0, 0, 0, 0, context};
            env->SetIntArrayRegion(result, 0, 10, d);
            break;
        }
        case SkPath::Verb::kLine_Verb: {
            jint d[10] = { rawBits(pts[0].fX), rawBits(pts[0].fY), rawBits(pts[1].fX), rawBits(pts[1].fY), 0, 0, 0, 0, 0, context};
            env->SetIntArrayRegion(result, 0, 10, d);
            break;
        }
        case SkPath::Verb::kQuad_Verb: {
            jint d[10] = { rawBits(pts[0].fX), rawBits(pts[0].fY), rawBits(pts[1].fX), rawBits(pts[1].fY), rawBits(pts[2].fX), rawBits(pts[2].fY), 0, 0, 0, context};
            env->SetIntArrayRegion(result, 0, 10, d);
            break;
        }
        case SkPath::Verb::kConic_Verb: {
            jint d[10] = { rawBits(pts[0].fX), rawBits(pts[0].fY), rawBits(pts[1].fX), rawBits(pts[1].fY), rawBits(pts[2].fX), rawBits(pts[2].fY), 0, 0, rawBits(instance->conicWeight()), context};
            env->SetIntArrayRegion(result, 0, 10, d);
            break;
        }
        case SkPath::Verb::kCubic_Verb: {
            jint d[10] = { rawBits(pts[0].fX), rawBits(pts[0].fY), rawBits(pts[1].fX), rawBits(pts[1].fY), rawBits(pts[2].fX), rawBits(pts[2].fY), rawBits(pts[3].fX), rawBits(pts[3].fY), 0, context};
            env->SetIntArrayRegion(result, 0, 10, d);
            break;
        }
    }

}
