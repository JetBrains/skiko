package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

class PathEffect internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        fun makePath1D(path: Path, advance: Float, phase: Float, style: Style): PathEffect {
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

        fun makePath2D(matrix: Matrix33, path: Path): PathEffect {
            return try {
                Stats.onNativeCall()
                PathEffect(
                    interopScope {
                        _nMakePath2D(
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
                    _nMakeLine2D(width, toInterop(matrix.mat))
                }
            )
        }

        fun makeCorner(radius: Float): PathEffect {
            Stats.onNativeCall()
            return PathEffect(_nMakeCorner(radius))
        }

        fun makeDash(intervals: FloatArray, phase: Float): PathEffect {
            Stats.onNativeCall()
            return PathEffect(
                interopScope {
                    _nMakeDash(toInterop(intervals), intervals.size, phase)
                }
            )
        }

        fun makeDiscrete(segLength: Float, dev: Float, seed: Int): PathEffect {
            Stats.onNativeCall()
            return PathEffect(_nMakeDiscrete(segLength, dev, seed))
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
            PathEffect(_nMakeSum(_ptr, getPtr(second)))
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

@ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakeCompose")
// No ForceNativeThreadState: holds references to two other path effects and cannot own typefaces or GPU objects, so it
// is short and cannot block.
// Native call path: SkPathEffect::MakeCompose -> SkComposePathEffect::Make
private external fun PathEffect_nMakeCompose(outerPtr: NativePointer, innerPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakeSum")
// No ForceNativeThreadState: holds references to two other path effects and cannot own typefaces or GPU objects, so it
// is short and cannot block.
// Native call path: SkPathEffect::MakeSum -> SkSumPathEffect::Make
private external fun _nMakeSum(firstPtr: NativePointer, secondPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakePath1D")
// No ForceNativeThreadState: takes a reference to the path data and stores three numbers, so it is short and cannot
// block.
// Native call path: SkPath1DPathEffect::Make
private external fun _nMakePath1D(pathPtr: NativePointer, advance: Float, phase: Float, style: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakePath2D")
// No ForceNativeThreadState: inverts one small matrix and takes a reference to the path data, so it is short and cannot
// block.
// Native call path: SkPath2DPathEffect::Make
private external fun _nMakePath2D(matrix: InteropPointer, pathPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakeLine2D")
// No ForceNativeThreadState: inverts one small matrix and stores a width, so it is short and cannot block.
// Native call path: SkLine2DPathEffect::Make
private external fun _nMakeLine2D(width: Float, matrix: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakeCorner")
// No ForceNativeThreadState: allocates a small object that holds one number, so it is short and cannot block.
// Native call path: SkCornerPathEffect::Make
private external fun _nMakeCorner(radius: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakeDash")
// Needs ForceNativeThreadState: validates and copies every dash interval, so its time grows with the number of
// intervals.
// Native call path: SkDashPathEffect::Make -> SkDashImpl::SkDashImpl
@ForceNativeThreadState
private external fun _nMakeDash(intervals: InteropPointer, count: Int, phase: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakeDiscrete")
// No ForceNativeThreadState: checks two numbers and allocates a small object holding three numbers, so it is short and
// cannot block.
// Native call path: SkDiscretePathEffect::Make
private external fun _nMakeDiscrete(segLength: Float, dev: Float, seed: Int): NativePointer
