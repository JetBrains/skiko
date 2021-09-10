
// This file has been auto generated.

#include "SkData.h"
#include "common.h"

static void deleteData(SkData* data) {
    // std::cout << "Deleting [SkData " << data << "]" << std::endl;
    data->unref();
}

extern "C" jlong org_jetbrains_skia_Data__1nGetFinalizer(kref __Kinstance) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteData));
}

extern "C" jlong org_jetbrains_skia_Data__1nSize
  (kref __Kinstance, jlong ptr) {
    SkData* instance = reinterpret_cast<SkData*>(static_cast<uintptr_t>(ptr));
    return instance->size();
}


extern "C" jobject org_jetbrains_skia_Data__1nToByteBuffer
  (kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_Data__1nToByteBuffer");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Data__1nToByteBuffer
  (kref __Kinstance, jlong ptr) {
    SkData* instance = reinterpret_cast<SkData*>(static_cast<uintptr_t>(ptr));
    return env->NewDirectByteBuffer(instance->writable_data(), instance->size());
}
#endif



extern "C" jbyteArray org_jetbrains_skia_Data__1nBytes
  (kref __Kinstance, jlong ptr, jlong offset, jlong length) {
    TODO("implement org_jetbrains_skia_Data__1nBytes");
}
     
#if 0 
extern "C" jbyteArray org_jetbrains_skia_Data__1nBytes
  (kref __Kinstance, jlong ptr, jlong offset, jlong length) {
    SkData* instance = reinterpret_cast<SkData*>(static_cast<uintptr_t>(ptr));
    jbyteArray bytesArray = env->NewByteArray((jsize) length);
    const jbyte* bytes = reinterpret_cast<const jbyte*>(instance->bytes() + offset);
    env->SetByteArrayRegion(bytesArray, 0, (jsize) length, bytes);
    return bytesArray;
}
#endif


extern "C" jboolean org_jetbrains_skia_Data__1nEquals
  (kref __Kinstance, jlong ptr, jlong otherPtr) {
    SkData* instance = reinterpret_cast<SkData*>(static_cast<uintptr_t>(ptr));
    SkData* other = reinterpret_cast<SkData*>(static_cast<uintptr_t>(otherPtr));
    return instance->equals(other);
}


extern "C" jlong org_jetbrains_skia_Data__1nMakeFromBytes
  (kref __Kinstance, jbyteArray bytesArray, jlong offset, jlong length) {
    TODO("implement org_jetbrains_skia_Data__1nMakeFromBytes");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Data__1nMakeFromBytes
  (kref __Kinstance, jbyteArray bytesArray, jlong offset, jlong length) {
    jbyte* bytes = reinterpret_cast<jbyte*>(malloc(length));
    if (!bytes) return 0;
    env->GetByteArrayRegion(bytesArray, (jsize) offset, (jsize) length, bytes);
    SkData* instance = SkData::MakeFromMalloc(bytes, length).release();
    return reinterpret_cast<jlong>(instance);
}
#endif



extern "C" jlong org_jetbrains_skia_Data__1nMakeFromFileName
  (kref __Kinstance, jstring pathStr) {
    TODO("implement org_jetbrains_skia_Data__1nMakeFromFileName");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Data__1nMakeFromFileName
  (kref __Kinstance, jstring pathStr) {
    SkString path = skString(env, pathStr);
    SkData* instance = SkData::MakeFromFileName(path.c_str()).release();
    return reinterpret_cast<jlong>(instance);
}
#endif


extern "C" jlong org_jetbrains_skia_Data__1nMakeSubset
  (kref __Kinstance, jlong ptr, jlong offset, jlong length) {
    SkData* instance = reinterpret_cast<SkData*>(static_cast<uintptr_t>(ptr));
    SkData* subset = SkData::MakeSubset(instance, offset, length).release();
    return reinterpret_cast<jlong>(subset);
}

extern "C" jlong org_jetbrains_skia_Data__1nMakeEmpty
  (kref __Kinstance) {
    SkData* instance = SkData::MakeEmpty().release();
    return reinterpret_cast<jlong>(instance);
}
