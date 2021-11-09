
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
  (KNativePointer ptr, KNativePointer fontPtr, KShort* glyphsArr, KInt glyphsLen, KFloat x, KFloat y, KFloat* rectFloats) {

    SkTextBlobBuilder* instance = reinterpret_cast<SkTextBlobBuilder*>(ptr);
    SkFont* font = reinterpret_cast<SkFont*>(fontPtr);
    std::unique_ptr<SkRect> bounds = skikoMpp::skrect::toSkRect(reinterpret_cast<float*>(rectFloats));
    SkTextBlobBuilder::RunBuffer run = instance->allocRun(*font, glyphsLen, x, y, bounds.get());
    memcpy(run.glyphs, glyphsArr, glyphsLen * sizeof(KShort));
}


SKIKO_EXPORT void org_jetbrains_skia_TextBlobBuilder__1nAppendRunPosH
  (KNativePointer ptr, KNativePointer fontPtr, KShort* glyphsArr, KInt glyphsLen, KFloat* xsArr, KFloat y, KFloat* rectFloats) {
    SkTextBlobBuilder* instance = reinterpret_cast<SkTextBlobBuilder*>((ptr));
    SkFont* font = reinterpret_cast<SkFont*>((fontPtr));

    std::unique_ptr<SkRect> bounds = skikoMpp::skrect::toSkRect(reinterpret_cast<float*>(rectFloats));
    SkTextBlobBuilder::RunBuffer run = instance->allocRunPosH(*font, glyphsLen, y, bounds.get());

    memcpy(run.glyphs, glyphsArr, glyphsLen * sizeof(KShort));
    memcpy(run.pos, xsArr, glyphsLen * sizeof(KFloat)); // 1 float per position
}


SKIKO_EXPORT void org_jetbrains_skia_TextBlobBuilder__1nAppendRunPos
  (KNativePointer ptr, KNativePointer fontPtr, KShort* glyphsArr, KInt glyphsLen, KFloat* posArr, KFloat* rectFloats) {
    SkTextBlobBuilder* instance = reinterpret_cast<SkTextBlobBuilder*>(ptr);
    SkFont* font = reinterpret_cast<SkFont*>(fontPtr);

    std::unique_ptr<SkRect> bounds = skikoMpp::skrect::toSkRect(reinterpret_cast<float*>(rectFloats));
    SkTextBlobBuilder::RunBuffer run = instance->allocRunPos(*font, glyphsLen, bounds.get());

    memcpy(run.glyphs, glyphsArr, glyphsLen * sizeof(KShort));
    memcpy(run.pos, posArr, 2 * glyphsLen * sizeof(KFloat)); // 2 floats per position
}

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

