@file:org.jetbrains.skia.QuasiJsModule("./skiko.mjs")
@file:org.jetbrains.skia.QuasiJsNonModule
@file:org.jetbrains.skia.QuasiJsQualifier("wasmExports")
package org.jetbrains.skia.svg

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetX")
internal external fun SVGSVG_nGetX(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetY")
internal external fun SVGSVG_nGetY(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetWidth")
internal external fun SVGSVG_nGetWidth(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetHeight")
internal external fun SVGSVG_nGetHeight(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetPreserveAspectRatio")
internal external fun SVGSVG_nGetPreserveAspectRatio(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetViewBox")
internal external fun SVGSVG_nGetViewBox(ptr: NativePointer, result: InteropPointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetIntrinsicSize")
internal external fun SVGSVG_nGetIntrinsicSize(ptr: NativePointer, width: Float, height: Float, dpi: Float, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetX")
internal external fun SVGSVG_nSetX(ptr: NativePointer, value: Float, unit: Int)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetY")
internal external fun SVGSVG_nSetY(ptr: NativePointer, value: Float, unit: Int)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetWidth")
internal external fun SVGSVG_nSetWidth(ptr: NativePointer, value: Float, unit: Int)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetHeight")
internal external fun SVGSVG_nSetHeight(ptr: NativePointer, value: Float, unit: Int)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetPreserveAspectRatio")
internal external fun SVGSVG_nSetPreserveAspectRatio(ptr: NativePointer, align: Int, scale: Int)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetViewBox")
internal external fun SVGSVG_nSetViewBox(ptr: NativePointer, l: Float, t: Float, r: Float, b: Float)
