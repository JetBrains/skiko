#include <jni.h>
#include "interop.hh"


extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_StdVectorDecoderKt_StdVectorDecoder_1nGetArraySize
    (JNIEnv* env, jclass jclass, jlong ptr) {
        std::vector<jlong>* vect = reinterpret_cast<std::vector<jlong> *>(ptr);
        return static_cast<jint>(vect->size());
    }

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_StdVectorDecoderKt_StdVectorDecoder_1nDisposeArray
    (JNIEnv* env, jclass jclass, jlong ptr) {
        std::vector<jlong>* vect = reinterpret_cast<std::vector<jlong> *>(ptr);
        delete vect;
    }

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_StdVectorDecoderKt_StdVectorDecoder_1nGetArrayElement
    (JNIEnv* env, jclass jclass, jlong ptr, jint index) {
        std::vector<jlong>* vect = reinterpret_cast<std::vector<jlong> *>(ptr);
        return vect->at(index);
    }