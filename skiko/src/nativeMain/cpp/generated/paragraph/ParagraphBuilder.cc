
// This file has been auto generated.

#include <iostream>
#include <string>
#include "ParagraphBuilder.h"
using namespace std;
using namespace skia::textlayout;
#include "common.h"

static void deleteParagraphBuilder(ParagraphBuilder* instance) {
    delete instance;
}

extern "C" jlong org_jetbrains_skia_paragraph_ParagraphBuilder__1nMake
  (jlong paragraphStylePtr, jlong fontCollectionPtr) {
    ParagraphStyle* paragraphStyle = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(paragraphStylePtr));
    FontCollection* fontCollection = reinterpret_cast<FontCollection*>(static_cast<uintptr_t>(fontCollectionPtr));
    ParagraphBuilder* instance = ParagraphBuilder::make(*paragraphStyle, sk_ref_sp(fontCollection)).release();
    return reinterpret_cast<jlong>(instance);
}

extern "C" jlong org_jetbrains_skia_paragraph_ParagraphBuilder__1nGetFinalizer
  () {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteParagraphBuilder));
}

extern "C" void org_jetbrains_skia_paragraph_ParagraphBuilder__1nPushStyle
  (jlong ptr, jlong textStylePtr) {
    ParagraphBuilder* instance = reinterpret_cast<ParagraphBuilder*>(static_cast<uintptr_t>(ptr));
    TextStyle* textStyle = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(textStylePtr));
    instance->pushStyle(*textStyle);
}

extern "C" void org_jetbrains_skia_paragraph_ParagraphBuilder__1nPopStyle
  (jlong ptr, jlong textStylePtr) {
    ParagraphBuilder* instance = reinterpret_cast<ParagraphBuilder*>(static_cast<uintptr_t>(ptr));
    instance->pop();
}


extern "C" void org_jetbrains_skia_paragraph_ParagraphBuilder__1nAddText
  (jlong ptr, jstring textString) {
    TODO("implement org_jetbrains_skia_paragraph_ParagraphBuilder__1nAddText");
}
     
#if 0 
extern "C" void org_jetbrains_skia_paragraph_ParagraphBuilder__1nAddText
  (jlong ptr, jstring textString) {
    ParagraphBuilder* instance = reinterpret_cast<ParagraphBuilder*>(static_cast<uintptr_t>(ptr));
    SkString text = skString(env, textString);
    instance->addText(text.c_str(), text.size());
}
#endif


extern "C" void org_jetbrains_skia_paragraph_ParagraphBuilder__1nAddPlaceholder
  (jlong ptr, jfloat width, jfloat height, jint alignment, jint baselinePosition, jfloat baseline) {
    ParagraphBuilder* instance = reinterpret_cast<ParagraphBuilder*>(static_cast<uintptr_t>(ptr));
    instance->addPlaceholder({width, height, static_cast<PlaceholderAlignment>(alignment), static_cast<TextBaseline>(baselinePosition), baseline});
}

extern "C" void org_jetbrains_skia_paragraph_ParagraphBuilder__1nSetParagraphStyle
  (jlong ptr, jlong stylePtr) {
    ParagraphBuilder* instance = reinterpret_cast<ParagraphBuilder*>(static_cast<uintptr_t>(ptr));
    ParagraphStyle* style = reinterpret_cast<ParagraphStyle*>(static_cast<uintptr_t>(stylePtr));
    instance->setParagraphStyle(*style);
}

extern "C" jlong org_jetbrains_skia_paragraph_ParagraphBuilder__1nBuild
  (jlong ptr) {
    ParagraphBuilder* instance = reinterpret_cast<ParagraphBuilder*>(static_cast<uintptr_t>(ptr));
    Paragraph* paragraph = instance->Build().release();
    return reinterpret_cast<jlong>(paragraph);
}
