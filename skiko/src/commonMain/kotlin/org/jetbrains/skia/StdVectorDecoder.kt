package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer

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