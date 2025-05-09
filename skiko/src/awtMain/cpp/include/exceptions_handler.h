#if SK_BUILD_FOR_WIN

#include <jni.h>
#include <windows.h>
#include <stdio.h>

void throwJavaRenderExceptionByExceptionCode(JNIEnv *env, const char * function, DWORD code);
void logJava(JNIEnv *env, const char * msg);
void logJava(JNIEnv *env, jlong msg);
void throwJavaRenderExceptionByErrorCode(JNIEnv *env, const char * function, DWORD code);

#endif