package org.jetbrains.skia.paragraph

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ModuleImport
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetFinalizer")
internal external fun Paragraph_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetMaxWidth")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetMaxWidth")
internal external fun Paragraph_nGetMaxWidth(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetHeight")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetHeight")
internal external fun Paragraph_nGetHeight(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetMinIntrinsicWidth")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetMinIntrinsicWidth")
internal external fun Paragraph_nGetMinIntrinsicWidth(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetMaxIntrinsicWidth")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetMaxIntrinsicWidth")
internal external fun Paragraph_nGetMaxIntrinsicWidth(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetAlphabeticBaseline")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetAlphabeticBaseline")
internal external fun Paragraph_nGetAlphabeticBaseline(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetIdeographicBaseline")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetIdeographicBaseline")
internal external fun Paragraph_nGetIdeographicBaseline(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetLongestLine")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetLongestLine")
internal external fun Paragraph_nGetLongestLine(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nDidExceedMaxLines")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nDidExceedMaxLines")
internal external fun Paragraph_nDidExceedMaxLines(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nLayout")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nLayout")
internal external fun Paragraph_nLayout(ptr: NativePointer, width: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nPaint")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nPaint")
internal external fun Paragraph_nPaint(ptr: NativePointer, canvasPtr: NativePointer, x: Float, y: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetRectsForRange")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetRectsForRange")
internal external fun Paragraph_nGetRectsForRange(
    ptr: NativePointer,
    start: Int,
    end: Int,
    rectHeightMode: Int,
    rectWidthMode: Int
): InteropPointer


@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetRectsForPlaceholders")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetRectsForPlaceholders")
internal external fun Paragraph_nGetRectsForPlaceholders(ptr: NativePointer): InteropPointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetGlyphPositionAtCoordinate")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetGlyphPositionAtCoordinate")
internal external fun Paragraph_nGetGlyphPositionAtCoordinate(ptr: NativePointer, dx: Float, dy: Float): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetWordBoundary")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetWordBoundary")
internal external fun Paragraph_nGetWordBoundary(ptr: NativePointer, offset: Int, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetLineMetrics")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetLineMetrics")
internal external fun Paragraph_nGetLineMetrics(ptr: NativePointer, textPtr: NativePointer): InteropPointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetLineNumber")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetLineNumber")
internal external fun Paragraph_nGetLineNumber(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nMarkDirty")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nMarkDirty")
internal external fun Paragraph_nMarkDirty(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetUnresolvedGlyphsCount")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetUnresolvedGlyphsCount")
internal external fun Paragraph_nGetUnresolvedGlyphsCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nUpdateAlignment")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nUpdateAlignment")
internal external fun Paragraph_nUpdateAlignment(ptr: NativePointer, Align: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nUpdateFontSize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nUpdateFontSize")
internal external fun Paragraph_nUpdateFontSize(ptr: NativePointer, from: Int, to: Int, size: Float, textPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nUpdateForegroundPaint")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nUpdateForegroundPaint")
internal external fun Paragraph_nUpdateForegroundPaint(ptr: NativePointer, from: Int, to: Int, paintPtr: NativePointer, textPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nUpdateBackgroundPaint")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nUpdateBackgroundPaint")
internal external fun Paragraph_nUpdateBackgroundPaint(ptr: NativePointer, from: Int, to: Int, paintPtr: NativePointer, textPtr: NativePointer)
