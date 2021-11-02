#include <iostream>
#include <jni.h>
#include "SkBitmap.h"
#include "SkCodec.h"
#include "SkData.h"
#include "interop.hh"

static void deleteCodec(SkCodec* instance) {
    delete instance;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_CodecKt_Codec_1nGetFinalizer(JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteCodec));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_CodecKt__1nMakeFromData
  (JNIEnv* env, jclass jclass, jlong dataPtr) {
    SkData* data = reinterpret_cast<SkData*>(static_cast<uintptr_t>(dataPtr));
    std::unique_ptr<SkCodec> instance = SkCodec::MakeFromData(sk_ref_sp(data));
    return reinterpret_cast<jlong>(instance.release());
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CodecKt_Codec_1nGetImageInfo
  (JNIEnv* env, jclass jclass, jlong ptr, jintArray imageInfoResult, jlongArray colorSpaceResultPtr) {
    auto instance = reinterpret_cast<SkCodec*>(static_cast<uintptr_t>(ptr));
    SkImageInfo imageInfo = instance->getInfo();
    skija::ImageInfo::writeImageInfoForInterop(env, imageInfo, imageInfoResult, colorSpaceResultPtr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_CodecKt__1nGetSize
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkCodec* instance = reinterpret_cast<SkCodec*>(static_cast<uintptr_t>(ptr));
    return packISize(instance->dimensions());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_CodecKt__1nGetEncodedOrigin
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkCodec* instance = reinterpret_cast<SkCodec*>(static_cast<uintptr_t>(ptr));
    return static_cast<jint>(instance->getOrigin());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_CodecKt__1nGetEncodedImageFormat
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkCodec* instance = reinterpret_cast<SkCodec*>(static_cast<uintptr_t>(ptr));
    return static_cast<jint>(instance->getEncodedFormat());
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_CodecKt_Codec_1nReadPixels
  (JNIEnv* env, jclass jclass, jlong ptr, jlong bitmapPtr, jint frame, jint priorFrame) {
    SkCodec* instance = reinterpret_cast<SkCodec*>(static_cast<uintptr_t>(ptr));
    SkBitmap* bitmap = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(bitmapPtr));
    SkCodec::Options opts;
    opts.fFrameIndex = frame;
    opts.fPriorFrame = priorFrame;
    SkCodec::Result result = instance->getPixels(bitmap->info(), bitmap->getPixels(), bitmap->rowBytes(), &opts);
    return static_cast<jint>(result);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_CodecKt__1nGetFrameCount
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkCodec* instance = reinterpret_cast<SkCodec*>(static_cast<uintptr_t>(ptr));
    return instance->getFrameCount();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CodecKt__1nGetFrameInfo
  (JNIEnv* env, jclass jclass, jlong ptr, jint frame, jintArray result) {
    SkCodec* instance = reinterpret_cast<SkCodec*>(static_cast<uintptr_t>(ptr));
    SkCodec::FrameInfo info;
    instance->getFrameInfo(frame, &info);
    skija::AnimationFrameInfo::copyToInterop(env, info, result);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_CodecKt__1nGetFramesInfo
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkCodec* instance = reinterpret_cast<SkCodec*>(static_cast<uintptr_t>(ptr));
    auto infos = new std::vector<SkCodec::FrameInfo> { instance->getFrameInfo() };
    return reinterpret_cast<jlong>(infos);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_CodecKt__1nGetRepetitionCount
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkCodec* instance = reinterpret_cast<SkCodec*>(static_cast<uintptr_t>(ptr));
    return instance->getRepetitionCount();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CodecKt_FramesInfo_1nDelete
  (JNIEnv* env, jclass jclass, jlong ptr) {
    delete reinterpret_cast<std::vector<SkCodec::FrameInfo>*>(static_cast<uintptr_t>(ptr));
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_CodecKt_FramesInfo_1nGetSize
  (JNIEnv* env, jclass jclass, jlong ptr) {
    auto infos = reinterpret_cast<std::vector<SkCodec::FrameInfo>*>(static_cast<uintptr_t>(ptr));
    return infos->size();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_CodecKt_FramesInfo_1nGetInfos
  (JNIEnv* env, jclass jclass, jlong ptr, jintArray result) {
    auto infos = reinterpret_cast<std::vector<SkCodec::FrameInfo>*>(static_cast<uintptr_t>(ptr));
    skija::AnimationFrameInfo::copyToInterop(env, *infos, result);
}