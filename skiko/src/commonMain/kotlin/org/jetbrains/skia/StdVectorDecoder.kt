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

// <editor-fold defaultstate="collapsed" desc="Native External Functions">
@ExternalSymbolName("org_jetbrains_skia_StdVectorDecoder__1nGetArraySize")
// No ForceNativeThreadState: returns the stored size of a vector, so it is short and cannot block.
// Native call path: std::vector::size
private external fun StdVectorDecoder_nGetArraySize(array: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_StdVectorDecoder__1nDisposeArray")
// Needs ForceNativeThreadState: calls a caller-supplied destructor for each non-null element, which can release a
// CoreText font; work grows with the vector size.
// Native call path: disposePtr -> SkRefCnt::unref
@ForceNativeThreadState
private external fun StdVectorDecoder_nDisposeArray(array: NativePointer, disposePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_StdVectorDecoder__1nReleaseElement")
// No ForceNativeThreadState: moves one pointer out of the vector and clears its slot without running any destructor, so
// it is short and cannot block.
// Native call path: std::vector::operator[]
private external fun StdVectorDecoder_nReleaseElement(array: NativePointer, index: Int): NativePointer
// </editor-fold>