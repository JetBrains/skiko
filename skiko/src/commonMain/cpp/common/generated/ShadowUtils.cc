
// This file has been auto generated.

#include "SkShadowUtils.h"
#include "common.h"

SKIKO_EXPORT void org_jetbrains_skia_ShadowUtils__1nDrawShadow
  (KInteropPointer __Kinstance, KNativePointer canvasPtr, KNativePointer pathPtr, KFloat zPlaneX, KFloat zPlaneY, KFloat zPlaneZ,
        KFloat lightPosX, KFloat lightPosY, KFloat lightPosZ, KFloat lightRadius, KInt ambientColor, KInt spotColor, KInt flags) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    SkPath* path = reinterpret_cast<SkPath*>((pathPtr));
    SkShadowUtils::DrawShadow(canvas, *path, {zPlaneX, zPlaneY, zPlaneZ}, {lightPosX, lightPosY, lightPosZ}, lightRadius, ambientColor, spotColor, flags);
}

SKIKO_EXPORT int org_jetbrains_skia_ShadowUtils__1nComputeTonalAmbientColor
  (KInteropPointer __Kinstance, KInt ambientColor, KInt spotColor) {
    SkColor outAmbientColor, outSpotColor;
    SkShadowUtils::ComputeTonalColors(ambientColor, spotColor, &outAmbientColor, &outSpotColor);
    return outAmbientColor;
}

SKIKO_EXPORT int org_jetbrains_skia_ShadowUtils__1nComputeTonalSpotColor
  (KInteropPointer __Kinstance, KInt ambientColor, KInt spotColor) {
    SkColor outAmbientColor, outSpotColor;
    SkShadowUtils::ComputeTonalColors(ambientColor, spotColor, &outAmbientColor, &outSpotColor);
    return outSpotColor;
}
