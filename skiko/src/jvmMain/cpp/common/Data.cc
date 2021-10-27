#include <jni.h>
#include "interop.hh"
#include "SkData.h"

static void deleteData(SkData* data) {
    // std::cout << "Deleting [SkData " << data << "]" << std::endl;
    data->unref();
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_DataKt_Data_1nGetFinalizer(JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteData));
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_DataKt__1nSize
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkData* instance = reinterpret_cast<SkData*>(static_cast<uintptr_t>(ptr));
    return instance->size();
}

extern "C" JNIEXPORT jobject JNICALL Java_org_jetbrains_skia_DataKt__1nToByteBuffer
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkData* instance = reinterpret_cast<SkData*>(static_cast<uintptr_t>(ptr));
    return env->NewDirectByteBuffer(instance->writable_data(), instance->size());
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_DataKt__1nBytes
  (JNIEnv* env, jclass jclass, jlong ptr, jint offset, jint length, jbyteArray destBytes) {
    SkData* instance = reinterpret_cast<SkData*>(static_cast<uintptr_t>(ptr));
    const jbyte* bytes = reinterpret_cast<const jbyte*>(instance->bytes() + offset);
    env->SetByteArrayRegion(destBytes, 0, (jsize) length, bytes);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_DataKt__1nEquals
  (JNIEnv* env, jclass jclass, jlong ptr, jlong otherPtr) {
    SkData* instance = reinterpret_cast<SkData*>(static_cast<uintptr_t>(ptr));
    SkData* other = reinterpret_cast<SkData*>(static_cast<uintptr_t>(otherPtr));
    return instance->equals(other);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_DataKt__1nMakeFromBytes
  (JNIEnv* env, jclass jclass, jbyteArray bytesArray, jint offset, jint length) {
    jbyte* bytes = reinterpret_cast<jbyte*>(malloc(length));
    if (!bytes) return 0;
    env->GetByteArrayRegion(bytesArray, (jsize) offset, (jsize) length, bytes);
    SkData* instance = SkData::MakeFromMalloc(bytes, length).release();
    return reinterpret_cast<jlong>(instance);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_DataKt__1nMakeWithoutCopy
    (JNIEnv* env, jclass jclass, jlong memoryAddr, jint length) {

    SkData* instance = SkData::MakeWithoutCopy(reinterpret_cast<void*>(memoryAddr), length).release();
    return reinterpret_cast<jlong>(instance);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_DataKt__1nMakeFromFileName
  (JNIEnv* env, jclass jclass, jstring pathStr) {
    SkString path = skString(env, pathStr);
    SkData* instance = SkData::MakeFromFileName(path.c_str()).release();
    return reinterpret_cast<jlong>(instance);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_DataKt__1nMakeSubset
  (JNIEnv* env, jclass jclass, jlong ptr, jint offset, jint length) {
    SkData* instance = reinterpret_cast<SkData*>(static_cast<uintptr_t>(ptr));
    SkData* subset = SkData::MakeSubset(instance, offset, length).release();
    return reinterpret_cast<jlong>(subset);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_DataKt__1nMakeEmpty
  (JNIEnv* env, jclass jclass) {
    SkData* instance = SkData::MakeEmpty().release();
    return reinterpret_cast<jlong>(instance);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_DataKt__1nMakeUninitialized
  (JNIEnv* env, jclass jclass, jint length) {
    SkData* instance = SkData::MakeUninitialized(length).release();
    return reinterpret_cast<jlong>(instance);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_DataKt__1nWritableData
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkData* instance = reinterpret_cast<SkData*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(instance->writable_data());
}
