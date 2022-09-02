#include "SkShadowUtils.h"
#include "SkPoint3.h"
#include "common.h"

SKIKO_EXPORT void org_jetbrains_skia_ShadowUtils__1nDrawShadow
  (KNativePointer canvasPtr, KNativePointer pathPtr, KFloat zPlaneX, KFloat zPlaneY, KFloat zPlaneZ,
        KFloat lightPosX, KFloat lightPosY, KFloat lightPosZ, KFloat lightRadius, KInt ambientColor, KInt spotColor, KInt flags) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    SkPath* path = reinterpret_cast<SkPath*>((pathPtr));
    SkShadowUtils::DrawShadow(canvas, *path, SkPoint3::Make(zPlaneX, zPlaneY, zPlaneZ), SkPoint3::Make(lightPosX, lightPosY, lightPosZ), lightRadius, ambientColor, spotColor, flags);
}

SKIKO_EXPORT int org_jetbrains_skia_ShadowUtils__1nComputeTonalAmbientColor
  (KInt ambientColor, KInt spotColor) {
    SkColor outAmbientColor, outSpotColor;
    SkShadowUtils::ComputeTonalColors(ambientColor, spotColor, &outAmbientColor, &outSpotColor);
    return outAmbientColor;
}

SKIKO_EXPORT int org_jetbrains_skia_ShadowUtils__1nComputeTonalSpotColor
  (KInt ambientColor, KInt spotColor) {
    SkColor outAmbientColor, outSpotColor;
    SkShadowUtils::ComputeTonalColors(ambientColor, spotColor, &outAmbientColor, &outSpotColor);
    return outSpotColor;
}
