
// This file has been auto generated.

#include <iostream>
#include "ParagraphCache.h"
#include "ParagraphStyle.h"
using namespace skia::textlayout;
#include "common.h"

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphCache__1nAbandon
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    ParagraphCache* instance = reinterpret_cast<ParagraphCache*>((ptr));
    instance->abandon();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphCache__1nReset
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    ParagraphCache* instance = reinterpret_cast<ParagraphCache*>((ptr));
    instance->reset();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_paragraph_ParagraphCache__1nUpdateParagraph
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer paragraphPtr) {
    ParagraphCache* instance = reinterpret_cast<ParagraphCache*>((ptr));
    ParagraphImpl* paragraph = reinterpret_cast<ParagraphImpl*>((paragraphPtr));
    return instance->updateParagraph(paragraph);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_paragraph_ParagraphCache__1nFindParagraph
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer paragraphPtr) {
    ParagraphCache* instance = reinterpret_cast<ParagraphCache*>((ptr));
    ParagraphImpl* paragraph = reinterpret_cast<ParagraphImpl*>((paragraphPtr));
    return instance->findParagraph(paragraph);
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphCache__1nPrintStatistics
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer paragraphPtr) {
    ParagraphCache* instance = reinterpret_cast<ParagraphCache*>((ptr));
    instance->printStatistics();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_ParagraphCache__1nSetEnabled
  (KInteropPointer __Kinstance, KNativePointer ptr, KBoolean value) {
    ParagraphCache* instance = reinterpret_cast<ParagraphCache*>((ptr));
    instance->turnOn(value);
}

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_ParagraphCache__1nGetCount
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    ParagraphCache* instance = reinterpret_cast<ParagraphCache*>((ptr));
    return instance->count();
}
