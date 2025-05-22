#include <jni.h>
#include "interop.hh"
#include "SkStream.h"

static void deleteDynamicMemoryWStream(SkDynamicMemoryWStream* out) {
    delete out;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_DynamicMemoryWStreamKt_DynamicMemoryWStream_1nGetFinalizer
  (JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteDynamicMemoryWStream));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_DynamicMemoryWStreamKt_DynamicMemoryWStream_1nMake
  (JNIEnv* env, jclass jclass) {
    return reinterpret_cast<jlong>(new SkDynamicMemoryWStream());
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_DynamicMemoryWStreamKt__1nBytesWritten
  (JNIEnv* env, jclass jclass, jlong stream) {
    SkDynamicMemoryWStream* sk_stream = jlongToPtr<SkDynamicMemoryWStream*>(stream);
    size_t result = sk_stream->bytesWritten();
    return static_cast<jlong>(result);
}

extern "C" JNIEXPORT int8_t JNICALL Java_org_jetbrains_skia_DynamicMemoryWStreamKt__1nRead
  (JNIEnv* env, jclass jclass, jlong stream, jbyteArray buffer, jint offset, jint size) {
    SkDynamicMemoryWStream* sk_stream = jlongToPtr<SkDynamicMemoryWStream*>(stream);
    jbyte* rawArray = env->GetByteArrayElements(buffer, 0);
    bool result = sk_stream->read(rawArray, offset, size);
    env->ReleaseByteArrayElements(buffer, rawArray, 0);
    if (result) {
        return true;
    } else {
        return false;
    }
}