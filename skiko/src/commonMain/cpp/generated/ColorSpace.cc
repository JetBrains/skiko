
// This file has been auto generated.

#include <iostream>
#include "SkColorSpace.h"
#include "common.h"

static void unrefColorSpace(SkColorSpace* ptr) {
    // std::cout << "Deleting [SkColorSpace " << ptr << "]" << std::endl;
    ptr->unref();
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ColorSpace__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>((&unrefColorSpace));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ColorSpace__1nMakeSRGB() {
    SkColorSpace* ptr = SkColorSpace::MakeSRGB().release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ColorSpace__1nMakeSRGBLinear() {
    SkColorSpace* ptr = SkColorSpace::MakeSRGBLinear().release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ColorSpace__1nMakeDisplayP3() {
    SkColorSpace* ptr = SkColorSpace::MakeRGB(SkNamedTransferFn::kSRGB, SkNamedGamut::kDisplayP3).release();
    return reinterpret_cast<KNativePointer>(ptr);
}


SKIKO_EXPORT void org_jetbrains_skia_ColorSpace__nConvert(
    KNativePointer fromPtr, KNativePointer toPtr, float r, float g, float b, float a, float* result) {
    SkColorSpace* from = reinterpret_cast<SkColorSpace*>((fromPtr));
    SkColorSpace* to = reinterpret_cast<SkColorSpace*>((toPtr));

    skcms_TransferFunction fromFn;
    from->transferFn(&fromFn);

    skcms_TransferFunction toFn;
    to->invTransferFn(&toFn);

    result[0] = skcms_TransferFunction_eval(&toFn, skcms_TransferFunction_eval(&fromFn, r));
    result[1] = skcms_TransferFunction_eval(&toFn, skcms_TransferFunction_eval(&fromFn, g));
    result[2] = skcms_TransferFunction_eval(&toFn, skcms_TransferFunction_eval(&fromFn, b));
    result[3] = skcms_TransferFunction_eval(&toFn, skcms_TransferFunction_eval(&fromFn, a));
}


SKIKO_EXPORT KBoolean org_jetbrains_skia_ColorSpace__1nIsGammaCloseToSRGB
  (KNativePointer ptr) {
    SkColorSpace* instance = reinterpret_cast<SkColorSpace*>((ptr));
    return instance->gammaCloseToSRGB();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_ColorSpace__1nIsGammaLinear
  (KNativePointer ptr) {
    SkColorSpace* instance = reinterpret_cast<SkColorSpace*>((ptr));
    return instance->gammaIsLinear();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_ColorSpace__1nIsSRGB
  (KNativePointer ptr) {
    SkColorSpace* instance = reinterpret_cast<SkColorSpace*>((ptr));
    return instance->isSRGB();
}
