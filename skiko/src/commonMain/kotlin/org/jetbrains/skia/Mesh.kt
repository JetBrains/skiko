package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

/**
 * A vertex buffer, a topology, optionally an index buffer, and a compatible [MeshSpecification].
 *
 * The data in the vertex buffer is expected to contain the attributes described by the specification
 * for [vertexCount] vertices, beginning at [vertexOffset]. [vertexOffset] must be aligned to the
 * [MeshSpecification]'s vertex stride. The size of the buffer must be at least
 * `vertexOffset + specification.stride * vertexCount` (even if vertex attributes contain pad at the
 * end of the stride). If the specified bounds do not contain all the points output by the
 * specification's vertex program when applied to the vertices in the custom mesh, then the result is
 * undefined.
 *
 * [makeIndexed] may be used to create an indexed mesh. [indexCount] indices are read from the index
 * buffer at the specified offset, which must be aligned to 2. The indices are always unsigned 16-bit
 * integers. The index count must be at least 3.
 *
 * If [make] is used, the implicit index sequence is 0, 1, 2, 3, ... and [vertexCount] must be at
 * least 3.
 *
 * Both [make] and [makeIndexed] take a [Data] with the uniform values. See
 * [MeshSpecification.uniformSize] and [MeshSpecification.uniforms] for sizing and packing uniforms
 * into the [Data]; [MeshUniformBuilder] does that packing.
 *
 * [specification], [vertexBuffer], [indexBuffer] and [uniforms] each return a fresh reference to the
 * object the mesh holds, which the caller closes.
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
         * Creates a non-indexed mesh.
         *
         * @param children values for the children the two programs declare, in the order
         *                 [MeshSpecification.children] reports. Each must match the kind its
         *                 [MeshSpecification.Child.type] names.
         *
         * @throws IllegalArgumentException carrying the reason the mesh was rejected, for example
         *                                  that the uniform data was too small
         */
        fun make(
            specification: MeshSpecification,
            mode: Mode,
            vertexBuffer: MeshVertexBuffer,
            vertexCount: Int,
            bounds: Rect,
            vertexOffset: Int = 0,
            uniforms: Data? = null,
            children: Array<RuntimeEffect.Child?>? = null
        ): Mesh {
            Stats.onNativeCall()
            val encodedChildren = encodeChildren(children)
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
                                toInterop(encodedChildren.pointers),
                                toInterop(encodedChildren.types),
                                encodedChildren.types.size,
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
         * Creates an indexed mesh.
         *
         * @throws IllegalArgumentException carrying the reason the mesh was rejected, for example
         *                                  that the index buffer was too small
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
            children: Array<RuntimeEffect.Child?>? = null
        ): Mesh {
            Stats.onNativeCall()
            val encodedChildren = encodeChildren(children)
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
                                toInterop(encodedChildren.pointers),
                                toInterop(encodedChildren.types),
                                encodedChildren.types.size,
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

    /** The specification the mesh was made with. */
    val specification: MeshSpecification
        get() = try {
            Stats.onNativeCall()
            MeshSpecification(Mesh_nGetSpecification(_ptr))
        } finally {
            reachabilityBarrier(this)
        }

    /** How the mesh assembles its vertices into triangles. */
    val mode: Mode
        get() = try {
            Stats.onNativeCall()
            Mode.entries[Mesh_nGetMode(_ptr)]
        } finally {
            reachabilityBarrier(this)
        }

    /** The buffer the vertex data is read from. */
    val vertexBuffer: MeshVertexBuffer
        get() = try {
            Stats.onNativeCall()
            MeshVertexBuffer(Mesh_nGetVertexBuffer(_ptr))
        } finally {
            reachabilityBarrier(this)
        }

    /** Byte offset of the first vertex in [vertexBuffer]. */
    val vertexOffset: Int
        get() = try {
            Stats.onNativeCall()
            Mesh_nGetVertexOffset(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /** The number of vertices read from [vertexBuffer]. */
    val vertexCount: Int
        get() = try {
            Stats.onNativeCall()
            Mesh_nGetVertexCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /** The buffer the indices are read from, or `null` when the mesh is not indexed. */
    val indexBuffer: MeshIndexBuffer?
        get() = try {
            Stats.onNativeCall()
            val ptr = Mesh_nGetIndexBuffer(_ptr)
            if (ptr == Native.NullPointer) null else MeshIndexBuffer(ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /** Byte offset of the first index in [indexBuffer]; zero when the mesh is not indexed. */
    val indexOffset: Int
        get() = try {
            Stats.onNativeCall()
            Mesh_nGetIndexOffset(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /** The number of indices read from [indexBuffer]; zero when the mesh is not indexed. */
    val indexCount: Int
        get() = try {
            Stats.onNativeCall()
            Mesh_nGetIndexCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * The packed uniform values the programs read. Empty when the mesh was made without uniforms.
     */
    val uniforms: Data
        get() = try {
            Stats.onNativeCall()
            Data(Mesh_nGetUniforms(_ptr))
        } finally {
            reachabilityBarrier(this)
        }

    /** The device-space bounds the mesh was made with. */
    val bounds: Rect
        get() = try {
            Stats.onNativeCall()
            Rect.fromInteropPointer { Mesh_nGetBounds(_ptr, it) }
        } finally {
            reachabilityBarrier(this)
        }

    private object _FinalizerHolder {
        val PTR = Mesh_nGetFinalizer()
    }
}

/**
 * Children reach the native binding as parallel arrays of a pointer and the kind it must be upcast
 * from, which the pointer alone cannot say.
 */
private class EncodedChildren(val pointers: NativePointerArray, val types: IntArray)

/** The kind sent for a child left unbound, which no [MeshSpecification.Child.Type] names. */
private const val UNBOUND_CHILD = -1

private fun encodeChildren(children: Array<RuntimeEffect.Child?>?): EncodedChildren {
    val count = children?.size ?: 0
    val pointers = NativePointerArray(count)
    val types = IntArray(count)
    for (i in 0 until count) {
        when (val child = children!![i]) {
            null -> types[i] = UNBOUND_CHILD
            is Shader -> {
                pointers[i] = getPtr(child)
                types[i] = MeshSpecification.Child.Type.SHADER.ordinal
            }
            is ColorFilter -> {
                pointers[i] = getPtr(child)
                types[i] = MeshSpecification.Child.Type.COLOR_FILTER.ordinal
            }
            is Blender -> {
                pointers[i] = getPtr(child)
                types[i] = MeshSpecification.Child.Type.BLENDER.ordinal
            }
        }
    }
    return EncodedChildren(pointers, types)
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
    childTypes: InteropPointer,
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
    childTypes: InteropPointer,
    childCount: Int,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nGetSpecification")
private external fun Mesh_nGetSpecification(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nGetMode")
private external fun Mesh_nGetMode(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nGetVertexBuffer")
private external fun Mesh_nGetVertexBuffer(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nGetVertexOffset")
private external fun Mesh_nGetVertexOffset(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nGetVertexCount")
private external fun Mesh_nGetVertexCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nGetIndexBuffer")
private external fun Mesh_nGetIndexBuffer(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nGetIndexOffset")
private external fun Mesh_nGetIndexOffset(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nGetIndexCount")
private external fun Mesh_nGetIndexCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nGetUniforms")
private external fun Mesh_nGetUniforms(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nGetBounds")
private external fun Mesh_nGetBounds(ptr: NativePointer, bounds: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nResultGetMesh")
private external fun Mesh_nResultGetMesh(resultPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nResultGetError")
private external fun Mesh_nResultGetError(resultPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nResultDestroy")
private external fun Mesh_nResultDestroy(resultPtr: NativePointer)
