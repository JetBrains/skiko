@file:org.jetbrains.skia.QuasiJsModule("./skiko.mjs")
@file:org.jetbrains.skia.QuasiJsNonModule
@file:org.jetbrains.skia.QuasiJsQualifier("wasmExports")
package org.jetbrains.skia.paragraph

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetFinalizer")
internal external fun ParagraphStyle_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nMake")
internal external fun ParagraphStyle_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetHeight")
internal external fun ParagraphStyle_nGetHeight(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nEquals")
internal external fun ParagraphStyle_nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetStrutStyle")
internal external fun ParagraphStyle_nGetStrutStyle(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetStrutStyle")
internal external fun ParagraphStyle_nSetStrutStyle(ptr: NativePointer, stylePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetTextStyle")
internal external fun ParagraphStyle_nGetTextStyle(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetTextStyle")
internal external fun ParagraphStyle_nSetTextStyle(ptr: NativePointer, textStylePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetDirection")
internal external fun ParagraphStyle_nGetDirection(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetDirection")
internal external fun ParagraphStyle_nSetDirection(ptr: NativePointer, direction: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetAlignment")
internal external fun ParagraphStyle_nGetAlignment(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetAlignment")
internal external fun ParagraphStyle_nSetAlignment(ptr: NativePointer, align: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetMaxLinesCount")
internal external fun ParagraphStyle_nGetMaxLinesCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetMaxLinesCount")
internal external fun ParagraphStyle_nSetMaxLinesCount(ptr: NativePointer, maxLines: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetEllipsis")
internal external fun ParagraphStyle_nGetEllipsis(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetEllipsis")
internal external fun ParagraphStyle_nSetEllipsis(ptr: NativePointer, ellipsis: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetHeight")
internal external fun ParagraphStyle_nSetHeight(ptr: NativePointer, height: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetHeightMode")
internal external fun ParagraphStyle_nGetHeightMode(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetHeightMode")
internal external fun ParagraphStyle_nSetHeightMode(ptr: NativePointer, v: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetEffectiveAlignment")
internal external fun ParagraphStyle_nGetEffectiveAlignment(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nIsHintingEnabled")
internal external fun ParagraphStyle_nIsHintingEnabled(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nDisableHinting")
internal external fun ParagraphStyle_nDisableHinting(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetFontRastrSettings")
internal external fun ParagraphStyle_nSetFontRastrSettings(ptr: NativePointer, edging: Int, hinting: Int, subpixel: Boolean)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetEdging")
internal external fun ParagraphStyle_nGetEdging(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetHinting")
internal external fun ParagraphStyle_nGetHinting(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetSubpixel")
internal external fun ParagraphStyle_nGetSubpixel(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetApplyRoundingHack")
internal external fun ParagraphStyle_nGetApplyRoundingHack(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetApplyRoundingHack")
internal external fun ParagraphStyle_nSetApplyRoundingHack(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetTextIndent")
internal external fun ParagraphStyle_nSetTextIndent(ptr: NativePointer, firstLine: Float, restLine: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetTextIndent")
internal external fun ParagraphStyle_nGetTextIndent(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetReplaceTabCharacters")
internal external fun ParagraphStyle_nGetReplaceTabCharacters(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetReplaceTabCharacters")
internal external fun ParagraphStyle_nSetReplaceTabCharacters(ptr: NativePointer, value: Boolean)
