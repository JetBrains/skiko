@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.getPtr
import kotlin.jvm.JvmStatic

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
            return MaskFilter(_nMakeTable(table))
        }

        fun makeGamma(gamma: Float): MaskFilter {
            Stats.onNativeCall()
            return MaskFilter(_nMakeGamma(gamma))
        }

        fun makeClip(min: Int, max: Int): MaskFilter {
            Stats.onNativeCall()
            return MaskFilter(_nMakeClip(min.toByte(), max.toByte()))
        }

        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_MaskFilter__1nMakeBlur")
        external fun _nMakeBlur(mode: Int, sigma: Float, respectCTM: Boolean): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_MaskFilter__1nMakeShader")
        external fun _nMakeShader(shaderPtr: NativePointer): NativePointer

        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_MaskFilter__1nMakeTable")
        external fun _nMakeTable(table: ByteArray?): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_MaskFilter__1nMakeGamma")
        external fun _nMakeGamma(gamma: Float): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_MaskFilter__1nMakeClip")
        external fun _nMakeClip(min: Byte, max: Byte): NativePointer

        init {
            staticLoad()
        }
    }
}