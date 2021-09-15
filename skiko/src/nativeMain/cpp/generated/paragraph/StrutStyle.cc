
// This file has been auto generated.

#include <iostream>
#include <vector>
#include "ParagraphStyle.h"
using namespace std;
using namespace skia::textlayout;
#include "common.h"

static void deleteStrutStyle(StrutStyle* instance) {
    delete instance;
}

extern "C" jlong org_jetbrains_skia_paragraph_StrutStyle__1nGetFinalizer
  () {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteStrutStyle));
}

extern "C" jlong org_jetbrains_skia_paragraph_StrutStyle__1nMake
  () {
    StrutStyle* instance = new StrutStyle();
    return reinterpret_cast<jlong>(instance);
}

extern "C" jboolean org_jetbrains_skia_paragraph_StrutStyle__1nEquals
  (jlong ptr, jlong otherPtr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>(static_cast<uintptr_t>(ptr));
    StrutStyle* other = reinterpret_cast<StrutStyle*>(static_cast<uintptr_t>(otherPtr));
    return *instance == *other;
}


extern "C" jobjectArray org_jetbrains_skia_paragraph_StrutStyle__1nGetFontFamilies
  (jlong ptr) {
    TODO("implement org_jetbrains_skia_paragraph_StrutStyle__1nGetFontFamilies");
}
     
#if 0 
extern "C" jobjectArray org_jetbrains_skia_paragraph_StrutStyle__1nGetFontFamilies
  (jlong ptr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>(static_cast<uintptr_t>(ptr));
    return javaStringArray(env, instance->getFontFamilies());
}
#endif



extern "C" void org_jetbrains_skia_paragraph_StrutStyle__1nSetFontFamilies
  (jlong ptr, jobjectArray familiesArr) {
    TODO("implement org_jetbrains_skia_paragraph_StrutStyle__1nSetFontFamilies");
}
     
#if 0 
extern "C" void org_jetbrains_skia_paragraph_StrutStyle__1nSetFontFamilies
  (jlong ptr, jobjectArray familiesArr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>(static_cast<uintptr_t>(ptr));
    instance->setFontFamilies(skStringVector(env, familiesArr));
}
#endif



extern "C" jint org_jetbrains_skia_paragraph_StrutStyle__1nGetFontStyle
  (jlong ptr) {
    TODO("implement org_jetbrains_skia_paragraph_StrutStyle__1nGetFontStyle");
}
     
#if 0 
extern "C" jint org_jetbrains_skia_paragraph_StrutStyle__1nGetFontStyle
  (jlong ptr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>(static_cast<uintptr_t>(ptr));
    return skija::FontStyle::toJava(instance->getFontStyle());
}
#endif



extern "C" void org_jetbrains_skia_paragraph_StrutStyle__1nSetFontStyle
  (jlong ptr, jint style) {
    TODO("implement org_jetbrains_skia_paragraph_StrutStyle__1nSetFontStyle");
}
     
#if 0 
extern "C" void org_jetbrains_skia_paragraph_StrutStyle__1nSetFontStyle
  (jlong ptr, jint style) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>(static_cast<uintptr_t>(ptr));
    instance->setFontStyle(skija::FontStyle::fromJava(style));
}
#endif


extern "C" jfloat org_jetbrains_skia_paragraph_StrutStyle__1nGetFontSize
  (jlong ptr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>(static_cast<uintptr_t>(ptr));
    return instance->getFontSize();
}

extern "C" void org_jetbrains_skia_paragraph_StrutStyle__1nSetFontSize
  (jlong ptr, jfloat size) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>(static_cast<uintptr_t>(ptr));
    instance->setFontSize(size);
}

extern "C" jfloat org_jetbrains_skia_paragraph_StrutStyle__1nGetHeight
  (jlong ptr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>(static_cast<uintptr_t>(ptr));
    return instance->getHeight();
}

extern "C" void org_jetbrains_skia_paragraph_StrutStyle__1nSetHeight
  (jlong ptr, jfloat height) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>(static_cast<uintptr_t>(ptr));
    instance->setHeight(height);
}

extern "C" jfloat org_jetbrains_skia_paragraph_StrutStyle__1nGetLeading
  (jlong ptr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>(static_cast<uintptr_t>(ptr));
    return instance->getLeading();
}

extern "C" void org_jetbrains_skia_paragraph_StrutStyle__1nSetLeading
  (jlong ptr, jfloat leading) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>(static_cast<uintptr_t>(ptr));
    instance->setLeading(leading);
}

extern "C" jboolean org_jetbrains_skia_paragraph_StrutStyle__1nIsEnabled
  (jlong ptr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>(static_cast<uintptr_t>(ptr));
    return instance->getStrutEnabled();
}

extern "C" void org_jetbrains_skia_paragraph_StrutStyle__1nSetEnabled
  (jlong ptr, jboolean value) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>(static_cast<uintptr_t>(ptr));
    instance->setStrutEnabled(value);
}

extern "C" jboolean org_jetbrains_skia_paragraph_StrutStyle__1nIsHeightForced
  (jlong ptr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>(static_cast<uintptr_t>(ptr));
    return instance->getForceStrutHeight();
}

extern "C" void org_jetbrains_skia_paragraph_StrutStyle__1nSetHeightForced
  (jlong ptr, jboolean value) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>(static_cast<uintptr_t>(ptr));
    instance->setForceStrutHeight(value);
}

extern "C" jboolean org_jetbrains_skia_paragraph_StrutStyle__1nIsHeightOverridden
  (jlong ptr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>(static_cast<uintptr_t>(ptr));
    return instance->getHeightOverride();
}

extern "C" void org_jetbrains_skia_paragraph_StrutStyle__1nSetHeightOverridden
  (jlong ptr, jboolean value) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>(static_cast<uintptr_t>(ptr));
    instance->setHeightOverride(value);
}
