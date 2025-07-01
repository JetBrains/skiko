package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.*

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeComposed")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nMakeComposed")
internal external fun _nMakeComposed(outer: NativePointer, inner: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeBlend")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nMakeBlend")
internal external fun _nMakeBlend(color: Int, blendMode: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeMatrix")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nMakeMatrix")
internal external fun _nMakeMatrix(rowMajor: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeHSLAMatrix")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nMakeHSLAMatrix")
internal external fun _nMakeHSLAMatrix(rowMajor: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nGetLinearToSRGBGamma")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nGetLinearToSRGBGamma")
internal external fun _nGetLinearToSRGBGamma(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nGetSRGBToLinearGamma")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nGetSRGBToLinearGamma")
internal external fun _nGetSRGBToLinearGamma(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeLerp")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nMakeLerp")
internal external fun _nMakeLerp(t: Float, dstPtr: NativePointer, srcPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeLighting")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nMakeLighting")
internal external fun _nMakeLighting(colorMul: Int, colorAdd: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeHighContrast")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nMakeHighContrast")
internal external fun _nMakeHighContrast(grayscale: Boolean, inversionMode: Int, contrast: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeTable")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nMakeTable")
internal external fun _nMakeTable(table: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeOverdraw")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nMakeOverdraw")
internal external fun _nMakeOverdraw(c0: Int, c1: Int, c2: Int, c3: Int, c4: Int, c5: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nGetLuma")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nGetLuma")
internal external fun _nGetLuma(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeTableARGB")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nMakeTableARGB")
internal external fun _nMakeTableARGB(
    a: InteropPointer,
    r: InteropPointer,
    g: InteropPointer,
    b: InteropPointer,
): NativePointer
