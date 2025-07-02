@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeEmpty")
internal external fun Shader_nMakeEmpty(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeWithColorFilter")
internal external fun Shader_nMakeWithColorFilter(ptr: NativePointer, colorFilterPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeLinearGradient")
internal external fun Shader_nMakeLinearGradient(
    x0: Float,
    y0: Float,
    x1: Float,
    y1: Float,
    colors: InteropPointer,
    positions: InteropPointer,
    count: Int,
    tileType: Int,
    flags: Int,
    matrix: InteropPointer
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeLinearGradientCS")
internal external fun Shader_nMakeLinearGradientCS(
    x0: Float,
    y0: Float,
    x1: Float,
    y1: Float,
    colors: InteropPointer,
    colorSpacePtr: NativePointer,
    positions: InteropPointer,
    count: Int,
    tileType: Int,
    flags: Int,
    matrix: InteropPointer
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeRadialGradient")
internal external fun Shader_nMakeRadialGradient(
    x: Float,
    y: Float,
    r: Float,
    colors: InteropPointer,
    positions: InteropPointer,
    count: Int,
    tileType: Int,
    flags: Int,
    matrix: InteropPointer
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeRadialGradientCS")
internal external fun Shader_nMakeRadialGradientCS(
    x: Float,
    y: Float,
    r: Float,
    colors: InteropPointer,
    colorSpacePtr: NativePointer,
    positions: InteropPointer,
    count: Int,
    tileType: Int,
    flags: Int,
    matrix: InteropPointer
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeTwoPointConicalGradient")
internal external fun Shader_nMakeTwoPointConicalGradient(
    x0: Float,
    y0: Float,
    r0: Float,
    x1: Float,
    y1: Float,
    r1: Float,
    colors: InteropPointer,
    positions: InteropPointer,
    count: Int,
    tileType: Int,
    flags: Int,
    matrix: InteropPointer
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeTwoPointConicalGradientCS")
internal external fun Shader_nMakeTwoPointConicalGradientCS(
    x0: Float,
    y0: Float,
    r0: Float,
    x1: Float,
    y1: Float,
    r1: Float,
    colors: InteropPointer,
    colorSpacePtr: NativePointer,
    positions: InteropPointer,
    count: Int,
    tileType: Int,
    flags: Int,
    matrix: InteropPointer
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeSweepGradient")
internal external fun Shader_nMakeSweepGradient(
    x: Float,
    y: Float,
    startAngle: Float,
    endAngle: Float,
    colors: InteropPointer,
    positions: InteropPointer,
    count: Int,
    tileType: Int,
    flags: Int,
    matrix: InteropPointer
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeSweepGradientCS")
internal external fun Shader_nMakeSweepGradientCS(
    x: Float,
    y: Float,
    startAngle: Float,
    endAngle: Float,
    colors: InteropPointer,
    colorSpacePtr: NativePointer,
    positions: InteropPointer,
    count: Int,
    tileType: Int,
    flags: Int,
    matrix: InteropPointer
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeFractalNoise")
internal external fun Shader_nMakeFractalNoise(
    baseFrequencyX: Float,
    baseFrequencyY: Float,
    numOctaves: Int,
    seed: Float,
    tileWidth: Int,
    tileHeight: Int,
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeTurbulence")
internal external fun Shader_nMakeTurbulence(
    baseFrequencyX: Float,
    baseFrequencyY: Float,
    numOctaves: Int,
    seed: Float,
    tileWidth: Int,
    tileHeight: Int,
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeColor")
internal external fun Shader_nMakeColor(color: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeColorCS")
internal external fun Shader_nMakeColorCS(r: Float, g: Float, b: Float, a: Float, colorSpacePtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeBlend")
internal external fun Shader_nMakeBlend(blendMode: Int, dst: NativePointer, src: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeWithLocalMatrix")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Shader__1nMakeWithLocalMatrix")
internal external fun Shader_nMakeWithLocalMatrix(ptr: NativePointer, localMatrix: InteropPointer): NativePointer
