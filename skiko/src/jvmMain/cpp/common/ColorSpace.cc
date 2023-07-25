#include <iostream>
#include <jni.h>
#include "SkColorSpace.h"
#include "interop.hh"

static void unrefColorSpace(SkColorSpace* ptr) {
    // std::cout << "Deleting [SkColorSpace " << ptr << "]" << std::endl;
    ptr->unref();
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ColorSpaceKt_ColorSpace_1nGetFinalizer(JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&unrefColorSpace));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ColorSpaceKt__1nMakeSRGB(JNIEnv* env, jclass jclass) {
    SkColorSpace* ptr = SkColorSpace::MakeSRGB().release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ColorSpaceKt__1nMakeSRGBLinear(JNIEnv* env, jclass jclass) {
    SkColorSpace* ptr = SkColorSpace::MakeSRGBLinear().release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ColorSpaceKt__1nMakeRGB
  (JNIEnv* env, jclass jclass, jfloatArray jtransferFunction, jfloatArray jtoXYZD50) {
    jfloat *tf = env->GetFloatArrayElements(jtransferFunction, 0);
    jfloat *mat = env->GetFloatArrayElements(jtoXYZD50, 0);
    skcms_TransferFunction transferFn = { tf[0], tf[1], tf[2], tf[3], tf[4], tf[5], tf[6] };
    skcms_Matrix3x3 toXYZ = {{{ mat[0], mat[1], mat[2] }, { mat[3], mat[4], mat[5] }, { mat[6], mat[7], mat[8] }}};
    SkColorSpace* ptr = SkColorSpace::MakeRGB(transferFn, toXYZ).release();
    env->ReleaseFloatArrayElements(jtransferFunction, tf, 0);
    env->ReleaseFloatArrayElements(jtoXYZD50, mat, 0);
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_ColorSpaceKt__1nConvert
  (JNIEnv* env, jobject jclass, jlong fromPtr, jlong toPtr, float r, float g, float b, float a, jfloatArray jresult) {
    SkColorSpace* from = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(fromPtr));
    SkColorSpace* to = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(toPtr));

    skcms_TransferFunction fromFn;
    from->transferFn(&fromFn);

    skcms_TransferFunction toFn;
    to->invTransferFn(&toFn);

    float result[4];
    result[0] = skcms_TransferFunction_eval(&toFn, skcms_TransferFunction_eval(&fromFn, r));
    result[1] = skcms_TransferFunction_eval(&toFn, skcms_TransferFunction_eval(&fromFn, g));
    result[2] = skcms_TransferFunction_eval(&toFn, skcms_TransferFunction_eval(&fromFn, b));
    result[3] = skcms_TransferFunction_eval(&toFn, skcms_TransferFunction_eval(&fromFn, a));

    env->SetFloatArrayRegion(jresult, 0, 4, &result[0]);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ColorSpaceKt__1nIsGammaCloseToSRGB
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkColorSpace* instance = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(ptr));
    return instance->gammaCloseToSRGB();
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ColorSpaceKt__1nIsGammaLinear
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkColorSpace* instance = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(ptr));
    return instance->gammaIsLinear();
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ColorSpaceKt__1nIsSRGB
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkColorSpace* instance = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(ptr));
    return instance->isSRGB();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_ColorSpaceKt__1nGetTransferFunction
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray jresult) {
    SkColorSpace* instance = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(ptr));
    skcms_TransferFunction tf;
    instance->transferFn(&tf);
    jfloat array[7] = { tf.g, tf.a, tf.b, tf.c, tf.d, tf.e, tf.f };
    env->SetFloatArrayRegion(jresult, 0, 7, array);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_ColorSpaceKt__1nGetToXYZD50
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray jresult) {
    SkColorSpace* instance = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(ptr));
    skcms_Matrix3x3 toXYZ;
    instance->toXYZD50(&toXYZ);
    jfloat array[9] = {
        toXYZ.vals[0][0], toXYZ.vals[0][1], toXYZ.vals[0][2],
        toXYZ.vals[1][0], toXYZ.vals[1][1], toXYZ.vals[1][2],
        toXYZ.vals[2][0], toXYZ.vals[2][1], toXYZ.vals[2][2]
    };
    env->SetFloatArrayRegion(jresult, 0, 9, array);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ColorSpaceKt__1nEquals
  (JNIEnv* env, jclass jclass, jlong ptr, jlong otherPtr) {
    SkColorSpace* instance = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(ptr));
    SkColorSpace* other = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(otherPtr));
    return SkColorSpace::Equals(instance, other);
}
