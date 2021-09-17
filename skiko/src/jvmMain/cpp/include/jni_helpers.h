#pragma once

#include <jni.h>

#if SK_BUILD_FOR_WIN
#include <windows.h>
#include <sstream>
void logJavaException(JNIEnv *env, const char * function, DWORD sehCode);
#endif

template<typename T>
T inline fromJavaPointer(jlong ptr) { return reinterpret_cast<T>(static_cast<uintptr_t>(ptr)); }

template<typename T>
jlong inline toJavaPointer(T ptr) { return static_cast<jlong>(reinterpret_cast<uintptr_t>(ptr)); }
