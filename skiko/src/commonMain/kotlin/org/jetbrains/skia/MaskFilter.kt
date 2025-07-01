package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier

class MaskFilter internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        fun makeBlur(mode: FilterBlurMode, sigma: Float, respectCTM: Boolean = true): MaskFilter {
            Stats.onNativeCall()
            return MaskFilter(MaskFilter_nMakeBlur(mode.ordinal, sigma, respectCTM))
        }

        fun makeShader(s: Shader?): MaskFilter {
            return try {
                Stats.onNativeCall()
                MaskFilter(MaskFilter_nMakeShader(getPtr(s)))
            } finally {
                reachabilityBarrier(s)
            }
        }

        fun makeTable(table: ByteArray): MaskFilter {
            require(table.size == 256) { "Expected 256 elements, got " + table.size }

            Stats.onNativeCall()
            return MaskFilter(
                interopScope {
                    MaskFilter_nMakeTable(toInterop(table))
                }
            )
        }

        fun makeGamma(gamma: Float): MaskFilter {
            Stats.onNativeCall()
            return MaskFilter(MaskFilter_nMakeGamma(gamma))
        }

        fun makeClip(min: Int, max: Int): MaskFilter {
            Stats.onNativeCall()
            return MaskFilter(MaskFilter_nMakeClip(min.toByte(), max.toByte()))
        }

        init {
            staticLoad()
        }
    }
}