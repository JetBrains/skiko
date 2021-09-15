
// This file has been auto generated.

#include <iostream>
#include "ParagraphStyle.h"
using namespace std;
using namespace skia::textlayout;
#include "common.h"

static void deleteParagraphStyle(ParagraphStyle* instance) {
    delete instance;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_ParagraphStyle__1nGetFinalizer
  (KInteropPointer __Kinstance) {
    return reinterpret_cast<KNativePointer>((&deleteParagraphStyle));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_ParagraphStyle__1nMake
  (KInteropPointer __Kinstance) {
    ParagraphStyle* instance = new ParagraphStyle();
    return reinterpret_cast<KNativePointer>(instance);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_paragraph_ParagraphStyle__1nEquals
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer otherPtr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    ParagraphStyle* other = reinterpret_cast<ParagraphStyle*>((otherPtr));
    return *instance == *other;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_ParagraphStyle__1nGetStrutStyle
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    StrutStyle* res = new StrutStyle();
    *res = instance->getStrutStyle();
    return reinterpret_cast<KNativePointer>(res);
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetStrutStyle
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer stylePtr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    StrutStyle* style = reinterpret_cast<StrutStyle*>((stylePtr));
    instance->setStrutStyle(*style);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_ParagraphStyle__1nGetTextStyle
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    const TextStyle& style = instance->getTextStyle();
    TextStyle* res = new TextStyle(style, style.isPlaceholder());
    return reinterpret_cast<KNativePointer>(res);
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetTextStyle
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer textStylePtr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    TextStyle* textStyle = reinterpret_cast<TextStyle*>((textStylePtr));
    instance->setTextStyle(*textStyle);
}

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_ParagraphStyle__1nGetDirection
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    return static_cast<KInt>(instance->getTextDirection());
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetDirection
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt textDirection) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    instance->setTextDirection(static_cast<TextDirection>(textDirection));
}

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_ParagraphStyle__1nGetAlignment
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    return static_cast<KInt>(instance->getTextAlign());
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetAlignment
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt textAlign) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    instance->setTextAlign(static_cast<TextAlign>(textAlign));
}

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_ParagraphStyle__1nGetMaxLinesCount
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    return static_cast<KInt>(instance->getMaxLines());
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetMaxLinesCount
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt count) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    instance->setMaxLines(count);
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_paragraph_ParagraphStyle__1nGetEllipsis
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_paragraph_ParagraphStyle__1nGetEllipsis");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_paragraph_ParagraphStyle__1nGetEllipsis
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    return instance->ellipsized() ? javaString(env, instance->getEllipsis()) : nullptr;
}
#endif



SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetEllipsis
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointer ellipsis) {
    TODO("implement org_jetbrains_skia_paragraph_ParagraphStyle__1nSetEllipsis");
}
     
#if 0 
SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetEllipsis
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointer ellipsis) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    instance->setEllipsis(skString(env, ellipsis));
}
#endif


SKIKO_EXPORT KFloat org_jetbrains_skia_paragraph_ParagraphStyle__1nGetHeight
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    return instance->getHeight();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetHeight
  (KInteropPointer __Kinstance, KNativePointer ptr, KFloat height) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    instance->setHeight(height);
}

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_ParagraphStyle__1nGetHeightMode
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    return static_cast<KInt>(instance->getTextHeightBehavior());
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetHeightMode
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt heightMode) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    instance->setTextHeightBehavior(static_cast<TextHeightBehavior>(heightMode));
}

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_ParagraphStyle__1nGetEffectiveAlignment
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    return static_cast<KInt>(instance->effective_align());
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_paragraph_ParagraphStyle__1nIsHintingEnabled
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    return instance->hintingIsOn();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphStyle__1nDisableHinting
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    instance->turnHintingOff();
}
