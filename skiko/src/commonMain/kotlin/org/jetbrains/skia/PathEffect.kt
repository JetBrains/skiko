package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier

class PathEffect internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        fun makePath1D(path: Path, advance: Float, phase: Float, style: Style): PathEffect {
            return try {
                Stats.onNativeCall()
                PathEffect(
                    PathEffect_nMakePath1D(
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

        fun makePath2D(matrix: Matrix33, path: Path): PathEffect {
            return try {
                Stats.onNativeCall()
                PathEffect(
                    interopScope {
                        PathEffect_nMakePath2D(
                            toInterop(matrix.mat),
                            getPtr(path)
                        )
                    }
                )
            } finally {
                reachabilityBarrier(path)
            }
        }

        fun makeLine2D(width: Float, matrix: Matrix33): PathEffect {
            Stats.onNativeCall()
            return PathEffect(
                interopScope {
                    PathEffect_nMakeLine2D(width, toInterop(matrix.mat))
                }
            )
        }

        fun makeCorner(radius: Float): PathEffect {
            Stats.onNativeCall()
            return PathEffect(PathEffect_nMakeCorner(radius))
        }

        fun makeDash(intervals: FloatArray, phase: Float): PathEffect {
            Stats.onNativeCall()
            return PathEffect(
                interopScope {
                    PathEffect_nMakeDash(toInterop(intervals), intervals.size, phase)
                }
            )
        }

        fun makeDiscrete(segLength: Float, dev: Float, seed: Int): PathEffect {
            Stats.onNativeCall()
            return PathEffect(PathEffect_nMakeDiscrete(segLength, dev, seed))
        }

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
            PathEffect(PathEffect_nMakeSum(_ptr, getPtr(second)))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(second)
        }
    }

    fun makeCompose(inner: PathEffect?): PathEffect {
        return try {
            Stats.onNativeCall()
            PathEffect(PathEffect_nMakeCompose(_ptr, getPtr(inner)))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(inner)
        }
    }
}
