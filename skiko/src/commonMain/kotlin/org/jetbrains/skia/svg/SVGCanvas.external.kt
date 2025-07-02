@file:org.jetbrains.skia.QuasiJsModule("./skiko.mjs")
@file:org.jetbrains.skia.QuasiJsNonModule
@file:org.jetbrains.skia.QuasiJsQualifier("wasmExports")
package org.jetbrains.skia.svg

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_svg_SVGCanvasKt__1nMake")
internal external fun _nMake(left: Float, top: Float, right: Float, bottom: Float, wstreamPtr: NativePointer, flags: Int): NativePointer

