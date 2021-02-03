#pragma once

template<typename T>
T fromJavaPointer(jlong ptr) { return reinterpret_cast<T>(static_cast<uintptr_t>(ptr)); }

template<typename T>
jlong toJavaPointer(T ptr) { return static_cast<jlong>(reinterpret_cast<uintptr_t>(ptr)); }