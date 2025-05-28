#if SK_BUILD_FOR_WIN

#include <jni.h>
#include <windows.h>
#include <stdio.h>

void throwJavaRenderExceptionByExceptionCode(JNIEnv *env, const char * function, DWORD code);
void throwJavaRenderExceptionByErrorCode(JNIEnv *env, const char * function, DWORD code);
void throwJavaRenderExceptionWithMessage(JNIEnv *env, const char * function, const char * msg);

#endif