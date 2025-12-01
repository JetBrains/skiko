#include <iostream>
#include <jni.h>
#include "SkColorSpace.h"
#include "interop.hh"

static void copySkcmsTransferFunctionToJFloatArray(JNIEnv* env, const skcms_TransferFunction& tf, jfloatArray& jresult) {
    jfloat array[7] = { tf.g, tf.a, tf.b, tf.c, tf.d, tf.e, tf.f };
    env->SetFloatArrayRegion(jresult, 0, 7, array);
}

static void copyJFloatArrayToSkcmsTransferFunction(JNIEnv* env, jfloatArray& jtransferFunction, skcms_TransferFunction* result) {
    jfloat* tf = env->GetFloatArrayElements(jtransferFunction, 0);
    *result = { tf[0], tf[1], tf[2], tf[3], tf[4], tf[5], tf[6] };
    env->ReleaseFloatArrayElements(jtransferFunction, tf, 0);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_TransferFunctionKt__1nGetSRGB
  (JNIEnv* env, jclass jclass, jfloatArray jresult) {
    copySkcmsTransferFunctionToJFloatArray(env, SkNamedTransferFn::kSRGB, jresult);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_TransferFunctionKt__1nGetGamma2Dot2
  (JNIEnv* env, jclass jclass, jfloatArray jresult) {
    copySkcmsTransferFunctionToJFloatArray(env, SkNamedTransferFn::k2Dot2, jresult);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_TransferFunctionKt__1nGetLinear
  (JNIEnv* env, jclass jclass, jfloatArray jresult) {
    copySkcmsTransferFunctionToJFloatArray(env, SkNamedTransferFn::kLinear, jresult);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_TransferFunctionKt__1nGetRec2020
  (JNIEnv* env, jclass jclass, jfloatArray jresult) {
    copySkcmsTransferFunctionToJFloatArray(env, SkNamedTransferFn::kRec2020, jresult);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_TransferFunctionKt__1nGetPQ
  (JNIEnv* env, jclass jclass, jfloatArray jresult) {
    copySkcmsTransferFunctionToJFloatArray(env, SkNamedTransferFn::kPQ, jresult);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_TransferFunctionKt__1nGetHLG
  (JNIEnv* env, jclass jclass, jfloatArray jresult) {
    copySkcmsTransferFunctionToJFloatArray(env, SkNamedTransferFn::kHLG, jresult);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_TransferFunctionKt__1nMakePQish
  (JNIEnv* env, jclass jclass, jfloat A, jfloat B, jfloat C, jfloat D, jfloat E, jfloat F, jfloatArray jresult) {
    skcms_TransferFunction transferFn;
    bool success = skcms_TransferFunction_makePQish(&transferFn, A, B, C, D, E, F);
    if (success)
        copySkcmsTransferFunctionToJFloatArray(env, transferFn, jresult);
    return success;
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_TransferFunctionKt__1nMakeScaledHLGish
  (JNIEnv* env, jclass jclass, jfloat K, jfloat R, jfloat G, jfloat a, jfloat b, jfloat c, jfloatArray jresult) {
    skcms_TransferFunction transferFn;
    bool success = skcms_TransferFunction_makeScaledHLGish(&transferFn, K, R, G, a, b, c);
    if (success)
        copySkcmsTransferFunctionToJFloatArray(env, transferFn, jresult);
    return success;
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_TransferFunctionKt__1nGetType
  (JNIEnv* env, jclass jclass, jfloatArray jtransferFunction) {
    skcms_TransferFunction transferFn;
    copyJFloatArrayToSkcmsTransferFunction(env, jtransferFunction, &transferFn);
    return static_cast<jint>(skcms_TransferFunction_getType(&transferFn));
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_TransferFunctionKt__1nEval
  (JNIEnv* env, jclass jclass, jfloatArray jtransferFunction, jfloat x) {
    skcms_TransferFunction transferFn;
    copyJFloatArrayToSkcmsTransferFunction(env, jtransferFunction, &transferFn);
    return skcms_TransferFunction_eval(&transferFn, x);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_TransferFunctionKt__1nInvert
  (JNIEnv* env, jclass jclass, jfloatArray jtransferFunction, jfloatArray jresult) {
    skcms_TransferFunction transferFn;
    skcms_TransferFunction resultTransferFn;
    copyJFloatArrayToSkcmsTransferFunction(env, jtransferFunction, &transferFn);
    bool success = skcms_TransferFunction_invert(&transferFn, &resultTransferFn);
    if (success)
        copySkcmsTransferFunctionToJFloatArray(env, resultTransferFn, jresult);
    return success;
}
