@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.getPtr

class MaskFilter internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        fun makeBlur(mode: FilterBlurMode, sigma: Float, respectCTM: Boolean = true): MaskFilter {
            Stats.onNativeCall()
            return MaskFilter(_nMakeBlur(mode.ordinal, sigma, respectCTM))
        }

        fun makeShader(s: Shader?): MaskFilter {
            return try {
                Stats.onNativeCall()
                MaskFilter(_nMakeShader(getPtr(s)))
            } finally {
                reachabilityBarrier(s)
            }
        }

        fun makeTable(table: ByteArray?): MaskFilter {
            Stats.onNativeCall()
            return MaskFilter(MaskFilter_nMakeTable(table))
        }

        fun makeGamma(gamma: Float): MaskFilter {
            Stats.onNativeCall()
            return MaskFilter(_nMakeGamma(gamma))
        }

        fun makeClip(min: Int, max: Int): MaskFilter {
            Stats.onNativeCall()
            return MaskFilter(_nMakeClip(min.toByte(), max.toByte()))
        }

        init {
            staticLoad()
        }
    }
}

@ExternalSymbolName("org_jetbrains_skia_MaskFilter__1nMakeBlur")
private external fun _nMakeBlur(mode: Int, sigma: Float, respectCTM: Boolean): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MaskFilter__1nMakeShader")
private external fun _nMakeShader(shaderPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MaskFilter__1nMakeTable")
private external fun MaskFilter_nMakeTable(table: ByteArray?): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MaskFilter__1nMakeGamma")
private external fun _nMakeGamma(gamma: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MaskFilter__1nMakeClip")
private external fun _nMakeClip(min: Byte, max: Byte): NativePointer
