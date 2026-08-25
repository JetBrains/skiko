package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

/**
 * A specification for custom meshes. Specifies the vertex buffer attributes and stride, the
 * vertex program that produces a user-defined set of varyings, and a fragment program that ingests
 * the interpolated varyings and produces local coordinates for shading and optionally a color.
 *
 * The varyings must include a `float2` named `position`. If the passed varyings does not
 * contain such a varying then one is implicitly added to the final specification and the SkSL
 * `Varyings` struct described below. It is an error to have a varying named `position` that has a
 * type other than `float2`.
 *
 * The provided attributes and varyings are used to create `Attributes` and `Varyings` structs in
 * SkSL that are used by the shaders. Each attribute from the [Attribute] array becomes a member of
 * the SkSL `Attributes` struct and likewise for the varyings.
 *
 * The signature of the vertex program must be `Varyings main(const Attributes)`.
 *
 * The signature of the fragment program must be either `float2 main(const Varyings)` or
 * `float2 main(const Varyings, out (half4|float4) color)`, where the return value is the local
 * coordinates that will be used to access [Shader]. If the color variant is used, the returned
 * color will be blended with [Paint]'s [Shader] (or [Paint] color in absence of a [Shader]) using
 * the [Blender] passed to [Canvas.drawMesh]. To use interpolated local space positions as the
 * shader coordinates, equivalent to how [Path]s are shaded, return the position field from the
 * `Varyings` struct as the coordinates.
 *
 * The vertex and fragment programs may both contain uniforms. Uniforms with the same name are
 * assumed to be shared between stages. It is an error to specify uniforms in the vertex and
 * fragment program with the same name but different types, dimensionality, or layouts.
 */
class MeshSpecification internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    /**
     * A vertex attribute that will be consumed by the vertex program. Attributes need not be
     * tightly packed but attribute offsets must be aligned to [OFFSET_ALIGNMENT] and
     * `offset + size` may not be greater than the vertex stride.
     */
    data class Attribute(val type: Type, val offset: Int, val name: String) {
        /** The CPU representation of an [Attribute] and its type in the shader. */
        enum class Type {
            /** float; `float` in the shader. */
            FLOAT,

            /** Two floats; `float2` in the shader. */
            FLOAT2,

            /** Three floats; `float3` in the shader. */
            FLOAT3,

            /** Four floats; `float4` in the shader. */
            FLOAT4,

            /** Four bytes; `half4` in the shader. */
            UBYTE4_UNORM;
        }
    }

    /** A varying that will be written by the vertex program and read by the fragment program. */
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

    /**
     * Reflected description of a uniform variable in the specification's SkSL, including the offset
     * into the [Data] where its value should be placed.
     */
    data class Uniform(
        val name: String,
        val offset: Int,
        val type: Type,
        val count: Int,
        val sizeInBytes: Int,
        val flags: Int
    ) {
        /** The SkSL type of a [Uniform]. */
        enum class Type {
            FLOAT,
            FLOAT2,
            FLOAT3,
            FLOAT4,
            FLOAT2X2,
            FLOAT3X3,
            FLOAT4X4,
            INT,
            INT2,
            INT3,
            INT4;
        }

        /** The uniform is declared as an array. [count] contains the array length. */
        val isArray: Boolean
            get() = flags and ARRAY_FLAG != 0

        /**
         * The uniform is declared with `layout(color)`. Colors should be supplied as unpremultiplied,
         * extended-range (unclamped) sRGB, that is, as [Color4f]. The uniform will be automatically
         * transformed to unpremultiplied extended-range working-space colors.
         */
        val isColor: Boolean
            get() = flags and COLOR_FLAG != 0

        /** The uniform is present in the vertex shader. */
        val isVertex: Boolean
            get() = flags and VERTEX_FLAG != 0

        /** The uniform is present in the fragment shader. */
        val isFragment: Boolean
            get() = flags and FRAGMENT_FLAG != 0

        /** The SkSL uniform uses a medium-precision type, that is, `half` instead of `float`. */
        val isHalfPrecision: Boolean
            get() = flags and HALF_PRECISION_FLAG != 0

        // The bits SkRuntimeEffect::Uniform::Flags defines.
        private companion object {
            const val ARRAY_FLAG = 0x1
            const val COLOR_FLAG = 0x2
            const val VERTEX_FLAG = 0x4
            const val FRAGMENT_FLAG = 0x8
            const val HALF_PRECISION_FLAG = 0x10
        }
    }

    /** Reflected description of a uniform child in the specification's SkSL. */
    data class Child(val name: String, val type: Type, val index: Int) {
        /** The runtime effect type of a [Child]. */
        enum class Type {
            SHADER,
            COLOR_FILTER,
            BLENDER;
        }
    }

    companion object {
        /** Enforced when creating a specification. */
        const val MAX_STRIDE = 1024

        /** Enforced when creating a specification. */
        const val MAX_ATTRIBUTES = 8

        /** Enforced when creating a specification. */
        const val STRIDE_ALIGNMENT = 4

        /** Enforced when creating a specification. */
        const val OFFSET_ALIGNMENT = 4

        /** Enforced when creating a specification. */
        const val MAX_VARYINGS = 6

        /**
         * @param attributes     The vertex attributes that will be consumed by [vertexShader].
         *                       Attributes need not be tightly packed but attribute offsets must be
         *                       aligned to [OFFSET_ALIGNMENT] and `offset + size` may not be greater
         *                       than [vertexStride]. At least one attribute is required.
         * @param vertexStride   The offset between successive attribute values. This must be aligned
         *                       to [STRIDE_ALIGNMENT].
         * @param varyings       The varyings that will be written by [vertexShader] and read by
         *                       [fragmentShader]. This may be empty.
         * @param vertexShader   The vertex shader code that computes a vertex position and the
         *                       varyings from the attributes.
         * @param fragmentShader The fragment code that computes a local coordinate and optionally a
         *                       color from the varyings. The local coordinate is used to sample
         *                       [Shader].
         * @param colorSpace     The color space of the color produced by [fragmentShader]. Ignored
         *                       if the fragment program's `main()` function does not have a color
         *                       out param. `null` means sRGB.
         * @param alphaType      The alpha type of the color produced by [fragmentShader]. Ignored if
         *                       the fragment program's `main()` function does not have a color out
         *                       param. Cannot be [ColorAlphaType.UNKNOWN].
         *
         * @throws IllegalArgumentException carrying the reason the specification was rejected
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

    /** The offset between successive attribute values in a buffer laid out for this specification. */
    val stride: Int
        get() = try {
            Stats.onNativeCall()
            MeshSpecification_nGetStride(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * The color space of the color produced by the fragment program, or `null` if its `main()`
     * function does not have a color out param.
     */
    val colorSpace: ColorSpace?
        get() = try {
            Stats.onNativeCall()
            val ptr = MeshSpecification_nGetColorSpace(_ptr)
            if (ptr == Native.NullPointer) null else ColorSpace(ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /** The vertex attributes consumed by the vertex program. */
    val attributes: List<Attribute>
        get() {
            Stats.onNativeCall()
            try {
                val count = MeshSpecification_nGetAttributeCount(_ptr)
                if (count == 0) return emptyList()
                // Every attribute contributes two ints: its type ordinal and its byte offset.
                val fields = withResult(IntArray(count * 2)) { MeshSpecification_nGetAttributeFields(_ptr, it) }
                return List(count) { i ->
                    Attribute(
                        type = Attribute.Type.entries[fields[i * 2]],
                        offset = fields[i * 2 + 1],
                        name = withStringResult { MeshSpecification_nGetAttributeName(_ptr, i) }
                    )
                }
            } finally {
                reachabilityBarrier(this)
            }
        }

    /**
     * Combined size of all `uniform` variables. When creating a [Mesh] with this specification
     * provide a [Data] of this size, containing values for all of those variables. Use [uniforms]
     * to get the offset of each uniform within the [Data].
     */
    val uniformSize: Int
        get() = try {
            Stats.onNativeCall()
            MeshSpecification_nGetUniformSize(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Provides info about individual uniforms including the offset into a [Data] where each uniform
     * value should be placed.
     */
    val uniforms: List<Uniform>
        get() {
            Stats.onNativeCall()
            try {
                val count = MeshSpecification_nGetUniformCount(_ptr)
                if (count == 0) return emptyList()
                // Every uniform contributes five ints: offset, type ordinal, count, size and flags.
                val fields = withResult(IntArray(count * UNIFORM_FIELDS)) {
                    MeshSpecification_nGetUniformFields(_ptr, it)
                }
                return List(count) { i ->
                    val base = i * UNIFORM_FIELDS
                    Uniform(
                        name = withStringResult { MeshSpecification_nGetUniformName(_ptr, i) },
                        offset = fields[base],
                        type = Uniform.Type.entries[fields[base + 1]],
                        count = fields[base + 2],
                        sizeInBytes = fields[base + 3],
                        flags = fields[base + 4]
                    )
                }
            } finally {
                reachabilityBarrier(this)
            }
        }

    /** Provides basic info about individual children: names, indices and runtime effect type. */
    val children: List<Child>
        get() {
            Stats.onNativeCall()
            try {
                val count = MeshSpecification_nGetChildCount(_ptr)
                if (count == 0) return emptyList()
                // Every child contributes two ints: its type ordinal and its index.
                val fields = withResult(IntArray(count * 2)) { MeshSpecification_nGetChildFields(_ptr, it) }
                return List(count) { i ->
                    Child(
                        name = withStringResult { MeshSpecification_nGetChildName(_ptr, i) },
                        type = Child.Type.entries[fields[i * 2]],
                        index = fields[i * 2 + 1]
                    )
                }
            } finally {
                reachabilityBarrier(this)
            }
        }

    /** Returns the named attribute, or `null` if not found. */
    fun findAttribute(name: String): Attribute? = attributes.firstOrNull { it.name == name }

    /** Returns the named uniform variable's description, or `null` if not found. */
    fun findUniform(name: String): Uniform? = uniforms.firstOrNull { it.name == name }

    /** Returns the named child's description, or `null` if not found. */
    fun findChild(name: String): Child? = children.firstOrNull { it.name == name }

    /**
     * Returns the named varying, or `null` if not found. This finds the `position` varying that is
     * added implicitly as well as the ones the specification was given.
     */
    fun findVarying(name: String): Varying? {
        Stats.onNativeCall()
        val type = try {
            interopScope { MeshSpecification_nFindVaryingType(_ptr, toInterop(name)) }
        } finally {
            reachabilityBarrier(this)
        }
        return if (type < 0) null else Varying(Varying.Type.entries[type], name)
    }

    private object _FinalizerHolder {
        val PTR = MeshSpecification_nGetFinalizer()
    }
}

private const val UNIFORM_FIELDS = 5

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

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nGetColorSpace")
private external fun MeshSpecification_nGetColorSpace(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nGetAttributeCount")
private external fun MeshSpecification_nGetAttributeCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nGetAttributeFields")
private external fun MeshSpecification_nGetAttributeFields(ptr: NativePointer, fields: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nGetAttributeName")
private external fun MeshSpecification_nGetAttributeName(ptr: NativePointer, index: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nGetUniformSize")
private external fun MeshSpecification_nGetUniformSize(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nGetUniformCount")
private external fun MeshSpecification_nGetUniformCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nGetUniformFields")
private external fun MeshSpecification_nGetUniformFields(ptr: NativePointer, fields: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nGetUniformName")
private external fun MeshSpecification_nGetUniformName(ptr: NativePointer, index: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nGetChildCount")
private external fun MeshSpecification_nGetChildCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nGetChildFields")
private external fun MeshSpecification_nGetChildFields(ptr: NativePointer, fields: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nGetChildName")
private external fun MeshSpecification_nGetChildName(ptr: NativePointer, index: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nFindVaryingType")
private external fun MeshSpecification_nFindVaryingType(ptr: NativePointer, name: InteropPointer): Int

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nResultGetSpecification")
private external fun MeshSpecification_nResultGetSpecification(resultPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nResultGetError")
private external fun MeshSpecification_nResultGetError(resultPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nResultDestroy")
private external fun MeshSpecification_nResultDestroy(resultPtr: NativePointer)
