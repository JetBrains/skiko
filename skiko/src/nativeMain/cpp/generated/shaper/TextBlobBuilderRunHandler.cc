
// This file has been auto generated.

#include "SkShaper.h"
#include "common.h"

static void deleteTextBlobBuilderRunHandler(SkTextBlobBuilderRunHandler* instance) {
    // std::cout << "Deleting [SkTextBlobBuilderRunHandler " << instance << "]" << std::endl;
    delete instance;
}

extern "C" jlong org_jetbrains_skia_shaper_TextBlobBuilderRunHandler__1nGetFinalizer
  () {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteTextBlobBuilderRunHandler));
}

extern "C" jlong org_jetbrains_skia_shaper_TextBlobBuilderRunHandler__1nMake
  (jlong textPtr, jfloat offsetX, jfloat offsetY) {
    SkString* text = reinterpret_cast<SkString*>(static_cast<uintptr_t>(textPtr));
    auto instance = new SkTextBlobBuilderRunHandler(text->c_str(), {offsetX, offsetY});
    return reinterpret_cast<jlong>(instance);
}

extern "C" jlong org_jetbrains_skia_shaper_TextBlobBuilderRunHandler__1nMakeBlob
  (jlong ptr) {
    SkTextBlobBuilderRunHandler* instance = reinterpret_cast<SkTextBlobBuilderRunHandler*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(instance->makeBlob().release());
}
