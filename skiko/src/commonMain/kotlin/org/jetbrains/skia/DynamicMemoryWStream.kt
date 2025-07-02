package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
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

    fun bytesWritten(): Int = DynamicMemoryWStream_nBytesWritten(_ptr)

    fun read(buffer: ByteArray, offset: Int, size: Int): Boolean {
        check(buffer.size >= size) {
            "byteArray is not properly allocated. Use bytesWritten"
        }

        try {
            Stats.onNativeCall()
            interopScope {
                val byteArrayHandle = toInteropForResult(buffer)
                val successfulRead = DynamicMemoryWStream_nRead(_ptr, byteArrayHandle, offset, size)
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