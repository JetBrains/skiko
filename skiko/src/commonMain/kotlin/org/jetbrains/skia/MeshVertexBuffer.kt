package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

/** A CPU-backed vertex buffer to be used with [Mesh]. */
class MeshVertexBuffer internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        /**
         * @param data The data used to populate the buffer. Both the size of the data and the size
         *             of the resulting buffer, in bytes.
         * @throws IllegalArgumentException if [data] is empty
         */
        fun make(data: ByteArray): MeshVertexBuffer {
            require(data.isNotEmpty()) { "Vertex data must not be empty" }
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
         *
         * @throws IllegalArgumentException if [data] is empty
         */
        fun make(data: FloatArray): MeshVertexBuffer {
            require(data.isNotEmpty()) { "Vertex data must not be empty" }
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
// Needs ForceNativeThreadState: allocates and copies the whole array of bytes, so its time grows with the array length.
// Native call path: SkMeshes::MakeVertexBuffer -> CpuBuffer::Make -> SkData::MakeWithCopy
@ForceNativeThreadState
private external fun MeshVertexBuffer_nMake(data: InteropPointer, size: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MeshVertexBuffer__1nMakeFromFloats")
// Needs ForceNativeThreadState: allocates and copies the whole array of floats, so its time grows with the array
// length.
// Native call path: SkMeshes::MakeVertexBuffer -> CpuBuffer::Make -> SkData::MakeWithCopy
@ForceNativeThreadState
private external fun MeshVertexBuffer_nMakeFromFloats(data: InteropPointer, count: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MeshVertexBuffer__1nGetSize")
// No ForceNativeThreadState: returns the stored byte length, so it is short and cannot block.
// Native call path: SkMesh::VertexBuffer::size
private external fun MeshVertexBuffer_nGetSize(ptr: NativePointer): Int
