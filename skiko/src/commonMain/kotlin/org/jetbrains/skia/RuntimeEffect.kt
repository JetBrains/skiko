package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

class RuntimeEffect internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        fun makeForShader(sksl: String?): RuntimeEffect {
            Stats.onNativeCall()
            return RuntimeEffect(_nMakeForShader(sksl))
        }

        fun makeForColorFilter(sksl: String?): RuntimeEffect {
            Stats.onNativeCall()
            return RuntimeEffect(_nMakeForColorFilter(sksl))
        }

        init {
            staticLoad()
        }
    }

    fun makeShader(
        uniforms: Data?, children: Array<Shader?>?, localMatrix: Matrix33?,
        isOpaque: Boolean
    ): Shader {
        Stats.onNativeCall()
        val childCount = children?.size ?: 0
        val childrenPtrs = NativePointerArray(childCount)
        for (i in 0 until childCount) childrenPtrs[i] = getPtr(children!![i])
        val matrix = localMatrix?.mat
        return interopScope {
            Shader(_nMakeShader(_ptr, getPtr(uniforms), toInterop(childrenPtrs), toInterop(matrix), isOpaque))
        }
    }
}


@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1nMakeShader")
private external fun _nMakeShader(
    runtimeEffectPtr: NativePointer, uniformPtr: NativePointer, childrenPtrs: InteropPointer,
    localMatrix: InteropPointer, isOpaque: Boolean
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1nMakeForShader")
private external fun _nMakeForShader(sksl: String?): NativePointer

@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1nMakeForColorFilter")
private external fun _nMakeForColorFilter(sksl: String?): NativePointer
