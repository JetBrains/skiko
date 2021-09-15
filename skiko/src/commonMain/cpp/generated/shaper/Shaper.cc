
// This file has been auto generated.

#include <iostream>
#include "SkShaper.h"
#include "src/utils/SkUTF.h"
#include "unicode/ubidi.h"
#include "common.h"

static void deleteShaper(SkShaper* instance) {
    // std::cout << "Deleting [SkShaper " << instance << "]" << std::endl;
    delete instance;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>((&deleteShaper));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nMakePrimitive
  () {
    return reinterpret_cast<KNativePointer>(SkShaper::MakePrimitive().release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nMakeShaperDrivenWrapper
  (KNativePointer fontMgrPtr) {
    SkFontMgr* fontMgr = reinterpret_cast<SkFontMgr*>((fontMgrPtr));
    return reinterpret_cast<KNativePointer>(SkShaper::MakeShaperDrivenWrapper(sk_ref_sp(fontMgr)).release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nMakeShapeThenWrap
  (KNativePointer fontMgrPtr) {
    SkFontMgr* fontMgr = reinterpret_cast<SkFontMgr*>((fontMgrPtr));
    return reinterpret_cast<KNativePointer>(SkShaper::MakeShapeThenWrap(sk_ref_sp(fontMgr)).release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nMakeShapeDontWrapOrReorder
  (KNativePointer fontMgrPtr) {
    SkFontMgr* fontMgr = reinterpret_cast<SkFontMgr*>((fontMgrPtr));
    return reinterpret_cast<KNativePointer>(SkShaper::MakeShapeDontWrapOrReorder(sk_ref_sp(fontMgr)).release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nMakeCoreText() {
    #ifdef SK_SHAPER_CORETEXT_AVAILABLE
        return reinterpret_cast<KNativePointer>(SkShaper::MakeCoreText().release());
    #else
        return 0;
    #endif
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nMake
  (KNativePointer fontMgrPtr) {
    SkFontMgr* fontMgr = reinterpret_cast<SkFontMgr*>((fontMgrPtr));
    return reinterpret_cast<KNativePointer>(SkShaper::Make(sk_ref_sp(fontMgr)).release());
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nShapeBlob
  (KNativePointer ptr, KInteropPointer textObj, KNativePointer fontPtr, KInteropPointer opts, KFloat width, KFloat offsetX, KFloat offsetY) {
    TODO("implement org_jetbrains_skia_shaper_Shaper__1nShapeBlob");
}
SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nShapeLine
  (KNativePointer ptr, KInteropPointer textObj, KNativePointer fontPtr, KInteropPointer opts) {
    TODO("implement org_jetbrains_skia_shaper_Shaper__1nShapeLine");
}
SKIKO_EXPORT void org_jetbrains_skia_shaper_Shaper__1nShape
  (KNativePointer ptr, KNativePointer textPtr, KInteropPointer fontRunIterObj, KInteropPointer bidiRunIterObj, KInteropPointer scriptRunIterObj, KInteropPointer languageRunIterObj, KInteropPointer opts, KFloat width, KInteropPointer runHandlerObj)
{
    TODO("implement org_jetbrains_skia_shaper_Shaper__1nShape");
}
