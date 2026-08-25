package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

/**
 * A CPU-backed index buffer to be used with [Mesh.makeIndexed]. The indices are always unsigned
 * 16-bit integers.
 */
class MeshIndexBuffer internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        /**
         * @param indices The data used to populate the buffer.
         * @throws IllegalArgumentException if [indices] is empty
         */
        fun make(indices: ShortArray): MeshIndexBuffer {
            require(indices.isNotEmpty()) { "Indices must not be empty" }
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

    /** The size of the buffer, in bytes. */
    val size: Int
        get() = try {
            Stats.onNativeCall()
            MeshIndexBuffer_nGetSize(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
}

@ExternalSymbolName("org_jetbrains_skia_MeshIndexBuffer__1nMake")
// Needs ForceNativeThreadState: allocates and copies the whole index array, so its time grows with the array length.
// Native call path: SkMeshes::MakeIndexBuffer -> CpuBuffer::Make -> SkData::MakeWithCopy
@ForceNativeThreadState
private external fun MeshIndexBuffer_nMake(indices: InteropPointer, indexCount: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MeshIndexBuffer__1nGetSize")
// No ForceNativeThreadState: returns the stored byte length, so it is short and cannot block.
// Native call path: SkMesh::IndexBuffer::size
private external fun MeshIndexBuffer_nGetSize(ptr: NativePointer): Int
