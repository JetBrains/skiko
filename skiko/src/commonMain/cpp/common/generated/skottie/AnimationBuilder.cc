
// This file has been auto generated.

#include "SkStream.h"
#include "Skottie.h"
#include "SkFontMgr.h"
using namespace skottie;
#include "common.h"

static void deleteAnimationBuilder(Animation::Builder* builder) {
    delete builder;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_skottie_AnimationBuilder__1nGetFinalizer
  (KInteropPointer __Kinstance) {
    return reinterpret_cast<KNativePointer>((&deleteAnimationBuilder));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_skottie_AnimationBuilder__1nMake
  (KInteropPointer __Kinstance, KInt flags) {
    return reinterpret_cast<KNativePointer>(new Animation::Builder(flags));
}

SKIKO_EXPORT void org_jetbrains_skia_skottie_AnimationBuilder__1nSetFontManager
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer fontMgrPtr) {
    Animation::Builder* instance = reinterpret_cast<Animation::Builder*>((ptr));
    sk_sp<SkFontMgr> fontMgr = sk_ref_sp(reinterpret_cast<SkFontMgr*>((fontMgrPtr)));
    instance->setFontManager(fontMgr);
}

SKIKO_EXPORT void org_jetbrains_skia_skottie_AnimationBuilder__1nSetLogger
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer loggerPtr) {
    Animation::Builder* instance = reinterpret_cast<Animation::Builder*>((ptr));
    sk_sp<skottie::Logger> logger = sk_ref_sp(reinterpret_cast<skottie::Logger*>((loggerPtr)));
    instance->setLogger(logger);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromString
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointer dataStr) {
    TODO("implement org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromString");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromString
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointer dataStr) {
    Animation::Builder* instance = reinterpret_cast<Animation::Builder*>((ptr));
    SkString data = skString(env, dataStr);
    sk_sp<Animation> animation = instance->make(data.c_str(), data.size());
    return reinterpret_cast<KNativePointer>(animation.release());
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromFile
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointer pathStr) {
    TODO("implement org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromFile");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromFile
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointer pathStr) {
    Animation::Builder* instance = reinterpret_cast<Animation::Builder*>((ptr));
    SkString path = skString(env, pathStr);
    sk_sp<Animation> animation = instance->makeFromFile(path.c_str());
    return reinterpret_cast<KNativePointer>(animation.release());
}
#endif


SKIKO_EXPORT KNativePointer org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromData
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer dataPtr) {
    Animation::Builder* instance = reinterpret_cast<Animation::Builder*>((ptr));
    SkData* data = reinterpret_cast<SkData*>((dataPtr));
    SkMemoryStream stream(sk_ref_sp(data));
    sk_sp<Animation> animation = instance->make(&stream);
    return reinterpret_cast<KNativePointer>(animation.release());
}
