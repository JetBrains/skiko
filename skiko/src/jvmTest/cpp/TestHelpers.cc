#include <jni.h>

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
