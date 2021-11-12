#include "common.h"

SKIKO_EXPORT KInt org_jetbrains_skia_StdVectorDecoder__1nGetArraySize
    (KNativePointer ptr) {
        std::vector<KNativePointer>* vec = reinterpret_cast<std::vector<KNativePointer> *>(ptr);
        return vec->size();
    }

SKIKO_EXPORT void org_jetbrains_skia_StdVectorDecoder__1nDisposeArray
    (KNativePointer ptr) {
        std::vector<KNativePointer>* vec = reinterpret_cast<std::vector<KNativePointer> *>(ptr);
        delete vec;
    }

SKIKO_EXPORT KNativePointer org_jetbrains_skia_StdVectorDecoder__1nGetArrayElement
    (KNativePointer ptr, KInt index) {
        std::vector<KNativePointer>* vec = reinterpret_cast<std::vector<KNativePointer> *>(ptr);
        return vec->at(index);
    }