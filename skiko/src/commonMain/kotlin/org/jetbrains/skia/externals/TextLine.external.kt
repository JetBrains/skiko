package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetFinalizer")
internal external fun TextLine_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetWidth")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetWidth")
internal external fun TextLine_nGetWidth(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetHeight")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetHeight")
internal external fun TextLine_nGetHeight(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetGlyphsLength")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetGlyphsLength")
internal external fun TextLine_nGetGlyphsLength(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetGlyphs")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetGlyphs")
internal external fun TextLine_nGetGlyphs(ptr: NativePointer, resultGlyphs: InteropPointer, resultLength: Int)

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetPositions")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetPositions")
internal external fun TextLine_nGetPositions(ptr: NativePointer, resultArray: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetAscent")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetAscent")
internal external fun TextLine_nGetAscent(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetCapHeight")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetCapHeight")
internal external fun TextLine_nGetCapHeight(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetXHeight")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetXHeight")
internal external fun TextLine_nGetXHeight(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetDescent")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetDescent")
internal external fun TextLine_nGetDescent(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetLeading")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetLeading")
internal external fun TextLine_nGetLeading(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetTextBlob")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetTextBlob")
internal external fun TextLine_nGetTextBlob(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetRunPositions")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetRunPositions")
internal external fun TextLine_nGetRunPositions(ptr: NativePointer, resultArray: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetRunPositionsCount")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetRunPositionsCount")
internal external fun TextLine_nGetRunPositionsCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetBreakPositionsCount")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetBreakPositionsCount")
internal external fun TextLine_nGetBreakPositionsCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetBreakPositions")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetBreakPositions")
internal external fun TextLine_nGetBreakPositions(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetBreakOffsetsCount")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetBreakOffsetsCount")
internal external fun TextLine_nGetBreakOffsetsCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetBreakOffsets")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetBreakOffsets")
internal external fun TextLine_nGetBreakOffsets(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetOffsetAtCoord")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetOffsetAtCoord")
internal external fun TextLine_nGetOffsetAtCoord(ptr: NativePointer, x: Float): Int

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetLeftOffsetAtCoord")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetLeftOffsetAtCoord")
internal external fun TextLine_nGetLeftOffsetAtCoord(ptr: NativePointer, x: Float): Int

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetCoordAtOffset")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetCoordAtOffset")
internal external fun TextLine_nGetCoordAtOffset(ptr: NativePointer, offset: Int): Float
