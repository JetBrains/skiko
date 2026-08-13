package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.withStringReferenceResult
import org.jetbrains.skia.impl.withStringResult

/**
 * A custom mesh backed by copied CPU vertex data and, optionally, 16-bit indices.
 */
class Mesh private constructor(
    ptr: NativePointer,
    private val colorSpace: ColorSpace
) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        init {
            staticLoad()
        }

        internal fun makePointer(
            specification: MeshSpecification,
            mode: VertexMode,
            vertexData: InteropPointer,
            vertexDataSize: Int,
            vertexCount: Int,
            indexData: InteropPointer?,
            indexCount: Int,
            bounds: Rect
        ): NativePointer {
            require(mode != VertexMode.TRIANGLE_FAN) { "SkMesh does not support TRIANGLE_FAN" }
            require(vertexCount >= 3) { "vertexCount must be at least 3" }
            if (indexCount != 0) require(indexCount >= 3) { "indexCount must be at least 3" }

            val resultPtr = _nMake(
                getPtr(specification),
                mode.ordinal,
                vertexData,
                vertexDataSize,
                vertexCount,
                indexData,
                indexCount,
                bounds.left,
                bounds.top,
                bounds.right,
                bounds.bottom
            )
            val errorPtr = _nMeshResultGetError(resultPtr)
            if (errorPtr != Native.NullPointer) {
                val error = withStringReferenceResult { errorPtr }
                _nMeshResultDestroy(resultPtr)
                throw IllegalArgumentException(error)
            }
            val meshPtr = _nMeshResultGetMesh(resultPtr)
            _nMeshResultDestroy(resultPtr)
            return meshPtr
        }
    }

    constructor(
        specification: MeshSpecification,
        mode: VertexMode,
        vertexBuffer: ByteArray,
        vertexCount: Int,
        bounds: Rect
    ) : this(
        makeFromBytes(specification, mode, vertexBuffer, vertexCount, null, bounds),
        specification.colorSpace
    )

    constructor(
        specification: MeshSpecification,
        mode: VertexMode,
        vertexBuffer: ByteArray,
        vertexCount: Int,
        indexBuffer: ShortArray,
        bounds: Rect
    ) : this(
        makeFromBytes(specification, mode, vertexBuffer, vertexCount, indexBuffer, bounds),
        specification.colorSpace
    )

    constructor(
        specification: MeshSpecification,
        mode: VertexMode,
        vertexBuffer: FloatArray,
        vertexCount: Int,
        bounds: Rect
    ) : this(
        makeFromFloats(specification, mode, vertexBuffer, vertexCount, null, bounds),
        specification.colorSpace
    )

    constructor(
        specification: MeshSpecification,
        mode: VertexMode,
        vertexBuffer: FloatArray,
        vertexCount: Int,
        indexBuffer: ShortArray,
        bounds: Rect
    ) : this(
        makeFromFloats(specification, mode, vertexBuffer, vertexCount, indexBuffer, bounds),
        specification.colorSpace
    )

    fun setFloatUniform(name: String, value: Float) = setFloatUniform(name, floatArrayOf(value))

    fun setFloatUniform(name: String, value1: Float, value2: Float) =
        setFloatUniform(name, floatArrayOf(value1, value2))

    fun setFloatUniform(name: String, value1: Float, value2: Float, value3: Float) =
        setFloatUniform(name, floatArrayOf(value1, value2, value3))

    fun setFloatUniform(name: String, value1: Float, value2: Float, value3: Float, value4: Float) =
        setFloatUniform(name, floatArrayOf(value1, value2, value3, value4))

    fun setFloatUniform(name: String, values: FloatArray) {
        Stats.onNativeCall()
        try {
            interopScope {
                checkNativeError(_nSetFloatUniform(_ptr, toInterop(name), toInterop(values), values.size))
            }
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun setIntUniform(name: String, value: Int) = setIntUniform(name, intArrayOf(value))

    fun setIntUniform(name: String, value1: Int, value2: Int) =
        setIntUniform(name, intArrayOf(value1, value2))

    fun setIntUniform(name: String, value1: Int, value2: Int, value3: Int) =
        setIntUniform(name, intArrayOf(value1, value2, value3))

    fun setIntUniform(name: String, value1: Int, value2: Int, value3: Int, value4: Int) =
        setIntUniform(name, intArrayOf(value1, value2, value3, value4))

    fun setIntUniform(name: String, values: IntArray) {
        Stats.onNativeCall()
        try {
            interopScope {
                checkNativeError(_nSetIntUniform(_ptr, toInterop(name), toInterop(values), values.size))
            }
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun setColorUniform(name: String, color: Int) {
        setColorUniform(name, Color4f(color))
    }

    fun setColorUniform(name: String, color: Color4f) {
        Stats.onNativeCall()
        val converted = ColorSpace.sRGB.convert(colorSpace, color)
        try {
            interopScope {
                checkNativeError(
                    _nSetColorUniform(
                        _ptr,
                        toInterop(name),
                        converted.r,
                        converted.g,
                        converted.b,
                        converted.a
                    )
                )
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(colorSpace)
        }
    }

    fun child(name: String, shader: Shader) {
        setChild(name, shader, 0)
    }

    fun child(name: String, colorFilter: ColorFilter) {
        setChild(name, colorFilter, 1)
    }

    fun child(name: String, blender: Blender) {
        setChild(name, blender, 2)
    }

    private fun setChild(name: String, child: Native, type: Int) {
        Stats.onNativeCall()
        try {
            interopScope {
                checkNativeError(_nSetChild(_ptr, toInterop(name), getPtr(child), type))
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(child)
        }
    }

    private object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }
}

private fun makeFromBytes(
    specification: MeshSpecification,
    mode: VertexMode,
    vertexBuffer: ByteArray,
    vertexCount: Int,
    indexBuffer: ShortArray?,
    bounds: Rect
): NativePointer {
    Stats.onNativeCall()
    return try {
        interopScope {
            Mesh.makePointer(
                specification,
                mode,
                toInterop(vertexBuffer),
                vertexBuffer.size,
                vertexCount,
                toInterop(indexBuffer),
                indexBuffer?.size ?: 0,
                bounds
            )
        }
    } finally {
        reachabilityBarrier(specification)
    }
}

private fun makeFromFloats(
    specification: MeshSpecification,
    mode: VertexMode,
    vertexBuffer: FloatArray,
    vertexCount: Int,
    indexBuffer: ShortArray?,
    bounds: Rect
): NativePointer {
    Stats.onNativeCall()
    return try {
        interopScope {
            Mesh.makePointer(
                specification,
                mode,
                toInterop(vertexBuffer),
                vertexBuffer.size * 4,
                vertexCount,
                toInterop(indexBuffer),
                indexBuffer?.size ?: 0,
                bounds
            )
        }
    } finally {
        reachabilityBarrier(specification)
    }
}

private fun checkNativeError(errorPtr: NativePointer) {
    if (errorPtr != Native.NullPointer) throw IllegalArgumentException(withStringResult(errorPtr))
}

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nGetFinalizer")
private external fun _nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nMake")
private external fun _nMake(
    specificationPtr: NativePointer,
    mode: Int,
    vertexData: InteropPointer,
    vertexDataSize: Int,
    vertexCount: Int,
    indexData: InteropPointer?,
    indexCount: Int,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nResultGetMesh")
private external fun _nMeshResultGetMesh(resultPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nResultGetError")
private external fun _nMeshResultGetError(resultPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nResultDestroy")
private external fun _nMeshResultDestroy(resultPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nSetFloatUniform")
private external fun _nSetFloatUniform(
    meshPtr: NativePointer,
    name: InteropPointer,
    values: InteropPointer,
    count: Int
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nSetIntUniform")
private external fun _nSetIntUniform(
    meshPtr: NativePointer,
    name: InteropPointer,
    values: InteropPointer,
    count: Int
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nSetColorUniform")
private external fun _nSetColorUniform(
    meshPtr: NativePointer,
    name: InteropPointer,
    r: Float,
    g: Float,
    b: Float,
    a: Float
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Mesh__1nSetChild")
private external fun _nSetChild(
    meshPtr: NativePointer,
    name: InteropPointer,
    childPtr: NativePointer,
    type: Int
): NativePointer
