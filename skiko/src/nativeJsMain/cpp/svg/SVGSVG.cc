#include "SkSVGSVG.h"
#include "SkSVGRenderContext.h"
#include "common.h"

SKIKO_EXPORT KInt org_jetbrains_skia_svg_SVGSVG__1nGetTag
  (KNativePointer ptr) {
    SkSVGNode* instance = reinterpret_cast<SkSVGNode*>((ptr));
    return static_cast<KInt>(instance->tag());
}

SKIKO_EXPORT void org_jetbrains_skia_svg_SVGSVG__1nGetX(KNativePointer ptr, KInteropPointer result) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(ptr);
    return skija::svg::SVGLength::copyToInterop(instance->getX(), result);
}

SKIKO_EXPORT void org_jetbrains_skia_svg_SVGSVG__1nGetY(KNativePointer ptr, KInteropPointer result) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(ptr);
    return skija::svg::SVGLength::copyToInterop(instance->getY(), result);
}

SKIKO_EXPORT void org_jetbrains_skia_svg_SVGSVG__1nGetHeight(KNativePointer ptr, KInteropPointer result) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(ptr);
    return skija::svg::SVGLength::copyToInterop(instance->getHeight(), result);
}

SKIKO_EXPORT void org_jetbrains_skia_svg_SVGSVG__1nGetWidth(KNativePointer ptr, KInteropPointer result) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(ptr);
    return skija::svg::SVGLength::copyToInterop(instance->getWidth(), result);
}

SKIKO_EXPORT void org_jetbrains_skia_svg_SVGSVG__1nGetPreserveAspectRatio
  (KNativePointer ptr, KInteropPointer result) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>((ptr));
    return skija::svg::SVGPreserveAspectRatio::copyToInterop(instance->getPreserveAspectRatio(), result);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_svg_SVGSVG__1nGetViewBox
  (KNativePointer ptr, KInteropPointer result) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>((ptr));
    SkTLazy<SkSVGViewBoxType> viewBox = instance->getViewBox();
    if (viewBox.isValid()) {
        skija::Rect::copyToInterop(*viewBox.get(), result);
        return true;
    } else {
        return false;
    }
}

SKIKO_EXPORT void org_jetbrains_skia_svg_SVGSVG__1nGetIntrinsicSize
  (KNativePointer ptr, float width, float height, float dpi, KInteropPointer result) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>((ptr));
    SkSVGLengthContext lc({width, height}, dpi);
    SkSize size = instance->intrinsicSize(lc);
    skija::Point::copyToInterop({size.width(), size.height()}, result);
}

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
