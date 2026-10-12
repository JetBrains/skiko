#include "SkData.h"
#include "SkImage.h"
#include "encode/SkWebpEncoder.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_encode_WebpEncoder__1nEncodeWebp
  (KNativePointer ptr, KInt compression, KFloat quality) {
    SkImage* image = reinterpret_cast<SkImage*>(ptr);
    if (image->isTextureBacked()) return nullptr;
    SkWebpEncoder::Options options;
    switch (compression) {
      case 0: options.fCompression = SkWebpEncoder::Compression::kLossy; break;
      case 1: options.fCompression = SkWebpEncoder::Compression::kLossless; break;
      default: return nullptr;
    }
    options.fQuality = quality;
    return reinterpret_cast<KNativePointer>(SkWebpEncoder::Encode(nullptr, image, options).release());
}
