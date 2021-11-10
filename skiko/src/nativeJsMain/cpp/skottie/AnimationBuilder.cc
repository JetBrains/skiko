
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
  () {
    return reinterpret_cast<KNativePointer>((&deleteAnimationBuilder));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_skottie_AnimationBuilder__1nMake
  (KInt flags) {
    return reinterpret_cast<KNativePointer>(new Animation::Builder(flags));
}

SKIKO_EXPORT void org_jetbrains_skia_skottie_AnimationBuilder__1nSetFontManager
  (KNativePointer ptr, KNativePointer fontMgrPtr) {
    Animation::Builder* instance = reinterpret_cast<Animation::Builder*>((ptr));
    sk_sp<SkFontMgr> fontMgr = sk_ref_sp(reinterpret_cast<SkFontMgr*>((fontMgrPtr)));
    instance->setFontManager(fontMgr);
}

SKIKO_EXPORT void org_jetbrains_skia_skottie_AnimationBuilder__1nSetLogger
  (KNativePointer ptr, KNativePointer loggerPtr) {
    Animation::Builder* instance = reinterpret_cast<Animation::Builder*>((ptr));
    sk_sp<skottie::Logger> logger = sk_ref_sp(reinterpret_cast<skottie::Logger*>((loggerPtr)));
    instance->setLogger(logger);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromString
  (KNativePointer ptr, KInteropPointer dataStr) {
    Animation::Builder* instance = reinterpret_cast<Animation::Builder*>((ptr));
    SkString data = skString(dataStr);
    sk_sp<Animation> animation = instance->make(data.c_str(), data.size());
    return reinterpret_cast<KNativePointer>(animation.release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromFile
  (KNativePointer ptr, KInteropPointer pathStr) {
    Animation::Builder* instance = reinterpret_cast<Animation::Builder*>((ptr));
    SkString path = skString(pathStr);
    sk_sp<Animation> animation = instance->makeFromFile(path.c_str());
    return reinterpret_cast<KNativePointer>(animation.release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromData
  (KNativePointer ptr, KNativePointer dataPtr) {
    Animation::Builder* instance = reinterpret_cast<Animation::Builder*>((ptr));
    SkData* data = reinterpret_cast<SkData*>((dataPtr));
    SkMemoryStream stream(sk_ref_sp(data));
    sk_sp<Animation> animation = instance->make(&stream);
    return reinterpret_cast<KNativePointer>(animation.release());
}
