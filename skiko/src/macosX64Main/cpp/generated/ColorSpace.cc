
// This file has been auto generated.

#include <iostream>
#include "SkColorSpace.h"
#include "common.h"

static void unrefColorSpace(SkColorSpace* ptr) {
    // std::cout << "Deleting [SkColorSpace " << ptr << "]" << std::endl;
    ptr->unref();
}

extern "C" jlong org_jetbrains_skia_ColorSpace__1nGetFinalizer(kref __Kinstance) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&unrefColorSpace));
}

extern "C" jlong org_jetbrains_skia_ColorSpace__1nMakeSRGB(kref __Kinstance) {
    SkColorSpace* ptr = SkColorSpace::MakeSRGB().release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" jlong org_jetbrains_skia_ColorSpace__1nMakeSRGBLinear(kref __Kinstance) {
    SkColorSpace* ptr = SkColorSpace::MakeSRGBLinear().release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" jlong org_jetbrains_skia_ColorSpace__1nMakeDisplayP3(kref __Kinstance) {
    SkColorSpace* ptr = SkColorSpace::MakeRGB(SkNamedTransferFn::kSRGB, SkNamedGamut::kDisplayP3).release();
    return reinterpret_cast<jlong>(ptr);
}


extern "C" jfloatArray org_jetbrains_skia_ColorSpace__1nConvert
  (kref __Kinstance, jlong fromPtr, jlong toPtr, float r, float g, float b, float a) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_ColorSpace__1nConvert");
}
     
#if 0 
extern "C" jfloatArray org_jetbrains_skia_ColorSpace__1nConvert
  (kref __Kinstance, jlong fromPtr, jlong toPtr, float r, float g, float b, float a) {
    SkColorSpace* from = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(fromPtr));
    SkColorSpace* to = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(toPtr));

    skcms_TransferFunction fromFn;
    from->transferFn(&fromFn);

    skcms_TransferFunction toFn;
    to->invTransferFn(&toFn);

    float r1 = skcms_TransferFunction_eval(&toFn, skcms_TransferFunction_eval(&fromFn, r));
    float g1 = skcms_TransferFunction_eval(&toFn, skcms_TransferFunction_eval(&fromFn, g));
    float b1 = skcms_TransferFunction_eval(&toFn, skcms_TransferFunction_eval(&fromFn, b));
    float a1 = skcms_TransferFunction_eval(&toFn, skcms_TransferFunction_eval(&fromFn, a));
    return javaFloatArray(env, {r1, g1, b1, a1});
}
#endif


extern "C" jlong org_jetbrains_skia_ColorSpace__1nIsGammaCloseToSRGB
  (kref __Kinstance, jlong ptr) {
    SkColorSpace* instance = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(ptr));
    return instance->gammaCloseToSRGB();
}

extern "C" jlong org_jetbrains_skia_ColorSpace__1nIsGammaLinear
  (kref __Kinstance, jlong ptr) {
    SkColorSpace* instance = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(ptr));
    return instance->gammaIsLinear();
}

extern "C" jlong org_jetbrains_skia_ColorSpace__1nIsSRGB
  (kref __Kinstance, jlong ptr) {
    SkColorSpace* instance = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(ptr));
    return instance->isSRGB();
}
