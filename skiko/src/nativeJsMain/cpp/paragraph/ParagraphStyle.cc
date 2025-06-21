#include <iostream>
#include "ParagraphStyle.h"
using namespace std;
using namespace skia::textlayout;
#include "common.h"

static void deleteParagraphStyle(ParagraphStyle* instance) {
    delete instance;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_ParagraphStyle__1nGetFinalizer
  () {
    return reinterpret_cast<KNativePointer>((&deleteParagraphStyle));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_ParagraphStyle__1nMake
  () {
    ParagraphStyle* instance = new ParagraphStyle();
    instance->setApplyRoundingHack(false);
    return reinterpret_cast<KNativePointer>(instance);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_paragraph_ParagraphStyle__1nGetReplaceTabCharacters
    (KNativePointer ptr) {
  ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
  return instance->getReplaceTabCharacters();
}


SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetReplaceTabCharacters
    (KNativePointer ptr, KBoolean value) {
  ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
  instance->setReplaceTabCharacters(value);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_paragraph_ParagraphStyle__1nEquals
  (KNativePointer ptr, KNativePointer otherPtr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    ParagraphStyle* other = reinterpret_cast<ParagraphStyle*>((otherPtr));
    return *instance == *other;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_ParagraphStyle__1nGetStrutStyle
  (KNativePointer ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    StrutStyle* res = new StrutStyle();
    *res = instance->getStrutStyle();
    return reinterpret_cast<KNativePointer>(res);
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetStrutStyle
  (KNativePointer ptr, KNativePointer stylePtr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    StrutStyle* style = reinterpret_cast<StrutStyle*>((stylePtr));
    instance->setStrutStyle(*style);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_ParagraphStyle__1nGetTextStyle
  (KNativePointer ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    const TextStyle& style = instance->getTextStyle();
    TextStyle* res = new TextStyle(style);
    return reinterpret_cast<KNativePointer>(res);
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetTextStyle
  (KNativePointer ptr, KNativePointer textStylePtr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    TextStyle* textStyle = reinterpret_cast<TextStyle*>((textStylePtr));
    instance->setTextStyle(*textStyle);
}

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_ParagraphStyle__1nGetDirection
  (KNativePointer ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    return static_cast<KInt>(instance->getTextDirection());
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetDirection
  (KNativePointer ptr, KInt textDirection) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    instance->setTextDirection(static_cast<TextDirection>(textDirection));
}

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_ParagraphStyle__1nGetAlignment
  (KNativePointer ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    return static_cast<KInt>(instance->getTextAlign());
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetAlignment
  (KNativePointer ptr, KInt textAlign) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    instance->setTextAlign(static_cast<TextAlign>(textAlign));
}

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_ParagraphStyle__1nGetMaxLinesCount
  (KNativePointer ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    return static_cast<KInt>(instance->getMaxLines());
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetMaxLinesCount
  (KNativePointer ptr, KInt count) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    instance->setMaxLines(count);
}

SKIKO_EXPORT KInteropPointer org_jetbrains_skia_paragraph_ParagraphStyle__1nGetEllipsis
  (KNativePointer ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(ptr);
     return instance->ellipsized() ? new SkString(instance->getEllipsis()) : nullptr;
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetEllipsis
  (KNativePointer ptr, KInteropPointer ellipsis) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(ptr);
    instance->setEllipsis(skString(ellipsis));
}

SKIKO_EXPORT KFloat org_jetbrains_skia_paragraph_ParagraphStyle__1nGetHeight
  (KNativePointer ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    return instance->getHeight();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetHeight
  (KNativePointer ptr, KFloat height) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    instance->setHeight(height);
}

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_ParagraphStyle__1nGetHeightMode
  (KNativePointer ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    return static_cast<KInt>(instance->getTextHeightBehavior());
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetHeightMode
  (KNativePointer ptr, KInt heightMode) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    instance->setTextHeightBehavior(static_cast<TextHeightBehavior>(heightMode));
}

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_ParagraphStyle__1nGetEffectiveAlignment
  (KNativePointer ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    return static_cast<KInt>(instance->effective_align());
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_paragraph_ParagraphStyle__1nIsHintingEnabled
  (KNativePointer ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    return instance->hintingIsOn();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphStyle__1nDisableHinting
  (KNativePointer ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    instance->turnHintingOff();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetFontRastrSettings
  (KNativePointer ptr, KInt edging, KInt hinting, KBoolean subpixel) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(ptr);
    FontRastrSettings fontRastrSettings;
    fontRastrSettings.fEdging = static_cast<SkFont::Edging>(edging);
    fontRastrSettings.fHinting = static_cast<SkFontHinting>(hinting);
    fontRastrSettings.fSubpixel = subpixel;
    instance->setFontRastrSettings(fontRastrSettings);
}

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_ParagraphStyle__1nGetEdging
  (KNativePointer ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(ptr);
    FontRastrSettings fontRastrSettings = instance->getFontRastrSettings();
    return static_cast<KInt>(fontRastrSettings.fEdging);
}

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_ParagraphStyle__1nGetHinting
  (KNativePointer ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(ptr);
    FontRastrSettings fontRastrSettings = instance->getFontRastrSettings();
    return static_cast<KInt>(fontRastrSettings.fHinting);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_paragraph_ParagraphStyle__1nGetSubpixel
  (KNativePointer  ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(ptr);
    FontRastrSettings fontRastrSettings = instance->getFontRastrSettings();
    return fontRastrSettings.fSubpixel;
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_paragraph_ParagraphStyle__1nGetApplyRoundingHack
  (KNativePointer  ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(ptr);
    return instance->getApplyRoundingHack();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetApplyRoundingHack
  (KNativePointer  ptr, KBoolean val) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(ptr);
    instance->setApplyRoundingHack(val);
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphStyle__1nSetTextIndent
  (KNativePointer ptr, KFloat firstLine, KFloat restLine) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    TextIndent indent;
    indent.setFirstLine(firstLine);
    indent.setRestLine(restLine);
    instance->setTextIndent(indent);
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphStyle__1nGetTextIndent
  (KNativePointer ptr, KInteropPointer result) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>((ptr));
    TextIndent* indent = reinterpret_cast<TextIndent*>(result);
    *indent = instance->getTextIndent();
}
