@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")

package org.jetbrains.skia.svg

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.QuasiJsModule
import org.jetbrains.skia.QuasiJsNonModule
import org.jetbrains.skia.QuasiJsQualifier
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_svg_SVGCanvasKt__1nMake")
internal external fun SVGCanvas_nMake(left: Float, top: Float, right: Float, bottom: Float, wstreamPtr: NativePointer, flags: Int): NativePointer

