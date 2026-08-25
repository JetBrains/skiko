package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

/**
 * Packs values for the uniforms declared by a [MeshSpecification]'s programs into the single [Data]
 * that [Mesh.make] and [Mesh.makeIndexed] accept.
 *
 * Uniforms left unset hold zeroes. Each `uniform` call writes at the offset the specification
 * assigns to that name, so the same builder may be reused and individual uniforms overwritten;
 * [build] copies the current values and can be called any number of times.
 *
 * [MeshSpecification.uniformSize] and [MeshSpecification.uniforms] describe the same layout, for
 * callers that would rather assemble the [Data] themselves.
 *
 * ```
 * val uniforms = MeshUniformBuilder(specification).use {
 *     it.uniform("time", 0.25f)
 *     it.uniform("resolution", 800f, 600f)
 *     it.build()
 * }
 * ```
 */
class MeshUniformBuilder internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        init {
            staticLoad()
        }
    }

    constructor(specification: MeshSpecification) : this(MeshUniformBuilder_nMake(getPtr(specification))) {
        Stats.onNativeCall()
        reachabilityBarrier(specification)
    }

    /** Writes an `int` uniform. */
    fun uniform(name: String, value: Int) = uniform(name, intArrayOf(value))

    /** Writes an `int2` uniform. */
    fun uniform(name: String, value1: Int, value2: Int) = uniform(name, intArrayOf(value1, value2))

    /** Writes an `int3` uniform. */
    fun uniform(name: String, value1: Int, value2: Int, value3: Int) =
        uniform(name, intArrayOf(value1, value2, value3))

    /** Writes an `int4` uniform. */
    fun uniform(name: String, value1: Int, value2: Int, value3: Int, value4: Int) =
        uniform(name, intArrayOf(value1, value2, value3, value4))

    /**
     * Writes an integer uniform, or an array of them, from [value].
     *
     * @throws IllegalArgumentException if the specification declares no uniform called [name], or
     *                                  declares it with a different size, or declares it as a
     *                                  floating point uniform
     */
    fun uniform(name: String, value: IntArray) {
        Stats.onNativeCall()
        val status = try {
            interopScope {
                MeshUniformBuilder_nUniformInts(_ptr, toInterop(name), toInterop(value), value.size)
            }
        } finally {
            reachabilityBarrier(this)
        }
        checkStatus(status, name, value.size, "integer")
    }

    /** Writes a `float` uniform. */
    fun uniform(name: String, value: Float) = uniform(name, floatArrayOf(value))

    /** Writes a `float2` uniform. */
    fun uniform(name: String, value1: Float, value2: Float) = uniform(name, floatArrayOf(value1, value2))

    /** Writes a `float3` uniform. */
    fun uniform(name: String, value1: Float, value2: Float, value3: Float) =
        uniform(name, floatArrayOf(value1, value2, value3))

    /** Writes a `float4` uniform. */
    fun uniform(name: String, value1: Float, value2: Float, value3: Float, value4: Float) =
        uniform(name, floatArrayOf(value1, value2, value3, value4))

    /** Writes a `float2x2` uniform. */
    fun uniform(name: String, value: Matrix22) = uniform(name, value.mat.transposed(2))

    /** Writes a `float3x3` uniform. */
    fun uniform(name: String, value: Matrix33) = uniform(name, value.mat.transposed(3))

    /** Writes a `float4x4` uniform. */
    fun uniform(name: String, value: Matrix44) = uniform(name, value.mat.transposed(4))

    /**
     * Writes a floating point uniform, or an array or matrix of them, from [value].
     *
     * `half` uniforms are written as floats too: the specification stores every floating point
     * uniform at full precision and narrows it when the shader runs. A matrix is written in
     * column-major order, the order SkSL stores it in.
     *
     * @throws IllegalArgumentException if the specification declares no uniform called [name], or
     *                                  declares it with a different size, or declares it as an
     *                                  integer uniform
     */
    fun uniform(name: String, value: FloatArray) {
        Stats.onNativeCall()
        val status = try {
            interopScope {
                MeshUniformBuilder_nUniformFloats(_ptr, toInterop(name), toInterop(value), value.size)
            }
        } finally {
            reachabilityBarrier(this)
        }
        checkStatus(status, name, value.size, "floating point")
    }

    /** Copies the values written so far into a [Data] sized for the specification. */
    fun build(): Data {
        Stats.onNativeCall()
        return try {
            Data(MeshUniformBuilder_nBuild(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
    }

    private fun checkStatus(status: Int, name: String, valueSize: Int, valueKind: String) {
        when (status) {
            STATUS_OK -> Unit
            STATUS_UNKNOWN_UNIFORM ->
                throw IllegalArgumentException("The specification declares no uniform called '$name'")
            STATUS_SIZE_MISMATCH ->
                throw IllegalArgumentException("Uniform '$name' is not $valueSize values wide")
            STATUS_KIND_MISMATCH ->
                throw IllegalArgumentException("Uniform '$name' does not hold $valueKind values")
            else -> error("Writing uniform '$name' returned status $status")
        }
    }

    private object _FinalizerHolder {
        val PTR = MeshUniformBuilder_nGetFinalizer()
    }
}

private const val STATUS_OK = 0
private const val STATUS_UNKNOWN_UNIFORM = 1
private const val STATUS_SIZE_MISMATCH = 2
private const val STATUS_KIND_MISMATCH = 3

@ExternalSymbolName("org_jetbrains_skia_MeshUniformBuilder__1nGetFinalizer")
// No ForceNativeThreadState: returns the address of the deletion function, so it is short and cannot block.
// Native call path: deleteMeshUniformBuilder
private external fun MeshUniformBuilder_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MeshUniformBuilder__1nMake")
// Needs ForceNativeThreadState: allocates and zero-fills the uniform buffer sized from the specification,
// so its time grows with the declared uniform size.
// Native call path: MeshUniformBuilder::MeshUniformBuilder -> std::vector
@ForceNativeThreadState
private external fun MeshUniformBuilder_nMake(specificationPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MeshUniformBuilder__1nUniformInts")
// Needs ForceNativeThreadState: copies the uniform's name, searches the declared uniforms one by one and copies the
// values; work grows with the name length, uniform count and value count.
// Native call path: skString -> SkString; MeshUniformBuilder::write -> SkMeshSpecification::findUniform
@ForceNativeThreadState
private external fun MeshUniformBuilder_nUniformInts(
    builderPtr: NativePointer,
    uniformName: InteropPointer,
    values: InteropPointer,
    count: Int
): Int

@ExternalSymbolName("org_jetbrains_skia_MeshUniformBuilder__1nUniformFloats")
// Needs ForceNativeThreadState: copies the uniform's name, searches the declared uniforms one by one and copies the
// values; work grows with the name length, uniform count and value count.
// Native call path: skString -> SkString; MeshUniformBuilder::write -> SkMeshSpecification::findUniform
@ForceNativeThreadState
private external fun MeshUniformBuilder_nUniformFloats(
    builderPtr: NativePointer,
    uniformName: InteropPointer,
    values: InteropPointer,
    count: Int
): Int

@ExternalSymbolName("org_jetbrains_skia_MeshUniformBuilder__1nBuild")
// Needs ForceNativeThreadState: allocates and copies the whole uniform buffer, so its time grows with the buffer size;
// for an empty buffer it can wait for another thread still initializing the shared empty data object.
// Native call path: MeshUniformBuilder::build -> SkData::MakeWithCopy | SkData::MakeEmpty
@ForceNativeThreadState
private external fun MeshUniformBuilder_nBuild(builderPtr: NativePointer): NativePointer

private fun FloatArray.transposed(n: Int): FloatArray =
    FloatArray(n * n) { this[it % n * n + it / n] }
