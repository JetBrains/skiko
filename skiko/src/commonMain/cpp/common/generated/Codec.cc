
// This file has been auto generated.

#include <iostream>
#include "SkBitmap.h"
#include "SkCodec.h"
#include "SkData.h"
#include "common.h"

static void deleteCodec(SkCodec* instance) {
    delete instance;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Codec__1nGetFinalizer(KInteropPointer __Kinstance) {
    return reinterpret_cast<KNativePointer>(&deleteCodec);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Codec__1nMakeFromData
  (KInteropPointer __Kinstance, KNativePointer dataPtr) {
    SkData* data = reinterpret_cast<SkData*>((dataPtr));
    std::unique_ptr<SkCodec> instance = SkCodec::MakeFromData(sk_ref_sp(data));
    return reinterpret_cast<KNativePointer>(instance.release());
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Codec__1nGetImageInfo
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Codec__1nGetImageInfo");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Codec__1nGetImageInfo
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkCodec* instance = reinterpret_cast<SkCodec*>((ptr));
    return skija::ImageInfo::toJava(env, instance->getInfo());
}
#endif


SKIKO_EXPORT KLong org_jetbrains_skia_Codec__1nGetSize
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkCodec* instance = reinterpret_cast<SkCodec*>((ptr));
    return packISize(instance->dimensions());
}

SKIKO_EXPORT KLong org_jetbrains_skia_Codec__1nGetEncodedOrigin
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkCodec* instance = reinterpret_cast<SkCodec*>((ptr));
    return static_cast<KInt>(instance->getOrigin());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Codec__1nGetEncodedImageFormat
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkCodec* instance = reinterpret_cast<SkCodec*>((ptr));
    return static_cast<KInt>(instance->getEncodedFormat());
}

SKIKO_EXPORT KInt org_jetbrains_skia_Codec__1nReadPixels
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer bitmapPtr, KInt frame, KInt priorFrame) {
    SkCodec* instance = reinterpret_cast<SkCodec*>((ptr));
    SkBitmap* bitmap = reinterpret_cast<SkBitmap*>((bitmapPtr));
    SkCodec::Options opts;
    opts.fFrameIndex = frame;
    opts.fPriorFrame = priorFrame;
    SkCodec::Result result = instance->getPixels(bitmap->info(), bitmap->getPixels(), bitmap->rowBytes(), &opts);
    return static_cast<KInt>(result);
}

SKIKO_EXPORT KInt org_jetbrains_skia_Codec__1nGetFrameCount
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkCodec* instance = reinterpret_cast<SkCodec*>((ptr));
    return instance->getFrameCount();
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Codec__1nGetFrameInfo
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt frame) {
    TODO("implement org_jetbrains_skia_Codec__1nGetFrameInfo");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Codec__1nGetFrameInfo
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt frame) {
    SkCodec* instance = reinterpret_cast<SkCodec*>((ptr));
    SkCodec::FrameInfo info;
    instance->getFrameInfo(frame, &info);
    return skija::AnimationFrameInfo::toJava(env, info);
}
#endif



SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Codec__1nGetFramesInfo
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt frame) {
    TODO("implement org_jetbrains_skia_Codec__1nGetFramesInfo");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Codec__1nGetFramesInfo
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt frame) {
    SkCodec* instance = reinterpret_cast<SkCodec*>((ptr));
    SkCodec::FrameInfo info;
    std::vector<SkCodec::FrameInfo> frames = instance->getFrameInfo();
    KInteropPointerArray res = env->NewObjectArray(frames.size(), skija::AnimationFrameInfo::cls, nullptr);
    if (java::lang::Throwable::exceptionThrown(env))
        return nullptr;
    for (int i = 0; i < frames.size(); ++i) {
        skija::AutoLocal<KInteropPointer> infoObj(env, skija::AnimationFrameInfo::toJava(env, frames[i]));
        env->SetObjectArrayElement(res, i, infoObj.get());
    }
    return res;
}
#endif


SKIKO_EXPORT KInt org_jetbrains_skia_Codec__1nGetRepetitionCount
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkCodec* instance = reinterpret_cast<SkCodec*>((ptr));
    return instance->getRepetitionCount();
}
