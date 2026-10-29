package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.nativeCall
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

        return nativeCall {
            interopScope {
                val byteArrayHandle = toInteropForResult(buffer)
                val successfulRead = _nRead(_ptr, byteArrayHandle, offset, size)
                if (successfulRead) {
                    byteArrayHandle.fromInterop(buffer)
                }
                 successfulRead
            }
        }
    }

    init {
        Stats.onNativeCall()
    }
}

@ExternalSymbolName("org_jetbrains_skia_DynamicMemoryWStream__1nMake")
// No ForceNativeThreadState: creates an empty memory stream with null block pointers and no data blocks, so it is short
// and cannot block.
// Native call path: SkDynamicMemoryWStream::SkDynamicMemoryWStream
private external fun DynamicMemoryWStream_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_DynamicMemoryWStream__1nGetFinalizer")
// No ForceNativeThreadState: returns the address of a delete function without calling it, so it is short and cannot
// block.
// Native call path: _nGetFinalizer
private external fun DynamicMemoryWStream_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_DynamicMemoryWStream__1nBytesWritten")
// Needs ForceNativeThreadState: in debug builds, walks every block of the stream, so its time grows with the number of
// blocks.
// Native call path: SkDynamicMemoryWStream::bytesWritten -> validate
@ForceNativeThreadState
private external fun _nBytesWritten(stream: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_DynamicMemoryWStream__1nRead")
// Needs ForceNativeThreadState: walks the block list and copies a caller-chosen number of bytes, so its time grows with
// that size.
// Native call path: SkDynamicMemoryWStream::read -> memcpy
@ForceNativeThreadState
private external fun _nRead(stream: NativePointer, buffer: InteropPointer, offset: Int, size: Int): Boolean