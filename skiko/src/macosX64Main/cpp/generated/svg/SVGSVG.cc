
// This file has been auto generated.

#include "SkSVGSVG.h"
#include "SkSVGRenderContext.h"
#include "common.h"

extern "C" jint org_jetbrains_skia_svg_SVGSVG__1nGetTag
  (kref __Kinstance, jlong ptr) {
    SkSVGNode* instance = reinterpret_cast<SkSVGNode*>(static_cast<uintptr_t>(ptr));
    return static_cast<jint>(instance->tag());
}


extern "C" jobject org_jetbrains_skia_svg_SVGSVG__1nGetX
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_svg_SVGSVG__1nGetX");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_svg_SVGSVG__1nGetX
  (kref __Kinstance, jlong ptr) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    return skija::svg::SVGLength::toJava(env, instance->getX());
}
#endif



extern "C" jobject org_jetbrains_skia_svg_SVGSVG__1nGetY
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_svg_SVGSVG__1nGetY");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_svg_SVGSVG__1nGetY
  (kref __Kinstance, jlong ptr) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    return skija::svg::SVGLength::toJava(env, instance->getY());
}
#endif



extern "C" jobject org_jetbrains_skia_svg_SVGSVG__1nGetWidth
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_svg_SVGSVG__1nGetWidth");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_svg_SVGSVG__1nGetWidth
  (kref __Kinstance, jlong ptr) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    return skija::svg::SVGLength::toJava(env, instance->getWidth());
}
#endif



extern "C" jobject org_jetbrains_skia_svg_SVGSVG__1nGetHeight
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_svg_SVGSVG__1nGetHeight");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_svg_SVGSVG__1nGetHeight
  (kref __Kinstance, jlong ptr) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    return skija::svg::SVGLength::toJava(env, instance->getHeight());
}
#endif



extern "C" jobject org_jetbrains_skia_svg_SVGSVG__1nGetPreserveAspectRatio
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_svg_SVGSVG__1nGetPreserveAspectRatio");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_svg_SVGSVG__1nGetPreserveAspectRatio
  (kref __Kinstance, jlong ptr) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    return skija::svg::SVGPreserveAspectRatio::toJava(env, instance->getPreserveAspectRatio());
}
#endif



extern "C" jobject org_jetbrains_skia_svg_SVGSVG__1nGetViewBox
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_svg_SVGSVG__1nGetViewBox");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_svg_SVGSVG__1nGetViewBox
  (kref __Kinstance, jlong ptr) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    SkTLazy<SkSVGViewBoxType> viewBox = instance->getViewBox();
    return viewBox.isValid() ? skija::Rect::fromSkRect(env, *viewBox.get()) : nullptr;
}
#endif



extern "C" jobject org_jetbrains_skia_svg_SVGSVG__1nGetIntrinsicSize
  (kref __Kinstance, jlong ptr, float width, float height, float dpi) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_svg_SVGSVG__1nGetIntrinsicSize");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_svg_SVGSVG__1nGetIntrinsicSize
  (kref __Kinstance, jlong ptr, float width, float height, float dpi) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    SkSVGLengthContext lc({width, height}, dpi);
    SkSize size = instance->intrinsicSize(lc);
    return skija::Point::fromSkPoint(env, {size.width(), size.height()});
}
#endif


extern "C" void org_jetbrains_skia_svg_SVGSVG__1nSetX
  (kref __Kinstance, jlong ptr, float value, int unit) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    SkSVGLength lenght(value, static_cast<SkSVGLength::Unit>(unit));
    instance->setX(lenght);
}

extern "C" void org_jetbrains_skia_svg_SVGSVG__1nSetY
  (kref __Kinstance, jlong ptr, float value, int unit) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    SkSVGLength lenght(value, static_cast<SkSVGLength::Unit>(unit));
    instance->setY(lenght);
}

extern "C" void org_jetbrains_skia_svg_SVGSVG__1nSetWidth
  (kref __Kinstance, jlong ptr, float value, int unit) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    SkSVGLength lenght(value, static_cast<SkSVGLength::Unit>(unit));
    instance->setWidth(lenght);
}

extern "C" void org_jetbrains_skia_svg_SVGSVG__1nSetHeight
  (kref __Kinstance, jlong ptr, float value, int unit) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    SkSVGLength lenght(value, static_cast<SkSVGLength::Unit>(unit));
    instance->setHeight(lenght);
}

extern "C" void org_jetbrains_skia_svg_SVGSVG__1nSetPreserveAspectRatio
  (kref __Kinstance, jlong ptr, jint align, jint scale) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    instance->setPreserveAspectRatio(SkSVGPreserveAspectRatio { static_cast<SkSVGPreserveAspectRatio::Align>(align),
                                                                static_cast<SkSVGPreserveAspectRatio::Scale>(scale) });
}

extern "C" void org_jetbrains_skia_svg_SVGSVG__1nSetViewBox
  (kref __Kinstance, jlong ptr, float l, float t, float r, float b) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    instance->setViewBox(SkRect::MakeLTRB(l, t, r, b));
}
