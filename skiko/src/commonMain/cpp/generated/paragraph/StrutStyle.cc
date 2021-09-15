
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

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_StrutStyle__1nGetFinalizer
  () {
    return reinterpret_cast<KNativePointer>((&deleteStrutStyle));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_StrutStyle__1nMake
  () {
    StrutStyle* instance = new StrutStyle();
    return reinterpret_cast<KNativePointer>(instance);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_paragraph_StrutStyle__1nEquals
  (KNativePointer ptr, KNativePointer otherPtr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    StrutStyle* other = reinterpret_cast<StrutStyle*>((otherPtr));
    return *instance == *other;
}


SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_paragraph_StrutStyle__1nGetFontFamilies
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_paragraph_StrutStyle__1nGetFontFamilies");
}
     
#if 0 
SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_paragraph_StrutStyle__1nGetFontFamilies
  (KNativePointer ptr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    return javaStringArray(env, instance->getFontFamilies());
}
#endif



SKIKO_EXPORT void org_jetbrains_skia_paragraph_StrutStyle__1nSetFontFamilies
  (KNativePointer ptr, KInteropPointerArray familiesArr) {
    TODO("implement org_jetbrains_skia_paragraph_StrutStyle__1nSetFontFamilies");
}
     
#if 0 
SKIKO_EXPORT void org_jetbrains_skia_paragraph_StrutStyle__1nSetFontFamilies
  (KNativePointer ptr, KInteropPointerArray familiesArr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    instance->setFontFamilies(skStringVector(env, familiesArr));
}
#endif



SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_StrutStyle__1nGetFontStyle
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_paragraph_StrutStyle__1nGetFontStyle");
}
     
#if 0 
SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_StrutStyle__1nGetFontStyle
  (KNativePointer ptr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    return skija::FontStyle::toJava(instance->getFontStyle());
}
#endif



SKIKO_EXPORT void org_jetbrains_skia_paragraph_StrutStyle__1nSetFontStyle
  (KNativePointer ptr, KInt style) {
    TODO("implement org_jetbrains_skia_paragraph_StrutStyle__1nSetFontStyle");
}
     
#if 0 
SKIKO_EXPORT void org_jetbrains_skia_paragraph_StrutStyle__1nSetFontStyle
  (KNativePointer ptr, KInt style) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    instance->setFontStyle(skija::FontStyle::fromJava(style));
}
#endif


SKIKO_EXPORT KFloat org_jetbrains_skia_paragraph_StrutStyle__1nGetFontSize
  (KNativePointer ptr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    return instance->getFontSize();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_StrutStyle__1nSetFontSize
  (KNativePointer ptr, KFloat size) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    instance->setFontSize(size);
}

SKIKO_EXPORT KFloat org_jetbrains_skia_paragraph_StrutStyle__1nGetHeight
  (KNativePointer ptr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    return instance->getHeight();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_StrutStyle__1nSetHeight
  (KNativePointer ptr, KFloat height) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    instance->setHeight(height);
}

SKIKO_EXPORT KFloat org_jetbrains_skia_paragraph_StrutStyle__1nGetLeading
  (KNativePointer ptr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    return instance->getLeading();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_StrutStyle__1nSetLeading
  (KNativePointer ptr, KFloat leading) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    instance->setLeading(leading);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_paragraph_StrutStyle__1nIsEnabled
  (KNativePointer ptr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    return instance->getStrutEnabled();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_StrutStyle__1nSetEnabled
  (KNativePointer ptr, KBoolean value) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    instance->setStrutEnabled(value);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_paragraph_StrutStyle__1nIsHeightForced
  (KNativePointer ptr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    return instance->getForceStrutHeight();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_StrutStyle__1nSetHeightForced
  (KNativePointer ptr, KBoolean value) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    instance->setForceStrutHeight(value);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_paragraph_StrutStyle__1nIsHeightOverridden
  (KNativePointer ptr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    return instance->getHeightOverride();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_StrutStyle__1nSetHeightOverridden
  (KNativePointer ptr, KBoolean value) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    instance->setHeightOverride(value);
}
