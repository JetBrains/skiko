@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextBlob__1nGetFinalizer")
internal external fun TextBlob_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextBlob__1nGetUniqueId")
internal external fun TextBlob_nGetUniqueId(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_TextBlob__1nSerializeToData")
internal external fun TextBlob_nSerializeToData(ptr: NativePointer /*, SkSerialProcs */): NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextBlob__1nMakeFromData")
internal external fun TextBlob_nMakeFromData(dataPtr: NativePointer /*, SkDeserialProcs */): NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextBlob__1nBounds")
internal external fun TextBlob_nBounds(ptr: NativePointer, resultRect: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_TextBlob__1nGetInterceptsLength")
internal external fun TextBlob_nGetInterceptsLength(ptr: NativePointer, lower: Float, upper: Float, paintPtr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_TextBlob__1nGetIntercepts")
internal external fun TextBlob_nGetIntercepts(ptr: NativePointer, lower: Float, upper: Float, paintPtr: NativePointer, resultArray: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_TextBlob__1nMakeFromPosH")
internal external fun TextBlob_nMakeFromPosH(glyphs: InteropPointer, glyphsLen: Int, xpos: InteropPointer, ypos: Float, fontPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextBlob__1nMakeFromPos")
internal external fun TextBlob_nMakeFromPos(glyphs: InteropPointer, glyphsLen: Int, pos: InteropPointer, fontPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextBlob__1nMakeFromRSXform")
internal external fun TextBlob_nMakeFromRSXform(glyphs: InteropPointer, glyphsLen: Int, xform: InteropPointer, fontPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextBlob__1nGetGlyphsLength")
internal external fun TextBlob_nGetGlyphsLength(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_TextBlob__1nGetGlyphs")
internal external fun TextBlob_nGetGlyphs(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_TextBlob__1nGetPositionsLength")
internal external fun TextBlob_nGetPositionsLength(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_TextBlob__1nGetPositions")
internal external fun TextBlob_nGetPositions(ptr: NativePointer, resultArray: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_TextBlob__1nGetClustersLength")
internal external fun TextBlob_nGetClustersLength(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_TextBlob__1nGetClusters")
internal external fun TextBlob_nGetClusters(ptr: NativePointer, resultArray: InteropPointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_TextBlob__1nGetTightBounds")
internal external fun TextBlob_nGetTightBounds(ptr: NativePointer, resultArray: InteropPointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_TextBlob__1nGetBlockBounds")
internal external fun TextBlob_nGetBlockBounds(ptr: NativePointer, resultArray: InteropPointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_TextBlob__1nGetFirstBaseline")
internal external fun TextBlob_nGetFirstBaseline(ptr: NativePointer, resultArray: InteropPointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_TextBlob__1nGetLastBaseline")
internal external fun TextBlob_nGetLastBaseline(ptr: NativePointer, resultArray: InteropPointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_TextBlob_Iter__1nCreate")
internal external fun Iter_nCreate(textBlobPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextBlob_Iter__1nGetFinalizer")
internal external fun Iter_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextBlob_Iter__1nFetch")
internal external fun Iter_nFetch(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_TextBlob_Iter__1nGetTypeface")
internal external fun Iter_nGetTypeface(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextBlob_Iter__1nHasNext")
internal external fun Iter_nHasNext(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_TextBlob_Iter__1nGetGlyphCount")
internal external fun Iter_nGetGlyphCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_TextBlob_Iter__1nGetGlyphs")
internal external fun Iter_nGetGlyphs(ptr: NativePointer, glyphs: InteropPointer, max: Int): Int
