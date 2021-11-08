
// This file has been auto generated.

#include <cstring>
#include <iostream>
#include "SkShaper.h"
#include "common.h"
#include "TextLine.hh"

static void unrefTextLine(TextLine* ptr) {
    ptr->unref();
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_TextLine__1nGetFinalizer
  () {
    return reinterpret_cast<KNativePointer>(&unrefTextLine);
}


SKIKO_EXPORT KFloat org_jetbrains_skia_TextLine__1nGetAscent
  (KNativePointer ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(ptr);
    return instance->fAscent;
}

SKIKO_EXPORT KFloat org_jetbrains_skia_TextLine__1nGetCapHeight
  (KNativePointer ptr) {
  TextLine* instance = reinterpret_cast<TextLine*>(ptr);
  return instance->fCapHeight;
}


SKIKO_EXPORT KFloat org_jetbrains_skia_TextLine__1nGetXHeight
  (KNativePointer ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>((ptr));
    return instance->fXHeight;
}

SKIKO_EXPORT KFloat org_jetbrains_skia_TextLine__1nGetDescent
  (KNativePointer ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(ptr);
    return instance->fDescent;
}



SKIKO_EXPORT KFloat org_jetbrains_skia_TextLine__1nGetLeading
  (KNativePointer ptr) {
  TextLine* instance = reinterpret_cast<TextLine*>(ptr);
  return instance->fLeading;
}

SKIKO_EXPORT KFloat org_jetbrains_skia_TextLine__1nGetWidth
  (KNativePointer ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>((ptr));
    return instance->fWidth;
}

SKIKO_EXPORT KFloat org_jetbrains_skia_TextLine__1nGetHeight
  (KNativePointer ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>((ptr));
    return -instance->fAscent + instance->fDescent + instance->fLeading;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_TextLine__1nGetTextBlob
  (KNativePointer ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>((ptr));
    if (instance->fBlob == nullptr)
        return 0;
    instance->fBlob->ref();
    return reinterpret_cast<KNativePointer>(instance->fBlob.get());
}

SKIKO_EXPORT KInt org_jetbrains_skia_TextLine__1nGetGlyphsLength
  (KNativePointer ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>((ptr));
    return instance->fGlyphCount;
}

SKIKO_EXPORT void org_jetbrains_skia_TextLine__1nGetGlyphs
  (KNativePointer ptr, KInteropPointer glyphs, KInt resultLength) {
    TextLine* instance = reinterpret_cast<TextLine*>((ptr));

    size_t idx = 0;
    size_t freeBytesN = resultLength * sizeof(uint16_t);

    KShort* glyphsPtr = reinterpret_cast<KShort*>(glyphs);
    for (auto& run: instance->fRuns) {
        size_t addBytesLen = run.fGlyphCount * sizeof(uint16_t);
        if (freeBytesN - addBytesLen >= 0) {
            memcpy(&glyphsPtr[idx], run.fGlyphs, addBytesLen);
            idx += run.fGlyphCount;
            freeBytesN -= addBytesLen;
        } else {
            SkDEBUGFAIL("Incorrect resultGlyphs size");
        }
    }
    SkASSERTF(idx == instance->fGlyphCount, "TextLine.cc: idx = %d != instance->fGlyphCount = %d", idx, instance->fGlyphCount);
}

// Ensure resultArray has sufficient length (glyphCount * 2)
SKIKO_EXPORT void org_jetbrains_skia_TextLine__1nGetPositions
  (KNativePointer ptr, KFloat* resultArray) {
    TextLine* instance = reinterpret_cast<TextLine*>(ptr);
    size_t idx = 0;
    for (auto& run: instance->fRuns) {
        memcpy(resultArray + idx, run.fPos, run.fGlyphCount * sizeof(SkPoint));
        idx += 2 * run.fGlyphCount;
    }
    SkASSERTF(idx == 2 * instance->fGlyphCount, "TextLine.cc: idx = %d != 2 * instance->fGlyphCount = %d", idx, 2 * instance->fGlyphCount);
}

SKIKO_EXPORT KInt org_jetbrains_skia_TextLine__1nGetRunPositionsCount
  (KNativePointer ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(ptr);
    return instance->fRuns.size();
}

SKIKO_EXPORT void org_jetbrains_skia_TextLine__1nGetRunPositions
  (KNativePointer ptr, KFloat* resultArray) {
    TextLine* instance = reinterpret_cast<TextLine*>(ptr);
    size_t size = instance->fRuns.size();

    for (size_t idx = 0; idx < size; ++idx)
        resultArray[idx] = instance->fRuns[idx].fPosition;
}

SKIKO_EXPORT KInt org_jetbrains_skia_TextLine__1nGetBreakPositionsCount
  (KNativePointer ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(ptr);
    size_t count = 0;
    for (auto& run: instance->fRuns)
        count += run.fBreakPositions.size();
    return count;
}

SKIKO_EXPORT void org_jetbrains_skia_TextLine__1nGetBreakPositions
  (KNativePointer ptr, KFloat* resultArray) {
    TextLine* instance = reinterpret_cast<TextLine*>(ptr);
    size_t added = 0;
    for (auto& run: instance->fRuns) {
        size_t count = run.fBreakPositions.size();
        std::memcpy(resultArray + added, run.fBreakPositions.data(), count * sizeof(SkScalar));
        added += count;
    }
}


SKIKO_EXPORT KInt org_jetbrains_skia_TextLine__1nGetBreakOffsetsCount
  (KNativePointer ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(ptr);
    size_t count = 0;
    for (auto& run: instance->fRuns)
        count += run.fBreakOffsets.size();
    return count;
}

SKIKO_EXPORT void org_jetbrains_skia_TextLine__1nGetBreakOffsets
  (KNativePointer ptr, KInt* resultArray) {
    TextLine* instance = reinterpret_cast<TextLine*>(ptr);

    size_t added = 0;
    for (auto& run: instance->fRuns) {
        size_t count = run.fBreakOffsets.size();
        std::memcpy(&resultArray[added], run.fBreakOffsets.data(), count * sizeof(KInt));
        added += count;
    }
}

SKIKO_EXPORT KInt org_jetbrains_skia_TextLine__1nGetOffsetAtCoord
  (KNativePointer ptr, KFloat x) {
    TextLine* instance = reinterpret_cast<TextLine*>((ptr));

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

    return (KInt) instance->fRuns.back().fBreakOffsets.back();
}

SKIKO_EXPORT KInt org_jetbrains_skia_TextLine__1nGetLeftOffsetAtCoord
  (KNativePointer ptr, KFloat x) {
    TextLine* instance = reinterpret_cast<TextLine*>((ptr));

    if (instance->fRuns.empty())
        return 0;

    for (auto& run: instance->fRuns) {
        for (uint32_t idx = 0; idx < run.fBreakPositions.size() - 1; ++idx) {
            SkScalar breakRight = run.fBreakPositions[idx + 1];
            if (x < breakRight)
                return run.fBreakOffsets[idx];
        }
    }

    return (KInt) instance->fRuns.back().fBreakOffsets.back();
}

SKIKO_EXPORT KFloat org_jetbrains_skia_TextLine__1nGetCoordAtOffset
  (KNativePointer ptr, KInt offset16) {
    TextLine* instance = reinterpret_cast<TextLine*>((ptr));

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


