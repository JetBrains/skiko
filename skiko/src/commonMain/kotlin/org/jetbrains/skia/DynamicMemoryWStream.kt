package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier

class DynamicMemoryWStream() : WStream(DynamicMemoryWStream_nMake(), _FinalizerHolder.PTR) {
    companion object {
        init {
            staticLoad()
        }
    }

    private object _FinalizerHolder {
        val PTR = DynamicMemoryWStream_nGetFinalizer()
    }

    fun bytesWritten(): Int = _nBytesWritten(_ptr)

    fun read(buffer: ByteArray, offset: Int, size: Int): Boolean {
        check(buffer.size >= size) {
            "byteArray is not properly allocated. Use bytesWritten"
        }

        try {
            Stats.onNativeCall()
            interopScope {
                val byteArrayHandle = toInteropForResult(buffer)
                val successfulRead = _nRead(_ptr, byteArrayHandle, offset, size)
                if (successfulRead) {
                    byteArrayHandle.fromInterop(buffer)
                }
                return successfulRead
            }
        } finally {
            reachabilityBarrier(this)
        }
    }

    init {
        Stats.onNativeCall()
    }
}

@ExternalSymbolName("org_jetbrains_skia_DynamicMemoryWStream__1nMake")
private external fun DynamicMemoryWStream_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_DynamicMemoryWStream__1nGetFinalizer")
private external fun DynamicMemoryWStream_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_DynamicMemoryWStream__1nBytesWritten")
private external fun _nBytesWritten(stream: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_DynamicMemoryWStream__1nRead")
private external fun _nRead(stream: NativePointer, buffer: InteropPointer, offset: Int, size: Int): Boolean