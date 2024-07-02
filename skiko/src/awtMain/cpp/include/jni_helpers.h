#pragma once

#include <cstdint>
#include <string>
#include <jni.h>

template<typename T>
T inline fromJavaPointer(jlong ptr) { return reinterpret_cast<T>(static_cast<uintptr_t>(ptr)); }

template<typename T>
jlong inline toJavaPointer(T ptr) { return static_cast<jlong>(reinterpret_cast<uintptr_t>(ptr)); }

std::wstring inline toStdString(JNIEnv *env, jstring jstr) {
    const jchar* jchars = env->GetStringCritical(jstr, NULL);
    std::wstring wstr = reinterpret_cast<const wchar_t*>(jchars);
    env->ReleaseStringCritical(jstr, jchars);
    return wstr;
}
