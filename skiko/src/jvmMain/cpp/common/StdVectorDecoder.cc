#include <jni.h>
#include "interop.hh"

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_StdVectorDecoderKt_StdVectorDecoder_1nGetArraySize
    (JNIEnv* env, jclass jclass, jlong ptr) {
        std::vector<jlong>* vec = reinterpret_cast<std::vector<jlong> *>(ptr);
        return static_cast<jint>(vec->size());
    }

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_StdVectorDecoderKt_StdVectorDecoder_1nDisposeArray
    (JNIEnv* env, jclass jclass, jlong ptr) {
        std::vector<jlong>* vec = reinterpret_cast<std::vector<jlong> *>(ptr);
        delete vec;
    }

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_StdVectorDecoderKt_StdVectorDecoder_1nGetArrayElement
    (JNIEnv* env, jclass jclass, jlong ptr, jint index) {
        std::vector<jlong>* vec = reinterpret_cast<std::vector<jlong> *>(ptr);
        return vec->at(index);
    }