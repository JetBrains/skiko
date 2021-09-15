
// This file has been auto generated.

#include "SkShadowUtils.h"
#include "common.h"

extern "C" void org_jetbrains_skia_ShadowUtils__1nDrawShadow
  (jlong canvasPtr, jlong pathPtr, jfloat zPlaneX, jfloat zPlaneY, jfloat zPlaneZ,
        jfloat lightPosX, jfloat lightPosY, jfloat lightPosZ, jfloat lightRadius, jint ambientColor, jint spotColor, jint flags) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    SkPath* path = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(pathPtr));
    SkShadowUtils::DrawShadow(canvas, *path, {zPlaneX, zPlaneY, zPlaneZ}, {lightPosX, lightPosY, lightPosZ}, lightRadius, ambientColor, spotColor, flags);
}

extern "C" int org_jetbrains_skia_ShadowUtils__1nComputeTonalAmbientColor
  (jint ambientColor, jint spotColor) {
    SkColor outAmbientColor, outSpotColor;
    SkShadowUtils::ComputeTonalColors(ambientColor, spotColor, &outAmbientColor, &outSpotColor);
    return outAmbientColor;
}

extern "C" int org_jetbrains_skia_ShadowUtils__1nComputeTonalSpotColor
  (jint ambientColor, jint spotColor) {
    SkColor outAmbientColor, outSpotColor;
    SkShadowUtils::ComputeTonalColors(ambientColor, spotColor, &outAmbientColor, &outSpotColor);
    return outSpotColor;
}
