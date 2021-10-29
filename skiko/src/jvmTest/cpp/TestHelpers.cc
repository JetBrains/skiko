#include <jni.h>

#include "SkString.h"

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_tests_TestHelpersKt__1nFillByteArrayOf5
(JNIEnv* env, jclass jclass, jbyteArray jbarray) {
    jbyte *result_bytes = env->GetByteArrayElements(jbarray, NULL);
    result_bytes[0] = 1;
    result_bytes[1] = 2;
    result_bytes[2] = 3;
    result_bytes[3] = 4;
    result_bytes[4] = 5;
    env->ReleaseByteArrayElements(jbarray, result_bytes, 0);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_tests_TestHelpersKt__1nFillFloatArrayOf5
(JNIEnv* env, jclass jclass, jfloatArray jfarray) {
    jfloat *result_float = env->GetFloatArrayElements(jfarray, NULL);
    result_float[0] = 0.0;
    result_float[1] = 1.1;
    result_float[2] = 2.2;
    result_float[3] = 3.3;
    result_float[4] = -4.4;
    env->ReleaseFloatArrayElements(jfarray, result_float, 0);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_tests_TestHelpersKt__1nFillShortArrayOf5
(JNIEnv* env, jclass jclass, jshortArray jsarray) {
    jshort *result_short = env->GetShortArrayElements(jsarray, NULL);
    result_short[0] = 0;
    result_short[1] = 1;
    result_short[2] = 2;
    result_short[3] = -3;
    result_short[4] = 4;
    env->ReleaseShortArrayElements(jsarray, result_short, 0);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_tests_TestHelpersKt__1nFillIntArrayOf5
(JNIEnv* env, jclass jclass, jintArray jiarray) {
    jint *result_int = env->GetIntArrayElements(jiarray, NULL);
    result_int[0] = 0;
    result_int[1] = 1;
    result_int[2] = -22;
    result_int[3] = 3;
    result_int[4] = 4;
    env->ReleaseIntArrayElements(jiarray, result_int, 0);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_tests_TestHelpersKt__1nFillDoubleArrayOf5
(JNIEnv* env, jclass jclass, jdoubleArray jdarray) {
    jdouble *result_double = env->GetDoubleArrayElements(jdarray, NULL);
    result_double[0] = -0.001;
    result_double[1] = 0.00222;
    result_double[2] = 2.71828;
    result_double[3] = 3.1415;
    result_double[4] = 10000000.9991;
    env->ReleaseDoubleArrayElements(jdarray, result_double, 0);
}

static inline jlong ptrToJlong(void* ptr) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(ptr));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_tests_TestHelpersKt__1nStringByIndex
(JNIEnv* env, jclass jclass, jint index) {
    switch (index) {
        case 0: return ptrToJlong(new SkString("Hello"));
        case 1: return ptrToJlong(new SkString("Привет"));
        case 2: return ptrToJlong(new SkString("你好"));
        default: assert(false);
    }
}

