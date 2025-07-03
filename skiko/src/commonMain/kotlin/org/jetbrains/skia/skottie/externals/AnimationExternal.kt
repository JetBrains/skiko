@file:org.jetbrains.skia.QuasiJsModule("./skiko.mjs")
@file:org.jetbrains.skia.QuasiJsNonModule
@file:org.jetbrains.skia.QuasiJsQualifier("wasmExports")
package org.jetbrains.skia.skottie

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nGetFinalizer")
internal external fun Animation_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nMakeFromString")
internal external fun Animation_nMakeFromString(data: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nMakeFromFile")
internal external fun Animation_nMakeFromFile(path: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nMakeFromData")
internal external fun Animation_nMakeFromData(dataPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nRender")
internal external fun Animation_nRender(
    ptr: NativePointer,
    canvasPtr: NativePointer,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    flags: Int
)


@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nSeek")
internal external fun Animation_nSeek(ptr: NativePointer, t: Float, icPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nSeekFrame")
internal external fun Animation_nSeekFrame(ptr: NativePointer, t: Float, icPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nSeekFrameTime")
internal external fun Animation_nSeekFrameTime(ptr: NativePointer, t: Float, icPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nGetDuration")
internal external fun Animation_nGetDuration(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nGetFPS")
internal external fun Animation_nGetFPS(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nGetInPoint")
internal external fun Animation_nGetInPoint(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nGetOutPoint")
internal external fun Animation_nGetOutPoint(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nGetVersion")
internal external fun Animation_nGetVersion(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nGetSize")
internal external fun Animation_nGetSize(ptr: NativePointer, dst: InteropPointer)
