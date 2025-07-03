@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextBlobBuilder__1nGetFinalizer")
internal external fun TextBlobBuilder_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextBlobBuilder__1nMake")
internal external fun TextBlobBuilder_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextBlobBuilder__1nBuild")
internal external fun TextBlobBuilder_nBuild(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextBlobBuilder__1nAppendRun")
internal external fun TextBlobBuilder_nAppendRun(
    ptr: NativePointer, fontPtr: NativePointer,
    glyphs: InteropPointer, glyphsLen: Int,
    x: Float, y: Float,
    bounds: InteropPointer
)

@ExternalSymbolName("org_jetbrains_skia_TextBlobBuilder__1nAppendRunPosH")
internal external fun TextBlobBuilder_nAppendRunPosH(
    ptr: NativePointer,
    fontPtr: NativePointer,
    glyphs: InteropPointer,
    glyphsLen: Int,
    xs: InteropPointer,
    y: Float,
    bounds: InteropPointer
)


@ExternalSymbolName("org_jetbrains_skia_TextBlobBuilder__1nAppendRunPos")
internal external fun TextBlobBuilder_nAppendRunPos(
    ptr: NativePointer, fontPtr: NativePointer,
    glyphs: InteropPointer, glyphsLen: Int,
    pos: InteropPointer,
    bounds: InteropPointer
)

@ExternalSymbolName("org_jetbrains_skia_TextBlobBuilder__1nAppendRunRSXform")
internal external fun TextBlobBuilder_nAppendRunRSXform(
    ptr: NativePointer, fontPtr: NativePointer,
    glyphs: InteropPointer, glyphsLen: Int,
    xform: InteropPointer
)
