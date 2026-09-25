package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

/** A CPU-backed vertex buffer to be used with [Mesh]. */
class MeshVertexBuffer internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        /**
         * @param data The data used to populate the buffer. Both the size of the data and the size
         *             of the resulting buffer, in bytes.
         */
        fun make(data: ByteArray): MeshVertexBuffer {
            Stats.onNativeCall()
            val ptr = interopScope {
                MeshVertexBuffer_nMake(toInterop(data), data.size)
            }
            check(ptr != Native.NullPointer) { "Failed to allocate a vertex buffer of ${data.size} bytes" }
            return MeshVertexBuffer(ptr)
        }

        /**
         * Populates the buffer from [data], for a vertex layout made only of float attributes. The
         * floats are stored in the host's byte order, the same bytes [make] would be given.
         */
        fun make(data: FloatArray): MeshVertexBuffer {
            Stats.onNativeCall()
            val ptr = interopScope {
                MeshVertexBuffer_nMakeFromFloats(toInterop(data), data.size)
            }
            check(ptr != Native.NullPointer) {
                "Failed to allocate a vertex buffer of ${data.size * 4} bytes"
            }
            return MeshVertexBuffer(ptr)
        }

        init {
            staticLoad()
        }
    }

    /** The size of the buffer, in bytes. */
    val size: Int
        get() = try {
            Stats.onNativeCall()
            MeshVertexBuffer_nGetSize(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
}

@ExternalSymbolName("org_jetbrains_skia_MeshVertexBuffer__1nMake")
private external fun MeshVertexBuffer_nMake(data: InteropPointer, size: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MeshVertexBuffer__1nMakeFromFloats")
private external fun MeshVertexBuffer_nMakeFromFloats(data: InteropPointer, count: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MeshVertexBuffer__1nGetSize")
private external fun MeshVertexBuffer_nGetSize(ptr: NativePointer): Int
