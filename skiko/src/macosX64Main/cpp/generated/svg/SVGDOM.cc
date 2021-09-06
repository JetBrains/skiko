
// This file has been auto generated.

#include "SkCanvas.h"
#include "SkData.h"
#include "SkStream.h"
#include "SkSVGDOM.h"
#include "SkSVGSVG.h"
#include "common.h"

extern "C" jlong org_jetbrains_skia_svg_SVGDOM__1nMakeFromData
  (kref __Kinstance, jlong dataPtr) {
    SkData* data = reinterpret_cast<SkData*>(static_cast<uintptr_t>(dataPtr));
    SkMemoryStream stream(sk_ref_sp(data));
    sk_sp<SkSVGDOM> instance = SkSVGDOM::MakeFromStream(stream);
    return reinterpret_cast<jlong>(instance.release());
}

extern "C" jlong org_jetbrains_skia_svg_SVGDOM__1nGetRoot
  (kref __Kinstance, jlong ptr) {
    SkSVGDOM* instance = reinterpret_cast<SkSVGDOM*>(static_cast<uintptr_t>(ptr));
    SkSVGSVG* root = instance->getRoot();
    root->ref();
    return reinterpret_cast<jlong>(root);
}


extern "C" jobject org_jetbrains_skia_svg_SVGDOM__1nGetContainerSize
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_svg_SVGDOM__1nGetContainerSize");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_svg_SVGDOM__1nGetContainerSize
  (kref __Kinstance, jlong ptr) {
    SkSVGDOM* instance = reinterpret_cast<SkSVGDOM*>(static_cast<uintptr_t>(ptr));
    const SkSize& size = instance->containerSize();
    return skija::Point::make(env, size.fWidth, size.fHeight);
}
#endif


extern "C" void org_jetbrains_skia_svg_SVGDOM__1nSetContainerSize
  (kref __Kinstance, jlong ptr, jfloat width, jfloat height) {
    SkSVGDOM* instance = reinterpret_cast<SkSVGDOM*>(static_cast<uintptr_t>(ptr));
    instance->setContainerSize(SkSize{width, height});
}

extern "C" void org_jetbrains_skia_svg_SVGDOM__1nRender
  (kref __Kinstance, jlong ptr, jlong canvasPtr) {
    SkSVGDOM* instance = reinterpret_cast<SkSVGDOM*>(static_cast<uintptr_t>(ptr));
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    instance->render(canvas);
}
