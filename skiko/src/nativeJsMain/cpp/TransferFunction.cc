#include <iostream>
#include "SkColorSpace.h"
#include "common.h"

static void copySkcmsTransferFunctionToKFloatArray(const skcms_TransferFunction& tf, KFloat* result) {
    result[0] = tf.g;
    result[1] = tf.a;
    result[2] = tf.b;
    result[3] = tf.c;
    result[4] = tf.d;
    result[5] = tf.e;
    result[6] = tf.f;
}

static void copyKFloatArrayToSkcmsTransferFunction(KFloat* tf, skcms_TransferFunction* result) {
    *result = { tf[0], tf[1], tf[2], tf[3], tf[4], tf[5], tf[6] };
}

SKIKO_EXPORT void org_jetbrains_skia_TransferFunction__1nGetSRGB
  (KFloat* result) {
    copySkcmsTransferFunctionToKFloatArray(env, SkNamedTransferFn::kSRGB, result);
}

SKIKO_EXPORT void org_jetbrains_skia_TransferFunction__1nGetGamma2Dot2
  (KFloat* result) {
    copySkcmsTransferFunctionToKFloatArray(env, SkNamedTransferFn::k2Dot2, result);
}

SKIKO_EXPORT void org_jetbrains_skia_TransferFunction__1nGetLinear
  (KFloat* result) {
    copySkcmsTransferFunctionToKFloatArray(env, SkNamedTransferFn::kLinear, result);
}

SKIKO_EXPORT void org_jetbrains_skia_TransferFunction__1nGetRec2020
  (KFloat* result) {
    copySkcmsTransferFunctionToKFloatArray(env, SkNamedTransferFn::kRec2020, result);
}

SKIKO_EXPORT void org_jetbrains_skia_TransferFunction__1nGetPQ
  (KFloat* result) {
    copySkcmsTransferFunctionToKFloatArray(env, SkNamedTransferFn::kPQ, result);
}

SKIKO_EXPORT void org_jetbrains_skia_TransferFunction__1nGetHLG
  (KFloat* result) {
    copySkcmsTransferFunctionToKFloatArray(env, SkNamedTransferFn::kHLG, result);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_TransferFunction__1nMakePQish
  (KFloat A, KFloat B, KFloat C, KFloat D, KFloat E, KFloat F, KFloat* result) {
    skcms_TransferFunction transferFn;
    bool success = skcms_TransferFunction_makePQish(&transferFn, A, B, C, D, E, F);
    if (success)
        copySkcmsTransferFunctionToKFloatArray(env, transferFn, result);
    return success;
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_TransferFunction__1nMakeScaledHLGish
  (KFloat K, KFloat R, KFloat G, KFloat a, KFloat b, KFloat c, KFloat* result) {
    skcms_TransferFunction transferFn;
    bool success = skcms_TransferFunction_makeScaledHLGish(&transferFn, K, R, G, a, b, c);
    if (success)
        copySkcmsTransferFunctionToKFloatArray(env, transferFn, result);
    return success;
}

SKIKO_EXPORT KInt org_jetbrains_skia_TransferFunction__1nGetType
  (KFloat* transferFunction) {
    skcms_TransferFunction transferFn;
    copyKFloatArrayToSkcmsTransferFunction(env, transferFunction, &transferFn);
    return static_cast<KInt>(skcms_TransferFunction_getType(&transferFn));
}

SKIKO_EXPORT KInt org_jetbrains_skia_TransferFunction__1nEval
  (KFloat* transferFunction, KFloat x) {
    skcms_TransferFunction transferFn;
    copyKFloatArrayToSkcmsTransferFunction(env, transferFunction, &transferFn);
    return skcms_TransferFunction_eval(&transferFn, x);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_TransferFunction__1nInvert
  (KFloat* transferFunction, KFloat* result) {
    skcms_TransferFunction transferFn;
    skcms_TransferFunction resultTransferFn;
    copyKFloatArrayToSkcmsTransferFunction(env, transferFunction, &transferFn);
    bool success = skcms_TransferFunction_invert(&transferFn, &resultTransferFn);
    if (success)
        copySkcmsTransferFunctionToKFloatArray(env, resultTransferFn, result);
    return success;
}
