#if SK_BUILD_FOR_WIN

#include <jni.h>
#include <windows.h>
#include <stdio.h>

void throwJavaRenderException(JNIEnv *env, const char * function, DWORD sehCode);

#endif