#include <iostream>
#include <jni.h>
#include "../interop.hh"
#include "interop.hh"
#include "ParagraphStyle.h"

using namespace std;
using namespace skia::textlayout;

static void deleteParagraphStyle(ParagraphStyle* instance) {
    delete instance;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nGetFinalizer
  (JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteParagraphStyle));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nMake
  (JNIEnv* env, jclass jclass) {
    ParagraphStyle* instance = new ParagraphStyle();
    instance->setApplyRoundingHack(false);
    return reinterpret_cast<jlong>(instance);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nGetReplaceTabCharacters
    (JNIEnv* env, jclass jclass, jlong ptr) {
  ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
  return instance->getReplaceTabCharacters();
}


extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nSetReplaceTabCharacters
    (JNIEnv* env, jclass jclass, jlong ptr, jboolean value) {
  ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
  instance->setReplaceTabCharacters(value);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nEquals
  (JNIEnv* env, jclass jclass, jlong ptr, jlong otherPtr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    ParagraphStyle* other = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(otherPtr));
    return *instance == *other;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nGetStrutStyle
  (JNIEnv* env, jclass jclass, jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    StrutStyle* res = new StrutStyle();
    *res = instance->getStrutStyle();
    return reinterpret_cast<jlong>(res);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nSetStrutStyle
  (JNIEnv* env, jclass jclass, jlong ptr, jlong stylePtr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    StrutStyle* style = reinterpret_cast<StrutStyle*>(static_cast<uintptr_t>(stylePtr));
    instance->setStrutStyle(*style);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nGetTextStyle
  (JNIEnv* env, jclass jclass, jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    const TextStyle& style = instance->getTextStyle();
    TextStyle* res = new TextStyle(style);
    return reinterpret_cast<jlong>(res);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nSetTextStyle
  (JNIEnv* env, jclass jclass, jlong ptr, jlong textStylePtr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    TextStyle* textStyle = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(textStylePtr));
    instance->setTextStyle(*textStyle);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nGetDirection
  (JNIEnv* env, jclass jclass, jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    return static_cast<jint>(instance->getTextDirection());
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nSetDirection
  (JNIEnv* env, jclass jclass, jlong ptr, jint textDirection) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    instance->setTextDirection(static_cast<TextDirection>(textDirection));
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nGetAlignment
  (JNIEnv* env, jclass jclass, jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    return static_cast<jint>(instance->getTextAlign());
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nSetAlignment
  (JNIEnv* env, jclass jclass, jlong ptr, jint textAlign) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    instance->setTextAlign(static_cast<TextAlign>(textAlign));
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nGetMaxLinesCount
  (JNIEnv* env, jclass jclass, jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    return static_cast<jint>(instance->getMaxLines());
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nSetMaxLinesCount
  (JNIEnv* env, jclass jclass, jlong ptr, jint count) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    instance->setMaxLines(count);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nGetEllipsis
  (JNIEnv* env, jclass jclass, jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    return instance->ellipsized() ? reinterpret_cast<jlong>(new SkString(instance->getEllipsis())) : 0;
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nSetEllipsis
  (JNIEnv* env, jclass jclass, jlong ptr, jstring ellipsis) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    instance->setEllipsis(skString(env, ellipsis));
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nGetHeight
  (JNIEnv* env, jclass jclass, jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    return instance->getHeight();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nSetHeight
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat height) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    instance->setHeight(height);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nGetHeightMode
  (JNIEnv* env, jclass jclass, jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    return static_cast<jint>(instance->getTextHeightBehavior());
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nSetHeightMode
  (JNIEnv* env, jclass jclass, jlong ptr, jint heightMode) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    instance->setTextHeightBehavior(static_cast<TextHeightBehavior>(heightMode));
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nGetEffectiveAlignment
  (JNIEnv* env, jclass jclass, jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    return static_cast<jint>(instance->effective_align());
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nIsHintingEnabled
  (JNIEnv* env, jclass jclass, jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    return instance->hintingIsOn();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nSetFontRastrSettings
  (JNIEnv* env, jclass jclass, jlong ptr, jint edging, jint hinting, jboolean subpixel) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    FontRastrSettings fontRastrSettings;
    fontRastrSettings.fEdging = static_cast<SkFont::Edging>(edging);
    fontRastrSettings.fHinting = static_cast<SkFontHinting>(hinting);
    fontRastrSettings.fSubpixel = subpixel;
    instance->setFontRastrSettings(fontRastrSettings);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nGetEdging
  (JNIEnv* env, jclass jclass, jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    FontRastrSettings fontRastrSettings = instance->getFontRastrSettings();
    return static_cast<jint>(fontRastrSettings.fEdging);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nGetHinting
  (JNIEnv* env, jclass jclass, jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    FontRastrSettings fontRastrSettings = instance->getFontRastrSettings();
    return static_cast<jint>(fontRastrSettings.fHinting);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nGetSubpixel
  (JNIEnv* env, jclass jclass, jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    FontRastrSettings fontRastrSettings = instance->getFontRastrSettings();
    return fontRastrSettings.fSubpixel;
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nDisableHinting
  (JNIEnv* env, jclass jclass, jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    instance->turnHintingOff();
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nGetApplyRoundingHack
  (JNIEnv* env, jclass jclass, jlong ptr) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    return instance->getApplyRoundingHack();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nSetApplyRoundingHack
  (JNIEnv* env, jclass jclass, jlong ptr, jboolean val) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    instance->setApplyRoundingHack(val);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nSetTextIndent
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat firstLine, jfloat restLine) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    TextIndent indent;
    indent.setFirstLine(firstLine);
    indent.setRestLine(restLine);
    instance->setTextIndent(indent);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_paragraph_ParagraphStyleExternalKt_ParagraphStyle_1nGetTextIndent
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray result) {
    ParagraphStyle* instance = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(ptr));
    TextIndent indent = instance->getTextIndent();
    float resultData[2] = { indent.getFirstLine(), indent.getRestLine() };
    env->SetFloatArrayRegion(result, 0, 2, resultData);
}
