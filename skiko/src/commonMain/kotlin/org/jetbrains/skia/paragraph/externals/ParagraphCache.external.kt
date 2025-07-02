@file:org.jetbrains.skia.QuasiJsModule("./skiko.mjs")
@file:org.jetbrains.skia.QuasiJsNonModule
@file:org.jetbrains.skia.QuasiJsQualifier("wasmExports")
package org.jetbrains.skia.paragraph

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphCache__1nAbandon")
internal external fun ParagraphCache_nAbandon(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphCache__1nReset")
internal external fun ParagraphCache_nReset(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphCache__1nUpdateParagraph")
internal external fun ParagraphCache_nUpdateParagraph(ptr: NativePointer, paragraphPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphCache__1nFindParagraph")
internal external fun ParagraphCache_nFindParagraph(ptr: NativePointer, paragraphPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphCache__1nPrintStatistics")
internal external fun ParagraphCache_nPrintStatistics(ptr: NativePointer, paragraphPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphCache__1nSetEnabled")
internal external fun ParagraphCache_nSetEnabled(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphCache__1nGetCount")
internal external fun ParagraphCache_nGetCount(ptr: NativePointer): Int
