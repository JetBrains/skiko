package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

/**
 * A specification for a custom mesh: the layout of the vertex buffer, the vertex program that turns
 * attributes into varyings, and the fragment program that turns varyings into local coordinates and
 * optionally a color.
 *
 * The varyings must include a `float2` named `position`. If [Companion.make] is not given such a
 * varying, one is added implicitly. A varying named `position` with any other type is an error.
 *
 * The attributes and varyings become members of the `Attributes` and `Varyings` structs available to
 * the shaders. The vertex program must have the signature `Varyings main(const Attributes)`. The
 * fragment program must have the signature `float2 main(const Varyings)` or
 * `float2 main(const Varyings, out (half4|float4) color)`; its return value is the local coordinate
 * used to sample the [Paint]'s [Shader].
 *
 * Both programs may declare uniforms. Uniforms of the same name are shared between the stages, and
 * declaring them with different types in the two stages is an error. Values for all uniforms are
 * packed into a single [Data] by [MeshUniformBuilder] and handed to [Mesh.make].
 */
class MeshSpecification internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    /**
     * A single value read from the vertex buffer, made available to the vertex program as a member
     * of the `Attributes` struct.
     *
     * @param type   the layout of the value in the vertex buffer
     * @param offset byte offset of the value within a vertex; must be a multiple of
     *               [OFFSET_ALIGNMENT], and `offset + size` may not exceed the vertex stride
     * @param name   the member name in the `Attributes` struct
     */
    data class Attribute(val type: Type, val offset: Int, val name: String) {
        /** The layout of an [Attribute] in the vertex buffer and its type in SkSL. */
        enum class Type {
            /** One float; `float` in SkSL. */
            FLOAT,

            /** Two floats; `float2` in SkSL. */
            FLOAT2,

            /** Three floats; `float3` in SkSL. */
            FLOAT3,

            /** Four floats; `float4` in SkSL. */
            FLOAT4,

            /** Four bytes normalized to `[0, 1]`; `half4` in SkSL. */
            UBYTE4_UNORM;
        }
    }

    /**
     * A value written by the vertex program and interpolated across the primitive before the
     * fragment program reads it, as a member of the `Varyings` struct.
     */
    data class Varying(val type: Type, val name: String) {
        /** The SkSL type of a [Varying]. */
        enum class Type {
            FLOAT,
            FLOAT2,
            FLOAT3,
            FLOAT4,
            HALF,
            HALF2,
            HALF3,
            HALF4;
        }
    }

    companion object {
        /** The largest vertex stride a specification may declare. */
        const val MAX_STRIDE = 1024

        /** The largest number of attributes a specification may declare. */
        const val MAX_ATTRIBUTES = 8

        /** The vertex stride must be a multiple of this. */
        const val STRIDE_ALIGNMENT = 4

        /** Every [Attribute.offset] must be a multiple of this. */
        const val OFFSET_ALIGNMENT = 4

        /** The largest number of varyings a specification may declare. */
        const val MAX_VARYINGS = 6

        /**
         * Creates a specification from the vertex layout and the two programs.
         *
         * @param attributes     the attributes consumed by [vertexShader]. They need not be tightly
         *                       packed, but at least one is required.
         * @param vertexStride   the byte offset between successive vertices; must be a multiple of
         *                       [STRIDE_ALIGNMENT] and at most [MAX_STRIDE]
         * @param varyings       the varyings written by [vertexShader] and read by [fragmentShader];
         *                       may be empty
         * @param vertexShader   SkSL computing the varyings from the attributes
         * @param fragmentShader SkSL computing a local coordinate, and optionally a color, from the
         *                       varyings
         * @param colorSpace     the color space of the color produced by [fragmentShader]. Ignored
         *                       when the fragment program has no color out parameter. `null` means
         *                       sRGB.
         * @param alphaType      the alpha type of the color produced by [fragmentShader]. Ignored
         *                       when the fragment program has no color out parameter. May not be
         *                       [ColorAlphaType.UNKNOWN].
         *
         * @throws IllegalArgumentException if the specification is rejected, carrying the message
         *                                  reported by Skia
         */
        fun make(
            attributes: Array<Attribute>,
            vertexStride: Int,
            varyings: Array<Varying>,
            vertexShader: String,
            fragmentShader: String,
            colorSpace: ColorSpace? = null,
            alphaType: ColorAlphaType = ColorAlphaType.PREMUL
        ): MeshSpecification {
            require(attributes.isNotEmpty()) { "At least one attribute is required" }
            require(alphaType != ColorAlphaType.UNKNOWN) { "alphaType may not be ColorAlphaType.UNKNOWN" }

            // Every attribute contributes two ints: its type ordinal and its byte offset.
            val attributeTypesAndOffsets = IntArray(attributes.size * 2)
            for (i in attributes.indices) {
                attributeTypesAndOffsets[i * 2] = attributes[i].type.ordinal
                attributeTypesAndOffsets[i * 2 + 1] = attributes[i].offset
            }
            val attributeNames = Array(attributes.size) { attributes[it].name }
            val varyingTypes = IntArray(varyings.size) { varyings[it].type.ordinal }
            val varyingNames = Array(varyings.size) { varyings[it].name }

            Stats.onNativeCall()
            return try {
                interopScope {
                    MeshSpecification(
                        specificationFromResult(
                            MeshSpecification_nMake(
                                toInterop(attributeTypesAndOffsets),
                                toInterop(attributeNames),
                                attributes.size,
                                vertexStride,
                                toInterop(varyingTypes),
                                toInterop(varyingNames),
                                varyings.size,
                                toInterop(vertexShader),
                                toInterop(fragmentShader),
                                getPtr(colorSpace),
                                alphaType.ordinal
                            )
                        )
                    )
                }
            } finally {
                reachabilityBarrier(colorSpace)
            }
        }

        init {
            staticLoad()
        }
    }

    /** The byte offset between successive vertices in a buffer laid out for this specification. */
    val stride: Int
        get() = try {
            Stats.onNativeCall()
            MeshSpecification_nGetStride(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    private object _FinalizerHolder {
        val PTR = MeshSpecification_nGetFinalizer()
    }
}

/**
 * Unwraps the native `SkMeshSpecification::Result`, always destroying it, and translates a non-empty
 * error into an exception.
 */
private fun specificationFromResult(resultPtr: NativePointer): NativePointer {
    try {
        val errorPtr = MeshSpecification_nResultGetError(resultPtr)
        if (errorPtr != Native.NullPointer) {
            // The error string is owned by the result.
            throw IllegalArgumentException(withStringReferenceResult { errorPtr })
        }
        return MeshSpecification_nResultGetSpecification(resultPtr)
    } finally {
        MeshSpecification_nResultDestroy(resultPtr)
    }
}

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nGetFinalizer")
private external fun MeshSpecification_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nMake")
private external fun MeshSpecification_nMake(
    attributeTypesAndOffsets: InteropPointer,
    attributeNames: InteropPointer,
    attributeCount: Int,
    vertexStride: Int,
    varyingTypes: InteropPointer,
    varyingNames: InteropPointer,
    varyingCount: Int,
    vertexShader: InteropPointer,
    fragmentShader: InteropPointer,
    colorSpacePtr: NativePointer,
    alphaType: Int
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nGetStride")
private external fun MeshSpecification_nGetStride(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nResultGetSpecification")
private external fun MeshSpecification_nResultGetSpecification(resultPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nResultGetError")
private external fun MeshSpecification_nResultGetError(resultPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nResultDestroy")
private external fun MeshSpecification_nResultDestroy(resultPtr: NativePointer)
