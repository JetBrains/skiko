#include "common.h"
#include "SkStream.h"

static void deleteDynamicMemoryWStream(SkDynamicMemoryWStream* out) {
    delete out;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_DynamicMemoryWStream__1nGetFinalizer
  () {
    return reinterpret_cast<KNativePointer>((&deleteDynamicMemoryWStream));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_DynamicMemoryWStream__1nMake
  () {
    return reinterpret_cast<KNativePointer>(new SkDynamicMemoryWStream());
}

SKIKO_EXPORT KInt org_jetbrains_skia_DynamicMemoryWStream__1nBytesWritten
  (KNativePointer stream) {
    SkDynamicMemoryWStream* sk_stream = reinterpret_cast<SkDynamicMemoryWStream*>(stream);
    size_t result = sk_stream->bytesWritten();
    return static_cast<KInt>(result);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_DynamicMemoryWStream__1nRead
  (KNativePointer stream, KByte* buffer, KInt offset, KInt size) {
    SkDynamicMemoryWStream* sk_stream = reinterpret_cast<SkDynamicMemoryWStream*>(stream);
    return sk_stream->read(buffer, offset, size);
}