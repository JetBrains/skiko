package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_StdVectorDecoder__1nGetArraySize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_StdVectorDecoder__1nGetArraySize")
private external fun StdVectorDecoder_nGetArraySize(array: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_StdVectorDecoder__1nDisposeArray")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_StdVectorDecoder__1nDisposeArray")
private external fun StdVectorDecoder_nDisposeArray(array: NativePointer, disposePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_StdVectorDecoder__1nReleaseElement")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_StdVectorDecoder__1nReleaseElement")
private external fun StdVectorDecoder_nReleaseElement(array: NativePointer, index: Int): NativePointer


class ArrayDecoder(private val ptr: NativePointer, private val disposePtr: NativePointer) {
    fun dispose() {
        StdVectorDecoder_nDisposeArray(ptr, disposePtr)
    }

    fun release(index: Int): NativePointer {
        return StdVectorDecoder_nReleaseElement(ptr, index)
    }

    val size: Int
        get() = StdVectorDecoder_nGetArraySize(ptr)
}

internal inline fun <T> arrayDecoderScope(arrayDecoderBlock: () -> ArrayDecoder, block: (arrayCode: ArrayDecoder) -> T): T {
    var arrayDecoder: ArrayDecoder? = null
    return try {
        arrayDecoder = arrayDecoderBlock()
        block.invoke(arrayDecoder)
    } finally {
        arrayDecoder?.dispose()
    }
}