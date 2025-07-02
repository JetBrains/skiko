@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_Picture__1nMakeFromData")
internal external fun Picture_nMakeFromData(dataPtr: NativePointer /*, SkDeserialProcs */): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Picture__1nGetCullRect")
internal external fun Picture_nGetCullRect(ptr: NativePointer, ltrb: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Picture__1nGetUniqueId")
internal external fun Picture_nGetUniqueId(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Picture__1nSerializeToData")
internal external fun Picture_nSerializeToData(ptr: NativePointer /*, SkSerialProcs */): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Picture__1nMakePlaceholder")
internal external fun Picture_nMakePlaceholder(left: Float, top: Float, right: Float, bottom: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Picture__1nGetApproximateOpCount")
internal external fun Picture_nGetApproximateOpCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Picture__1nGetApproximateBytesUsed")
internal external fun Picture_nGetApproximateBytesUsed(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Picture__1nMakeShader")
internal external fun Picture_nMakeShader(
    ptr: NativePointer,
    tmx: Int,
    tmy: Int,
    filterMode: Int,
    localMatrix: InteropPointer,
    hasTile: Boolean,
    tileL: Float,
    tileT: Float,
    tileR: Float,
    tileB: Float,
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Picture__1nPlayback")
internal external fun Picture_nPlayback(ptr: NativePointer, canvasPtr: NativePointer, data: InteropPointer)
