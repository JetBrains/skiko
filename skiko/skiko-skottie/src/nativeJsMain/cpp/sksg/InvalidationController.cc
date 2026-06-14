#include "SkSGInvalidationController.h"
#include "common.h"
#include "../common.h"

using sksg::InvalidationController;

static void deleteInvalidationController(InvalidationController* controller) {
    delete controller;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_sksg_InvalidationController_nGetFinalizer() {
    return reinterpret_cast<KNativePointer>(&deleteInvalidationController);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_sksg_InvalidationController_nMake() {
    InvalidationController* instance = new InvalidationController();
    return reinterpret_cast<KNativePointer>(instance);
}

SKIKO_EXPORT void org_jetbrains_skia_sksg_InvalidationController_nInvalidate
  (KNativePointer ptr, KFloat left, KFloat top, KFloat right, KFloat bottom, KFloat* matrixArr) {
    InvalidationController* instance = reinterpret_cast<InvalidationController*>(ptr);
    SkRect bounds {left, top, right, bottom};
    std::unique_ptr<SkMatrix> matrix = skMatrix(matrixArr);
    instance->inval(bounds, *matrix.get());
}

SKIKO_EXPORT void org_jetbrains_skia_sksg_InvalidationController_nGetBounds
  (KNativePointer ptr, KInteropPointer result) {
    InvalidationController* instance = reinterpret_cast<InvalidationController*>(ptr);
    skija::Rect::copyToInterop(instance->bounds(), result);
}

SKIKO_EXPORT void org_jetbrains_skia_sksg_InvalidationController_nReset(KNativePointer ptr) {
    InvalidationController* instance = reinterpret_cast<InvalidationController*>(ptr);
    instance->reset();
}
