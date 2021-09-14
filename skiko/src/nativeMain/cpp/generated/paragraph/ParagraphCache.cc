
// This file has been auto generated.

#include <iostream>
#include "ParagraphCache.h"
#include "ParagraphStyle.h"
using namespace skia::textlayout;
#include "common.h"

extern "C" void org_jetbrains_skia_paragraph_ParagraphCache__1nAbandon
  (jlong ptr) {
    ParagraphCache* instance = reinterpret_cast<ParagraphCache*>(static_cast<uintptr_t>(ptr));
    instance->abandon();
}

extern "C" void org_jetbrains_skia_paragraph_ParagraphCache__1nReset
  (jlong ptr) {
    ParagraphCache* instance = reinterpret_cast<ParagraphCache*>(static_cast<uintptr_t>(ptr));
    instance->reset();
}

extern "C" jboolean org_jetbrains_skia_paragraph_ParagraphCache__1nUpdateParagraph
  (jlong ptr, jlong paragraphPtr) {
    ParagraphCache* instance = reinterpret_cast<ParagraphCache*>(static_cast<uintptr_t>(ptr));
    ParagraphImpl* paragraph = reinterpret_cast<ParagraphImpl*>(static_cast<uintptr_t>(paragraphPtr));
    return instance->updateParagraph(paragraph);
}

extern "C" jboolean org_jetbrains_skia_paragraph_ParagraphCache__1nFindParagraph
  (jlong ptr, jlong paragraphPtr) {
    ParagraphCache* instance = reinterpret_cast<ParagraphCache*>(static_cast<uintptr_t>(ptr));
    ParagraphImpl* paragraph = reinterpret_cast<ParagraphImpl*>(static_cast<uintptr_t>(paragraphPtr));
    return instance->findParagraph(paragraph);
}

extern "C" void org_jetbrains_skia_paragraph_ParagraphCache__1nPrintStatistics
  (jlong ptr, jlong paragraphPtr) {
    ParagraphCache* instance = reinterpret_cast<ParagraphCache*>(static_cast<uintptr_t>(ptr));
    instance->printStatistics();
}

extern "C" void org_jetbrains_skia_paragraph_ParagraphCache__1nSetEnabled
  (jlong ptr, jboolean value) {
    ParagraphCache* instance = reinterpret_cast<ParagraphCache*>(static_cast<uintptr_t>(ptr));
    instance->turnOn(value);
}

extern "C" jint org_jetbrains_skia_paragraph_ParagraphCache__1nGetCount
  (jlong ptr) {
    ParagraphCache* instance = reinterpret_cast<ParagraphCache*>(static_cast<uintptr_t>(ptr));
    return instance->count();
}
