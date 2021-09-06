
// This file has been auto generated.

#include "SkStream.h"
#include "Skottie.h"
#include "SkFontMgr.h"
using namespace skottie;
#include "common.h"

static void deleteAnimationBuilder(Animation::Builder* builder) {
    delete builder;
}

extern "C" jlong org_jetbrains_skia_skottie_AnimationBuilder__1nGetFinalizer
  (kref __Kinstance) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteAnimationBuilder));
}

extern "C" jlong org_jetbrains_skia_skottie_AnimationBuilder__1nMake
  (kref __Kinstance, jint flags) {
    return reinterpret_cast<jlong>(new Animation::Builder(flags));
}

extern "C" void org_jetbrains_skia_skottie_AnimationBuilder__1nSetFontManager
  (kref __Kinstance, jlong ptr, jlong fontMgrPtr) {
    Animation::Builder* instance = reinterpret_cast<Animation::Builder*>(static_cast<uintptr_t>(ptr));
    sk_sp<SkFontMgr> fontMgr = sk_ref_sp(reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(fontMgrPtr)));
    instance->setFontManager(fontMgr);
}

extern "C" void org_jetbrains_skia_skottie_AnimationBuilder__1nSetLogger
  (kref __Kinstance, jlong ptr, jlong loggerPtr) {
    Animation::Builder* instance = reinterpret_cast<Animation::Builder*>(static_cast<uintptr_t>(ptr));
    sk_sp<skottie::Logger> logger = sk_ref_sp(reinterpret_cast<skottie::Logger*>(static_cast<uintptr_t>(loggerPtr)));
    instance->setLogger(logger);
}


extern "C" jlong org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromString
  (kref __Kinstance, jlong ptr, jstring dataStr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromString");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromString
  (kref __Kinstance, jlong ptr, jstring dataStr) {
    Animation::Builder* instance = reinterpret_cast<Animation::Builder*>(static_cast<uintptr_t>(ptr));
    SkString data = skString(env, dataStr);
    sk_sp<Animation> animation = instance->make(data.c_str(), data.size());
    return reinterpret_cast<jlong>(animation.release());
}
#endif



extern "C" jlong org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromFile
  (kref __Kinstance, jlong ptr, jstring pathStr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromFile");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromFile
  (kref __Kinstance, jlong ptr, jstring pathStr) {
    Animation::Builder* instance = reinterpret_cast<Animation::Builder*>(static_cast<uintptr_t>(ptr));
    SkString path = skString(env, pathStr);
    sk_sp<Animation> animation = instance->makeFromFile(path.c_str());
    return reinterpret_cast<jlong>(animation.release());
}
#endif


extern "C" jlong org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromData
  (kref __Kinstance, jlong ptr, jlong dataPtr) {
    Animation::Builder* instance = reinterpret_cast<Animation::Builder*>(static_cast<uintptr_t>(ptr));
    SkData* data = reinterpret_cast<SkData*>(static_cast<uintptr_t>(dataPtr));
    SkMemoryStream stream(sk_ref_sp(data));
    sk_sp<Animation> animation = instance->make(&stream);
    return reinterpret_cast<jlong>(animation.release());
}
