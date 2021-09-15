
// This file has been auto generated.

#include "SkStream.h"
#include "SkSGInvalidationController.h"
#include "Skottie.h"
using namespace skottie;
#include "common.h"

static void deleteAnimation(Animation* animation) {
    delete animation;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_skottie_Animation__1nGetFinalizer
  (KInteropPointer __Kinstance) {
    return reinterpret_cast<KNativePointer>((&deleteAnimation));
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_skottie_Animation__1nMakeFromString
  (KInteropPointer __Kinstance, KInteropPointer dataStr) {
    TODO("implement org_jetbrains_skia_skottie_Animation__1nMakeFromString");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_skottie_Animation__1nMakeFromString
  (KInteropPointer __Kinstance, KInteropPointer dataStr) {
    SkString data = skString(env, dataStr);
    sk_sp<Animation> instance = Animation::Make(data.c_str(), data.size());
    return reinterpret_cast<KNativePointer>(instance.release());
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_skottie_Animation__1nMakeFromFile
  (KInteropPointer __Kinstance, KInteropPointer pathStr) {
    TODO("implement org_jetbrains_skia_skottie_Animation__1nMakeFromFile");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_skottie_Animation__1nMakeFromFile
  (KInteropPointer __Kinstance, KInteropPointer pathStr) {
    SkString path = skString(env, pathStr);
    sk_sp<Animation> instance = Animation::MakeFromFile(path.c_str());
    return reinterpret_cast<KNativePointer>(instance.release());
}
#endif


SKIKO_EXPORT KNativePointer org_jetbrains_skia_skottie_Animation__1nMakeFromData
  (KInteropPointer __Kinstance, KNativePointer dataPtr) {
    SkData* data = reinterpret_cast<SkData*>((dataPtr));
    SkMemoryStream stream(sk_ref_sp(data));
    sk_sp<Animation> instance = Animation::Make(&stream);
    return reinterpret_cast<KNativePointer>(instance.release());
}

SKIKO_EXPORT void org_jetbrains_skia_skottie_Animation__1nRender
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer canvasPtr, KFloat left, KFloat top, KFloat right, KFloat bottom, KInt flags) {
    Animation* instance = reinterpret_cast<Animation*>((ptr));
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    SkRect bounds {left, top, right, bottom};
    instance->render(canvas, &bounds, flags);
}

SKIKO_EXPORT void org_jetbrains_skia_skottie_Animation__1nSeek
  (KInteropPointer __Kinstance, KNativePointer ptr, KFloat t, KNativePointer icPtr) {
    Animation* instance = reinterpret_cast<Animation*>((ptr));
    sksg::InvalidationController* controller = reinterpret_cast<sksg::InvalidationController*>((icPtr));
    instance->seek(t, controller);
}

SKIKO_EXPORT void org_jetbrains_skia_skottie_Animation__1nSeekFrame
  (KInteropPointer __Kinstance, KNativePointer ptr, KFloat t, KNativePointer icPtr) {
    Animation* instance = reinterpret_cast<Animation*>((ptr));
    sksg::InvalidationController* controller = reinterpret_cast<sksg::InvalidationController*>((icPtr));
    instance->seekFrame((double) t, controller);
}

SKIKO_EXPORT void org_jetbrains_skia_skottie_Animation__1nSeekFrameTime
  (KInteropPointer __Kinstance, KNativePointer ptr, KFloat t, KNativePointer icPtr) {
    Animation* instance = reinterpret_cast<Animation*>((ptr));
    sksg::InvalidationController* controller = reinterpret_cast<sksg::InvalidationController*>((icPtr));
    instance->seekFrameTime((double) t, controller);
}

SKIKO_EXPORT KFloat org_jetbrains_skia_skottie_Animation__1nGetDuration
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    Animation* instance = reinterpret_cast<Animation*>((ptr));
    return (KFloat) instance->duration();
}

SKIKO_EXPORT KFloat org_jetbrains_skia_skottie_Animation__1nGetFPS
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    Animation* instance = reinterpret_cast<Animation*>((ptr));
    return (KFloat) instance->fps();
}

SKIKO_EXPORT KFloat org_jetbrains_skia_skottie_Animation__1nGetInPoint
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    Animation* instance = reinterpret_cast<Animation*>((ptr));
    return (KFloat) instance->inPoint();
}

SKIKO_EXPORT KFloat org_jetbrains_skia_skottie_Animation__1nGetOutPoint
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    Animation* instance = reinterpret_cast<Animation*>((ptr));
    return (KFloat) instance->outPoint();
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_skottie_Animation__1nGetVersion
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_skottie_Animation__1nGetVersion");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_skottie_Animation__1nGetVersion
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    Animation* instance = reinterpret_cast<Animation*>((ptr));
    return javaString(env, instance->version());
}
#endif



SKIKO_EXPORT KInteropPointer org_jetbrains_skia_skottie_Animation__1nGetSize
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_skottie_Animation__1nGetSize");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_skottie_Animation__1nGetSize
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    Animation* instance = reinterpret_cast<Animation*>((ptr));
    const SkSize& size = instance->size();
    return skija::Point::make(env, size.fWidth, size.fHeight);
}
#endif

