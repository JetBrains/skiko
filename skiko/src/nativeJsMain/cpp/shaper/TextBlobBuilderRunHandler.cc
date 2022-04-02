#include "SkShaper.h"
#include "common.h"

static void deleteTextBlobBuilderRunHandler(SkTextBlobBuilderRunHandler* instance) {
    // std::cout << "Deleting [SkTextBlobBuilderRunHandler " << instance << "]" << std::endl;
    delete instance;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_TextBlobBuilderRunHandler__1nGetFinalizer
  () {
    return reinterpret_cast<KNativePointer>((&deleteTextBlobBuilderRunHandler));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_TextBlobBuilderRunHandler__1nMake
  (KNativePointer textPtr, KFloat offsetX, KFloat offsetY) {
    SkString* text = reinterpret_cast<SkString*>((textPtr));
    auto instance = new SkTextBlobBuilderRunHandler(text->c_str(), {offsetX, offsetY});
    return reinterpret_cast<KNativePointer>(instance);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_TextBlobBuilderRunHandler__1nMakeBlob
  (KNativePointer ptr) {
    SkTextBlobBuilderRunHandler* instance = reinterpret_cast<SkTextBlobBuilderRunHandler*>((ptr));
    return reinterpret_cast<KNativePointer>(instance->makeBlob().release());
}
