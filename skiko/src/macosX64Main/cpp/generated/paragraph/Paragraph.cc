
// This file has been auto generated.

#include <iostream>
#include "DartTypes.h"
#include "Paragraph.h"
using namespace std;
using namespace skia::textlayout;
#include "common.h"

static void deleteParagraph(Paragraph* instance) {
    delete instance;
}

extern "C" jlong org_jetbrains_skia_paragraph_Paragraph__1nGetFinalizer
  (kref __Kinstance) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteParagraph));
}

extern "C" jfloat org_jetbrains_skia_paragraph_Paragraph__1nGetMaxWidth
  (kref __Kinstance, jlong ptr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>(static_cast<uintptr_t>(ptr));
    return instance->getMaxWidth();
}

extern "C" jfloat org_jetbrains_skia_paragraph_Paragraph__1nGetHeight
  (kref __Kinstance, jlong ptr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>(static_cast<uintptr_t>(ptr));
    return instance->getHeight();
}

extern "C" jfloat org_jetbrains_skia_paragraph_Paragraph__1nGetMinIntrinsicWidth
  (kref __Kinstance, jlong ptr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>(static_cast<uintptr_t>(ptr));
    return instance->getMinIntrinsicWidth();
}

extern "C" jfloat org_jetbrains_skia_paragraph_Paragraph__1nGetMaxIntrinsicWidth
  (kref __Kinstance, jlong ptr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>(static_cast<uintptr_t>(ptr));
    return instance->getMaxIntrinsicWidth();
}

extern "C" jfloat org_jetbrains_skia_paragraph_Paragraph__1nGetAlphabeticBaseline
  (kref __Kinstance, jlong ptr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>(static_cast<uintptr_t>(ptr));
    return instance->getAlphabeticBaseline();
}

extern "C" jfloat org_jetbrains_skia_paragraph_Paragraph__1nGetIdeographicBaseline
  (kref __Kinstance, jlong ptr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>(static_cast<uintptr_t>(ptr));
    return instance->getIdeographicBaseline();
}

extern "C" jfloat org_jetbrains_skia_paragraph_Paragraph__1nGetLongestLine
  (kref __Kinstance, jlong ptr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>(static_cast<uintptr_t>(ptr));
    return instance->getLongestLine();
}

extern "C" jboolean org_jetbrains_skia_paragraph_Paragraph__1nDidExceedMaxLines
  (kref __Kinstance, jlong ptr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>(static_cast<uintptr_t>(ptr));
    return instance->didExceedMaxLines();
}

extern "C" void org_jetbrains_skia_paragraph_Paragraph__1nLayout
  (kref __Kinstance, jlong ptr, jfloat width) {
    Paragraph* instance = reinterpret_cast<Paragraph*>(static_cast<uintptr_t>(ptr));
    instance->layout(width);
}

extern "C" void org_jetbrains_skia_paragraph_Paragraph__1nPaint
  (kref __Kinstance, jlong ptr, jlong canvasPtr, jfloat x, jfloat y) {
    Paragraph* instance = reinterpret_cast<Paragraph*>(static_cast<uintptr_t>(ptr));
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    instance->paint(canvas, x, y);
}


extern "C" jobjectArray org_jetbrains_skia_paragraph_Paragraph__1nGetRectsForRange
  (kref __Kinstance, jlong ptr, jint start, jint end, jint rectHeightStyle, jint rectWidthStyle) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_paragraph_Paragraph__1nGetRectsForRange");
}
     
#if 0 
extern "C" jobjectArray org_jetbrains_skia_paragraph_Paragraph__1nGetRectsForRange
  (kref __Kinstance, jlong ptr, jint start, jint end, jint rectHeightStyle, jint rectWidthStyle) {
    Paragraph* instance = reinterpret_cast<Paragraph*>(static_cast<uintptr_t>(ptr));
    std::vector<TextBox> rects = instance->getRectsForRange(start, end, static_cast<RectHeightStyle>(rectHeightStyle), static_cast<RectWidthStyle>(rectWidthStyle));
    jobjectArray rectsArray = env->NewObjectArray((jsize) rects.size(), skija::paragraph::TextBox::cls, nullptr);
    for (int i = 0; i < rects.size(); ++i) {
        TextBox box = rects[i];
        jobject boxObj = env->NewObject(skija::paragraph::TextBox::cls, skija::paragraph::TextBox::ctor, box.rect.fLeft, box.rect.fTop, box.rect.fRight, box.rect.fBottom, static_cast<jint>(box.direction));
        env->SetObjectArrayElement(rectsArray, i, boxObj);
        env->DeleteLocalRef(boxObj);
    }
    return rectsArray;
}
#endif



extern "C" jobjectArray org_jetbrains_skia_paragraph_Paragraph__1nGetRectsForPlaceholders
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_paragraph_Paragraph__1nGetRectsForPlaceholders");
}
     
#if 0 
extern "C" jobjectArray org_jetbrains_skia_paragraph_Paragraph__1nGetRectsForPlaceholders
  (kref __Kinstance, jlong ptr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>(static_cast<uintptr_t>(ptr));
    std::vector<TextBox> rects = instance->getRectsForPlaceholders();
    jobjectArray rectsArray = env->NewObjectArray((jsize) rects.size(), skija::paragraph::TextBox::cls, nullptr);
    for (int i = 0; i < rects.size(); ++i) {
        TextBox box = rects[i];
        jobject boxObj = env->NewObject(skija::paragraph::TextBox::cls, skija::paragraph::TextBox::ctor, box.rect.fLeft, box.rect.fTop, box.rect.fRight, box.rect.fBottom, static_cast<jint>(box.direction));
        env->SetObjectArrayElement(rectsArray, i, boxObj);
        env->DeleteLocalRef(boxObj);
    }
    return rectsArray;
}
#endif


extern "C" jint org_jetbrains_skia_paragraph_Paragraph__1nGetGlyphPositionAtCoordinate
  (kref __Kinstance, jlong ptr, jfloat dx, jfloat dy) {
    Paragraph* instance = reinterpret_cast<Paragraph*>(static_cast<uintptr_t>(ptr));
    PositionWithAffinity p = instance->getGlyphPositionAtCoordinate(dx, dy);
    if (p.affinity == Affinity::kDownstream)
        return p.position;
    else
        return -p.position-1;
}

extern "C" jlong org_jetbrains_skia_paragraph_Paragraph__1nGetWordBoundary
  (kref __Kinstance, jlong ptr, jint offset) {
    Paragraph* instance = reinterpret_cast<Paragraph*>(static_cast<uintptr_t>(ptr));
    SkRange<size_t> range = instance->getWordBoundary(offset);
    return packTwoInts(range.start & 0xFFFFFFFF, range.end & 0xFFFFFFFF);
}


extern "C" jobjectArray org_jetbrains_skia_paragraph_Paragraph__1nGetLineMetrics
  (kref __Kinstance, jlong ptr, jlong textPtr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_paragraph_Paragraph__1nGetLineMetrics");
}
     
#if 0 
extern "C" jobjectArray org_jetbrains_skia_paragraph_Paragraph__1nGetLineMetrics
  (kref __Kinstance, jlong ptr, jlong textPtr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>(static_cast<uintptr_t>(ptr));
    SkString* text = reinterpret_cast<SkString*>(static_cast<uintptr_t>(textPtr));
    std::vector<LineMetrics> res;
    instance->getLineMetrics(res);
    jobjectArray resArray = env->NewObjectArray((jsize) res.size(), skija::paragraph::LineMetrics::cls, nullptr);
    auto conv = skija::UtfIndicesConverter(*text);
    for (int i = 0; i < res.size(); ++i) {
        LineMetrics lm = res[i];
        size_t startIndex = conv.from8To16(lm.fStartIndex);
        size_t endExcludingWhitespaces = conv.from8To16(lm.fEndExcludingWhitespaces);
        size_t endIndex = conv.from8To16(lm.fEndIndex);
        size_t endIncludingNewline = conv.from8To16(lm.fEndIncludingNewline);
        jobject lmObj = env->NewObject(skija::paragraph::LineMetrics::cls, skija::paragraph::LineMetrics::ctor, startIndex, endIndex, endExcludingWhitespaces, endIncludingNewline, lm.fHardBreak, lm.fAscent, lm.fDescent, lm.fUnscaledAscent, lm.fHeight, lm.fWidth, lm.fLeft, lm.fBaseline, lm.fLineNumber);
        env->SetObjectArrayElement(resArray, i, lmObj);
        env->DeleteLocalRef(lmObj);
    }
    return resArray;
}
#endif


extern "C" jlong org_jetbrains_skia_paragraph_Paragraph__1nGetLineNumber
  (kref __Kinstance, jlong ptr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>(static_cast<uintptr_t>(ptr));
    return instance->lineNumber();
}

extern "C" void org_jetbrains_skia_paragraph_Paragraph__1nMarkDirty
  (kref __Kinstance, jlong ptr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>(static_cast<uintptr_t>(ptr));
    return instance->markDirty();
}

extern "C" jint org_jetbrains_skia_paragraph_Paragraph__1nGetUnresolvedGlyphsCount
  (kref __Kinstance, jlong ptr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>(static_cast<uintptr_t>(ptr));
    return instance->unresolvedGlyphs();
}

extern "C" void org_jetbrains_skia_paragraph_Paragraph__1nUpdateAlignment
  (kref __Kinstance, jlong ptr, jint textAlignment) {
    Paragraph* instance = reinterpret_cast<Paragraph*>(static_cast<uintptr_t>(ptr));
    return instance->updateTextAlign(static_cast<TextAlign>(textAlignment));
}

extern "C" void org_jetbrains_skia_paragraph_Paragraph__1nUpdateFontSize
  (kref __Kinstance, jlong ptr, jint from, jint to, jfloat fontSize, jlong textPtr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>(static_cast<uintptr_t>(ptr));
    SkString* text = reinterpret_cast<SkString*>(static_cast<uintptr_t>(textPtr));
    auto conv = skija::UtfIndicesConverter(*text);
    return instance->updateFontSize(conv.from16To8(from), conv.from16To8(to), fontSize);
}

extern "C" void org_jetbrains_skia_paragraph_Paragraph__1nUpdateForegroundPaint
  (kref __Kinstance, jlong ptr, jint from, jint to, jlong paintPtr, jlong textPtr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>(static_cast<uintptr_t>(ptr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    SkString* text = reinterpret_cast<SkString*>(static_cast<uintptr_t>(textPtr));
    auto conv = skija::UtfIndicesConverter(*text);
    return instance->updateForegroundPaint(conv.from16To8(from), conv.from16To8(to), *paint);
}

extern "C" void org_jetbrains_skia_paragraph_Paragraph__1nUpdateBackgroundPaint
  (kref __Kinstance, jlong ptr, jint from, jint to, jlong paintPtr, jlong textPtr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>(static_cast<uintptr_t>(ptr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    SkString* text = reinterpret_cast<SkString*>(static_cast<uintptr_t>(textPtr));
    auto conv = skija::UtfIndicesConverter(*text);
    return instance->updateBackgroundPaint(conv.from16To8(from), conv.from16To8(to), *paint);
}
