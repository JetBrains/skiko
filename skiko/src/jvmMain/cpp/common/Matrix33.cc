#include <iostream>
#include <jni.h>
#include "SkColorSpace.h"
#include "interop.hh"

static void copySkcmsMatrix3x3ToJFloatArray(JNIEnv* env, const skcms_Matrix3x3& matrix, jfloatArray& jresult) {
    jfloat array[9] = {
        matrix.vals[0][0], matrix.vals[0][1], matrix.vals[0][2],
        matrix.vals[1][0], matrix.vals[1][1], matrix.vals[1][2],
        matrix.vals[2][0], matrix.vals[2][1], matrix.vals[2][2]
    };
    env->SetFloatArrayRegion(jresult, 0, 9, array);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_Matrix33Kt__1nGetSRGB
  (JNIEnv* env, jclass jclass, jfloatArray jresult) {
    copySkcmsMatrix3x3ToJFloatArray(env, SkNamedGamut::kSRGB, jresult);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_Matrix33Kt__1nGetAdobeRGB
  (JNIEnv* env, jclass jclass, jfloatArray jresult) {
    copySkcmsMatrix3x3ToJFloatArray(env, SkNamedGamut::kAdobeRGB, jresult);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_Matrix33Kt__1nGetDisplayP3
  (JNIEnv* env, jclass jclass, jfloatArray jresult) {
    copySkcmsMatrix3x3ToJFloatArray(env, SkNamedGamut::kDisplayP3, jresult);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_Matrix33Kt__1nGetRec2020
  (JNIEnv* env, jclass jclass, jfloatArray jresult) {
    copySkcmsMatrix3x3ToJFloatArray(env, SkNamedGamut::kRec2020, jresult);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_Matrix33Kt__1nGetXYZ
  (JNIEnv* env, jclass jclass, jfloatArray jresult) {
    copySkcmsMatrix3x3ToJFloatArray(env, SkNamedGamut::kXYZ, jresult);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_Matrix33Kt__1nAdaptToXYZD50
  (JNIEnv* env, jclass jclass, jfloat wx, jfloat wy, jfloatArray jresult) {
    skcms_Matrix3x3 toXYZD50;
    bool success = skcms_AdaptToXYZD50(wx, wy, &toXYZD50);
    if (success)
        copySkcmsMatrix3x3ToJFloatArray(env, toXYZD50, jresult);
    return success;
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_Matrix33Kt__1nPrimariesToXYZD50
  (JNIEnv* env, jclass jclass, jfloat rx, jfloat ry, jfloat gx, jfloat gy, jfloat bx, jfloat by, jfloat wx, jfloat wy, jfloatArray jresult) {
    skcms_Matrix3x3 toXYZD50;
    bool success = skcms_PrimariesToXYZD50(rx, ry, gx, gy, bx, by, wx, wy, &toXYZD50);
    if (success)
        copySkcmsMatrix3x3ToJFloatArray(env, toXYZD50, jresult);
    return success;
}
