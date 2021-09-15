
// This file has been auto generated.

#include "SkCanvas.h"
#include "SkData.h"
#include "SkStream.h"
#include "SkSVGDOM.h"
#include "SkSVGSVG.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_svg_SVGDOM__1nMakeFromData
  (KNativePointer dataPtr) {
    SkData* data = reinterpret_cast<SkData*>((dataPtr));
    SkMemoryStream stream(sk_ref_sp(data));
    sk_sp<SkSVGDOM> instance = SkSVGDOM::MakeFromStream(stream);
    return reinterpret_cast<KNativePointer>(instance.release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_svg_SVGDOM__1nGetRoot
  (KNativePointer ptr) {
    SkSVGDOM* instance = reinterpret_cast<SkSVGDOM*>((ptr));
    SkSVGSVG* root = instance->getRoot();
    root->ref();
    return reinterpret_cast<KNativePointer>(root);
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_svg_SVGDOM__1nGetContainerSize
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_svg_SVGDOM__1nGetContainerSize");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_svg_SVGDOM__1nGetContainerSize
  (KNativePointer ptr) {
    SkSVGDOM* instance = reinterpret_cast<SkSVGDOM*>((ptr));
    const SkSize& size = instance->containerSize();
    return skija::Point::make(env, size.fWidth, size.fHeight);
}
#endif


SKIKO_EXPORT void org_jetbrains_skia_svg_SVGDOM__1nSetContainerSize
  (KNativePointer ptr, KFloat width, KFloat height) {
    SkSVGDOM* instance = reinterpret_cast<SkSVGDOM*>((ptr));
    instance->setContainerSize(SkSize{width, height});
}

SKIKO_EXPORT void org_jetbrains_skia_svg_SVGDOM__1nRender
  (KNativePointer ptr, KNativePointer canvasPtr) {
    SkSVGDOM* instance = reinterpret_cast<SkSVGDOM*>((ptr));
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    instance->render(canvas);
}
