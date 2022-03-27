package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Native.Companion.NullPointer
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

    fun uniform(name: String, value: Float) {
        Stats.onNativeCall()
        interopScope {
            _nUniformFloat(_ptr, toInterop(name), value)
        }
    }
}

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nMakeFromRuntimeEffect")
private external fun _nMakeFromRuntimeEffect(effectPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nGetFinalizer")
private external fun RuntimeShaderBuilder_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nUniformInt")
private external fun _nUniformInt(builderPtr: NativePointer, uniformName: InteropPointer, uniformValue: Int)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloat")
private external fun _nUniformFloat(builderPtr: NativePointer, uniformName: InteropPointer, uniformValue: Float)
