#include "common.h"

SKIKO_EXPORT KLong org_jetbrains_skia_StdVectorDecoder__1nGetArraySize
    (KNativePointer ptr) {
        std::vector<KLong>* vect = reinterpret_cast<std::vector<KLong> *>(ptr);
        return static_cast<KLong>(vect->size());
    }

SKIKO_EXPORT void org_jetbrains_skia_StdVectorDecoder__1nDisposeArray
    (KNativePointer ptr) {
        std::vector<KLong>* vect = reinterpret_cast<std::vector<KLong> *>(ptr);
        delete vect;
    }

SKIKO_EXPORT KLong org_jetbrains_skia_StdVectorDecoder__1nGetArrayElement
    (KNativePointer ptr, KInt index) {
        std::vector<KLong>* vect = reinterpret_cast<std::vector<KLong> *>(ptr);
        return vect->at(index);
    }