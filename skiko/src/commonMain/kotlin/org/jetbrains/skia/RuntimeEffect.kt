package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.NativePointerArray
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier

class RuntimeEffect internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        fun makeForShader(sksl: String): RuntimeEffect {
            Stats.onNativeCall()
            return interopScope {
                makeFromResultPtr(RuntimeEffect_nMakeForShader(toInterop(sksl)))
            }
        }

        fun makeForColorFilter(sksl: String): RuntimeEffect {
            Stats.onNativeCall()
            return interopScope {
                makeFromResultPtr(RuntimeEffect_nMakeForColorFilter(toInterop(sksl)))
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
                Shader(RuntimeEffect_nMakeShader(_ptr, getPtr(uniforms), toInterop(childrenPtrs), childCount, toInterop(matrix)))
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(uniforms)
            reachabilityBarrier(children)
        }
    }
}

internal expect fun RuntimeEffect.Companion.makeFromResultPtr(ptr: NativePointer): RuntimeEffect