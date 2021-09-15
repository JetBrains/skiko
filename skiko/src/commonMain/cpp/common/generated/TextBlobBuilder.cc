
// This file has been auto generated.

#include <iostream>
#include "SkTextBlob.h"
#include "common.h"

static void deleteTextBlobBuilder(SkTextBlobBuilder* ptr) {
    delete ptr;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_TextBlobBuilder__1nGetFinalizer
  () {
    return reinterpret_cast<KNativePointer>((&deleteTextBlobBuilder));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_TextBlobBuilder__1nMake
  () {
    return reinterpret_cast<KNativePointer>(new SkTextBlobBuilder());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_TextBlobBuilder__1nBuild
  (KNativePointer ptr) {
    SkTextBlobBuilder* instance = reinterpret_cast<SkTextBlobBuilder*>((ptr));
    return reinterpret_cast<KNativePointer>(instance->make().release());
}


SKIKO_EXPORT void org_jetbrains_skia_TextBlobBuilder__1nAppendRun
  (KNativePointer ptr, KNativePointer fontPtr, KShort* glyphsArr, KFloat x, KFloat y, KInteropPointer boundsObj) {
    TODO("implement org_jetbrains_skia_TextBlobBuilder__1nAppendRun");
}
     
#if 0 
SKIKO_EXPORT void org_jetbrains_skia_TextBlobBuilder__1nAppendRun
  (KNativePointer ptr, KNativePointer fontPtr, KShort* glyphsArr, KFloat x, KFloat y, KInteropPointer boundsObj) {
    SkTextBlobBuilder* instance = reinterpret_cast<SkTextBlobBuilder*>((ptr));
    SkFont* font = reinterpret_cast<SkFont*>((fontPtr));
    jsize len = env->GetArrayLength(glyphsArr);
    std::unique_ptr<SkRect> bounds = skija::Rect::toSkRect(env, boundsObj);
    SkTextBlobBuilder::RunBuffer run = instance->allocRun(*font, len, x, y, bounds.get());
    env->GetShortArrayRegion(glyphsArr, 0, len, reinterpret_cast<KShort*>(run.glyphs));
}
#endif



SKIKO_EXPORT void org_jetbrains_skia_TextBlobBuilder__1nAppendRunPosH
  (KNativePointer ptr, KNativePointer fontPtr, KShort* glyphsArr, KFloat* xsArr, KFloat y, KInteropPointer boundsObj) {
    TODO("implement org_jetbrains_skia_TextBlobBuilder__1nAppendRunPosH");
}
     
#if 0 
SKIKO_EXPORT void org_jetbrains_skia_TextBlobBuilder__1nAppendRunPosH
  (KNativePointer ptr, KNativePointer fontPtr, KShort* glyphsArr, KFloat* xsArr, KFloat y, KInteropPointer boundsObj) {
    SkTextBlobBuilder* instance = reinterpret_cast<SkTextBlobBuilder*>((ptr));
    SkFont* font = reinterpret_cast<SkFont*>((fontPtr));
    jsize len = env->GetArrayLength(glyphsArr);
    std::unique_ptr<SkRect> bounds = skija::Rect::toSkRect(env, boundsObj);
    SkTextBlobBuilder::RunBuffer run = instance->allocRunPosH(*font, len, y, bounds.get());
    env->GetShortArrayRegion(glyphsArr, 0, len, reinterpret_cast<KShort*>(run.glyphs));
    env->GetFloatArrayRegion(xsArr, 0, len, reinterpret_cast<KFloat*>(run.pos));
}
#endif



SKIKO_EXPORT void org_jetbrains_skia_TextBlobBuilder__1nAppendRunPos
  (KNativePointer ptr, KNativePointer fontPtr, KShort* glyphsArr, KFloat* posArr, KInteropPointer boundsObj) {
    TODO("implement org_jetbrains_skia_TextBlobBuilder__1nAppendRunPos");
}
     
#if 0 
SKIKO_EXPORT void org_jetbrains_skia_TextBlobBuilder__1nAppendRunPos
  (KNativePointer ptr, KNativePointer fontPtr, KShort* glyphsArr, KFloat* posArr, KInteropPointer boundsObj) {
    SkTextBlobBuilder* instance = reinterpret_cast<SkTextBlobBuilder*>((ptr));
    SkFont* font = reinterpret_cast<SkFont*>((fontPtr));
    jsize len = env->GetArrayLength(glyphsArr);
    std::unique_ptr<SkRect> bounds = skija::Rect::toSkRect(env, boundsObj);
    SkTextBlobBuilder::RunBuffer run = instance->allocRunPos(*font, len, bounds.get());
    env->GetShortArrayRegion(glyphsArr, 0, len, reinterpret_cast<KShort*>(run.glyphs));
    env->GetFloatArrayRegion(posArr, 0, len * 2, reinterpret_cast<KFloat*>(run.pos));
}
#endif



SKIKO_EXPORT void org_jetbrains_skia_TextBlobBuilder__1nAppendRunRSXform
  (KNativePointer ptr, KNativePointer fontPtr, KShort* glyphsArr, KFloat* xformArr) {
    TODO("implement org_jetbrains_skia_TextBlobBuilder__1nAppendRunRSXform");
}
     
#if 0 
SKIKO_EXPORT void org_jetbrains_skia_TextBlobBuilder__1nAppendRunRSXform
  (KNativePointer ptr, KNativePointer fontPtr, KShort* glyphsArr, KFloat* xformArr) {
    SkTextBlobBuilder* instance = reinterpret_cast<SkTextBlobBuilder*>((ptr));
    SkFont* font = reinterpret_cast<SkFont*>((fontPtr));
    jsize len = env->GetArrayLength(glyphsArr);
    SkTextBlobBuilder::RunBuffer run = instance->allocRunRSXform(*font, len);
    env->GetShortArrayRegion(glyphsArr, 0, len, reinterpret_cast<KShort*>(run.glyphs));
    env->GetFloatArrayRegion(xformArr, 0, len * 4, reinterpret_cast<KFloat*>(run.pos));
}
#endif

