package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

/**
 * Vertex data for a [Mesh], laid out as described by its [MeshSpecification].
 *
 * The buffer is CPU-backed: it may be drawn through any [Canvas], and the same instance may be
 * shared between meshes and between surfaces.
 */
class MeshVertexBuffer internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        /**
         * Creates a buffer holding a copy of [data].
         *
         * [data] must contain the attributes declared by the specification the buffer will be drawn
         * with, for each vertex, at a spacing of [MeshSpecification.stride] bytes.
         */
        fun make(data: ByteArray): MeshVertexBuffer {
            Stats.onNativeCall()
            val ptr = interopScope {
                MeshVertexBuffer_nMake(toInterop(data), data.size)
            }
            check(ptr != Native.NullPointer) { "Failed to allocate a vertex buffer of ${data.size} bytes" }
            return MeshVertexBuffer(ptr)
        }

        init {
            staticLoad()
        }
    }

    /** The size of the buffer in bytes. */
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

@ExternalSymbolName("org_jetbrains_skia_MeshVertexBuffer__1nGetSize")
private external fun MeshVertexBuffer_nGetSize(ptr: NativePointer): Int
