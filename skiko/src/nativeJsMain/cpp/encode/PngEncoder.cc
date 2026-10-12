#include "SkData.h"
#include "SkImage.h"
#include "encode/SkPngEncoder.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_encode_PngEncoder__1nEncodePng
  (KNativePointer ptr, KInt compressionLevel, KInt filterFlags) {
    SkImage* image = reinterpret_cast<SkImage*>(ptr);
    if (image->isTextureBacked()) return nullptr;
    SkPngEncoder::Options options;
    options.fZLibLevel = compressionLevel;
    options.fFilterFlags = static_cast<SkPngEncoder::FilterFlag>(filterFlags);
    return reinterpret_cast<KNativePointer>(SkPngEncoder::Encode(nullptr, image, options).release());
}
