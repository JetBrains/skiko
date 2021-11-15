#include <jni.h>
#include "interop.hh"

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_StdVectorDecoderKt_StdVectorDecoder_1nGetArraySize
    (JNIEnv* env, jclass jclass, jlong ptr) {
        std::vector<void*>* vec = reinterpret_cast<std::vector<void*> *>(ptr);
        return static_cast<jint>(vec->size());
    }

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_StdVectorDecoderKt_StdVectorDecoder_1nReleaseElement
    (JNIEnv* env, jclass jclass, jlong ptr, jint index) {
        std::vector<void*>* vec = reinterpret_cast<std::vector<void*> *>(ptr);
        auto res = (*vec)[index];
        (*vec)[index] = nullptr;
        return reinterpret_cast<jlong>(res);
    }

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_StdVectorDecoderKt_StdVectorDecoder_1nDisposeArray
    (JNIEnv* env, jclass jclass, jlong ptr, jlong disposePtr) {
        std::vector<void*>* vec = reinterpret_cast<std::vector<void*> *>(ptr);

        void (*dctr)(void*) = reinterpret_cast<void (*)(void*)>(disposePtr);
        while (!vec->empty()){
            auto res = vec->back();
            if (res != nullptr) {
                dctr(res);
            }

            vec->pop_back();
        }

        delete vec;
    }