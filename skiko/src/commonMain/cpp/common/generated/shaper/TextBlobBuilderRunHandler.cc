
// This file has been auto generated.

#include "SkShaper.h"
#include "common.h"

static void deleteTextBlobBuilderRunHandler(SkTextBlobBuilderRunHandler* instance) {
    // std::cout << "Deleting [SkTextBlobBuilderRunHandler " << instance << "]" << std::endl;
    delete instance;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_TextBlobBuilderRunHandler__1nGetFinalizer
  (KInteropPointer __Kinstance) {
    return reinterpret_cast<KNativePointer>((&deleteTextBlobBuilderRunHandler));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_TextBlobBuilderRunHandler__1nMake
  (KInteropPointer __Kinstance, KNativePointer textPtr, KFloat offsetX, KFloat offsetY) {
    SkString* text = reinterpret_cast<SkString*>((textPtr));
    auto instance = new SkTextBlobBuilderRunHandler(text->c_str(), {offsetX, offsetY});
    return reinterpret_cast<KNativePointer>(instance);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_TextBlobBuilderRunHandler__1nMakeBlob
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkTextBlobBuilderRunHandler* instance = reinterpret_cast<SkTextBlobBuilderRunHandler*>((ptr));
    return reinterpret_cast<KNativePointer>(instance->makeBlob().release());
}
