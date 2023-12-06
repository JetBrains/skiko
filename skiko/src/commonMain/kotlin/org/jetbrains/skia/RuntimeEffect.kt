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
}

internal expect fun RuntimeEffect.Companion.makeFromResultPtr(ptr: NativePointer): RuntimeEffect

@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1nMakeShader")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_RuntimeEffect__1nMakeShader")
private external fun _nMakeShader(
    runtimeEffectPtr: NativePointer, uniformPtr: NativePointer, childrenPtrs: InteropPointer,
    childCount: Int, localMatrix: InteropPointer
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1nMakeForShader")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_RuntimeEffect__1nMakeForShader")
private external fun _nMakeForShader(sksl: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1nMakeForColorFilter")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_RuntimeEffect__1nMakeForColorFilter")
private external fun _nMakeForColorFilter(sksl: InteropPointer): NativePointer

//  The functions below can be used only in JS and native targets

@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1Result_nGetPtr")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_RuntimeEffect__1Result_nGetPtr")
internal external fun Result_nGetPtr(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1Result_nGetError")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_RuntimeEffect__1Result_nGetError")
internal external fun Result_nGetError(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1Result_nDestroy")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_RuntimeEffect__1Result_nDestroy")
internal external fun Result_nDestroy(ptr: NativePointer)