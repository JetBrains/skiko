
// This file has been auto generated.

#include <iostream>
#include "ParagraphStyle.h"
using namespace std;
using namespace skia::textlayout;
#include "common.h"

static void deleteParagraphStyle(ParagraphStyle* instance) {
    delete instance;
}

extern "C" jlong org_jetbrains_skia_paragraph_ParagraphStyle__1nGetFinalizer
  () {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteParagraphStyle));
}

extern "C" jlong org_jetbrains_skia_paragraph_ParagraphStyle__1nMake
  () {
    ParagraphStyle* instance = new ParagraphStyle();
    return reinterpret_cast<jlong>(instance);
}

extern "C" jboolean org_jetbrains_skia_paragraph_ParagraphStyle__1nEquals
  (jlong ptr, jlong otherPtr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    ParagraphStyle* other = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(otherPtr));
    return *instance == *other;
}

extern "C" jlong org_jetbrains_skia_paragraph_ParagraphStyle__1nGetStrutStyle
  (jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    StrutStyle* res = new StrutStyle();
    *res = instance->getStrutStyle();
    return reinterpret_cast<jlong>(res);
}

extern "C" void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetStrutStyle
  (jlong ptr, jlong stylePtr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    StrutStyle* style = reinterpret_cast<StrutStyle*>(static_cast<uintptr_t>(stylePtr));
    instance->setStrutStyle(*style);
}

extern "C" jlong org_jetbrains_skia_paragraph_ParagraphStyle__1nGetTextStyle
  (jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    const TextStyle& style = instance->getTextStyle();
    TextStyle* res = new TextStyle(style, style.isPlaceholder());
    return reinterpret_cast<jlong>(res);
}

extern "C" void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetTextStyle
  (jlong ptr, jlong textStylePtr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    TextStyle* textStyle = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(textStylePtr));
    instance->setTextStyle(*textStyle);
}

extern "C" jint org_jetbrains_skia_paragraph_ParagraphStyle__1nGetDirection
  (jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    return static_cast<jint>(instance->getTextDirection());
}

extern "C" void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetDirection
  (jlong ptr, jint textDirection) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    instance->setTextDirection(static_cast<TextDirection>(textDirection));
}

extern "C" jint org_jetbrains_skia_paragraph_ParagraphStyle__1nGetAlignment
  (jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    return static_cast<jint>(instance->getTextAlign());
}

extern "C" void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetAlignment
  (jlong ptr, jint textAlign) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    instance->setTextAlign(static_cast<TextAlign>(textAlign));
}

extern "C" jint org_jetbrains_skia_paragraph_ParagraphStyle__1nGetMaxLinesCount
  (jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    return static_cast<jint>(instance->getMaxLines());
}

extern "C" void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetMaxLinesCount
  (jlong ptr, jint count) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    instance->setMaxLines(count);
}


extern "C" jstring org_jetbrains_skia_paragraph_ParagraphStyle__1nGetEllipsis
  (jlong ptr) {
    TODO("implement org_jetbrains_skia_paragraph_ParagraphStyle__1nGetEllipsis");
}
     
#if 0 
extern "C" jstring org_jetbrains_skia_paragraph_ParagraphStyle__1nGetEllipsis
  (jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    return instance->ellipsized() ? javaString(env, instance->getEllipsis()) : nullptr;
}
#endif



extern "C" void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetEllipsis
  (jlong ptr, jstring ellipsis) {
    TODO("implement org_jetbrains_skia_paragraph_ParagraphStyle__1nSetEllipsis");
}
     
#if 0 
extern "C" void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetEllipsis
  (jlong ptr, jstring ellipsis) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    instance->setEllipsis(skString(env, ellipsis));
}
#endif


extern "C" jfloat org_jetbrains_skia_paragraph_ParagraphStyle__1nGetHeight
  (jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    return instance->getHeight();
}

extern "C" void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetHeight
  (jlong ptr, jfloat height) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    instance->setHeight(height);
}

extern "C" jint org_jetbrains_skia_paragraph_ParagraphStyle__1nGetHeightMode
  (jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    return static_cast<jint>(instance->getTextHeightBehavior());
}

extern "C" void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetHeightMode
  (jlong ptr, jint heightMode) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    instance->setTextHeightBehavior(static_cast<TextHeightBehavior>(heightMode));
}

extern "C" jint org_jetbrains_skia_paragraph_ParagraphStyle__1nGetEffectiveAlignment
  (jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    return static_cast<jint>(instance->effective_align());
}

extern "C" jboolean org_jetbrains_skia_paragraph_ParagraphStyle__1nIsHintingEnabled
  (jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    return instance->hintingIsOn();
}

extern "C" void org_jetbrains_skia_paragraph_ParagraphStyle__1nDisableHinting
  (jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    instance->turnHintingOff();
}
