@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_PaintFilterCanvas__1nMake")
internal external fun PaintFilterCanvas_nMake(canvasPtr: NativePointer, unrollDrawable: Boolean): NativePointer
