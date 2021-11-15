#include "common.h"

SKIKO_EXPORT KInt org_jetbrains_skia_StdVectorDecoder__1nGetArraySize
    (KNativePointer ptr) {
        std::vector<KNativePointer>* vec = reinterpret_cast<std::vector<KNativePointer> *>(ptr);
        return vec->size();
    }

SKIKO_EXPORT KNativePointer org_jetbrains_skia_StdVectorDecoder__1nReleaseElement
    (KNativePointer ptr, KInt index) {
        std::vector<KNativePointer>* vec = reinterpret_cast<std::vector<KNativePointer> *>(ptr);
        auto res = (*vec)[index];
        (*vec)[index] = 0;
        return res;
    }

SKIKO_EXPORT void org_jetbrains_skia_StdVectorDecoder__1nDisposeArray
    (KNativePointer ptr) {
        std::vector<KNativePointer>* vec = reinterpret_cast<std::vector<KNativePointer> *>(ptr);

        void (*dctr)(SkString*) = reinterpret_cast<void (*)(SkString*)>(vec->back());
        vec->pop_back();

        while (!vec->empty()){
            SkString* res = reinterpret_cast<SkString*>(vec->back());
            if (res != nullptr) {
                dctr(res);
            }

            vec->pop_back();
        }

        delete vec;
    }
