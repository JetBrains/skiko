#include "SkCanvas.h"
#include "SkData.h"
#include "SkFontMgr.h"
#include "SkStream.h"
#include "SkSVGDOM.h"
#include "SkSVGSVG.h"
#include "common.h"
#include "modules/skresources/include/SkResources.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_svg_SVGDOM__1nMakeFromData
  (KNativePointer dataPtr) {
    SkData* data = reinterpret_cast<SkData*>(dataPtr);
    SkMemoryStream stream(sk_ref_sp(data));
    // No font manager and no resource provider: <text> that needs a font, and <image>, are not drawn.
    // Callers that need either use Builder.
    sk_sp<SkSVGDOM> instance = SkSVGDOM::MakeFromStream(stream);
    return reinterpret_cast<KNativePointer>(instance.release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_svg_SVGDOM__1nGetRoot
  (KNativePointer ptr) {
    SkSVGDOM* instance = reinterpret_cast<SkSVGDOM*>(ptr);
    SkSVGSVG* root = instance->getRoot();
    root->ref();
    return reinterpret_cast<KNativePointer>(root);
}

SKIKO_EXPORT void org_jetbrains_skia_svg_SVGDOM__1nGetContainerSize
  (KNativePointer ptr, KInteropPointer dst) {
    SkSVGDOM* instance = reinterpret_cast<SkSVGDOM*>(ptr);
    const SkSize& size = instance->containerSize();
    skija::Point::copyToInterop({size.fWidth, size.fHeight}, dst);
}

SKIKO_EXPORT void org_jetbrains_skia_svg_SVGDOM__1nSetContainerSize
  (KNativePointer ptr, KFloat width, KFloat height) {
    SkSVGDOM* instance = reinterpret_cast<SkSVGDOM*>(ptr);
    instance->setContainerSize(SkSize{width, height});
}

SKIKO_EXPORT void org_jetbrains_skia_svg_SVGDOM__1nRender
  (KNativePointer ptr, KNativePointer canvasPtr) {
    SkSVGDOM* instance = reinterpret_cast<SkSVGDOM*>(ptr);
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(canvasPtr);
    instance->render(canvas);
}

static void deleteSVGDOMBuilder(SkSVGDOM::Builder* builder) {
    delete builder;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_svg_SVGDOM__1nBuilderGetFinalizer
  () {
    return reinterpret_cast<KNativePointer>(&deleteSVGDOMBuilder);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_svg_SVGDOM__1nBuilderMake
  () {
    return reinterpret_cast<KNativePointer>(new SkSVGDOM::Builder());
}

SKIKO_EXPORT void org_jetbrains_skia_svg_SVGDOM__1nBuilderSetFontManager
  (KNativePointer ptr, KNativePointer fontMgrPtr) {
    SkSVGDOM::Builder* instance = reinterpret_cast<SkSVGDOM::Builder*>(ptr);
    instance->setFontManager(sk_ref_sp(reinterpret_cast<SkFontMgr*>(fontMgrPtr)));
}

SKIKO_EXPORT void org_jetbrains_skia_svg_SVGDOM__1nBuilderSetResourceProvider
  (KNativePointer ptr, KNativePointer resourceProviderPtr) {
    SkSVGDOM::Builder* instance = reinterpret_cast<SkSVGDOM::Builder*>(ptr);
    instance->setResourceProvider(sk_ref_sp(reinterpret_cast<skresources::ResourceProvider*>(resourceProviderPtr)));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_svg_SVGDOM__1nBuilderBuildFromData
  (KNativePointer ptr, KNativePointer dataPtr) {
    SkSVGDOM::Builder* instance = reinterpret_cast<SkSVGDOM::Builder*>(ptr);
    SkData* data = reinterpret_cast<SkData*>(dataPtr);
    SkMemoryStream stream(sk_ref_sp(data));
    return reinterpret_cast<KNativePointer>(instance->make(stream).release());
}
