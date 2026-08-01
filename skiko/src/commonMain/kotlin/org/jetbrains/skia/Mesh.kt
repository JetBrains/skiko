package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

/**
 * A drawable mesh: a [MeshSpecification], the vertex data it describes, a topology, and the uniform
 * and child values its programs read.
 *
 * Draw it with [Canvas.drawMesh].
 */
class Mesh internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    /** How consecutive vertices are assembled into triangles. */
    enum class Mode {
        /** Each group of three vertices forms one triangle. */
        TRIANGLES,

        /** Each vertex forms a triangle with the two preceding it. */
        TRIANGLE_STRIP;
    }

    companion object {
        /**
         * Creates a mesh whose vertices are drawn in buffer order.
         *
         * @param specification describes the vertex layout and the programs to run
         * @param mode          how vertices are assembled into triangles
         * @param vertexBuffer  holds the vertex data; must be at least
         *                      `vertexOffset + specification.stride * vertexCount` bytes
         * @param vertexCount   the number of vertices to read; at least 3
         * @param bounds        conservative device-space bounds of the positions produced by the
         *                      vertex program. Drawing is undefined if a position falls outside.
         * @param vertexOffset  byte offset of the first vertex; must be a multiple of
         *                      [MeshSpecification.stride]
         * @param uniforms      values for the uniforms declared by the two programs, packed by
         *                      [MeshUniformBuilder]. Required if either program declares a uniform.
         * @param children      shaders bound to the `uniform shader` declarations of the two
         *                      programs, in declaration order
         *
         * @throws IllegalArgumentException if the mesh is rejected, carrying the message reported by
         *                                  Skia
         */
        fun make(
            specification: MeshSpecification,
            mode: Mode,
            vertexBuffer: MeshVertexBuffer,
            vertexCount: Int,
            bounds: Rect,
            vertexOffset: Int = 0,
            uniforms: Data? = null,
            children: Array<Shader?>? = null
        ): Mesh {
            Stats.onNativeCall()
            val childrenPtrs = childrenPointers(children)
            return try {
                interopScope {
                    Mesh(
                        meshFromResult(
                            Mesh_nMake(
                                getPtr(specification),
                                mode.ordinal,
                                getPtr(vertexBuffer),
                                vertexCount,
                                vertexOffset,
                                getPtr(uniforms),
                                toInterop(childrenPtrs),
                                childrenPtrs.size,
                                bounds.left,
                                bounds.top,
                                bounds.right,
                                bounds.bottom
                            )
                        )
                    )
                }
            } finally {
                reachabilityBarrier(specification)
                reachabilityBarrier(vertexBuffer)
                reachabilityBarrier(uniforms)
                reachabilityBarrier(children)
            }
        }

        /**
         * Creates a mesh whose vertices are drawn in the order given by [indexBuffer].
         *
         * @param indexBuffer holds the indices; must be at least `indexOffset + 2 * indexCount` bytes
         * @param indexCount  the number of indices to read; at least 3
         * @param indexOffset byte offset of the first index; must be a multiple of 2
         *
         * @see make for the remaining parameters
         *
         * @throws IllegalArgumentException if the mesh is rejected, carrying the message reported by
         *                                  Skia
         */
        fun makeIndexed(
            specification: MeshSpecification,
            mode: Mode,
            vertexBuffer: MeshVertexBuffer,
            vertexCount: Int,
            indexBuffer: MeshIndexBuffer,
            indexCount: Int,
            bounds: Rect,
            vertexOffset: Int = 0,
            indexOffset: Int = 0,
            uniforms: Data? = null,
            children: Array<Shader?>? = null
        ): Mesh {
            Stats.onNativeCall()
            val childrenPtrs = childrenPointers(children)
            return try {
                interopScope {
                    Mesh(
                        meshFromResult(
                            Mesh_nMakeIndexed(
                                getPtr(specification),
                                mode.ordinal,
                                getPtr(vertexBuffer),
                                vertexCount,
                                vertexOffset,
                                getPtr(indexBuffer),
                                indexCount,
                                indexOffset,
                                getPtr(uniforms),
                                toInterop(childrenPtrs),
                                childrenPtrs.size,
                                bounds.left,
                                bounds.top,
                                bounds.right,
                                bounds.bottom
                            )
                        )
                    )
                }
            } finally {
                reachabilityBarrier(specification)
                reachabilityBarrier(vertexBuffer)
                reachabilityBarrier(indexBuffer)
                reachabilityBarrier(uniforms)
                reachabilityBarrier(children)
            }
        }

        init {
            staticLoad()
        }
    }

    private object _FinalizerHolder {
        val PTR = Mesh_nGetFinalizer()
    }
}

private fun childrenPointers(children: Array<Shader?>?): NativePointerArray {
    val count = children?.size ?: 0
    val pointers = NativePointerArray(count)
    for (i in 0 until count) pointers[i] = getPtr(children!![i])
    return pointers
}

/**
 * Unwraps the native `SkMesh::Result`, always destroying it, and translates a non-empty error into
 * an exception.
 */
private fun meshFromResult(resultPtr: NativePointer): NativePointer {
    try {
        val errorPtr = Mesh_nResultGetError(resultPtr)
        if (errorPtr != Native.NullPointer) {
            // The error string is owned by the result.
            throw IllegalArgumentException(withStringReferenceResult { errorPtr })
        }
        return Mesh_nResultGetMesh(resultPtr)
    } finally {
        Mesh_nResultDestroy(resultPtr)
    }
}

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nGetFinalizer")
private external fun Mesh_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nMake")
private external fun Mesh_nMake(
    specificationPtr: NativePointer,
    mode: Int,
    vertexBufferPtr: NativePointer,
    vertexCount: Int,
    vertexOffset: Int,
    uniformsPtr: NativePointer,
    childrenPtrs: InteropPointer,
    childCount: Int,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nMakeIndexed")
private external fun Mesh_nMakeIndexed(
    specificationPtr: NativePointer,
    mode: Int,
    vertexBufferPtr: NativePointer,
    vertexCount: Int,
    vertexOffset: Int,
    indexBufferPtr: NativePointer,
    indexCount: Int,
    indexOffset: Int,
    uniformsPtr: NativePointer,
    childrenPtrs: InteropPointer,
    childCount: Int,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nResultGetMesh")
private external fun Mesh_nResultGetMesh(resultPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nResultGetError")
private external fun Mesh_nResultGetError(resultPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nResultDestroy")
private external fun Mesh_nResultDestroy(resultPtr: NativePointer)
