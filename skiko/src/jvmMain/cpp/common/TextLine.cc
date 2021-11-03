#include <cstring>
#include <iostream>
#include <jni.h>
#include "interop.hh"
#include "SkShaper.h"
#include "TextLine.hh"

static void unrefTextLine(TextLine* ptr) {
    ptr->unref();
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_TextLineKt_TextLine_1nGetFinalizer
  (JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&unrefTextLine));
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skia_TextLineKt__1nGetAscent
  (JNIEnv* env, jclass jclass, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    return instance->fAscent;
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skia_TextLineKt__1nGetCapHeight
  (JNIEnv* env, jclass jclass, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    return instance->fCapHeight;
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skia_TextLineKt__1nGetXHeight
  (JNIEnv* env, jclass jclass, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    return instance->fXHeight;
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skia_TextLineKt__1nGetDescent
  (JNIEnv* env, jclass jclass, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    return instance->fDescent;
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skia_TextLineKt__1nGetLeading
  (JNIEnv* env, jclass jclass, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    return instance->fLeading;
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skia_TextLineKt_TextLine_1nGetWidth
  (JNIEnv* env, jclass jclass, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    return instance->fWidth;
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skia_TextLineKt_TextLine_1nGetHeight
  (JNIEnv* env, jclass jclass, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    return -instance->fAscent + instance->fDescent + instance->fLeading;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_TextLineKt__1nGetTextBlob
  (JNIEnv* env, jclass jclass, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    if (instance->fBlob == nullptr)
        return 0;
    instance->fBlob->ref();
    return reinterpret_cast<jlong>(instance->fBlob.get());
}

extern "C" JNIEXPORT jint Java_org_jetbrains_skia_TextLineKt_TextLine_1nGetGlyphsLength
  (JNIEnv* env, jclass jclass, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    return instance->fGlyphCount;
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_TextLineKt_TextLine_1nGetGlyphs
  (JNIEnv* env, jclass jclass, jlong ptr, jshortArray resultGlyphs) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    jshort* glyphs = env->GetShortArrayElements(resultGlyphs, nullptr);

    size_t idx = 0;
    for (auto& run: instance->fRuns) {
        memcpy(glyphs + idx, run.fGlyphs, run.fGlyphCount * sizeof(uint16_t));
        idx += run.fGlyphCount;
    }
    env->ReleaseShortArrayElements(resultGlyphs, glyphs, 0);
    SkASSERTF(idx == instance->fGlyphCount, "TextLine.cc: idx = %d != instance->fGlyphCount = %d", idx, instance->fGlyphCount);
}

// Ensure resultArray has sufficient length (glyphCount * 2)
extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_TextLineKt_TextLine_1nGetPositions
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray resultArray) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    jfloat* positions = env->GetFloatArrayElements(resultArray, NULL);
    size_t idx = 0;
    for (auto& run: instance->fRuns) {
        memcpy(positions + idx, run.fPos, run.fGlyphCount * sizeof(SkPoint));
        idx += 2 * run.fGlyphCount;
    }
    env->ReleaseFloatArrayElements(resultArray, positions, 0);
    SkASSERTF(idx == 2 * instance->fGlyphCount, "TextLine.cc: idx = %d != 2 * instance->fGlyphCount = %d", idx, 2 * instance->fGlyphCount);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_TextLineKt__1nGetRunPositionsCount
  (JNIEnv* env, jclass jclass, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    return instance->fRuns.size();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_TextLineKt__1nGetRunPositions
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray resultArray) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    jfloat* positions = env->GetFloatArrayElements(resultArray, NULL);

    size_t size = instance->fRuns.size();
    for (size_t idx = 0; idx < size; ++idx)
        positions[idx] = instance->fRuns[idx].fPosition;

    env->ReleaseFloatArrayElements(resultArray, positions, 0);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_TextLineKt__1nGetBreakPositionsCount
  (JNIEnv* env, jclass jclass, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    size_t count = 0;
    for (auto& run: instance->fRuns)
        count += run.fBreakPositions.size();
    return count;
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_TextLineKt__1nGetBreakPositions
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray resultArray) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    jfloat* positions = env->GetFloatArrayElements(resultArray, NULL);
    size_t added = 0;
    for (auto& run: instance->fRuns) {
        size_t count = run.fBreakPositions.size();
        std::memcpy(positions + added, run.fBreakPositions.data(), count * sizeof(SkScalar));
        added += count;
    }
    env->ReleaseFloatArrayElements(resultArray, positions, 0);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_TextLineKt__1nGetBreakOffsetsCount
  (JNIEnv* env, jclass jclass, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    size_t count = 0;
    for (auto& run: instance->fRuns)
        count += run.fBreakOffsets.size();
    return count;
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_TextLineKt__1nGetBreakOffsets
  (JNIEnv* env, jclass jclass, jlong ptr, jintArray resultArray) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    jint* offsets = env->GetIntArrayElements(resultArray, NULL);

    size_t added = 0;
    for (auto& run: instance->fRuns) {
        size_t count = run.fBreakOffsets.size();
        std::memcpy(offsets + added, run.fBreakOffsets.data(), count * sizeof(uint32_t));
        added += count;
    }
    env->ReleaseIntArrayElements(resultArray, offsets, 0);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_TextLineKt__1nGetOffsetAtCoord
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat x) {
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

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_TextLineKt__1nGetLeftOffsetAtCoord
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat x) {
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

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skia_TextLineKt__1nGetCoordAtOffset
  (JNIEnv* env, jclass jclass, jlong ptr, jint offset16) {
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
