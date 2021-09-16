
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

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_Paragraph__1nGetFinalizer
  () {
    return reinterpret_cast<KNativePointer>((&deleteParagraph));
}

SKIKO_EXPORT KFloat org_jetbrains_skia_paragraph_Paragraph__1nGetMaxWidth
  (KNativePointer ptr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>((ptr));
    return instance->getMaxWidth();
}

SKIKO_EXPORT KFloat org_jetbrains_skia_paragraph_Paragraph__1nGetHeight
  (KNativePointer ptr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>((ptr));
    return instance->getHeight();
}

SKIKO_EXPORT KFloat org_jetbrains_skia_paragraph_Paragraph__1nGetMinIntrinsicWidth
  (KNativePointer ptr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>((ptr));
    return instance->getMinIntrinsicWidth();
}

SKIKO_EXPORT KFloat org_jetbrains_skia_paragraph_Paragraph__1nGetMaxIntrinsicWidth
  (KNativePointer ptr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>((ptr));
    return instance->getMaxIntrinsicWidth();
}

SKIKO_EXPORT KFloat org_jetbrains_skia_paragraph_Paragraph__1nGetAlphabeticBaseline
  (KNativePointer ptr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>((ptr));
    return instance->getAlphabeticBaseline();
}

SKIKO_EXPORT KFloat org_jetbrains_skia_paragraph_Paragraph__1nGetIdeographicBaseline
  (KNativePointer ptr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>((ptr));
    return instance->getIdeographicBaseline();
}

SKIKO_EXPORT KFloat org_jetbrains_skia_paragraph_Paragraph__1nGetLongestLine
  (KNativePointer ptr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>((ptr));
    return instance->getLongestLine();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_paragraph_Paragraph__1nDidExceedMaxLines
  (KNativePointer ptr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>((ptr));
    return instance->didExceedMaxLines();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_Paragraph__1nLayout
  (KNativePointer ptr, KFloat width) {
    Paragraph* instance = reinterpret_cast<Paragraph*>((ptr));
    instance->layout(width);
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_Paragraph__1nPaint
  (KNativePointer ptr, KNativePointer canvasPtr, KFloat x, KFloat y) {
    Paragraph* instance = reinterpret_cast<Paragraph*>((ptr));
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    instance->paint(canvas, x, y);
}


SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_paragraph_Paragraph__1nGetRectsForRange
  (KNativePointer ptr, KInt start, KInt end, KInt rectHeightStyle, KInt rectWidthStyle) {
    TODO("implement org_jetbrains_skia_paragraph_Paragraph__1nGetRectsForRange");
}
     
#if 0 
SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_paragraph_Paragraph__1nGetRectsForRange
  (KNativePointer ptr, KInt start, KInt end, KInt rectHeightStyle, KInt rectWidthStyle) {
    Paragraph* instance = reinterpret_cast<Paragraph*>((ptr));
    std::vector<TextBox> rects = instance->getRectsForRange(start, end, static_cast<RectHeightStyle>(rectHeightStyle), static_cast<RectWidthStyle>(rectWidthStyle));
    KInteropPointerArray rectsArray = env->NewObjectArray((jsize) rects.size(), skija::paragraph::TextBox::cls, nullptr);
    for (int i = 0; i < rects.size(); ++i) {
        TextBox box = rects[i];
        KInteropPointer boxObj = env->NewObject(skija::paragraph::TextBox::cls, skija::paragraph::TextBox::ctor, box.rect.fLeft, box.rect.fTop, box.rect.fRight, box.rect.fBottom, static_cast<KInt>(box.direction));
        env->SetObjectArrayElement(rectsArray, i, boxObj);
        env->DeleteLocalRef(boxObj);
    }
    return rectsArray;
}
#endif

SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_paragraph_Paragraph__1nGetRectsForPlaceholders
  (KNativePointer ptr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>((ptr));
    std::vector<TextBox> *vect = new std::vector<TextBox>(instance->getRectsForPlaceholders());
    return vect;
}

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_Paragraph__1nGetGlyphPositionAtCoordinate
  (KNativePointer ptr, KFloat dx, KFloat dy) {
    Paragraph* instance = reinterpret_cast<Paragraph*>((ptr));
    PositionWithAffinity p = instance->getGlyphPositionAtCoordinate(dx, dy);
    if (p.affinity == Affinity::kDownstream)
        return p.position;
    else
        return -p.position-1;
}

SKIKO_EXPORT KLong org_jetbrains_skia_paragraph_Paragraph__1nGetWordBoundary
  (KNativePointer ptr, KInt offset) {
    Paragraph* instance = reinterpret_cast<Paragraph*>((ptr));
    SkRange<size_t> range = instance->getWordBoundary(offset);
    return packTwoInts(range.start & 0xFFFFFFFF, range.end & 0xFFFFFFFF);
}


SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_paragraph_Paragraph__1nGetLineMetrics
  (KNativePointer ptr, KNativePointer textPtr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>((ptr));
    SkString* text = reinterpret_cast<SkString*>((textPtr));

    std::vector<LineMetrics>* res = new std::vector<LineMetrics>;
    instance->getLineMetrics(*res);
    return res;
}
     
SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_Paragraph__1nGetLineNumber
  (KNativePointer ptr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>((ptr));
    return instance->lineNumber();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_Paragraph__1nMarkDirty
  (KNativePointer ptr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>((ptr));
    return instance->markDirty();
}

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_Paragraph__1nGetUnresolvedGlyphsCount
  (KNativePointer ptr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>((ptr));
    return instance->unresolvedGlyphs();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_Paragraph__1nUpdateAlignment
  (KNativePointer ptr, KInt textAlignment) {
    Paragraph* instance = reinterpret_cast<Paragraph*>((ptr));
    return instance->updateTextAlign(static_cast<TextAlign>(textAlignment));
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_Paragraph__1nUpdateFontSize
  (KNativePointer ptr, KInt from, KInt to, KFloat fontSize, KNativePointer textPtr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>((ptr));
    SkString* text = reinterpret_cast<SkString*>((textPtr));
    auto conv = skija::UtfIndicesConverter(*text);
    return instance->updateFontSize(conv.from16To8(from), conv.from16To8(to), fontSize);
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_Paragraph__1nUpdateForegroundPaint
  (KNativePointer ptr, KInt from, KInt to, KNativePointer paintPtr, KNativePointer textPtr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>((ptr));
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    SkString* text = reinterpret_cast<SkString*>((textPtr));
    auto conv = skija::UtfIndicesConverter(*text);
    return instance->updateForegroundPaint(conv.from16To8(from), conv.from16To8(to), *paint);
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_Paragraph__1nUpdateBackgroundPaint
  (KNativePointer ptr, KInt from, KInt to, KNativePointer paintPtr, KNativePointer textPtr) {
    Paragraph* instance = reinterpret_cast<Paragraph*>((ptr));
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    SkString* text = reinterpret_cast<SkString*>((textPtr));
    auto conv = skija::UtfIndicesConverter(*text);
    return instance->updateBackgroundPaint(conv.from16To8(from), conv.from16To8(to), *paint);
}
