#include <jni.h>
#include "interop.hh"
#include <iostream>

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_StdVectorDecoderKt_StdVectorDecoder_1nGetArraySize
    (JNIEnv* env, jclass jclass, jlong ptr) {
        std::vector<jlong>* vec = reinterpret_cast<std::vector<jlong> *>(ptr);
        return static_cast<jint>(vec->size());
    }

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_StdVectorDecoderKt_StdVectorDecoder_1nReleaseElement
    (JNIEnv* env, jclass jclass, jlong ptr, jint index) {
        std::vector<jlong>* vec = reinterpret_cast<std::vector<jlong> *>(ptr);
        auto res = (*vec)[index];
        (*vec)[index] = 0;
        return res;
    }

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_StdVectorDecoderKt_StdVectorDecoder_1nDisposeArray
    (JNIEnv* env, jclass jclass, jlong ptr, jlong disposePtr) {
        std::vector<jlong>* vec = reinterpret_cast<std::vector<jlong> *>(ptr);

        void (*dctr)(SkString*) = reinterpret_cast<void (*)(SkString*)>(disposePtr);
        while (!vec->empty()){
            SkString* res = reinterpret_cast<SkString*>(vec->back());
            if (res != nullptr) {
                dctr(res);
            }

            vec->pop_back();
        }

        delete vec;
    }