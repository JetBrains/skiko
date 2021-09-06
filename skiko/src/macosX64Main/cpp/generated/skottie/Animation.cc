
// This file has been auto generated.

#include "SkStream.h"
#include "SkSGInvalidationController.h"
#include "Skottie.h"
using namespace skottie;
#include "common.h"

static void deleteAnimation(Animation* animation) {
    delete animation;
}

extern "C" jlong org_jetbrains_skia_skottie_Animation__1nGetFinalizer
  (kref __Kinstance) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteAnimation));
}


extern "C" jlong org_jetbrains_skia_skottie_Animation__1nMakeFromString
  (kref __Kinstance, jstring dataStr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_skottie_Animation__1nMakeFromString");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_skottie_Animation__1nMakeFromString
  (kref __Kinstance, jstring dataStr) {
    SkString data = skString(env, dataStr);
    sk_sp<Animation> instance = Animation::Make(data.c_str(), data.size());
    return reinterpret_cast<jlong>(instance.release());
}
#endif



extern "C" jlong org_jetbrains_skia_skottie_Animation__1nMakeFromFile
  (kref __Kinstance, jstring pathStr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_skottie_Animation__1nMakeFromFile");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_skottie_Animation__1nMakeFromFile
  (kref __Kinstance, jstring pathStr) {
    SkString path = skString(env, pathStr);
    sk_sp<Animation> instance = Animation::MakeFromFile(path.c_str());
    return reinterpret_cast<jlong>(instance.release());
}
#endif


extern "C" jlong org_jetbrains_skia_skottie_Animation__1nMakeFromData
  (kref __Kinstance, jlong dataPtr) {
    SkData* data = reinterpret_cast<SkData*>(static_cast<uintptr_t>(dataPtr));
    SkMemoryStream stream(sk_ref_sp(data));
    sk_sp<Animation> instance = Animation::Make(&stream);
    return reinterpret_cast<jlong>(instance.release());
}

extern "C" void org_jetbrains_skia_skottie_Animation__1nRender
  (kref __Kinstance, jlong ptr, jlong canvasPtr, jfloat left, jfloat top, jfloat right, jfloat bottom, jint flags) {
    Animation* instance = reinterpret_cast<Animation*>(static_cast<uintptr_t>(ptr));
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    SkRect bounds {left, top, right, bottom};
    instance->render(canvas, &bounds, flags);
}

extern "C" void org_jetbrains_skia_skottie_Animation__1nSeek
  (kref __Kinstance, jlong ptr, jfloat t, jlong icPtr) {
    Animation* instance = reinterpret_cast<Animation*>(static_cast<uintptr_t>(ptr));
    sksg::InvalidationController* controller = reinterpret_cast<sksg::InvalidationController*>(static_cast<uintptr_t>(icPtr));
    instance->seek(t, controller);
}

extern "C" void org_jetbrains_skia_skottie_Animation__1nSeekFrame
  (kref __Kinstance, jlong ptr, jfloat t, jlong icPtr) {
    Animation* instance = reinterpret_cast<Animation*>(static_cast<uintptr_t>(ptr));
    sksg::InvalidationController* controller = reinterpret_cast<sksg::InvalidationController*>(static_cast<uintptr_t>(icPtr));
    instance->seekFrame((double) t, controller);
}

extern "C" void org_jetbrains_skia_skottie_Animation__1nSeekFrameTime
  (kref __Kinstance, jlong ptr, jfloat t, jlong icPtr) {
    Animation* instance = reinterpret_cast<Animation*>(static_cast<uintptr_t>(ptr));
    sksg::InvalidationController* controller = reinterpret_cast<sksg::InvalidationController*>(static_cast<uintptr_t>(icPtr));
    instance->seekFrameTime((double) t, controller);
}

extern "C" jfloat org_jetbrains_skia_skottie_Animation__1nGetDuration
  (kref __Kinstance, jlong ptr) {
    Animation* instance = reinterpret_cast<Animation*>(static_cast<uintptr_t>(ptr));
    return (jfloat) instance->duration();
}

extern "C" jfloat org_jetbrains_skia_skottie_Animation__1nGetFPS
  (kref __Kinstance, jlong ptr) {
    Animation* instance = reinterpret_cast<Animation*>(static_cast<uintptr_t>(ptr));
    return (jfloat) instance->fps();
}

extern "C" jfloat org_jetbrains_skia_skottie_Animation__1nGetInPoint
  (kref __Kinstance, jlong ptr) {
    Animation* instance = reinterpret_cast<Animation*>(static_cast<uintptr_t>(ptr));
    return (jfloat) instance->inPoint();
}

extern "C" jfloat org_jetbrains_skia_skottie_Animation__1nGetOutPoint
  (kref __Kinstance, jlong ptr) {
    Animation* instance = reinterpret_cast<Animation*>(static_cast<uintptr_t>(ptr));
    return (jfloat) instance->outPoint();
}


extern "C" jstring org_jetbrains_skia_skottie_Animation__1nGetVersion
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_skottie_Animation__1nGetVersion");
}
     
#if 0 
extern "C" jstring org_jetbrains_skia_skottie_Animation__1nGetVersion
  (kref __Kinstance, jlong ptr) {
    Animation* instance = reinterpret_cast<Animation*>(static_cast<uintptr_t>(ptr));
    return javaString(env, instance->version());
}
#endif



extern "C" jobject org_jetbrains_skia_skottie_Animation__1nGetSize
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_skottie_Animation__1nGetSize");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_skottie_Animation__1nGetSize
  (kref __Kinstance, jlong ptr) {
    Animation* instance = reinterpret_cast<Animation*>(static_cast<uintptr_t>(ptr));
    const SkSize& size = instance->size();
    return skija::Point::make(env, size.fWidth, size.fHeight);
}
#endif

