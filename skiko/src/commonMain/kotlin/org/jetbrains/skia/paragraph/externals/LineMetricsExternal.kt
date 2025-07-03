@file:org.jetbrains.skia.QuasiJsModule("./skiko.mjs")
@file:org.jetbrains.skia.QuasiJsNonModule
@file:org.jetbrains.skia.QuasiJsQualifier("wasmExports")
package org.jetbrains.skia.paragraph

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.InteropPointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_LineMetrics__1nGetArraySize")
internal external fun LineMetrics_nGetArraySize(array: InteropPointer): Int
@ExternalSymbolName("org_jetbrains_skia_paragraph_LineMetrics__1nDisposeArray")
internal external fun LineMetrics_nDisposeArray(array: InteropPointer)
@ExternalSymbolName("org_jetbrains_skia_paragraph_LineMetrics__1nGetArrayElement")
internal external fun LineMetrics_nGetArrayElement(array: InteropPointer, index: Int, intArgs: InteropPointer, doubleArgs: InteropPointer)
