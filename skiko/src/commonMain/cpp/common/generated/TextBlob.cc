
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
  () {
    return reinterpret_cast<KNativePointer>((&unrefTextBlob));
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_TextBlob__1nBounds
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nBounds");
}

SKIKO_EXPORT KInt org_jetbrains_skia_TextBlob__1nGetUniqueId
  (KNativePointer ptr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>((ptr));
    return instance->uniqueID();
}

SKIKO_EXPORT KFloat* org_jetbrains_skia_TextBlob__1nGetIntercepts
  (KNativePointer ptr, KFloat lower, KFloat upper, KNativePointer paintPtr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nGetIntercepts");
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_TextBlob__1nMakeFromPosH
  (KShort* glyphsArr, KFloat* xposArr, KFloat ypos, KNativePointer fontPtr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nMakeFromPosH");
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_TextBlob__1nMakeFromPos
  (KShort* glyphsArr, KFloat* posArr, KNativePointer fontPtr ) {
    TODO("implement org_jetbrains_skia_TextBlob__1nMakeFromPos");
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


SKIKO_EXPORT KShort* org_jetbrains_skia_TextBlob__1nGetGlyphs
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nGetGlyphs");
}

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