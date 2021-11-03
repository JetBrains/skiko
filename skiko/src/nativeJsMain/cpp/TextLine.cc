
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
    TODO("implement org_jetbrains_skia_TextLine__1nGetAscent");
}

#if 0
SKIKO_EXPORT KFloat org_jetbrains_skia_TextLine__1nGetAscent
  (KNativePointer ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>((ptr));
    return instance->fAscent;
}
#endif



SKIKO_EXPORT KFloat org_jetbrains_skia_TextLine__1nGetCapHeight
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_TextLine__1nGetCapHeight");
}

#if 0
SKIKO_EXPORT KFloat org_jetbrains_skia_TextLine__1nGetCapHeight
  (KNativePointer ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>((ptr));
    return instance->fCapHeight;
}
#endif



SKIKO_EXPORT KFloat org_jetbrains_skia_TextLine__1nGetXHeight
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_TextLine__1nGetXHeight");
}

#if 0
SKIKO_EXPORT KFloat org_jetbrains_skia_TextLine__1nGetXHeight
  (KNativePointer ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>((ptr));
    return instance->fXHeight;
}
#endif



SKIKO_EXPORT KFloat org_jetbrains_skia_TextLine__1nGetDescent
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_TextLine__1nGetDescent");
}

#if 0
SKIKO_EXPORT KFloat org_jetbrains_skia_TextLine__1nGetDescent
  (KNativePointer ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>((ptr));
    return instance->fDescent;
}
#endif



SKIKO_EXPORT KFloat org_jetbrains_skia_TextLine__1nGetLeading
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_TextLine__1nGetLeading");
}

#if 0
SKIKO_EXPORT KFloat org_jetbrains_skia_TextLine__1nGetLeading
  (KNativePointer ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>((ptr));
    return instance->fLeading;
}
#endif



SKIKO_EXPORT KFloat org_jetbrains_skia_TextLine__1nGetWidth
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_TextLine__1nGetWidth");
}

#if 0
SKIKO_EXPORT KFloat org_jetbrains_skia_TextLine__1nGetWidth
  (KNativePointer ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>((ptr));
    return instance->fWidth;
}
#endif



SKIKO_EXPORT KFloat org_jetbrains_skia_TextLine__1nGetHeight
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_TextLine__1nGetHeight");
}

#if 0
SKIKO_EXPORT KFloat org_jetbrains_skia_TextLine__1nGetHeight
  (KNativePointer ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>((ptr));
    return -instance->fAscent + instance->fDescent + instance->fLeading;
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_TextLine__1nGetTextBlob
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_TextLine__1nGetTextBlob");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_TextLine__1nGetTextBlob
  (KNativePointer ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>((ptr));
    if (instance->fBlob == nullptr)
        return 0;
    instance->fBlob->ref();
    return reinterpret_cast<KNativePointer>(instance->fBlob.get());
}
#endif


SKIKO_EXPORT KInt org_jetbrains_skia_TextLine__1nGetGlyphsLength
  (KNativePointer ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>((ptr));
    return instance->fGlyphCount;
}

SKIKO_EXPORT void org_jetbrains_skia_TextLine__1nGetGlyphs
  (KNativePointer ptr, KInteropPointer glyphs) {
    TextLine* instance = reinterpret_cast<TextLine*>((ptr));
    size_t idx = 0;
    KShort* glyphsPtr = reinterpret_cast<KShort*>(glyphs);
    for (auto& run: instance->fRuns) {
        memcpy(glyphsPtr + idx, run.fGlyphs, run.fGlyphCount * sizeof(uint16_t));
        idx += run.fGlyphCount;
    }
    SkASSERTF(idx == instance->fGlyphCount, "TextLine.cc: idx = %d != instance->fGlyphCount = %d", idx, instance->fGlyphCount);
}


SKIKO_EXPORT KFloat* org_jetbrains_skia_TextLine__1nGetPositions
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_TextLine__1nGetPositions");
}

#if 0
SKIKO_EXPORT KFloat* org_jetbrains_skia_TextLine__1nGetPositions
  (KNativePointer ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>((ptr));
    std::vector<KFloat> positions(2 * instance->fGlyphCount);
    size_t idx = 0;
    for (auto& run: instance->fRuns) {
        memcpy(positions.data() + idx, run.fPos, run.fGlyphCount * sizeof(SkPoint));
        idx += 2 * run.fGlyphCount;
    }
    SkASSERTF(idx == 2 * instance->fGlyphCount, "TextLine.cc: idx = %d != 2 * instance->fGlyphCount = %d", idx, 2 * instance->fGlyphCount);
    return javaFloatArray(env, positions);
}
#endif



SKIKO_EXPORT KFloat* org_jetbrains_skia_TextLine__1nGetRunPositions
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_TextLine__1nGetRunPositions");
}

#if 0
SKIKO_EXPORT KFloat* org_jetbrains_skia_TextLine__1nGetRunPositions
  (KNativePointer ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>((ptr));
    std::vector<KFloat> positions(instance->fRuns.size());
    for (size_t idx = 0; idx < positions.size(); ++idx)
        positions[idx] = instance->fRuns[idx].fPosition;
    return javaFloatArray(env, positions);
}
#endif



SKIKO_EXPORT KFloat* org_jetbrains_skia_TextLine__1nGetBreakPositions
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_TextLine__1nGetBreakPositions");
}

#if 0
SKIKO_EXPORT KFloat* org_jetbrains_skia_TextLine__1nGetBreakPositions
  (KNativePointer ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>((ptr));
    std::vector<KFloat> positions;
    for (auto& run: instance->fRuns)
        positions.insert(positions.end(), run.fBreakPositions.begin(), run.fBreakPositions.end());
    return javaFloatArray(env, positions);
}
#endif



SKIKO_EXPORT KInt* org_jetbrains_skia_TextLine__1nGetBreakOffsets
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_TextLine__1nGetBreakOffsets");
}

#if 0
SKIKO_EXPORT KInt* org_jetbrains_skia_TextLine__1nGetBreakOffsets
  (KNativePointer ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>((ptr));
    std::vector<KInt> offsets;
    for (auto& run: instance->fRuns)
        offsets.insert(offsets.end(), run.fBreakOffsets.begin(), run.fBreakOffsets.end());
    return javaIntArray(env, offsets);
}
#endif



SKIKO_EXPORT KInt org_jetbrains_skia_TextLine__1nGetOffsetAtCoord
  (KNativePointer ptr, KFloat x) {
    TODO("implement org_jetbrains_skia_TextLine__1nGetOffsetAtCoord");
}

#if 0
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
#endif



SKIKO_EXPORT KInt org_jetbrains_skia_TextLine__1nGetLeftOffsetAtCoord
  (KNativePointer ptr, KFloat x) {
    TODO("implement org_jetbrains_skia_TextLine__1nGetLeftOffsetAtCoord");
}

#if 0
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
#endif



SKIKO_EXPORT KFloat org_jetbrains_skia_TextLine__1nGetCoordAtOffset
  (KNativePointer ptr, KInt offset16) {
    TODO("implement org_jetbrains_skia_TextLine__1nGetCoordAtOffset");
}

#if 0
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
#endif

