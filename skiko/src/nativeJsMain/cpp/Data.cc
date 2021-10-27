
// This file has been auto generated.

#include "SkData.h"
#include "common.h"

static void deleteData(SkData* data) {
    // std::cout << "Deleting [SkData " << data << "]" << std::endl;
    data->unref();
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Data__1nGetFinalizer(){
    return reinterpret_cast<KNativePointer>((&deleteData));
}

SKIKO_EXPORT KInt org_jetbrains_skia_Data__1nSize
  (KNativePointer ptr) {
    SkData* instance = reinterpret_cast<SkData*>((ptr));
    return instance->size();
}

SKIKO_EXPORT void org_jetbrains_skia_Data__1nBytes
  (KNativePointer ptr, int offset, int length, KByte* destBytes) {
    SkData* instance = reinterpret_cast<SkData*>(ptr);
    const KByte* source = reinterpret_cast<const KByte*>(instance->bytes() + offset);
    memcpy(destBytes, source, length);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Data__1nEquals
  (KNativePointer ptr, KNativePointer otherPtr) {
    SkData* instance = reinterpret_cast<SkData*>((ptr));
    SkData* other = reinterpret_cast<SkData*>((otherPtr));
    return instance->equals(other);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Data__1nMakeFromBytes
  (KByte* bytesArray, KInt offset, KInt length) {
    KByte* bytes = reinterpret_cast<KByte*>(malloc(length));
    if (!bytes) return 0;
    memcpy(bytes, bytesArray + offset, length);
    SkData* instance = SkData::MakeFromMalloc(bytes, length).release();
    return instance;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Data__1nMakeWithoutCopy
    (KNativePointer memoryAddr, KInt length) {

    SkData* instance = SkData::MakeWithoutCopy(reinterpret_cast<void*>(memoryAddr), length).release();
    return reinterpret_cast<KNativePointer>(instance);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Data__1nMakeFromFileName
  (KInteropPointer pathStr) {
    SkString path = skString(pathStr);
    sk_sp<SkData> instance = SkData::MakeFromFileName(path.c_str());
    SkData* ptr = instance.release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Data__1nMakeSubset
  (KNativePointer ptr, KInt offset, KInt length) {
    SkData* instance = reinterpret_cast<SkData*>((ptr));
    SkData* subset = SkData::MakeSubset(instance, offset, length).release();
    return reinterpret_cast<KNativePointer>(subset);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Data__1nMakeEmpty() {
    SkData* instance = SkData::MakeEmpty().release();
    return reinterpret_cast<KNativePointer>(instance);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Data__1nMakeUninitialized(KInt length) {
    SkData* instance = SkData::MakeUninitialized(length).release();
    return reinterpret_cast<KNativePointer>(instance);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Data__1nWritableData
  (KNativePointer ptr) {
    SkData* instance = reinterpret_cast<SkData*>(ptr);
    return reinterpret_cast<KNativePointer>(instance->writable_data());
}
