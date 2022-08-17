#if SK_BUILD_FOR_WIN

#include <jni.h>
#include <windows.h>
#include <stdio.h>

void logJavaException(JNIEnv *env, const char * function, DWORD sehCode);

#endif