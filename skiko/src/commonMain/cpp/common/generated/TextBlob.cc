
// This file has been auto generated.

#include <cstring>
#include <iostream>
#include "SkData.h"
#include "SkSerialProcs.h"
#include "SkTextBlob.h"
#include "common.h"

static void unrefTextBlob(SkTextBlob* ptr) {
    ptr->unref();
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_TextBlob__1nGetFinalizer
  (KInteropPointer __Kinstance) {
    return reinterpret_cast<KNativePointer>((&unrefTextBlob));
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_TextBlob__1nBounds
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nBounds");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_TextBlob__1nBounds
  (KNativePointer ptr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>((ptr));
    SkRect bounds = instance->bounds();
    return skija::Rect::fromSkRect(env, instance->bounds());
}
#endif


SKIKO_EXPORT KInt org_jetbrains_skia_TextBlob__1nGetUniqueId
  (KNativePointer ptr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>((ptr));
    return instance->uniqueID();
}


SKIKO_EXPORT KFloat* org_jetbrains_skia_TextBlob__1nGetIntercepts
  (KNativePointer ptr, KFloat lower, KFloat upper, KNativePointer paintPtr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nGetIntercepts");
}
     
SKIKO_EXPORT KFloat* org_jetbrains_skia_TextBlob__1nGetIntercepts
  (KNativePointer ptr, KFloat lower, KFloat upper, KNativePointer paintPtr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>((ptr));
    std::vector<float> bounds {lower, upper};
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    int len = instance->getIntercepts(bounds.data(), nullptr, paint);
    std::vector<float> intervals(len);
    instance->getIntercepts(bounds.data(), intervals.data(), paint);
    return javaFloatArray(env, intervals);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_TextBlob__1nMakeFromPosH
  (KShort* glyphsArr, KFloat* xposArr, KFloat ypos, KNativePointer fontPtr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nMakeFromPosH");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_TextBlob__1nMakeFromPosH
  (KShort* glyphsArr, KFloat* xposArr, KFloat ypos, KNativePointer fontPtr) {
    jsize len = env->GetArrayLength(glyphsArr);
    jshort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);
    KFloat* xpos = env->GetFloatArrayElements(xposArr, nullptr);
    SkFont* font = reinterpret_cast<SkFont*>((fontPtr));

    SkTextBlob* instance = SkTextBlob::MakeFromPosTextH(glyphs, len * sizeof(jshort), xpos, ypos, *font, SkTextEncoding::kGlyphID).release();

    env->ReleaseShortArrayElements(glyphsArr, glyphs, 0);
    env->ReleaseFloatArrayElements(xposArr, xpos, 0);

    return reinterpret_cast<KNativePointer>(instance);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_TextBlob__1nMakeFromPos
  (KShort* glyphsArr, KFloat* posArr, KNativePointer fontPtr ) {
    TODO("implement org_jetbrains_skia_TextBlob__1nMakeFromPos");
}
     
SKIKO_EXPORT KNativePointer org_jetbrains_skia_TextBlob__1nMakeFromRSXform
  (KShort* glyphsArr, KFloat* xformArr, KNativePointer fontPtr ) {
    TODO("implement org_jetbrains_skia_TextBlob__1nMakeFromRSXform");
}
     
SKIKO_EXPORT KNativePointer org_jetbrains_skia_TextBlob__1nMakeFromRSXform
  (KShort* glyphsArr, KFloat* xformArr, KNativePointer fontPtr ) {
    jsize len = env->GetArrayLength(glyphsArr);
    jshort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);
    KFloat* xform = env->GetFloatArrayElements(xformArr, nullptr);
    SkFont* font = reinterpret_cast<SkFont*>((fontPtr));

    SkTextBlob* instance = SkTextBlob::MakeFromRSXform(glyphs, len * sizeof(jshort), reinterpret_cast<SkRSXform*>(xform), *font, SkTextEncoding::kGlyphID).release();

    env->ReleaseShortArrayElements(glyphsArr, glyphs, 0);
    env->ReleaseFloatArrayElements(xformArr, xform, 0);

    return reinterpret_cast<KNativePointer>(instance);
}
#endif


SKIKO_EXPORT KNativePointer org_jetbrains_skia_TextBlob__1nSerializeToData
  (KNativePointer ptr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>((ptr));
    SkData* data = instance->serialize({}).release();
    return reinterpret_cast<KNativePointer>(data);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_TextBlob__1nMakeFromData
  (KNativePointer dataPtr) {
    SkData* data = reinterpret_cast<SkData*>((dataPtr));
    SkTextBlob* instance = SkTextBlob::Deserialize(data->data(), data->size(), {}).release();
    return reinterpret_cast<KNativePointer>(instance);
}


SKIKO_EXPORT KShort* org_jetbrains_skia_TextBlob__1nGetGlyphs
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nGetGlyphs");
}
     
#if 0 
SKIKO_EXPORT KShort* org_jetbrains_skia_TextBlob__1nGetGlyphs
  (KNativePointer ptr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>((ptr));
    SkTextBlob::Iter iter(*instance);
    SkTextBlob::Iter::Run run;
    std::vector<jshort> glyphs;
    size_t stored = 0;
    while (iter.next(&run)) {
        glyphs.resize(stored + run.fGlyphCount);
        memcpy(&glyphs[stored], run.fGlyphIndices, run.fGlyphCount * sizeof(uint16_t));
        stored += run.fGlyphCount;
    }
    return javaShortArray(env, glyphs);
}
#endif


SKIKO_EXPORT KFloat* org_jetbrains_skia_TextBlob__1nGetPositions
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nGetPositions");
}

SKIKO_EXPORT KInt* org_jetbrains_skia_TextBlob__1nGetClusters
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nGetClusters");
}
     
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_TextBlob__1nGetTightBounds
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nGetTightBounds");
}
     
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_TextBlob__1nGetBlockBounds
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nGetBlockBounds");
}
     


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_TextBlob__1nGetFirstBaseline
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nGetFirstBaseline");
}

SKIKO_EXPORT KInteropPointer org_jetbrains_skia_TextBlob__1nGetLastBaseline
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nGetLastBaseline");
}

