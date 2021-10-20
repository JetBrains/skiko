
// This file has been auto generated.

#include "SkSVGSVG.h"
#include "SkSVGRenderContext.h"
#include "common.h"

SKIKO_EXPORT KInt org_jetbrains_skia_svg_SVGSVG__1nGetTag
  (KNativePointer ptr) {
    SkSVGNode* instance = reinterpret_cast<SkSVGNode*>((ptr));
    return static_cast<KInt>(instance->tag());
}


SKIKO_EXPORT void org_jetbrains_skia_svg_SVGSVG__1nGetX(KNativePointer ptr, KInt* result) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(ptr);
    auto x = instance->getX();
    result[0] = rawBits(x.value());
    result[1] = static_cast<KInt>(x.unit());
}

SKIKO_EXPORT void org_jetbrains_skia_svg_SVGSVG__1nGetY(KNativePointer ptr, KInt* result) {
      SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(ptr);
      auto y = instance->getY();
      result[0] = rawBits(y.value());
      result[1] = static_cast<KInt>(y.unit());
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_svg_SVGSVG__1nGetY
  (KNativePointer ptr) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>((ptr));
    return skija::svg::SVGLength::toJava(env, instance->getY());
}
#endif



SKIKO_EXPORT KInteropPointer org_jetbrains_skia_svg_SVGSVG__1nGetWidth
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_svg_SVGSVG__1nGetWidth");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_svg_SVGSVG__1nGetWidth
  (KNativePointer ptr) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>((ptr));
    return skija::svg::SVGLength::toJava(env, instance->getWidth());
}
#endif



SKIKO_EXPORT KInteropPointer org_jetbrains_skia_svg_SVGSVG__1nGetHeight
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_svg_SVGSVG__1nGetHeight");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_svg_SVGSVG__1nGetHeight
  (KNativePointer ptr) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>((ptr));
    return skija::svg::SVGLength::toJava(env, instance->getHeight());
}
#endif



SKIKO_EXPORT KInteropPointer org_jetbrains_skia_svg_SVGSVG__1nGetPreserveAspectRatio
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_svg_SVGSVG__1nGetPreserveAspectRatio");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_svg_SVGSVG__1nGetPreserveAspectRatio
  (KNativePointer ptr) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>((ptr));
    return skija::svg::SVGPreserveAspectRatio::toJava(env, instance->getPreserveAspectRatio());
}
#endif



SKIKO_EXPORT KInteropPointer org_jetbrains_skia_svg_SVGSVG__1nGetViewBox
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_svg_SVGSVG__1nGetViewBox");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_svg_SVGSVG__1nGetViewBox
  (KNativePointer ptr) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>((ptr));
    SkTLazy<SkSVGViewBoxType> viewBox = instance->getViewBox();
    return viewBox.isValid() ? skija::Rect::fromSkRect(env, *viewBox.get()) : nullptr;
}
#endif



SKIKO_EXPORT KInteropPointer org_jetbrains_skia_svg_SVGSVG__1nGetIntrinsicSize
  (KNativePointer ptr, float width, float height, float dpi) {
    TODO("implement org_jetbrains_skia_svg_SVGSVG__1nGetIntrinsicSize");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_svg_SVGSVG__1nGetIntrinsicSize
  (KNativePointer ptr, float width, float height, float dpi) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>((ptr));
    SkSVGLengthContext lc({width, height}, dpi);
    SkSize size = instance->intrinsicSize(lc);
    return skija::Point::fromSkPoint(env, {size.width(), size.height()});
}
#endif


SKIKO_EXPORT void org_jetbrains_skia_svg_SVGSVG__1nSetX
  (KNativePointer ptr, float value, int unit) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>((ptr));
    SkSVGLength lenght(value, static_cast<SkSVGLength::Unit>(unit));
    instance->setX(lenght);
}

SKIKO_EXPORT void org_jetbrains_skia_svg_SVGSVG__1nSetY
  (KNativePointer ptr, float value, int unit) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>((ptr));
    SkSVGLength lenght(value, static_cast<SkSVGLength::Unit>(unit));
    instance->setY(lenght);
}

SKIKO_EXPORT void org_jetbrains_skia_svg_SVGSVG__1nSetWidth
  (KNativePointer ptr, float value, int unit) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>((ptr));
    SkSVGLength lenght(value, static_cast<SkSVGLength::Unit>(unit));
    instance->setWidth(lenght);
}

SKIKO_EXPORT void org_jetbrains_skia_svg_SVGSVG__1nSetHeight
  (KNativePointer ptr, float value, int unit) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>((ptr));
    SkSVGLength lenght(value, static_cast<SkSVGLength::Unit>(unit));
    instance->setHeight(lenght);
}

SKIKO_EXPORT void org_jetbrains_skia_svg_SVGSVG__1nSetPreserveAspectRatio
  (KNativePointer ptr, KInt align, KInt scale) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>((ptr));
    instance->setPreserveAspectRatio(SkSVGPreserveAspectRatio { static_cast<SkSVGPreserveAspectRatio::Align>(align),
                                                                static_cast<SkSVGPreserveAspectRatio::Scale>(scale) });
}

SKIKO_EXPORT void org_jetbrains_skia_svg_SVGSVG__1nSetViewBox
  (KNativePointer ptr, float l, float t, float r, float b) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>((ptr));
    instance->setViewBox(SkRect::MakeLTRB(l, t, r, b));
}
