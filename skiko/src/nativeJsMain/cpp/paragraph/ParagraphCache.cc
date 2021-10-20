
// This file has been auto generated.

#include <iostream>
#include "ParagraphCache.h"
#include "ParagraphStyle.h"
using namespace skia::textlayout;
#include "common.h"

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphCache__1nAbandon
  (KNativePointer ptr) {
    ParagraphCache* instance = reinterpret_cast<ParagraphCache*>((ptr));
    instance->abandon();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphCache__1nReset
  (KNativePointer ptr) {
    ParagraphCache* instance = reinterpret_cast<ParagraphCache*>((ptr));
    instance->reset();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_paragraph_ParagraphCache__1nUpdateParagraph
  (KNativePointer ptr, KNativePointer paragraphPtr) {
    ParagraphCache* instance = reinterpret_cast<ParagraphCache*>((ptr));
    ParagraphImpl* paragraph = reinterpret_cast<ParagraphImpl*>((paragraphPtr));
    return instance->updateParagraph(paragraph);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_paragraph_ParagraphCache__1nFindParagraph
  (KNativePointer ptr, KNativePointer paragraphPtr) {
    ParagraphCache* instance = reinterpret_cast<ParagraphCache*>((ptr));
    ParagraphImpl* paragraph = reinterpret_cast<ParagraphImpl*>((paragraphPtr));
    return instance->findParagraph(paragraph);
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphCache__1nPrintStatistics
  (KNativePointer ptr, KNativePointer paragraphPtr) {
    ParagraphCache* instance = reinterpret_cast<ParagraphCache*>((ptr));
    instance->printStatistics();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphCache__1nSetEnabled
  (KNativePointer ptr, KBoolean value) {
    ParagraphCache* instance = reinterpret_cast<ParagraphCache*>((ptr));
    instance->turnOn(value);
}

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_ParagraphCache__1nGetCount
  (KNativePointer ptr) {
    ParagraphCache* instance = reinterpret_cast<ParagraphCache*>((ptr));
    return instance->count();
}
