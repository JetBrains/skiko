#pragma once

#include <jni.h>
#include <sstream>
#include <stdexcept>

template<typename T>
T inline fromJavaPointer(jlong ptr) { return reinterpret_cast<T>(static_cast<uintptr_t>(ptr)); }

template<typename T>
jlong inline toJavaPointer(T ptr) { return static_cast<jlong>(reinterpret_cast<uintptr_t>(ptr)); }

std::string handleException(std::string function);

void logJavaException(JNIEnv *env, std::string message);