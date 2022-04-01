#include <cstring>
#include <iostream>
#include "SkData.h"
#include "SkSerialProcs.h"
#include "SkTextBlob.h"
#include "common.h"
#include "RunRecordClone.hh"
#include "mppinterop.h"
#include "TexBlobIter.hh"

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
  (KShort* glyphsArr, KInt glyphsLen, KFloat* xposArr, KFloat ypos, KNativePointer fontPtr) {
    SkFont* font = reinterpret_cast<SkFont*>(fontPtr);

    SkTextBlob* instance = SkTextBlob::MakeFromPosTextH(
        glyphsArr,
        glyphsLen * sizeof(short),
        xposArr, ypos,
        *font,
        SkTextEncoding::kGlyphID
    ).release();

    return reinterpret_cast<KNativePointer>(instance);
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
  (KShort* glyphsArr, KInt glyphsLen, KFloat* xformArr, KNativePointer fontPtr ) {
   SkFont* font = reinterpret_cast<SkFont*>(fontPtr);

   SkTextBlob* instance = SkTextBlob::MakeFromRSXform(
        glyphsArr, glyphsLen * sizeof(short),
        reinterpret_cast<SkRSXform*>(xformArr),
        *font, SkTextEncoding::kGlyphID
   ).release();

   return reinterpret_cast<KNativePointer>(instance);
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
      return skikoMpp::textblob::getGlyphsLength(instance);
}

SKIKO_EXPORT void org_jetbrains_skia_TextBlob__1nGetGlyphs
  (KNativePointer ptr, KShort* resultArray) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(ptr);
    skikoMpp::textblob::getGlyphs(instance, resultArray);
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

SKIKO_EXPORT KInt org_jetbrains_skia_TextBlob__1nGetClustersLength
  (KNativePointer ptr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(ptr);
    return skikoMpp::textblob::getClustersLength(instance);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_TextBlob__1nGetClusters
  (KNativePointer ptr, KInt* clusters) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(ptr);
    return skikoMpp::textblob::getClusters(instance, clusters);
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

SKIKO_EXPORT KBoolean org_jetbrains_skia_TextBlob__1nGetFirstBaseline
  (KNativePointer ptr, KFloat* resultArray) {
  return skikoMpp::textblob::getFirstBaseline(reinterpret_cast<SkTextBlob*>(ptr), resultArray);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_TextBlob__1nGetLastBaseline
  (KNativePointer ptr, KFloat* resultArray) {
  return skikoMpp::textblob::getLastBaseline(reinterpret_cast<SkTextBlob*>(ptr), resultArray);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_TextBlob_Iter__1nCreate(KNativePointer textBlobPtr) {
    return new TextBlobIter(reinterpret_cast<SkTextBlob*>(textBlobPtr));
}

static void deleteTextBlobIter(TextBlobIter* ptr) {
    delete ptr;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_TextBlob_Iter__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>(&deleteTextBlobIter);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_TextBlob_Iter__1nFetch(KNativePointer ptr) {
    TextBlobIter* instance = reinterpret_cast<TextBlobIter*>(ptr);
    return (KBoolean) instance->fetch();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_TextBlob_Iter__1nHasNext(KNativePointer ptr) {
    TextBlobIter* instance = reinterpret_cast<TextBlobIter*>(ptr);
    return (KBoolean) instance->hasNext();
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_TextBlob_Iter__1nGetTypeface(KNativePointer ptr) {
    TextBlobIter* instance = reinterpret_cast<TextBlobIter*>(ptr);
    return (KNativePointer) instance->getTypeface().release();
}

SKIKO_EXPORT KInt org_jetbrains_skia_TextBlob_Iter__1nGetGlyphCount(KNativePointer ptr) {
    TextBlobIter* instance = reinterpret_cast<TextBlobIter*>(ptr);
    return (KInt) instance->getGlyphCount();
}

SKIKO_EXPORT KInt org_jetbrains_skia_TextBlob_Iter__1nGetGlyphs(KNativePointer ptr, KInteropPointer dst, KInt max) {
    TextBlobIter* instance = reinterpret_cast<TextBlobIter*>(ptr);
    return (KInt) instance->writeGlyphs((uint16_t*) dst, max);
}
