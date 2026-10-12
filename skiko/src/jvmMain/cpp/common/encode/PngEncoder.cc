#include <cstdint>
#include <jni.h>
#include "SkData.h"
#include "SkImage.h"
#include "encode/SkPngEncoder.h"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_encode_PngEncoderKt__1nEncodePng
  (JNIEnv*, jclass, jlong ptr, jint compressionLevel, jint filterFlags) {
    SkImage* image = reinterpret_cast<SkImage*>(static_cast<uintptr_t>(ptr));
    if (image->isTextureBacked()) return 0;
    SkPngEncoder::Options options;
    options.fZLibLevel = compressionLevel;
    options.fFilterFlags = static_cast<SkPngEncoder::FilterFlag>(filterFlags);
    return reinterpret_cast<jlong>(SkPngEncoder::Encode(nullptr, image, options).release());
}
