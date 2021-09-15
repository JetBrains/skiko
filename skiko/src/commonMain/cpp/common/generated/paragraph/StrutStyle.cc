
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
  (KInteropPointer __Kinstance) {
    return reinterpret_cast<KNativePointer>((&deleteStrutStyle));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_StrutStyle__1nMake
  (KInteropPointer __Kinstance) {
    StrutStyle* instance = new StrutStyle();
    return reinterpret_cast<KNativePointer>(instance);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_paragraph_StrutStyle__1nEquals
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer otherPtr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    StrutStyle* other = reinterpret_cast<StrutStyle*>((otherPtr));
    return *instance == *other;
}


SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_paragraph_StrutStyle__1nGetFontFamilies
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_paragraph_StrutStyle__1nGetFontFamilies");
}
     
#if 0 
SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_paragraph_StrutStyle__1nGetFontFamilies
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    return javaStringArray(env, instance->getFontFamilies());
}
#endif



SKIKO_EXPORT void org_jetbrains_skia_paragraph_StrutStyle__1nSetFontFamilies
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointerArray familiesArr) {
    TODO("implement org_jetbrains_skia_paragraph_StrutStyle__1nSetFontFamilies");
}
     
#if 0 
SKIKO_EXPORT void org_jetbrains_skia_paragraph_StrutStyle__1nSetFontFamilies
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointerArray familiesArr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    instance->setFontFamilies(skStringVector(env, familiesArr));
}
#endif



SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_StrutStyle__1nGetFontStyle
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_paragraph_StrutStyle__1nGetFontStyle");
}
     
#if 0 
SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_StrutStyle__1nGetFontStyle
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    return skija::FontStyle::toJava(instance->getFontStyle());
}
#endif



SKIKO_EXPORT void org_jetbrains_skia_paragraph_StrutStyle__1nSetFontStyle
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt style) {
    TODO("implement org_jetbrains_skia_paragraph_StrutStyle__1nSetFontStyle");
}
     
#if 0 
SKIKO_EXPORT void org_jetbrains_skia_paragraph_StrutStyle__1nSetFontStyle
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt style) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    instance->setFontStyle(skija::FontStyle::fromJava(style));
}
#endif


SKIKO_EXPORT KFloat org_jetbrains_skia_paragraph_StrutStyle__1nGetFontSize
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    return instance->getFontSize();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_StrutStyle__1nSetFontSize
  (KInteropPointer __Kinstance, KNativePointer ptr, KFloat size) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    instance->setFontSize(size);
}

SKIKO_EXPORT KFloat org_jetbrains_skia_paragraph_StrutStyle__1nGetHeight
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    return instance->getHeight();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_StrutStyle__1nSetHeight
  (KInteropPointer __Kinstance, KNativePointer ptr, KFloat height) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    instance->setHeight(height);
}

SKIKO_EXPORT KFloat org_jetbrains_skia_paragraph_StrutStyle__1nGetLeading
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    return instance->getLeading();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_StrutStyle__1nSetLeading
  (KInteropPointer __Kinstance, KNativePointer ptr, KFloat leading) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    instance->setLeading(leading);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_paragraph_StrutStyle__1nIsEnabled
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    return instance->getStrutEnabled();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_StrutStyle__1nSetEnabled
  (KInteropPointer __Kinstance, KNativePointer ptr, KBoolean value) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    instance->setStrutEnabled(value);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_paragraph_StrutStyle__1nIsHeightForced
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    return instance->getForceStrutHeight();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_StrutStyle__1nSetHeightForced
  (KInteropPointer __Kinstance, KNativePointer ptr, KBoolean value) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    instance->setForceStrutHeight(value);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_paragraph_StrutStyle__1nIsHeightOverridden
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    return instance->getHeightOverride();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_StrutStyle__1nSetHeightOverridden
  (KInteropPointer __Kinstance, KNativePointer ptr, KBoolean value) {
    StrutStyle* instance = reinterpret_cast<StrutStyle*>((ptr));
    instance->setHeightOverride(value);
}
