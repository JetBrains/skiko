#include <iostream>
#include "SkBitmap.h"
#include "SkCodec.h"
#include "SkData.h"
#include "common.h"

static void deleteCodec(SkCodec* instance) {
    delete instance;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Codec__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>(&deleteCodec);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Codec__1nMakeFromData
  (KNativePointer dataPtr) {
    SkData* data = reinterpret_cast<SkData*>((dataPtr));
    std::unique_ptr<SkCodec> instance = SkCodec::MakeFromData(sk_ref_sp(data));
    return reinterpret_cast<KNativePointer>(instance.release());
}

SKIKO_EXPORT void org_jetbrains_skia_Codec__1nGetImageInfo
  (KNativePointer ptr, KInt* imageInfoResult, KNativePointer* colorSpacePtrsArray) {
    auto instance = reinterpret_cast<SkCodec*>(ptr);
    SkImageInfo imageInfo = instance->getInfo();
    skija::ImageInfo::writeImageInfoForInterop(imageInfo, imageInfoResult, colorSpacePtrsArray);
}

SKIKO_EXPORT KInt org_jetbrains_skia_Codec__1nGetSize
  (KNativePointer ptr) {
    SkCodec* instance = reinterpret_cast<SkCodec*>((ptr));
    return packISize(instance->dimensions());
}

SKIKO_EXPORT KInt org_jetbrains_skia_Codec__1nGetEncodedOrigin
  (KNativePointer ptr) {
    SkCodec* instance = reinterpret_cast<SkCodec*>((ptr));
    return static_cast<KInt>(instance->getOrigin());
}

SKIKO_EXPORT KInt org_jetbrains_skia_Codec__1nGetEncodedImageFormat
  (KNativePointer ptr) {
    SkCodec* instance = reinterpret_cast<SkCodec*>((ptr));
    return static_cast<KInt>(instance->getEncodedFormat());
}

SKIKO_EXPORT KInt org_jetbrains_skia_Codec__1nReadPixels
  (KNativePointer ptr, KNativePointer bitmapPtr, KInt frame, KInt priorFrame) {
    SkCodec* instance = reinterpret_cast<SkCodec*>((ptr));
    SkBitmap* bitmap = reinterpret_cast<SkBitmap*>((bitmapPtr));
    SkCodec::Options opts;
    opts.fFrameIndex = frame;
    opts.fPriorFrame = priorFrame;
    SkCodec::Result result = instance->getPixels(bitmap->info(), bitmap->getPixels(), bitmap->rowBytes(), &opts);
    return static_cast<KInt>(result);
}

SKIKO_EXPORT KInt org_jetbrains_skia_Codec__1nGetFrameCount
  (KNativePointer ptr) {
    SkCodec* instance = reinterpret_cast<SkCodec*>((ptr));
    return instance->getFrameCount();
}


SKIKO_EXPORT void org_jetbrains_skia_Codec__1nGetFrameInfo
  (KNativePointer ptr, KInt frame, KInteropPointer result) {
    auto* instance = reinterpret_cast<SkCodec*>((ptr));
    SkCodec::FrameInfo info{};
    instance->getFrameInfo(frame, &info);
    skija::AnimationFrameInfo::copyToInterop(info, result);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Codec__1nGetFramesInfo
  (KNativePointer ptr) {
    SkCodec* instance = reinterpret_cast<SkCodec*>((ptr));
    auto* infos = new std::vector<SkCodec::FrameInfo> { instance->getFrameInfo() };
    return reinterpret_cast<KNativePointer>(infos);
}

SKIKO_EXPORT void org_jetbrains_skia_Codec__1nFramesInfo_Delete
  (KNativePointer ptr) {
    delete reinterpret_cast<std::vector<SkCodec::FrameInfo>*>(ptr);
}

SKIKO_EXPORT KInt org_jetbrains_skia_Codec__1nFramesInfo_GetSize
  (KNativePointer ptr) {
    auto* infos = reinterpret_cast<std::vector<SkCodec::FrameInfo>*>(ptr);
    return static_cast<KInt>(infos->size());
}

SKIKO_EXPORT void org_jetbrains_skia_Codec__1nFramesInfo_GetInfos
  (KNativePointer ptr, KInteropPointer result) {
    auto* infos = reinterpret_cast<std::vector<SkCodec::FrameInfo>*>(ptr);
    skija::AnimationFrameInfo::copyToInterop(*infos, result);
}

SKIKO_EXPORT KInt org_jetbrains_skia_Codec__1nGetRepetitionCount
  (KNativePointer ptr) {
    SkCodec* instance = reinterpret_cast<SkCodec*>((ptr));
    return instance->getRepetitionCount();
}
