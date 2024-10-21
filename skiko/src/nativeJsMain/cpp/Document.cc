#include "SkDocument.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Document__1nBeginPage
  (KNativePointer ptr, KFloat width, KFloat height, KFloat* contentArr) {
    SkDocument* instance = reinterpret_cast<SkDocument*>((ptr));
    SkRect content;
    SkRect* contentPtr = nullptr;
    if (contentArr != nullptr) {
        content = { contentArr[0], contentArr[1], contentArr[2], contentArr[3] };
        contentPtr = &content;
    }
    SkCanvas* canvas = instance->beginPage(width, height, contentPtr);
    return reinterpret_cast<KNativePointer>(canvas);
}

SKIKO_EXPORT void org_jetbrains_skia_Document__1nEndPage
  (KNativePointer ptr) {
    SkDocument* instance = reinterpret_cast<SkDocument*>((ptr));
    instance->endPage();
}
