#include "common.h"

SKIKO_EXPORT KInt org_jetbrains_skia_StdVectorDecoder__1nGetArraySize
    (KNativePointer ptr) {
        std::vector<KNativePointer>* vect = reinterpret_cast<std::vector<KNativePointer> *>(ptr);
        return vect->size();
    }

SKIKO_EXPORT void org_jetbrains_skia_StdVectorDecoder__1nDisposeArray
    (KNativePointer ptr) {
        std::vector<KLong>* vect = reinterpret_cast<std::vector<KLong> *>(ptr);
        delete vect;
    }

SKIKO_EXPORT KNativePointer org_jetbrains_skia_StdVectorDecoder__1nGetArrayElement
    (KNativePointer ptr, KInt index) {
        std::vector<KNativePointer>* vect = reinterpret_cast<std::vector<KNativePointer> *>(ptr);
        return vect->at(index);
    }