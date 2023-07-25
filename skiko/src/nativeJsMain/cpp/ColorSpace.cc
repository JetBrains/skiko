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

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ColorSpace__1nMakeRGB
  (KFloat* tf, KFloat* mat) {
    skcms_TransferFunction transferFn = { tf[0], tf[1], tf[2], tf[3], tf[4], tf[5], tf[6] };
    skcms_Matrix3x3 toXYZ = {{{ mat[0], mat[1], mat[2] }, { mat[3], mat[4], mat[5] }, { mat[6], mat[7], mat[8] }}};
    SkColorSpace* ptr = SkColorSpace::MakeRGB(transferFn, toXYZ).release();
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


SKIKO_EXPORT void org_jetbrains_skia_ColorSpace__1nGetTransferFunction
  (KNativePointer ptr, KFloat* result) {
    SkColorSpace* instance = reinterpret_cast<SkColorSpace*>((ptr));
    skcms_TransferFunction tf;
    instance->transferFn(&tf);
    result[0] = tf.g;
    result[1] = tf.a;
    result[2] = tf.b;
    result[3] = tf.c;
    result[4] = tf.d;
    result[5] = tf.e;
    result[6] = tf.f;
}

SKIKO_EXPORT void org_jetbrains_skia_ColorSpace__1nGetToXYZD50
  (KNativePointer ptr, KFloat* result) {
    SkColorSpace* instance = reinterpret_cast<SkColorSpace*>((ptr));
    skcms_Matrix3x3 toXYZ;
    instance->toXYZD50(&matrix);
    result[0] = toXYZ.vals[0][0];
    result[1] = toXYZ.vals[0][1];
    result[2] = toXYZ.vals[0][2];
    result[3] = toXYZ.vals[1][0];
    result[4] = toXYZ.vals[1][1];
    result[5] = toXYZ.vals[1][2];
    result[6] = toXYZ.vals[2][0];
    result[7] = toXYZ.vals[2][1];
    result[8] = toXYZ.vals[2][2];
}


SKIKO_EXPORT KBoolean org_jetbrains_skia_ColorSpace__1nEquals
  (KNativePointer ptr, KNativePointer otherPtr) {
    SkColorSpace* instance = reinterpret_cast<SkColorSpace*>((ptr));
    SkColorSpace* other = reinterpret_cast<SkColorSpace*>((otherPtr));
    return SkColorSpace::Equals(instance, other);
}
