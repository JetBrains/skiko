package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.withStringReferenceResult
import kotlin.jvm.JvmInline

/**
 * Describes the vertex layout and SkSL programs used by a [Mesh].
 */
class MeshSpecification internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    internal val colorSpace = ColorSpace(_nGetColorSpace(ptr))

    @JvmInline
    value class AttributeType private constructor(internal val value: Int) {
        companion object {
            val FLOAT = AttributeType(0)
            val FLOAT2 = AttributeType(1)
            val FLOAT3 = AttributeType(2)
            val FLOAT4 = AttributeType(3)
            val UBYTE4 = AttributeType(4)
        }
    }

    @JvmInline
    value class VaryingType private constructor(internal val value: Int) {
        companion object {
            val FLOAT = VaryingType(0)
            val FLOAT2 = VaryingType(1)
            val FLOAT3 = VaryingType(2)
            val FLOAT4 = VaryingType(3)
            val HALF = VaryingType(4)
            val HALF2 = VaryingType(5)
            val HALF3 = VaryingType(6)
            val HALF4 = VaryingType(7)
        }
    }

    class Attribute(val type: AttributeType, val offset: Int, val name: String) {
        init {
            require(offset >= 0) { "Attribute offset must not be negative" }
        }
    }

    class Varying(val type: VaryingType, val name: String)

    companion object {
        const val MAX_STRIDE = 1024
        const val MAX_ATTRIBUTES = 8
        const val MAX_VARYINGS = 6
        const val STRIDE_ALIGNMENT = 4
        const val OFFSET_ALIGNMENT = 4

        init {
            staticLoad()
        }

        fun make(
            attributes: Array<Attribute>,
            vertexStride: Int,
            varyings: Array<Varying>,
            vertexShader: String,
            fragmentShader: String
        ): MeshSpecification = make(
            attributes,
            vertexStride,
            varyings,
            vertexShader,
            fragmentShader,
            ColorSpace.sRGB,
            ColorAlphaType.PREMUL
        )

        fun make(
            attributes: Array<Attribute>,
            vertexStride: Int,
            varyings: Array<Varying>,
            vertexShader: String,
            fragmentShader: String,
            colorSpace: ColorSpace
        ): MeshSpecification = make(
            attributes,
            vertexStride,
            varyings,
            vertexShader,
            fragmentShader,
            colorSpace,
            ColorAlphaType.PREMUL
        )

        fun make(
            attributes: Array<Attribute>,
            vertexStride: Int,
            varyings: Array<Varying>,
            vertexShader: String,
            fragmentShader: String,
            colorSpace: ColorSpace,
            alphaType: ColorAlphaType
        ): MeshSpecification {
            require(attributes.isNotEmpty()) { "At least one attribute is required" }
            require(attributes.size <= MAX_ATTRIBUTES) {
                "A maximum of $MAX_ATTRIBUTES attributes is supported"
            }
            require(vertexStride in 1..MAX_STRIDE) {
                "vertexStride must be between 1 and $MAX_STRIDE bytes"
            }
            require(vertexStride % STRIDE_ALIGNMENT == 0) {
                "vertexStride must be aligned to $STRIDE_ALIGNMENT bytes"
            }
            require(varyings.size <= MAX_VARYINGS) {
                "A maximum of $MAX_VARYINGS varyings is supported"
            }
            require(attributes.all { it.offset % OFFSET_ALIGNMENT == 0 }) {
                "Attribute offsets must be aligned to $OFFSET_ALIGNMENT bytes"
            }
            require(alphaType != ColorAlphaType.UNKNOWN) {
                "ColorAlphaType.UNKNOWN is not supported"
            }
            Stats.onNativeCall()
            val resultPtr = try {
                interopScope {
                    _nMake(
                        toInterop(attributes.map { it.type.value }.toIntArray()),
                        toInterop(attributes.map { it.offset }.toIntArray()),
                        toInterop(attributes.map { it.name }.toTypedArray()),
                        vertexStride,
                        toInterop(varyings.map { it.type.value }.toIntArray()),
                        toInterop(varyings.map { it.name }.toTypedArray()),
                        attributes.size,
                        varyings.size,
                        toInterop(vertexShader),
                        toInterop(fragmentShader),
                        getPtr(colorSpace),
                        alphaType.ordinal
                    )
                }
            } finally {
                reachabilityBarrier(colorSpace)
            }
            return makeFromResult(resultPtr)
        }

        private fun makeFromResult(resultPtr: NativePointer): MeshSpecification {
            val errorPtr = _nMeshSpecificationResultGetError(resultPtr)
            if (errorPtr != Native.NullPointer) {
                val error = withStringReferenceResult { errorPtr }
                _nMeshSpecificationResultDestroy(resultPtr)
                throw IllegalArgumentException(error)
            }
            val specification = MeshSpecification(_nMeshSpecificationResultGetSpecification(resultPtr))
            _nMeshSpecificationResultDestroy(resultPtr)
            return specification
        }
    }
}

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nMake")
private external fun _nMake(
    attributeTypes: InteropPointer,
    attributeOffsets: InteropPointer,
    attributeNames: InteropPointer,
    vertexStride: Int,
    varyingTypes: InteropPointer,
    varyingNames: InteropPointer,
    attributeCount: Int,
    varyingCount: Int,
    vertexShader: InteropPointer,
    fragmentShader: InteropPointer,
    colorSpacePtr: NativePointer,
    alphaType: Int
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nResultGetSpecification")
private external fun _nMeshSpecificationResultGetSpecification(resultPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nResultGetError")
private external fun _nMeshSpecificationResultGetError(resultPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nResultDestroy")
private external fun _nMeshSpecificationResultDestroy(resultPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_MeshSpecification__1nGetColorSpace")
private external fun _nGetColorSpace(specificationPtr: NativePointer): NativePointer
