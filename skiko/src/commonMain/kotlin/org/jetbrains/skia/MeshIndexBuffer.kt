package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

/**
 * Indices into a [MeshVertexBuffer], used by [Mesh.makeIndexed].
 *
 * Indices are unsigned 16-bit integers. The buffer is CPU-backed: it may be drawn through any
 * [Canvas], and the same instance may be shared between meshes and between surfaces.
 */
class MeshIndexBuffer internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        /** Creates a buffer holding a copy of [indices]. */
        fun make(indices: ShortArray): MeshIndexBuffer {
            Stats.onNativeCall()
            val ptr = interopScope {
                MeshIndexBuffer_nMake(toInterop(indices), indices.size)
            }
            check(ptr != Native.NullPointer) { "Failed to allocate an index buffer of ${indices.size} indices" }
            return MeshIndexBuffer(ptr)
        }

        init {
            staticLoad()
        }
    }

    /** The size of the buffer in bytes. */
    val size: Int
        get() = try {
            Stats.onNativeCall()
            MeshIndexBuffer_nGetSize(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
}

@ExternalSymbolName("org_jetbrains_skia_MeshIndexBuffer__1nMake")
private external fun MeshIndexBuffer_nMake(indices: InteropPointer, indexCount: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MeshIndexBuffer__1nGetSize")
private external fun MeshIndexBuffer_nGetSize(ptr: NativePointer): Int
