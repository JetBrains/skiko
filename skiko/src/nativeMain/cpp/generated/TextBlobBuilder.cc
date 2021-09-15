
// This file has been auto generated.

#include <iostream>
#include "SkTextBlob.h"
#include "common.h"

static void deleteTextBlobBuilder(SkTextBlobBuilder* ptr) {
    delete ptr;
}

extern "C" jlong org_jetbrains_skia_TextBlobBuilder__1nGetFinalizer
  () {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteTextBlobBuilder));
}

extern "C" jlong org_jetbrains_skia_TextBlobBuilder__1nMake
  () {
    return reinterpret_cast<jlong>(new SkTextBlobBuilder());
}

extern "C" jlong org_jetbrains_skia_TextBlobBuilder__1nBuild
  (jlong ptr) {
    SkTextBlobBuilder* instance = reinterpret_cast<SkTextBlobBuilder*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(instance->make().release());
}


extern "C" void org_jetbrains_skia_TextBlobBuilder__1nAppendRun
  (jlong ptr, jlong fontPtr, jshortArray glyphsArr, jfloat x, jfloat y, jobject boundsObj) {
    TODO("implement org_jetbrains_skia_TextBlobBuilder__1nAppendRun");
}
     
#if 0 
extern "C" void org_jetbrains_skia_TextBlobBuilder__1nAppendRun
  (jlong ptr, jlong fontPtr, jshortArray glyphsArr, jfloat x, jfloat y, jobject boundsObj) {
    SkTextBlobBuilder* instance = reinterpret_cast<SkTextBlobBuilder*>(static_cast<uintptr_t>(ptr));
    SkFont* font = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(fontPtr));
    jsize len = env->GetArrayLength(glyphsArr);
    std::unique_ptr<SkRect> bounds = skija::Rect::toSkRect(env, boundsObj);
    SkTextBlobBuilder::RunBuffer run = instance->allocRun(*font, len, x, y, bounds.get());
    env->GetShortArrayRegion(glyphsArr, 0, len, reinterpret_cast<jshort*>(run.glyphs));
}
#endif



extern "C" void org_jetbrains_skia_TextBlobBuilder__1nAppendRunPosH
  (jlong ptr, jlong fontPtr, jshortArray glyphsArr, jfloatArray xsArr, jfloat y, jobject boundsObj) {
    TODO("implement org_jetbrains_skia_TextBlobBuilder__1nAppendRunPosH");
}
     
#if 0 
extern "C" void org_jetbrains_skia_TextBlobBuilder__1nAppendRunPosH
  (jlong ptr, jlong fontPtr, jshortArray glyphsArr, jfloatArray xsArr, jfloat y, jobject boundsObj) {
    SkTextBlobBuilder* instance = reinterpret_cast<SkTextBlobBuilder*>(static_cast<uintptr_t>(ptr));
    SkFont* font = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(fontPtr));
    jsize len = env->GetArrayLength(glyphsArr);
    std::unique_ptr<SkRect> bounds = skija::Rect::toSkRect(env, boundsObj);
    SkTextBlobBuilder::RunBuffer run = instance->allocRunPosH(*font, len, y, bounds.get());
    env->GetShortArrayRegion(glyphsArr, 0, len, reinterpret_cast<jshort*>(run.glyphs));
    env->GetFloatArrayRegion(xsArr, 0, len, reinterpret_cast<jfloat*>(run.pos));
}
#endif



extern "C" void org_jetbrains_skia_TextBlobBuilder__1nAppendRunPos
  (jlong ptr, jlong fontPtr, jshortArray glyphsArr, jfloatArray posArr, jobject boundsObj) {
    TODO("implement org_jetbrains_skia_TextBlobBuilder__1nAppendRunPos");
}
     
#if 0 
extern "C" void org_jetbrains_skia_TextBlobBuilder__1nAppendRunPos
  (jlong ptr, jlong fontPtr, jshortArray glyphsArr, jfloatArray posArr, jobject boundsObj) {
    SkTextBlobBuilder* instance = reinterpret_cast<SkTextBlobBuilder*>(static_cast<uintptr_t>(ptr));
    SkFont* font = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(fontPtr));
    jsize len = env->GetArrayLength(glyphsArr);
    std::unique_ptr<SkRect> bounds = skija::Rect::toSkRect(env, boundsObj);
    SkTextBlobBuilder::RunBuffer run = instance->allocRunPos(*font, len, bounds.get());
    env->GetShortArrayRegion(glyphsArr, 0, len, reinterpret_cast<jshort*>(run.glyphs));
    env->GetFloatArrayRegion(posArr, 0, len * 2, reinterpret_cast<jfloat*>(run.pos));
}
#endif



extern "C" void org_jetbrains_skia_TextBlobBuilder__1nAppendRunRSXform
  (jlong ptr, jlong fontPtr, jshortArray glyphsArr, jfloatArray xformArr) {
    TODO("implement org_jetbrains_skia_TextBlobBuilder__1nAppendRunRSXform");
}
     
#if 0 
extern "C" void org_jetbrains_skia_TextBlobBuilder__1nAppendRunRSXform
  (jlong ptr, jlong fontPtr, jshortArray glyphsArr, jfloatArray xformArr) {
    SkTextBlobBuilder* instance = reinterpret_cast<SkTextBlobBuilder*>(static_cast<uintptr_t>(ptr));
    SkFont* font = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(fontPtr));
    jsize len = env->GetArrayLength(glyphsArr);
    SkTextBlobBuilder::RunBuffer run = instance->allocRunRSXform(*font, len);
    env->GetShortArrayRegion(glyphsArr, 0, len, reinterpret_cast<jshort*>(run.glyphs));
    env->GetFloatArrayRegion(xformArr, 0, len * 4, reinterpret_cast<jfloat*>(run.pos));
}
#endif

