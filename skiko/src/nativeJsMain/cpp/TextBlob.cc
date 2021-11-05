
// This file has been auto generated.

#include <cstring>
#include <iostream>
#include "SkData.h"
#include "SkSerialProcs.h"
#include "SkTextBlob.h"
#include "common.h"
#include "RunRecordClone.hh"
#include "mppinterop.h"

static void unrefTextBlob(SkTextBlob* ptr) {
    ptr->unref();
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_TextBlob__1nGetFinalizer
  () {
    return reinterpret_cast<KNativePointer>((&unrefTextBlob));
}


SKIKO_EXPORT void org_jetbrains_skia_TextBlob__1nBounds
  (KNativePointer ptr, KInteropPointer resultFloats) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(ptr);
    SkRect bounds = instance->bounds();
    skikoMpp::skrect::serializeAs4Floats(bounds, reinterpret_cast<float*>(resultFloats));
}

SKIKO_EXPORT KInt org_jetbrains_skia_TextBlob__1nGetUniqueId
  (KNativePointer ptr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>((ptr));
    return instance->uniqueID();
}

SKIKO_EXPORT KInt org_jetbrains_skia_TextBlob__1nGetInterceptsLength
    (KNativePointer ptr, KFloat lower, KFloat upper, KNativePointer paintPtr) {

    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(ptr);
    SkPaint* paint = reinterpret_cast<SkPaint*>(paintPtr);
    std::vector<float> bounds {lower, upper};
    int len = instance->getIntercepts(bounds.data(), nullptr, paint);
    return len;
}

SKIKO_EXPORT void org_jetbrains_skia_TextBlob__1nGetIntercepts
  (KNativePointer ptr, KFloat lower, KFloat upper, KNativePointer paintPtr, KFloat* resultArray) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(ptr);
    SkPaint* paint = reinterpret_cast<SkPaint*>(paintPtr);
    std::vector<float> bounds {lower, upper};
    instance->getIntercepts(bounds.data(), resultArray, paint);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_TextBlob__1nMakeFromPosH
  (KShort* glyphsArr, KFloat* xposArr, KFloat ypos, KNativePointer fontPtr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nMakeFromPosH");
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_TextBlob__1nMakeFromPos
  (KShort* glyphsArr, KInt glyphsLen, KFloat* posArr, KNativePointer fontPtr ) {
    SkFont* font = reinterpret_cast<SkFont*>(fontPtr);

    SkTextBlob* instance = SkTextBlob::MakeFromPosText(
        glyphsArr,
        glyphsLen * sizeof(KShort),
        reinterpret_cast<SkPoint*>(posArr),
        *font,
        SkTextEncoding::kGlyphID
    ).release();

    return reinterpret_cast<KNativePointer>(instance);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_TextBlob__1nMakeFromRSXform
  (KShort* glyphsArr, KFloat* xformArr, KNativePointer fontPtr ) {
    TODO("implement org_jetbrains_skia_TextBlob__1nMakeFromRSXform");
}

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

SKIKO_EXPORT KInt org_jetbrains_skia_TextBlob__1nGetGlyphsLength
  (KNativePointer ptr) {
      SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(ptr);
      SkTextBlob::Iter iter(*instance);
      SkTextBlob::Iter::Run run;
      int stored = 0;
      while (iter.next(&run)) {
          stored += run.fGlyphCount;
      }
      return stored;
}

SKIKO_EXPORT void org_jetbrains_skia_TextBlob__1nGetGlyphs
  (KNativePointer ptr, KShort* resultArray) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(ptr);
    SkTextBlob::Iter iter(*instance);
    SkTextBlob::Iter::Run run;

    size_t stored = 0;
    while (iter.next(&run)) {
        memcpy(resultArray + stored, run.fGlyphIndices, run.fGlyphCount * sizeof(uint16_t));
        stored += run.fGlyphCount;
    }
}

SKIKO_EXPORT KInt org_jetbrains_skia_TextBlob__1nGetPositionsLength
  (KNativePointer ptr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(ptr);
    return skikoMpp::textblob::getPositionsLength(instance);
}

SKIKO_EXPORT void org_jetbrains_skia_TextBlob__1nGetPositions
  (KNativePointer ptr, KFloat* resultArray) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(ptr);
    skikoMpp::textblob::getPositions(instance, resultArray);
}

SKIKO_EXPORT KInt* org_jetbrains_skia_TextBlob__1nGetClusters
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nGetClusters");
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_TextBlob__1nGetTightBounds
  (KNativePointer ptr, KFloat* resultArray) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(ptr);

    auto bounds = skikoMpp::textblob::getTightBounds(instance);
    if (!bounds) return false;

    skikoMpp::skrect::serializeAs4Floats(*bounds, resultArray);

    return true;
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_TextBlob__1nGetBlockBounds
  (KNativePointer ptr, KFloat* resultArray) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(ptr);
    std::unique_ptr<SkRect> bounds = skikoMpp::textblob::getBlockBounds(instance);
    if (!bounds) return false;
    skikoMpp::skrect::serializeAs4Floats(*bounds, resultArray);
    return true;
}

SKIKO_EXPORT KInteropPointer org_jetbrains_skia_TextBlob__1nGetFirstBaseline
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nGetFirstBaseline");
}

SKIKO_EXPORT KInteropPointer org_jetbrains_skia_TextBlob__1nGetLastBaseline
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nGetLastBaseline");
}
