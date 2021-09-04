
// This file has been auto generated.

#include <iostream>
#include "SkBitmap.h"
#include "SkCodec.h"
#include "SkData.h"
#include "common.h"

static void deleteCodec(SkCodec* instance) {
    delete instance;
}

extern "C" jlong org_jetbrains_skia_Codec__1nGetFinalizer(kref __Kinstance) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteCodec));
}

extern "C" jlong org_jetbrains_skia_Codec__1nMakeFromData
  (kref __Kinstance, jlong dataPtr) {
    SkData* data = reinterpret_cast<SkData*>(static_cast<uintptr_t>(dataPtr));
    std::unique_ptr<SkCodec> instance = SkCodec::MakeFromData(sk_ref_sp(data));
    return reinterpret_cast<jlong>(instance.release());
}


extern "C" jobject org_jetbrains_skia_Codec__1nGetImageInfo
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_Codec__1nGetImageInfo");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Codec__1nGetImageInfo
  (kref __Kinstance, jlong ptr) {
    SkCodec* instance = reinterpret_cast<SkCodec*>(static_cast<uintptr_t>(ptr));
    return skija::ImageInfo::toJava(env, instance->getInfo());
}
#endif


extern "C" jlong org_jetbrains_skia_Codec__1nGetSize
  (kref __Kinstance, jlong ptr) {
    SkCodec* instance = reinterpret_cast<SkCodec*>(static_cast<uintptr_t>(ptr));
    return packISize(instance->dimensions());
}

extern "C" jlong org_jetbrains_skia_Codec__1nGetEncodedOrigin
  (kref __Kinstance, jlong ptr) {
    SkCodec* instance = reinterpret_cast<SkCodec*>(static_cast<uintptr_t>(ptr));
    return static_cast<jint>(instance->getOrigin());
}

extern "C" jlong org_jetbrains_skia_Codec__1nGetEncodedImageFormat
  (kref __Kinstance, jlong ptr) {
    SkCodec* instance = reinterpret_cast<SkCodec*>(static_cast<uintptr_t>(ptr));
    return static_cast<jint>(instance->getEncodedFormat());
}

extern "C" jint org_jetbrains_skia_Codec__1nReadPixels
  (kref __Kinstance, jlong ptr, jlong bitmapPtr, jint frame, jint priorFrame) {
    SkCodec* instance = reinterpret_cast<SkCodec*>(static_cast<uintptr_t>(ptr));
    SkBitmap* bitmap = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(bitmapPtr));
    SkCodec::Options opts;
    opts.fFrameIndex = frame;
    opts.fPriorFrame = priorFrame;
    SkCodec::Result result = instance->getPixels(bitmap->info(), bitmap->getPixels(), bitmap->rowBytes(), &opts);
    return static_cast<jint>(result);
}

extern "C" jint org_jetbrains_skia_Codec__1nGetFrameCount
  (kref __Kinstance, jlong ptr) {
    SkCodec* instance = reinterpret_cast<SkCodec*>(static_cast<uintptr_t>(ptr));
    return instance->getFrameCount();
}


extern "C" jobject org_jetbrains_skia_Codec__1nGetFrameInfo
  (kref __Kinstance, jlong ptr, jint frame) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_Codec__1nGetFrameInfo");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Codec__1nGetFrameInfo
  (kref __Kinstance, jlong ptr, jint frame) {
    SkCodec* instance = reinterpret_cast<SkCodec*>(static_cast<uintptr_t>(ptr));
    SkCodec::FrameInfo info;
    instance->getFrameInfo(frame, &info);
    return skija::AnimationFrameInfo::toJava(env, info);
}
#endif



extern "C" jobject org_jetbrains_skia_Codec__1nGetFramesInfo
  (kref __Kinstance, jlong ptr, jint frame) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_Codec__1nGetFramesInfo");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Codec__1nGetFramesInfo
  (kref __Kinstance, jlong ptr, jint frame) {
    SkCodec* instance = reinterpret_cast<SkCodec*>(static_cast<uintptr_t>(ptr));
    SkCodec::FrameInfo info;
    std::vector<SkCodec::FrameInfo> frames = instance->getFrameInfo();
    jobjectArray res = env->NewObjectArray(frames.size(), skija::AnimationFrameInfo::cls, nullptr);
    if (java::lang::Throwable::exceptionThrown(env))
        return nullptr;
    for (int i = 0; i < frames.size(); ++i) {
        skija::AutoLocal<jobject> infoObj(env, skija::AnimationFrameInfo::toJava(env, frames[i]));
        env->SetObjectArrayElement(res, i, infoObj.get());
    }
    return res;
}
#endif


extern "C" jint org_jetbrains_skia_Codec__1nGetRepetitionCount
  (kref __Kinstance, jlong ptr) {
    SkCodec* instance = reinterpret_cast<SkCodec*>(static_cast<uintptr_t>(ptr));
    return instance->getRepetitionCount();
}
