@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_PaintFilterCanvas__1nInit")
internal external fun PaintFilterCanvas_nInit(ptr: NativePointer, onFilter: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_PaintFilterCanvas__1nGetOnFilterPaint")
internal external fun PaintFilterCanvas_nGetOnFilterPaint(ptr: NativePointer): NativePointer
