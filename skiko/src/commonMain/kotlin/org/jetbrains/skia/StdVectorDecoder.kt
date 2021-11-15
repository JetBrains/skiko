package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_StdVectorDecoder__1nGetArraySize")
private external fun StdVectorDecoder_nGetArraySize(array: NativePointer): Int
@ExternalSymbolName("org_jetbrains_skia_StdVectorDecoder__1nDisposeArray")
private external fun StdVectorDecoder_nDisposeArray(array: NativePointer)
@ExternalSymbolName("org_jetbrains_skia_StdVectorDecoder__1nReleaseElement")
private external fun StdVectorDecoder_nReleaseElement(array: NativePointer, index: Int): NativePointer


class ArrayDecoder(private val ptr: NativePointer) {
    fun dispose() {
        StdVectorDecoder_nDisposeArray(ptr)
    }

    fun release(index: Int): NativePointer {
        return StdVectorDecoder_nReleaseElement(ptr, index)
    }

    val size: Int
        get() = StdVectorDecoder_nGetArraySize(ptr)
}