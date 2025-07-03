@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_ShadowUtils__1nDrawShadow")
internal external fun ShadowUtils_nDrawShadow(
    canvasPtr: NativePointer,
    pathPtr: NativePointer,
    zPlaneX: Float,
    zPlaneY: Float,
    zPlaneZ: Float,
    lightPosX: Float,
    lightPosY: Float,
    lightPosZ: Float,
    lightRadius: Float,
    ambientColor: Int,
    spotColor: Int,
    flags: Int
)


@ExternalSymbolName("org_jetbrains_skia_ShadowUtils__1nComputeTonalAmbientColor")
internal external fun ShadowUtils_nComputeTonalAmbientColor(ambientColor: Int, spotColor: Int): Int

@ExternalSymbolName("org_jetbrains_skia_ShadowUtils__1nComputeTonalSpotColor")
internal external fun ShadowUtils_nComputeTonalSpotColor(ambientColor: Int, spotColor: Int): Int
