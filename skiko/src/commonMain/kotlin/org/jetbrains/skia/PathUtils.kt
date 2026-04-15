package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier

object PathUtils {

    /** Returns the filled equivalent of the stroked path. */
    fun fillPathWithPaint(src: Path, paint: Paint, dst: PathBuilder, cull: Rect?, resScale: Float): Boolean {
        return fillPathWithPaint(src, paint, dst, cull, Matrix33.makeScale(resScale))
    }

    /**
     * Returns the filled equivalent of the stroked path.
     *
     * @param src       SkPath read to create a filled version
     * @param paint     uses settings for stroke cap, width, miter, join, and patheffect.
     * @param dst       results are written to this builder.
     * @param cull      optional limit passed to SkPathEffect
     * @param matrix    matrix to take into acount for increased precision (if it scales up).
     * @return          true if the result can be filled, or false if it is a hairline (to be stroked).
     */
    fun fillPathWithPaint(src: Path, paint: Paint, dst: PathBuilder, cull: Rect?, matrix: Matrix33): Boolean {
        return try {
            Stats.onNativeCall()
            interopScope {
                if (cull == null) {
                    _nFillPathWithPaintMatrix(
                        getPtr(src),
                        getPtr(paint),
                        getPtr(dst),
                        toInterop(matrix.mat)
                    )
                } else {
                    _nFillPathWithPaintCull(
                        getPtr(src),
                        getPtr(paint),
                        getPtr(dst),
                        cull.left,
                        cull.top,
                        cull.right,
                        cull.bottom,
                        toInterop(matrix.mat)
                    )
                }
            }
        } finally {
            reachabilityBarrier(matrix)
            reachabilityBarrier(src)
            reachabilityBarrier(paint)
            reachabilityBarrier(dst)
        }
    }

    /**
     * Returns the filled equivalent of the stroked path.
     *
     * @param src   SkPath read to create a filled version
     * @param paint uses settings for stroke cap, width, miter, join, and patheffect.
     */
    fun fillPathWithPaint(src: Path, paint: Paint): Path {
        return try {
            Stats.onNativeCall()
            Path(_nFillPathWithPaint(getPtr(src), getPtr(paint)))
        } finally {
            reachabilityBarrier(src)
            reachabilityBarrier(paint)
        }
    }

    /**
     * Returns the filled equivalent of the stroked path.
     *
     * @param src   SkPath read to create a filled version
     * @param paint uses settings for stroke cap, width, miter, join, and patheffect.
     * @param dst   results are written to this builder.
     * @return      true if the result can be filled, or false if it is a hairline (to be stroked).
     */
    fun fillPathWithPaint(src: Path, paint: Paint, dst: PathBuilder): Boolean {
        return try {
            Stats.onNativeCall()
            _nFillPathWithPaintBuilder(getPtr(src), getPtr(paint), getPtr(dst))
        } finally {
            reachabilityBarrier(src)
            reachabilityBarrier(paint)
            reachabilityBarrier(dst)
        }
    }

    init {
        staticLoad()
    }
}

@ExternalSymbolName("org_jetbrains_skia_PathUtils__1nFillPathWithPaint")
private external fun _nFillPathWithPaint(
    srcPtr: NativePointer,
    paintPtr: NativePointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathUtils__1nFillPathWithPaintBuilder")
private external fun _nFillPathWithPaintBuilder(
    srcPtr: NativePointer,
    paintPtr: NativePointer,
    dstPtr: NativePointer
): Boolean

@ExternalSymbolName("org_jetbrains_skia_PathUtils__1nFillPathWithPaintMatrix")
private external fun _nFillPathWithPaintMatrix(
    srcPtr: NativePointer,
    paintPtr: NativePointer,
    dstPtr: NativePointer,
    matrix: InteropPointer
): Boolean

@ExternalSymbolName("org_jetbrains_skia_PathUtils__1nFillPathWithPaintCull")
private external fun _nFillPathWithPaintCull(
    srcPtr: NativePointer,
    paintPtr: NativePointer,
    dstPtr: NativePointer,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    matrix: InteropPointer
): Boolean
