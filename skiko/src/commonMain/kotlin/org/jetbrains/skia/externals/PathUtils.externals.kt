@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathUtils__1nFillPathWithPaint")
internal external fun PathUtils_nFillPathWithPaint(
    srcPtr: NativePointer,
    paintPtr: NativePointer,
    matrix: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathUtils__1nFillPathWithPaintCull")
internal external fun PathUtils_nFillPathWithPaintCull(
    srcPtr: NativePointer,
    paintPtr: NativePointer,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    matrix: InteropPointer
): NativePointer
