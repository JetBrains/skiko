package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier

class RuntimeShaderBuilder internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        init {
            staticLoad()
        }
    }

    constructor(effect: RuntimeEffect) : this(RuntimeShaderBuilder_nMakeFromRuntimeEffect(effect._ptr)) {
        Stats.onNativeCall()
        reachabilityBarrier(effect)
    }

    private object _FinalizerHolder {
        val PTR = RuntimeShaderBuilder_nGetFinalizer()
    }

    fun uniform(name: String, value: Int) {
        Stats.onNativeCall()
        interopScope {
            RuntimeShaderBuilder_nUniformInt(_ptr, toInterop(name), value)
        }
    }

    fun uniform(name: String, value1: Int, value2: Int) {
        Stats.onNativeCall()
        interopScope {
            RuntimeShaderBuilder_nUniformInt2(_ptr, toInterop(name), value1, value2)
        }
    }

    fun uniform(name: String, value1: Int, value2: Int, value3: Int) {
        Stats.onNativeCall()
        interopScope {
            RuntimeShaderBuilder_nUniformInt3(_ptr, toInterop(name), value1, value2, value3)
        }
    }

    fun uniform(name: String, value1: Int, value2: Int, value3: Int, value4: Int) {
        Stats.onNativeCall()
        interopScope {
            RuntimeShaderBuilder_nUniformInt4(_ptr, toInterop(name), value1, value2, value3, value4)
        }
    }

    fun uniform(name: String, value: Float) {
        Stats.onNativeCall()
        interopScope {
            RuntimeShaderBuilder_nUniformFloat(_ptr, toInterop(name), value)
        }
    }

    fun uniform(name: String, value1: Float, value2: Float) {
        Stats.onNativeCall()
        interopScope {
            RuntimeShaderBuilder_nUniformFloat2(_ptr, toInterop(name), value1, value2)
        }
    }

    fun uniform(name: String, value1: Float, value2: Float, value3: Float) {
        Stats.onNativeCall()
        interopScope {
            RuntimeShaderBuilder_nUniformFloat3(_ptr, toInterop(name), value1, value2, value3)
        }
    }

    fun uniform(name: String, value1: Float, value2: Float, value3: Float, value4: Float) {
        Stats.onNativeCall()
        interopScope {
            RuntimeShaderBuilder_nUniformFloat4(_ptr, toInterop(name), value1, value2, value3, value4)
        }
    }

    fun uniform(name: String, value: FloatArray) {
        Stats.onNativeCall()
        interopScope {
            RuntimeShaderBuilder_nUniformFloatArray(_ptr, toInterop(name), toInterop(value), value.size)
        }
    }


    fun uniform(name: String, value: Matrix22) {
        Stats.onNativeCall()
        interopScope {
            RuntimeShaderBuilder_nUniformFloatMatrix22(_ptr, toInterop(name), toInterop(value.mat))
        }
    }

    fun uniform(name: String, value: Matrix33) {
        Stats.onNativeCall()
        interopScope {
            RuntimeShaderBuilder_nUniformFloatMatrix33(_ptr, toInterop(name), toInterop(value.mat))
        }
    }

    fun uniform(name: String, value: Matrix44) {
        Stats.onNativeCall()
        interopScope {
            RuntimeShaderBuilder_nUniformFloatMatrix44(_ptr, toInterop(name), toInterop(value.mat))
        }
    }

    fun child(name: String, shader: Shader) {
        Stats.onNativeCall()
        try {
            interopScope {
                RuntimeShaderBuilder_nChildShader(_ptr, toInterop(name), getPtr(shader))
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(shader)
        }
    }

    fun child(name: String, colorFilter: ColorFilter) {
        Stats.onNativeCall()
        try {
            interopScope {
                RuntimeShaderBuilder_nChildColorFilter(_ptr, toInterop(name), getPtr(colorFilter))
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(colorFilter)
        }
    }

    fun makeShader(localMatrix: Matrix33? = null): Shader {
        Stats.onNativeCall()
        return try {
            interopScope {
                Shader(RuntimeShaderBuilder_nMakeShader(_ptr, toInterop(localMatrix?.mat)))
            }
        } finally {
            reachabilityBarrier(this)
        }
    }
}