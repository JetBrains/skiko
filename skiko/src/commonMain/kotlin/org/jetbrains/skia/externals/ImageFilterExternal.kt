@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeArithmetic")
internal external fun ImageFilter_nMakeArithmetic(
    k1: Float,
    k2: Float,
    k3: Float,
    k4: Float,
    enforcePMColor: Boolean,
    bg: NativePointer,
    fg: NativePointer,
    crop: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeBlend")
internal external fun ImageFilter_nMakeBlend(blendMode: Int, bg: NativePointer, fg: NativePointer, crop: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeBlur")
internal external fun ImageFilter_nMakeBlur(sigmaX: Float, sigmaY: Float, tileMode: Int, input: NativePointer, crop: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeColorFilter")
internal external fun ImageFilter_nMakeColorFilter(colorFilterPtr: NativePointer, input: NativePointer, crop: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeCompose")
internal external fun ImageFilter_nMakeCompose(outer: NativePointer, inner: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeDisplacementMap")
internal external fun ImageFilter_nMakeDisplacementMap(
    xChan: Int,
    yChan: Int,
    scale: Float,
    displacement: NativePointer,
    color: NativePointer,
    crop: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeDropShadow")
internal external fun ImageFilter_nMakeDropShadow(
    dx: Float,
    dy: Float,
    sigmaX: Float,
    sigmaY: Float,
    color: Int,
    input: NativePointer,
    crop: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeDropShadowOnly")
internal external fun ImageFilter_nMakeDropShadowOnly(
    dx: Float,
    dy: Float,
    sigmaX: Float,
    sigmaY: Float,
    color: Int,
    input: NativePointer,
    crop: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeImage")
internal external fun ImageFilter_nMakeImage(
    image: NativePointer,
    l0: Float,
    t0: Float,
    r0: Float,
    b0: Float,
    l1: Float,
    t1: Float,
    r1: Float,
    b1: Float,
    samplingModeVal1: Int,
    samplingModeVal2: Int,
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeMagnifier")
internal external fun ImageFilter_nMakeMagnifier(
    l: Float,
    t: Float,
    r: Float,
    b: Float,
    zoomAmount: Float,
    inset: Float,
    samplingModeVal1: Int,
    samplingModeVal2: Int,
    input: NativePointer,
    crop: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeMatrixConvolution")
internal external fun ImageFilter_nMakeMatrixConvolution(
    kernelW: Int,
    kernelH: Int,
    kernel: InteropPointer,
    gain: Float,
    bias: Float,
    offsetX: Int,
    offsetY: Int,
    tileMode: Int,
    convolveAlpha: Boolean,
    input: NativePointer,
    crop: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeMatrixTransform")
internal external fun ImageFilter_nMakeMatrixTransform(matrix: InteropPointer, samplingModeVal1: Int, samplingModeVal2: Int, input: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeMerge")
internal external fun ImageFilter_nMakeMerge(filters: InteropPointer, filtersLength: Int, crop: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeOffset")
internal external fun ImageFilter_nMakeOffset(dx: Float, dy: Float, input: NativePointer, crop: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeShader")
internal external fun ImageFilter_nMakeShader(shader: NativePointer, dither: Boolean, crop: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakePicture")
internal external fun ImageFilter_nMakePicture(picture: NativePointer, l: Float, t: Float, r: Float, b: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeRuntimeShader")
internal external fun ImageFilter_nMakeRuntimeShader(runtimeShaderBuilderPtr: NativePointer, childShaderName: InteropPointer, input: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeRuntimeShaderFromArray")
internal external fun ImageFilter_nMakeRuntimeShaderFromArray(runtimeShaderBuilderPtr: NativePointer, childShaderNames: InteropPointer, inputs: InteropPointer, inputLength: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeTile")
internal external fun ImageFilter_nMakeTile(
    l0: Float,
    t0: Float,
    r0: Float,
    b0: Float,
    l1: Float,
    t1: Float,
    r1: Float,
    b1: Float,
    input: NativePointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeDilate")
internal external fun ImageFilter_nMakeDilate(rx: Float, ry: Float, input: NativePointer, crop: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeErode")
internal external fun ImageFilter_nMakeErode(rx: Float, ry: Float, input: NativePointer, crop: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeDistantLitDiffuse")
internal external fun ImageFilter_nMakeDistantLitDiffuse(
    x: Float,
    y: Float,
    z: Float,
    lightColor: Int,
    surfaceScale: Float,
    kd: Float,
    input: NativePointer,
    crop: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakePointLitDiffuse")
internal external fun ImageFilter_nMakePointLitDiffuse(
    x: Float,
    y: Float,
    z: Float,
    lightColor: Int,
    surfaceScale: Float,
    kd: Float,
    input: NativePointer,
    crop: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeSpotLitDiffuse")
internal external fun ImageFilter_nMakeSpotLitDiffuse(
    x0: Float,
    y0: Float,
    z0: Float,
    x1: Float,
    y1: Float,
    z1: Float,
    falloffExponent: Float,
    cutoffAngle: Float,
    lightColor: Int,
    surfaceScale: Float,
    kd: Float,
    input: NativePointer,
    crop: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeDistantLitSpecular")
internal external fun ImageFilter_nMakeDistantLitSpecular(
    x: Float,
    y: Float,
    z: Float,
    lightColor: Int,
    surfaceScale: Float,
    ks: Float,
    shininess: Float,
    input: NativePointer,
    crop: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakePointLitSpecular")
internal external fun ImageFilter_nMakePointLitSpecular(
    x: Float,
    y: Float,
    z: Float,
    lightColor: Int,
    surfaceScale: Float,
    ks: Float,
    shininess: Float,
    input: NativePointer,
    crop: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeSpotLitSpecular")
internal external fun ImageFilter_nMakeSpotLitSpecular(
    x0: Float,
    y0: Float,
    z0: Float,
    x1: Float,
    y1: Float,
    z1: Float,
    falloffExponent: Float,
    cutoffAngle: Float,
    lightColor: Int,
    surfaceScale: Float,
    ks: Float,
    shininess: Float,
    input: NativePointer,
    crop: InteropPointer
): NativePointer
