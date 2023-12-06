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
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PathEffect__1nMakeCompose")
private external fun PathEffect_nMakeCompose(outerPtr: NativePointer, innerPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakeSum")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PathEffect__1nMakeSum")
private external fun _nMakeSum(firstPtr: NativePointer, secondPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakePath1D")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PathEffect__1nMakePath1D")
private external fun _nMakePath1D(pathPtr: NativePointer, advance: Float, phase: Float, style: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakePath2D")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PathEffect__1nMakePath2D")
private external fun _nMakePath2D(matrix: InteropPointer, pathPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakeLine2D")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PathEffect__1nMakeLine2D")
private external fun _nMakeLine2D(width: Float, matrix: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakeCorner")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PathEffect__1nMakeCorner")
private external fun _nMakeCorner(radius: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakeDash")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PathEffect__1nMakeDash")
private external fun _nMakeDash(intervals: InteropPointer, count: Int, phase: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakeDiscrete")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PathEffect__1nMakeDiscrete")
private external fun _nMakeDiscrete(segLength: Float, dev: Float, seed: Int): NativePointer
