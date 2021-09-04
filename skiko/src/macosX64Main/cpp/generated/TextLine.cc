
// This file has been auto generated.

#include <cstring>
#include <iostream>
#include "SkShaper.h"
#include "common.h"


extern "C" jlong org_jetbrains_skia_TextLine__1nGetFinalizer
  (kref __Kinstance) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_TextLine__1nGetFinalizer");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_TextLine__1nGetFinalizer
  (kref __Kinstance) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&unrefTextLine));
}
#endif



extern "C" jfloat org_jetbrains_skia_TextLine__1nGetAscent
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_TextLine__1nGetAscent");
}
     
#if 0 
extern "C" jfloat org_jetbrains_skia_TextLine__1nGetAscent
  (kref __Kinstance, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    return instance->fAscent;
}
#endif



extern "C" jfloat org_jetbrains_skia_TextLine__1nGetCapHeight
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_TextLine__1nGetCapHeight");
}
     
#if 0 
extern "C" jfloat org_jetbrains_skia_TextLine__1nGetCapHeight
  (kref __Kinstance, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    return instance->fCapHeight;
}
#endif



extern "C" jfloat org_jetbrains_skia_TextLine__1nGetXHeight
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_TextLine__1nGetXHeight");
}
     
#if 0 
extern "C" jfloat org_jetbrains_skia_TextLine__1nGetXHeight
  (kref __Kinstance, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    return instance->fXHeight;
}
#endif



extern "C" jfloat org_jetbrains_skia_TextLine__1nGetDescent
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_TextLine__1nGetDescent");
}
     
#if 0 
extern "C" jfloat org_jetbrains_skia_TextLine__1nGetDescent
  (kref __Kinstance, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    return instance->fDescent;
}
#endif



extern "C" jfloat org_jetbrains_skia_TextLine__1nGetLeading
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_TextLine__1nGetLeading");
}
     
#if 0 
extern "C" jfloat org_jetbrains_skia_TextLine__1nGetLeading
  (kref __Kinstance, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    return instance->fLeading;
}
#endif



extern "C" jfloat org_jetbrains_skia_TextLine__1nGetWidth
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_TextLine__1nGetWidth");
}
     
#if 0 
extern "C" jfloat org_jetbrains_skia_TextLine__1nGetWidth
  (kref __Kinstance, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    return instance->fWidth;
}
#endif



extern "C" jfloat org_jetbrains_skia_TextLine__1nGetHeight
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_TextLine__1nGetHeight");
}
     
#if 0 
extern "C" jfloat org_jetbrains_skia_TextLine__1nGetHeight
  (kref __Kinstance, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    return -instance->fAscent + instance->fDescent + instance->fLeading;
}
#endif



extern "C" jlong org_jetbrains_skia_TextLine__1nGetTextBlob
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_TextLine__1nGetTextBlob");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_TextLine__1nGetTextBlob
  (kref __Kinstance, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    if (instance->fBlob == nullptr)
        return 0;
    instance->fBlob->ref();
    return reinterpret_cast<jlong>(instance->fBlob.get());
}
#endif



extern "C" jshortArray org_jetbrains_skia_TextLine__1nGetGlyphs
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_TextLine__1nGetGlyphs");
}
     
#if 0 
extern "C" jshortArray org_jetbrains_skia_TextLine__1nGetGlyphs
  (kref __Kinstance, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    std::vector<jshort> glyphs(instance->fGlyphCount);
    size_t idx = 0;
    for (auto& run: instance->fRuns) {
        memcpy(glyphs.data() + idx, run.fGlyphs, run.fGlyphCount * sizeof(uint16_t));
        idx += run.fGlyphCount;
    }
    SkASSERTF(idx == instance->fGlyphCount, "TextLine.cc: idx = %d != instance->fGlyphCount = %d", idx, instance->fGlyphCount);
    return javaShortArray(env, glyphs);
}
#endif



extern "C" jfloatArray org_jetbrains_skia_TextLine__1nGetPositions
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_TextLine__1nGetPositions");
}
     
#if 0 
extern "C" jfloatArray org_jetbrains_skia_TextLine__1nGetPositions
  (kref __Kinstance, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    std::vector<jfloat> positions(2 * instance->fGlyphCount);
    size_t idx = 0;
    for (auto& run: instance->fRuns) {
        memcpy(positions.data() + idx, run.fPos, run.fGlyphCount * sizeof(SkPoint));
        idx += 2 * run.fGlyphCount;
    }
    SkASSERTF(idx == 2 * instance->fGlyphCount, "TextLine.cc: idx = %d != 2 * instance->fGlyphCount = %d", idx, 2 * instance->fGlyphCount);
    return javaFloatArray(env, positions);
}
#endif



extern "C" jfloatArray org_jetbrains_skia_TextLine__1nGetRunPositions
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_TextLine__1nGetRunPositions");
}
     
#if 0 
extern "C" jfloatArray org_jetbrains_skia_TextLine__1nGetRunPositions
  (kref __Kinstance, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    std::vector<jfloat> positions(instance->fRuns.size());
    for (size_t idx = 0; idx < positions.size(); ++idx)
        positions[idx] = instance->fRuns[idx].fPosition;
    return javaFloatArray(env, positions);
}
#endif



extern "C" jfloatArray org_jetbrains_skia_TextLine__1nGetBreakPositions
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_TextLine__1nGetBreakPositions");
}
     
#if 0 
extern "C" jfloatArray org_jetbrains_skia_TextLine__1nGetBreakPositions
  (kref __Kinstance, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    std::vector<jfloat> positions;
    for (auto& run: instance->fRuns)
        positions.insert(positions.end(), run.fBreakPositions.begin(), run.fBreakPositions.end());
    return javaFloatArray(env, positions);
}
#endif



extern "C" jintArray org_jetbrains_skia_TextLine__1nGetBreakOffsets
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_TextLine__1nGetBreakOffsets");
}
     
#if 0 
extern "C" jintArray org_jetbrains_skia_TextLine__1nGetBreakOffsets
  (kref __Kinstance, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    std::vector<jint> offsets;
    for (auto& run: instance->fRuns)
        offsets.insert(offsets.end(), run.fBreakOffsets.begin(), run.fBreakOffsets.end());
    return javaIntArray(env, offsets);
}
#endif



extern "C" jint org_jetbrains_skia_TextLine__1nGetOffsetAtCoord
  (kref __Kinstance, jlong ptr, jfloat x) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_TextLine__1nGetOffsetAtCoord");
}
     
#if 0 
extern "C" jint org_jetbrains_skia_TextLine__1nGetOffsetAtCoord
  (kref __Kinstance, jlong ptr, jfloat x) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));

    if (instance->fRuns.empty())
        return 0;

    for (auto& run: instance->fRuns) {
        SkScalar breakLeft = run.fBreakPositions[0];
        for (uint32_t idx = 0; idx < run.fBreakPositions.size() - 1; ++idx) {
            SkScalar breakRight = run.fBreakPositions[idx + 1];
            if (x < (breakLeft + breakRight) / 2)
                return run.fBreakOffsets[idx];
            breakLeft = breakRight;
        }
    }

    return (jint) instance->fRuns.back().fBreakOffsets.back();
}
#endif



extern "C" jint org_jetbrains_skia_TextLine__1nGetLeftOffsetAtCoord
  (kref __Kinstance, jlong ptr, jfloat x) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_TextLine__1nGetLeftOffsetAtCoord");
}
     
#if 0 
extern "C" jint org_jetbrains_skia_TextLine__1nGetLeftOffsetAtCoord
  (kref __Kinstance, jlong ptr, jfloat x) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));

    if (instance->fRuns.empty())
        return 0;

    for (auto& run: instance->fRuns) {
        for (uint32_t idx = 0; idx < run.fBreakPositions.size() - 1; ++idx) {
            SkScalar breakRight = run.fBreakPositions[idx + 1];
            if (x < breakRight)
                return run.fBreakOffsets[idx];
        }
    }

    return (jint) instance->fRuns.back().fBreakOffsets.back();
}
#endif



extern "C" jfloat org_jetbrains_skia_TextLine__1nGetCoordAtOffset
  (kref __Kinstance, jlong ptr, jint offset16) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_TextLine__1nGetCoordAtOffset");
}
     
#if 0 
extern "C" jfloat org_jetbrains_skia_TextLine__1nGetCoordAtOffset
  (kref __Kinstance, jlong ptr, jint offset16) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));

    for (auto& run: instance->fRuns) {
        if (offset16 > run.fBreakOffsets.back())
            continue;

        for (uint32_t idx = 0; idx < run.fBreakPositions.size() - 1; ++idx) {
            if (offset16 < run.fBreakOffsets[idx] && idx > 0)
                return run.fBreakPositions[idx - 1];
            if (offset16 <= run.fBreakOffsets[idx])
                return run.fBreakPositions[idx];
        }
    }

    return instance->fWidth;
}
#endif

