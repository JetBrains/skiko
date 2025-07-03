@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetFinalizer")
internal external fun Font_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nMakeClone")
internal external fun Font_nMakeClone(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nEquals")
internal external fun Font_nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetSize")
internal external fun Font_nGetSize(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_Font__1nMakeDefault")
internal external fun Font_nMakeDefault(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nMakeTypeface")
internal external fun Font_nMakeTypeface(typefacePtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nMakeTypefaceSize")
internal external fun Font_nMakeTypefaceSize(typefacePtr: NativePointer, size: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nMakeTypefaceSizeScaleSkew")
internal external fun Font_nMakeTypefaceSizeScaleSkew(typefacePtr: NativePointer, size: Float, scaleX: Float, skewX: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nIsAutoHintingForced")
internal external fun Font_nIsAutoHintingForced(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nAreBitmapsEmbedded")
internal external fun Font_nAreBitmapsEmbedded(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nIsSubpixel")
internal external fun Font_nIsSubpixel(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nIsLinearMetrics")
internal external fun Font_nIsLinearMetrics(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nIsEmboldened")
internal external fun Font_nIsEmboldened(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nIsBaselineSnapped")
internal external fun Font_nIsBaselineSnapped(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetAutoHintingForced")
internal external fun Font_nSetAutoHintingForced(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetBitmapsEmbedded")
internal external fun Font_nSetBitmapsEmbedded(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetSubpixel")
internal external fun Font_nSetSubpixel(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetLinearMetrics")
internal external fun Font_nSetLinearMetrics(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetEmboldened")
internal external fun Font_nSetEmboldened(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetBaselineSnapped")
internal external fun Font_nSetBaselineSnapped(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetEdging")
internal external fun Font_nGetEdging(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetEdging")
internal external fun Font_nSetEdging(ptr: NativePointer, value: Int)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetHinting")
internal external fun Font_nGetHinting(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetHinting")
internal external fun Font_nSetHinting(ptr: NativePointer, value: Int)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetTypeface")
internal external fun Font_nGetTypeface(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetScaleX")
internal external fun Font_nGetScaleX(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetSkewX")
internal external fun Font_nGetSkewX(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetTypeface")
internal external fun Font_nSetTypeface(ptr: NativePointer, typefacePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetSize")
internal external fun Font_nSetSize(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetScaleX")
internal external fun Font_nSetScaleX(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetSkewX")
internal external fun Font_nSetSkewX(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetUTF32Glyph")
internal external fun Font_nGetUTF32Glyph(ptr: NativePointer, uni: Int): Short

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetUTF32Glyphs")
internal external fun Font_nGetUTF32Glyphs(ptr: NativePointer, uni: InteropPointer, uniArrLen: Int, resultGlyphs: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetStringGlyphsCount")
internal external fun Font_nGetStringGlyphsCount(ptr: NativePointer, str: InteropPointer, len: Int): Int

@ExternalSymbolName("org_jetbrains_skia_Font__1nMeasureText")
internal external fun Font_nMeasureText(ptr: NativePointer, str: InteropPointer, len: Int, paintPtr: NativePointer, rect: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Font__1nMeasureTextWidth")
internal external fun Font_nMeasureTextWidth(ptr: NativePointer, str: InteropPointer, len: Int, paintPtr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetWidths")
internal external fun Font_nGetWidths(ptr: NativePointer, glyphs: InteropPointer, count: Int, width: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetBounds")
internal external fun Font_nGetBounds(ptr: NativePointer, glyphs: InteropPointer, count: Int, paintPtr: NativePointer, bounds: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetPositions")
internal external fun Font_nGetPositions(ptr: NativePointer, glyphs: InteropPointer, count: Int, x: Float, y: Float, positions: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetXPositions")
internal external fun Font_nGetXPositions(ptr: NativePointer, glyphs: InteropPointer, x: Float, count: Int, positions: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetPath")
internal external fun Font_nGetPath(ptr: NativePointer, glyph: Short): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetPaths")
internal external fun Font_nGetPaths(ptr: NativePointer, glyphs: InteropPointer, count: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetMetrics")
internal external fun Font_nGetMetrics(ptr: NativePointer, metrics: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetSpacing")
internal external fun Font_nGetSpacing(ptr: NativePointer): Float
