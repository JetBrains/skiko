
// This file has been auto generated.

#include <iostream>
#include "SkColorSpace.h"
#include "common.h"

static void unrefColorSpace(SkColorSpace* ptr) {
    // std::cout << "Deleting [SkColorSpace " << ptr << "]" << std::endl;
    ptr->unref();
}

extern "C" jlong org_jetbrains_skia_ColorSpace__1nGetFinalizer() {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&unrefColorSpace));
}

extern "C" jlong org_jetbrains_skia_ColorSpace__1nMakeSRGB() {
    SkColorSpace* ptr = SkColorSpace::MakeSRGB().release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" jlong org_jetbrains_skia_ColorSpace__1nMakeSRGBLinear() {
    SkColorSpace* ptr = SkColorSpace::MakeSRGBLinear().release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" jlong org_jetbrains_skia_ColorSpace__1nMakeDisplayP3() {
    SkColorSpace* ptr = SkColorSpace::MakeRGB(SkNamedTransferFn::kSRGB, SkNamedGamut::kDisplayP3).release();
    return reinterpret_cast<jlong>(ptr);
}


extern "C" void org_jetbrains_skia_ColorSpace__nConvert(
    jlong fromPtr, jlong toPtr, float r, float g, float b, float a, float* result) {
    SkColorSpace* from = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(fromPtr));
    SkColorSpace* to = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(toPtr));

    skcms_TransferFunction fromFn;
    from->transferFn(&fromFn);

    skcms_TransferFunction toFn;
    to->invTransferFn(&toFn);

    result[0] = skcms_TransferFunction_eval(&toFn, skcms_TransferFunction_eval(&fromFn, r));
    result[1] = skcms_TransferFunction_eval(&toFn, skcms_TransferFunction_eval(&fromFn, g));
    result[2] = skcms_TransferFunction_eval(&toFn, skcms_TransferFunction_eval(&fromFn, b));
    result[3] = skcms_TransferFunction_eval(&toFn, skcms_TransferFunction_eval(&fromFn, a));
}


extern "C" jlong org_jetbrains_skia_ColorSpace__1nIsGammaCloseToSRGB
  (jlong ptr) {
    SkColorSpace* instance = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(ptr));
    return instance->gammaCloseToSRGB();
}

extern "C" jlong org_jetbrains_skia_ColorSpace__1nIsGammaLinear
  (jlong ptr) {
    SkColorSpace* instance = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(ptr));
    return instance->gammaIsLinear();
}

extern "C" jlong org_jetbrains_skia_ColorSpace__1nIsSRGB
  (jlong ptr) {
    SkColorSpace* instance = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(ptr));
    return instance->isSRGB();
}
