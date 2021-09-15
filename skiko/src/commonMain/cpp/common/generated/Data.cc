
// This file has been auto generated.

#include "SkData.h"
#include "common.h"

static void deleteData(SkData* data) {
    // std::cout << "Deleting [SkData " << data << "]" << std::endl;
    data->unref();
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Data__1nGetFinalizer(KInteropPointer __Kinstance) {
    return reinterpret_cast<KNativePointer>((&deleteData));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Data__1nSize
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkData* instance = reinterpret_cast<SkData*>((ptr));
    return instance->size();
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Data__1nToByteBuffer
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Data__1nToByteBuffer");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Data__1nToByteBuffer
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkData* instance = reinterpret_cast<SkData*>((ptr));
    return env->NewDirectByteBuffer(instance->writable_data(), instance->size());
}
#endif



SKIKO_EXPORT jbyteArray org_jetbrains_skia_Data__1nBytes
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer offset, KNativePointer length) {
    TODO("implement org_jetbrains_skia_Data__1nBytes");
}
     
#if 0 
SKIKO_EXPORT jbyteArray org_jetbrains_skia_Data__1nBytes
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer offset, KNativePointer length) {
    SkData* instance = reinterpret_cast<SkData*>((ptr));
    jbyteArray bytesArray = env->NewByteArray((jsize) length);
    const jbyte* bytes = reinterpret_cast<const jbyte*>(instance->bytes() + offset);
    env->SetByteArrayRegion(bytesArray, 0, (jsize) length, bytes);
    return bytesArray;
}
#endif


SKIKO_EXPORT KBoolean org_jetbrains_skia_Data__1nEquals
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer otherPtr) {
    SkData* instance = reinterpret_cast<SkData*>((ptr));
    SkData* other = reinterpret_cast<SkData*>((otherPtr));
    return instance->equals(other);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Data__1nMakeFromBytes
  (KInteropPointer __Kinstance, jbyteArray bytesArray, KNativePointer offset, KNativePointer length) {
    TODO("implement org_jetbrains_skia_Data__1nMakeFromBytes");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Data__1nMakeFromBytes
  (KInteropPointer __Kinstance, jbyteArray bytesArray, KNativePointer offset, KNativePointer length) {
    jbyte* bytes = reinterpret_cast<jbyte*>(malloc(length));
    if (!bytes) return 0;
    env->GetByteArrayRegion(bytesArray, (jsize) offset, (jsize) length, bytes);
    SkData* instance = SkData::MakeFromMalloc(bytes, length).release();
    return reinterpret_cast<KNativePointer>(instance);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_Data__1nMakeFromFileName
  (KInteropPointer __Kinstance, KInteropPointer pathStr) {
    TODO("implement org_jetbrains_skia_Data__1nMakeFromFileName");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Data__1nMakeFromFileName
  (KInteropPointer __Kinstance, KInteropPointer pathStr) {
    SkString path = skString(env, pathStr);
    SkData* instance = SkData::MakeFromFileName(path.c_str()).release();
    return reinterpret_cast<KNativePointer>(instance);
}
#endif


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Data__1nMakeSubset
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer offset, KNativePointer length) {
    SkData* instance = reinterpret_cast<SkData*>((ptr));
    SkData* subset = SkData::MakeSubset(instance, offset, length).release();
    return reinterpret_cast<KNativePointer>(subset);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Data__1nMakeEmpty
  (KInteropPointer __Kinstance) {
    SkData* instance = SkData::MakeEmpty().release();
    return reinterpret_cast<KNativePointer>(instance);
}
