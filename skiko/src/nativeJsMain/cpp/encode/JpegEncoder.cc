#include "SkData.h"
#include "SkImage.h"
#include "encode/SkJpegEncoder.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_encode_JpegEncoder__1nEncodeJpeg
  (KNativePointer ptr, KInt quality, KInt downsample, KInt alphaOption) {
    SkImage* image = reinterpret_cast<SkImage*>(ptr);
    if (image->isTextureBacked()) return nullptr;
    SkJpegEncoder::Options options;
    options.fQuality = quality;
    switch (downsample) {
      case 0: options.fDownsample = SkJpegEncoder::Downsample::k420; break;
      case 1: options.fDownsample = SkJpegEncoder::Downsample::k422; break;
      case 2: options.fDownsample = SkJpegEncoder::Downsample::k444; break;
      default: return nullptr;
    }
    switch (alphaOption) {
      case 0: options.fAlphaOption = SkJpegEncoder::AlphaOption::kIgnore; break;
      case 1: options.fAlphaOption = SkJpegEncoder::AlphaOption::kBlendOnBlack; break;
      default: return nullptr;
    }
    return reinterpret_cast<KNativePointer>(SkJpegEncoder::Encode(nullptr, image, options).release());
}
