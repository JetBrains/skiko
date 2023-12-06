package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier

class RuntimeShaderBuilder internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        init {
            staticLoad()
        }
    }

    constructor(effect: RuntimeEffect) : this(_nMakeFromRuntimeEffect(effect._ptr)) {
        Stats.onNativeCall()
        reachabilityBarrier(effect)
    }

    private object _FinalizerHolder {
        val PTR = RuntimeShaderBuilder_nGetFinalizer()
    }

    fun uniform(name: String, value: Int) {
        Stats.onNativeCall()
        interopScope {
            _nUniformInt(_ptr, toInterop(name), value)
        }
    }

    fun uniform(name: String, value1: Int, value2: Int) {
        Stats.onNativeCall()
        interopScope {
            _nUniformInt2(_ptr, toInterop(name), value1, value2)
        }
    }

    fun uniform(name: String, value1: Int, value2: Int, value3: Int) {
        Stats.onNativeCall()
        interopScope {
            _nUniformInt3(_ptr, toInterop(name), value1, value2, value3)
        }
    }

    fun uniform(name: String, value1: Int, value2: Int, value3: Int, value4: Int) {
        Stats.onNativeCall()
        interopScope {
            _nUniformInt4(_ptr, toInterop(name), value1, value2, value3, value4)
        }
    }

    fun uniform(name: String, value: Float) {
        Stats.onNativeCall()
        interopScope {
            _nUniformFloat(_ptr, toInterop(name), value)
        }
    }

    fun uniform(name: String, value1: Float, value2: Float) {
        Stats.onNativeCall()
        interopScope {
            _nUniformFloat2(_ptr, toInterop(name), value1, value2)
        }
    }

    fun uniform(name: String, value1: Float, value2: Float, value3: Float) {
        Stats.onNativeCall()
        interopScope {
            _nUniformFloat3(_ptr, toInterop(name), value1, value2, value3)
        }
    }

    fun uniform(name: String, value1: Float, value2: Float, value3: Float, value4: Float) {
        Stats.onNativeCall()
        interopScope {
            _nUniformFloat4(_ptr, toInterop(name), value1, value2, value3, value4)
        }
    }

    fun uniform(name: String, value: Matrix22) {
        Stats.onNativeCall()
        interopScope {
            _nUniformFloatMatrix22(_ptr, toInterop(name), toInterop(value.mat))
        }
    }

    fun uniform(name: String, value: Matrix33) {
        Stats.onNativeCall()
        interopScope {
            _nUniformFloatMatrix33(_ptr, toInterop(name), toInterop(value.mat))
        }
    }

    fun uniform(name: String, value: Matrix44) {
        Stats.onNativeCall()
        interopScope {
            _nUniformFloatMatrix44(_ptr, toInterop(name), toInterop(value.mat))
        }
    }

    fun child(name: String, shader: Shader) {
        Stats.onNativeCall()
        try {
            interopScope {
                _nChildShader(_ptr, toInterop(name), getPtr(shader))
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
                _nChildColorFilter(_ptr, toInterop(name), getPtr(colorFilter))
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
                Shader(_nMakeShader(_ptr, toInterop(localMatrix?.mat)))
            }
        } finally {
            reachabilityBarrier(this)
        }
    }
}

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nMakeFromRuntimeEffect")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_RuntimeShaderBuilder__1nMakeFromRuntimeEffect")
private external fun _nMakeFromRuntimeEffect(effectPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_RuntimeShaderBuilder__1nGetFinalizer")
private external fun RuntimeShaderBuilder_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nUniformInt")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_RuntimeShaderBuilder__1nUniformInt")
private external fun _nUniformInt(builderPtr: NativePointer, uniformName: InteropPointer, uniformValue: Int)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nUniformInt2")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_RuntimeShaderBuilder__1nUniformInt2")
private external fun _nUniformInt2(builderPtr: NativePointer, uniformName: InteropPointer, uniformValue1: Int, uniformValue2: Int)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nUniformInt3")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_RuntimeShaderBuilder__1nUniformInt3")
private external fun _nUniformInt3(builderPtr: NativePointer, uniformName: InteropPointer, uniformValue1: Int, uniformValue2: Int, uniformValue3: Int)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nUniformInt4")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_RuntimeShaderBuilder__1nUniformInt4")
private external fun _nUniformInt4(builderPtr: NativePointer, uniformName: InteropPointer, uniformValue1: Int, uniformValue2: Int, uniformValue3: Int, uniformValue4: Int)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloat")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloat")
private external fun _nUniformFloat(builderPtr: NativePointer, uniformName: InteropPointer, uniformValue: Float)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloat2")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloat2")
private external fun _nUniformFloat2(builderPtr: NativePointer, uniformName: InteropPointer, uniformValue1: Float, uniformValue2: Float)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloat3")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloat3")
private external fun _nUniformFloat3(builderPtr: NativePointer, uniformName: InteropPointer, uniformValue1: Float, uniformValue2: Float, uniformValue3: Float)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloat4")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloat4")
private external fun _nUniformFloat4(builderPtr: NativePointer, uniformName: InteropPointer, uniformValue1: Float, uniformValue2: Float, uniformValue3: Float, uniformValue4: Float)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloatMatrix22")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloatMatrix22")
private external fun _nUniformFloatMatrix22(builderPtr: NativePointer, uniformName: InteropPointer, uniformMatrix22: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloatMatrix33")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloatMatrix33")
private external fun _nUniformFloatMatrix33(builderPtr: NativePointer, uniformName: InteropPointer, uniformMatrix33: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloatMatrix44")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloatMatrix44")
private external fun _nUniformFloatMatrix44(builderPtr: NativePointer, uniformName: InteropPointer, uniformMatrix44: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nChildShader")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_RuntimeShaderBuilder__1nChildShader")
private external fun _nChildShader(builderPtr: NativePointer, uniformName: InteropPointer, shaderPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nChildColorFilter")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_RuntimeShaderBuilder__1nChildColorFilter")
private external fun _nChildColorFilter(builderPtr: NativePointer, uniformName: InteropPointer, colorFilterPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nMakeShader")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_RuntimeShaderBuilder__1nMakeShader")
private external fun _nMakeShader(builderPtr: NativePointer, localMatrix: InteropPointer): NativePointer
