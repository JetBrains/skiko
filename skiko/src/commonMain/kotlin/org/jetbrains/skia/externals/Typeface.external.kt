@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetUniqueId")
internal external fun Typeface_nGetUniqueId(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nEquals")
internal external fun Typeface_nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetUTF32Glyphs")
internal external fun Typeface_nGetUTF32Glyphs(
    ptr: NativePointer,
    uni: InteropPointer,
    count: Int,
    glyphs: InteropPointer
)

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetUTF32Glyph")
internal external fun Typeface_nGetUTF32Glyph(ptr: NativePointer, unichar: Int): Short

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetBounds")
internal external fun Typeface_nGetBounds(ptr: NativePointer, bounds: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetFontStyle")
internal external fun Typeface_nGetFontStyle(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nIsFixedPitch")
internal external fun Typeface_nIsFixedPitch(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetVariationsCount")
internal external fun Typeface_nGetVariationsCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetVariations")
internal external fun Typeface_nGetVariations(ptr: NativePointer, variations: InteropPointer, count: Int)

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetVariationAxesCount")
internal external fun Typeface_nGetVariationAxesCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetVariationAxes")
internal external fun Typeface_nGetVariationAxes(ptr: NativePointer, axisData: InteropPointer, axisCount: Int)

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nMakeClone")
internal external fun Typeface_nMakeClone(
    ptr: NativePointer,
    variations: InteropPointer,
    variationsCount: Int,
    collectionIndex: Int
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nMakeEmptyTypeface")
internal external fun Typeface_nMakeEmptyTypeface(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetGlyphsCount")
internal external fun Typeface_nGetGlyphsCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetTablesCount")
internal external fun Typeface_nGetTablesCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetTableTagsCount")
internal external fun Typeface_nGetTableTagsCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetTableTags")
internal external fun Typeface_nGetTableTags(ptr: NativePointer, tags: InteropPointer, count: Int)

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetTableSize")
internal external fun Typeface_nGetTableSize(ptr: NativePointer, tag: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetTableData")
internal external fun Typeface_nGetTableData(ptr: NativePointer, tag: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetUnitsPerEm")
internal external fun Typeface_nGetUnitsPerEm(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetKerningPairAdjustments")
internal external fun Typeface_nGetKerningPairAdjustments(
    ptr: NativePointer,
    glyphs: InteropPointer,
    count: Int,
    adjustments: InteropPointer
): Boolean

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetFamilyNames")
internal external fun Typeface_nGetFamilyNames(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetFamilyName")
internal external fun Typeface_nGetFamilyName(ptr: NativePointer): NativePointer
