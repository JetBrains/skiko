package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

class RuntimeEffect internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        fun makeForShader(sksl: String): RuntimeEffect {
            Stats.onNativeCall()
            return interopScope {
                makeFromResultPtr(_nMakeForShader(toInterop(sksl)))
            }
        }

        fun makeForColorFilter(sksl: String): RuntimeEffect {
            Stats.onNativeCall()
            return interopScope {
                makeFromResultPtr(_nMakeForColorFilter(toInterop(sksl)))
            }
        }

        fun makeForBlender(sksl: String): RuntimeEffect {
            Stats.onNativeCall()
            return interopScope {
                makeFromResultPtr(_nMakeForBlender(toInterop(sksl)))
            }
        }

        init {
            staticLoad()
        }
    }

    fun makeShader(
        uniforms: Data?, children: Array<Shader?>?, localMatrix: Matrix33?
    ): Shader {
        Stats.onNativeCall()
        val childCount = children?.size ?: 0
        val childrenPtrs = NativePointerArray(childCount)
        for (i in 0 until childCount) childrenPtrs[i] = getPtr(children!![i])
        val matrix = localMatrix?.mat
        return try {
            interopScope {
                Shader(_nMakeShader(_ptr, getPtr(uniforms), toInterop(childrenPtrs), childCount, toInterop(matrix)))
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(uniforms)
            reachabilityBarrier(children)
        }
    }

    fun makeBlender(uniforms: Data?): Blender {
        Stats.onNativeCall()
        return try {
            interopScope {
                Blender(_nMakeBlender(_ptr, getPtr(uniforms)))
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(uniforms)
        }
    }
}

internal expect fun RuntimeEffect.Companion.makeFromResultPtr(ptr: NativePointer): RuntimeEffect

// Native: SkRuntimeEffect::makeShader loops over caller-supplied children
@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1nMakeShader")
@ForceNativeThreadState
private external fun _nMakeShader(
    runtimeEffectPtr: NativePointer, uniformPtr: NativePointer, childrenPtrs: InteropPointer,
    childCount: Int, localMatrix: InteropPointer
): NativePointer

// Runnable: SkRuntimeEffect::makeBlender validates child types and allocates SkRuntimeBlender; refs only
@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1nMakeBlender")
private external fun _nMakeBlender(
    runtimeEffectPtr: NativePointer, uniformPtr: NativePointer
): NativePointer

// Native: SkRuntimeEffect::MakeForShader compiles SkSL with SkSL::Compiler
@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1nMakeForShader")
@ForceNativeThreadState
private external fun _nMakeForShader(sksl: InteropPointer): NativePointer

// Native: SkRuntimeEffect::MakeForColorFilter compiles SkSL with SkSL::Compiler
@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1nMakeForColorFilter")
@ForceNativeThreadState
private external fun _nMakeForColorFilter(sksl: InteropPointer): NativePointer

// Native: SkRuntimeEffect::MakeForBlender compiles SkSL with SkSL::Compiler
@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1nMakeForBlender")
@ForceNativeThreadState
private external fun _nMakeForBlender(sksl: InteropPointer): NativePointer

//  The functions below can be used only in JS and native targets

// Runnable: sk_sp::release of Result.effect returns the raw pointer
@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1Result_nGetPtr")
internal external fun Result_nGetPtr(ptr: NativePointer): NativePointer

// Runnable: returns the address of Result.errorText or null
@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1Result_nGetError")
internal external fun Result_nGetError(ptr: NativePointer): NativePointer

// Runnable: delete Result frees SkString; effect is already released or null
@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1Result_nDestroy")
internal external fun Result_nDestroy(ptr: NativePointer)
