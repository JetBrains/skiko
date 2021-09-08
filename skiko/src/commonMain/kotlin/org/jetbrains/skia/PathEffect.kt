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

class PathEffect internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        fun makePath1D(path: Path?, advance: Float, phase: Float, style: Style): PathEffect {
            return try {
                Stats.onNativeCall()
                PathEffect(
                    _nMakePath1D(
                        getPtr(path),
                        advance,
                        phase,
                        style.ordinal
                    )
                )
            } finally {
                reachabilityBarrier(path)
            }
        }

        fun makePath2D(matrix: Matrix33, path: Path?): PathEffect {
            return try {
                Stats.onNativeCall()
                PathEffect(
                    _nMakePath2D(
                        matrix.mat,
                        getPtr(path)
                    )
                )
            } finally {
                reachabilityBarrier(path)
            }
        }

        fun makeLine2D(width: Float, matrix: Matrix33): PathEffect {
            Stats.onNativeCall()
            return PathEffect(_nMakeLine2D(width, matrix.mat))
        }

        fun makeCorner(radius: Float): PathEffect {
            Stats.onNativeCall()
            return PathEffect(_nMakeCorner(radius))
        }

        fun makeDash(intervals: FloatArray?, phase: Float): PathEffect {
            Stats.onNativeCall()
            return PathEffect(_nMakeDash(intervals, phase))
        }

        fun makeDiscrete(segLength: Float, dev: Float, seed: Int): PathEffect {
            Stats.onNativeCall()
            return PathEffect(_nMakeDiscrete(segLength, dev, seed))
        }

        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakeSum")
        external fun _nMakeSum(firstPtr: NativePointer, secondPtr: NativePointer): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakeCompose")
        external fun _nMakeCompose(outerPtr: NativePointer, innerPtr: NativePointer): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakePath1D")
        external fun _nMakePath1D(pathPtr: NativePointer, advance: Float, phase: Float, style: Int): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakePath2D")
        external fun _nMakePath2D(matrix: FloatArray?, pathPtr: NativePointer): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakeLine2D")
        external fun _nMakeLine2D(width: Float, matrix: FloatArray?): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakeCorner")
        external fun _nMakeCorner(radius: Float): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakeDash")
        external fun _nMakeDash(intervals: FloatArray?, phase: Float): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakeDiscrete")
        external fun _nMakeDiscrete(segLength: Float, dev: Float, seed: Int): NativePointer

        init {
            staticLoad()
        }
    }

    enum class Style {
        /** translate the shape to each position  */
        TRANSLATE,

        /** rotate the shape about its center  */
        ROTATE,

        /** transform each point, and turn lines into curves  */
        MORPH;
    }

    fun makeSum(second: PathEffect?): PathEffect {
        return try {
            Stats.onNativeCall()
            PathEffect(_nMakeSum(_ptr, getPtr(second)))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(second)
        }
    }

    fun makeCompose(inner: PathEffect?): PathEffect {
        return try {
            Stats.onNativeCall()
            PathEffect(_nMakeCompose(_ptr, getPtr(inner)))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(inner)
        }
    }
}