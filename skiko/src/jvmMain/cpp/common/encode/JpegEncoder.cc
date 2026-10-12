#include <cstdint>
#include <jni.h>
#include "SkData.h"
#include "SkImage.h"
#include "encode/SkJpegEncoder.h"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_encode_JpegEncoderKt__1nEncodeJpeg
  (JNIEnv*, jclass, jlong ptr, jint quality, jint downsample, jint alphaOption) {
    SkImage* image = reinterpret_cast<SkImage*>(static_cast<uintptr_t>(ptr));
    if (image->isTextureBacked()) return 0;
    SkJpegEncoder::Options options;
    options.fQuality = quality;
    switch (downsample) {
      case 0: options.fDownsample = SkJpegEncoder::Downsample::k420; break;
      case 1: options.fDownsample = SkJpegEncoder::Downsample::k422; break;
      case 2: options.fDownsample = SkJpegEncoder::Downsample::k444; break;
      default: return 0;
    }
    switch (alphaOption) {
      case 0: options.fAlphaOption = SkJpegEncoder::AlphaOption::kIgnore; break;
      case 1: options.fAlphaOption = SkJpegEncoder::AlphaOption::kBlendOnBlack; break;
      default: return 0;
    }
    return reinterpret_cast<jlong>(SkJpegEncoder::Encode(nullptr, image, options).release());
}
