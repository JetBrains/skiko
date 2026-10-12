#include <cstdint>
#include <jni.h>
#include "SkData.h"
#include "SkImage.h"
#include "encode/SkWebpEncoder.h"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_encode_WebpEncoderKt__1nEncodeWebp
  (JNIEnv*, jclass, jlong ptr, jint compression, jfloat quality) {
    SkImage* image = reinterpret_cast<SkImage*>(static_cast<uintptr_t>(ptr));
    if (image->isTextureBacked()) return 0;
    SkWebpEncoder::Options options;
    switch (compression) {
      case 0: options.fCompression = SkWebpEncoder::Compression::kLossy; break;
      case 1: options.fCompression = SkWebpEncoder::Compression::kLossless; break;
      default: return 0;
    }
    options.fQuality = quality;
    return reinterpret_cast<jlong>(SkWebpEncoder::Encode(nullptr, image, options).release());
}
