
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

extern "C" jlong org_jetbrains_skia_TextBlob__1nGetFinalizer
  () {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&unrefTextBlob));
}


extern "C" jobject org_jetbrains_skia_TextBlob__1nBounds
  (jlong ptr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nBounds");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_TextBlob__1nBounds
  (jlong ptr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
    SkRect bounds = instance->bounds();
    return skija::Rect::fromSkRect(env, instance->bounds());
}
#endif


extern "C" jint org_jetbrains_skia_TextBlob__1nGetUniqueId
  (jlong ptr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
    return instance->uniqueID();
}


extern "C" jfloatArray org_jetbrains_skia_TextBlob__1nGetIntercepts
  (jlong ptr, jfloat lower, jfloat upper, jlong paintPtr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nGetIntercepts");
}
     
#if 0 
extern "C" jfloatArray org_jetbrains_skia_TextBlob__1nGetIntercepts
  (jlong ptr, jfloat lower, jfloat upper, jlong paintPtr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
    std::vector<float> bounds {lower, upper};
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    int len = instance->getIntercepts(bounds.data(), nullptr, paint);
    std::vector<float> intervals(len);
    instance->getIntercepts(bounds.data(), intervals.data(), paint);
    return javaFloatArray(env, intervals);
}
#endif



extern "C" jlong org_jetbrains_skia_TextBlob__1nMakeFromPosH
  (jshortArray glyphsArr, jfloatArray xposArr, jfloat ypos, jlong fontPtr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nMakeFromPosH");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_TextBlob__1nMakeFromPosH
  (jshortArray glyphsArr, jfloatArray xposArr, jfloat ypos, jlong fontPtr) {
    jsize len = env->GetArrayLength(glyphsArr);
    jshort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);
    jfloat* xpos = env->GetFloatArrayElements(xposArr, nullptr);
    SkFont* font = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(fontPtr));

    SkTextBlob* instance = SkTextBlob::MakeFromPosTextH(glyphs, len * sizeof(jshort), xpos, ypos, *font, SkTextEncoding::kGlyphID).release();

    env->ReleaseShortArrayElements(glyphsArr, glyphs, 0);
    env->ReleaseFloatArrayElements(xposArr, xpos, 0);

    return reinterpret_cast<jlong>(instance);
}
#endif



extern "C" jlong org_jetbrains_skia_TextBlob__1nMakeFromPos
  (jshortArray glyphsArr, jfloatArray posArr, jlong fontPtr ) {
    TODO("implement org_jetbrains_skia_TextBlob__1nMakeFromPos");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_TextBlob__1nMakeFromPos
  (jshortArray glyphsArr, jfloatArray posArr, jlong fontPtr ) {
    jsize len = env->GetArrayLength(glyphsArr);
    jshort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);
    jfloat* pos = env->GetFloatArrayElements(posArr, nullptr);
    SkFont* font = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(fontPtr));

    SkTextBlob* instance = SkTextBlob::MakeFromPosText(glyphs, len * sizeof(jshort), reinterpret_cast<SkPoint*>(pos), *font, SkTextEncoding::kGlyphID).release();

    env->ReleaseShortArrayElements(glyphsArr, glyphs, 0);
    env->ReleaseFloatArrayElements(posArr, pos, 0);

    return reinterpret_cast<jlong>(instance);
}
#endif



extern "C" jlong org_jetbrains_skia_TextBlob__1nMakeFromRSXform
  (jshortArray glyphsArr, jfloatArray xformArr, jlong fontPtr ) {
    TODO("implement org_jetbrains_skia_TextBlob__1nMakeFromRSXform");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_TextBlob__1nMakeFromRSXform
  (jshortArray glyphsArr, jfloatArray xformArr, jlong fontPtr ) {
    jsize len = env->GetArrayLength(glyphsArr);
    jshort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);
    jfloat* xform = env->GetFloatArrayElements(xformArr, nullptr);
    SkFont* font = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(fontPtr));

    SkTextBlob* instance = SkTextBlob::MakeFromRSXform(glyphs, len * sizeof(jshort), reinterpret_cast<SkRSXform*>(xform), *font, SkTextEncoding::kGlyphID).release();

    env->ReleaseShortArrayElements(glyphsArr, glyphs, 0);
    env->ReleaseFloatArrayElements(xformArr, xform, 0);

    return reinterpret_cast<jlong>(instance);
}
#endif


extern "C" jlong org_jetbrains_skia_TextBlob__1nSerializeToData
  (jlong ptr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
    SkData* data = instance->serialize({}).release();
    return reinterpret_cast<jlong>(data);
}

extern "C" jlong org_jetbrains_skia_TextBlob__1nMakeFromData
  (jlong dataPtr) {
    SkData* data = reinterpret_cast<SkData*>(static_cast<uintptr_t>(dataPtr));
    SkTextBlob* instance = SkTextBlob::Deserialize(data->data(), data->size(), {}).release();
    return reinterpret_cast<jlong>(instance);
}


extern "C" jshortArray org_jetbrains_skia_TextBlob__1nGetGlyphs
  (jlong ptr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nGetGlyphs");
}
     
#if 0 
extern "C" jshortArray org_jetbrains_skia_TextBlob__1nGetGlyphs
  (jlong ptr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
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



extern "C" jfloatArray org_jetbrains_skia_TextBlob__1nGetPositions
  (jlong ptr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nGetPositions");
}
     
#if 0 
extern "C" jfloatArray org_jetbrains_skia_TextBlob__1nGetPositions
  (jlong ptr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
    SkTextBlob::Iter iter(*instance);
    SkTextBlob::Iter::Run run;
    std::vector<jfloat> positions;
    size_t stored = 0;
    while (iter.next(&run)) {
        // run.fGlyphIndices points directly to runRecord.glyphBuffer(), which comes directly after RunRecord itself
        auto runRecord = reinterpret_cast<const RunRecordClone*>(run.fGlyphIndices) - 1;
        unsigned scalarsPerGlyph = RunRecordClone::ScalarsPerGlyph(runRecord->positioning());
        positions.resize(stored + run.fGlyphCount * scalarsPerGlyph);
        memcpy(&positions[stored], runRecord->posBuffer(), run.fGlyphCount * scalarsPerGlyph * sizeof(SkScalar));
        stored += run.fGlyphCount * scalarsPerGlyph;
    }
    return javaFloatArray(env, positions);
}
#endif



extern "C" jintArray org_jetbrains_skia_TextBlob__1nGetClusters
  (jlong ptr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nGetClusters");
}
     
#if 0 
extern "C" jintArray org_jetbrains_skia_TextBlob__1nGetClusters
  (jlong ptr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
    SkTextBlob::Iter iter(*instance);
    SkTextBlob::Iter::Run run;
    std::vector<jint> clusters;
    size_t stored = 0;
    // uint32_t cluster8 = 0;
    uint32_t runStart16 = 0;
    while (iter.next(&run)) {
        // run.fGlyphIndices points directly to runRecord.glyphBuffer(), which comes directly after RunRecord itself
        auto runRecord = reinterpret_cast<const RunRecordClone*>(run.fGlyphIndices) - 1;
        if (!runRecord->isExtended())
            return nullptr;
        
        skija::UtfIndicesConverter conv(runRecord->textBuffer(), runRecord->textSize());
        clusters.resize(stored + run.fGlyphCount);
        uint32_t* clusterBuffer = runRecord->clusterBuffer();
        for (int i = 0; i < run.fGlyphCount; ++i)
            clusters[stored + i] = runStart16 + conv.from8To16(clusterBuffer[i]);
        runStart16 += conv.from8To16(runRecord->textSize());
        // memcpy(&clusters[stored], runRecord->clusterBuffer(), run.fGlyphCount * sizeof(uint32_t));
        
        stored += run.fGlyphCount;
    }
    return javaIntArray(env, clusters);
}
#endif



extern "C" jobject org_jetbrains_skia_TextBlob__1nGetTightBounds
  (jlong ptr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nGetTightBounds");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_TextBlob__1nGetTightBounds
  (jlong ptr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
    SkTextBlob::Iter iter(*instance);
    SkTextBlob::Iter::Run run;
    auto bounds = SkRect::MakeEmpty();
    SkRect tmpBounds;
    while (iter.next(&run)) {
        // run.fGlyphIndices points directly to runRecord.glyphBuffer(), which comes directly after RunRecord itself
        auto runRecord = reinterpret_cast<const RunRecordClone*>(run.fGlyphIndices) - 1;
        if (runRecord->positioning() != 2) // kFull_Positioning
            return nullptr;
        
        runRecord->fFont.measureText(runRecord->glyphBuffer(), run.fGlyphCount * sizeof(uint16_t), SkTextEncoding::kGlyphID, &tmpBounds, nullptr);
        SkScalar* posBuffer = runRecord->posBuffer();
        tmpBounds.offset(posBuffer[0], posBuffer[1]);
        bounds.join(tmpBounds);
    }
    return skija::Rect::fromSkRect(env, bounds);
}
#endif



extern "C" jobject org_jetbrains_skia_TextBlob__1nGetBlockBounds
  (jlong ptr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nGetBlockBounds");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_TextBlob__1nGetBlockBounds
  (jlong ptr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
    SkTextBlob::Iter iter(*instance);
    SkTextBlob::Iter::Run run;
    auto bounds = SkRect::MakeEmpty();
    SkFontMetrics metrics;
    
    while (iter.next(&run)) {
        // run.fGlyphIndices points directly to runRecord.glyphBuffer(), which comes directly after RunRecord itself
        auto runRecord = reinterpret_cast<const RunRecordClone*>(run.fGlyphIndices) - 1;
        if (runRecord->positioning() != 2) // kFull_Positioning
            return nullptr;

        SkScalar* posBuffer = runRecord->posBuffer();
        const SkFont& font = runRecord->fFont;
        font.getMetrics(&metrics);
        
        SkScalar lastLeft = posBuffer[(run.fGlyphCount - 1) * 2];
        SkScalar lastWidth;
        if (run.fGlyphCount > 1 && SkScalarNearlyEqual(posBuffer[(run.fGlyphCount - 2) * 2], lastLeft))
            lastWidth = 0;
        else
            font.getWidths(&run.fGlyphIndices[run.fGlyphCount - 1], 1, &lastWidth);
        
        auto runBounds = SkRect::MakeLTRB(posBuffer[0], posBuffer[1] + metrics.fAscent, lastLeft + lastWidth, posBuffer[1] + metrics.fDescent);
        bounds.join(runBounds);
    }
    return skija::Rect::fromSkRect(env, bounds);
}
#endif



extern "C" jobject org_jetbrains_skia_TextBlob__1nGetFirstBaseline
  (jlong ptr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nGetFirstBaseline");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_TextBlob__1nGetFirstBaseline
  (jlong ptr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
    SkTextBlob::Iter iter(*instance);
    SkTextBlob::Iter::Run run;
    while (iter.next(&run)) {
        // run.fGlyphIndices points directly to runRecord.glyphBuffer(), which comes directly after RunRecord itself
        auto runRecord = reinterpret_cast<const RunRecordClone*>(run.fGlyphIndices) - 1;
        if (runRecord->positioning() != 2) // kFull_Positioning
            return nullptr;

        return javaFloat(env, runRecord->posBuffer()[1]);
    }
    return nullptr;
}
#endif



extern "C" jobject org_jetbrains_skia_TextBlob__1nGetLastBaseline
  (jlong ptr) {
    TODO("implement org_jetbrains_skia_TextBlob__1nGetLastBaseline");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_TextBlob__1nGetLastBaseline
  (jlong ptr) {
    SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
    SkTextBlob::Iter iter(*instance);
    SkTextBlob::Iter::Run run;
    SkScalar baseline = 0;
    while (iter.next(&run)) {
        // run.fGlyphIndices points directly to runRecord.glyphBuffer(), which comes directly after RunRecord itself
        auto runRecord = reinterpret_cast<const RunRecordClone*>(run.fGlyphIndices) - 1;
        if (runRecord->positioning() != 2) // kFull_Positioning
            return nullptr;

        baseline = std::max(baseline, runRecord->posBuffer()[1]);
    }
    return javaFloat(env, baseline);
}
#endif

