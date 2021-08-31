package org.jetbrains.skija

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.skija.impl.RefCnt
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class PathEffect internal constructor(ptr: Long) : RefCnt(ptr) {
    companion object {
        fun makePath1D(path: Path?, advance: Float, phase: Float, style: Style): PathEffect {
            return try {
                Stats.onNativeCall()
                PathEffect(
                    _nMakePath1D(
                        Native.Companion.getPtr(path),
                        advance,
                        phase,
                        style.ordinal
                    )
                )
            } finally {
                Reference.reachabilityFence(path)
            }
        }

        fun makePath2D(matrix: Matrix33, path: Path?): PathEffect {
            return try {
                Stats.onNativeCall()
                PathEffect(
                    _nMakePath2D(
                        matrix.mat,
                        Native.Companion.getPtr(path)
                    )
                )
            } finally {
                Reference.reachabilityFence(path)
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

        @JvmStatic external fun _nMakeSum(firstPtr: Long, secondPtr: Long): Long
        @JvmStatic external fun _nMakeCompose(outerPtr: Long, innerPtr: Long): Long
        @JvmStatic external fun _nMakePath1D(pathPtr: Long, advance: Float, phase: Float, style: Int): Long
        @JvmStatic external fun _nMakePath2D(matrix: FloatArray?, pathPtr: Long): Long
        @JvmStatic external fun _nMakeLine2D(width: Float, matrix: FloatArray?): Long
        @JvmStatic external fun _nMakeCorner(radius: Float): Long
        @JvmStatic external fun _nMakeDash(intervals: FloatArray?, phase: Float): Long
        @JvmStatic external fun _nMakeDiscrete(segLength: Float, dev: Float, seed: Int): Long

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

        companion object {
            internal val _values = values()
        }
    }

    fun makeSum(second: PathEffect?): PathEffect {
        return try {
            Stats.onNativeCall()
            PathEffect(_nMakeSum(_ptr, Native.Companion.getPtr(second)))
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(second)
        }
    }

    fun makeCompose(inner: PathEffect?): PathEffect {
        return try {
            Stats.onNativeCall()
            PathEffect(_nMakeCompose(_ptr, Native.Companion.getPtr(inner)))
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(inner)
        }
    }
}