package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.nativeCallFromStatic

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
    fun fillPathWithPaint(src: Path, paint: Paint, dst: PathBuilder, cull: Rect?, matrix: Matrix33): Boolean =
        nativeCallFromStatic(src, paint, dst, matrix) { src, paint, dst, matrix ->
            interopScope {
                if (cull == null) {
                    _nFillPathWithPaintMatrix(
                        src._ptr,
                        paint._ptr,
                        dst._ptr,
                        toInterop(matrix.mat)
                    )
                } else {
                    _nFillPathWithPaintCull(
                        src._ptr,
                        paint._ptr,
                        dst._ptr,
                        cull.left,
                        cull.top,
                        cull.right,
                        cull.bottom,
                        toInterop(matrix.mat)
                    )
                }
            }
        }

    /**
     * Returns the filled equivalent of the stroked path.
     *
     * @param src   SkPath read to create a filled version
     * @param paint uses settings for stroke cap, width, miter, join, and patheffect.
     */
    fun fillPathWithPaint(src: Path, paint: Paint): Path = nativeCallFromStatic(src, paint) { src, paint ->
        Path(_nFillPathWithPaint(src._ptr, paint._ptr))
    }

    /**
     * Returns the filled equivalent of the stroked path.
     *
     * @param src   SkPath read to create a filled version
     * @param paint uses settings for stroke cap, width, miter, join, and patheffect.
     * @param dst   results are written to this builder.
     * @return      true if the result can be filled, or false if it is a hairline (to be stroked).
     */
    fun fillPathWithPaint(src: Path, paint: Paint, dst: PathBuilder): Boolean =
        nativeCallFromStatic(src, paint, dst) { src, paint, dst ->
            _nFillPathWithPaintBuilder(src._ptr, paint._ptr, dst._ptr)
        }

    init {
        staticLoad()
    }
}

@ExternalSymbolName("org_jetbrains_skia_PathUtils__1nFillPathWithPaint")
// Needs ForceNativeThreadState: applies the paint's path effect and stroke to the path, so its time grows with the path
// size.
// Native call path: FillPathWithPaint -> SkStrokeRec::applyToPath -> SkStroke::strokePath
@ForceNativeThreadState
private external fun _nFillPathWithPaint(
    srcPtr: NativePointer,
    paintPtr: NativePointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathUtils__1nFillPathWithPaintBuilder")
// Needs ForceNativeThreadState: applies the paint's path effect and stroke to the path, so its time grows with the path
// size.
// Native call path: FillPathWithPaint -> SkStrokeRec::applyToPath -> SkStroke::strokePath
@ForceNativeThreadState
private external fun _nFillPathWithPaintBuilder(
    srcPtr: NativePointer,
    paintPtr: NativePointer,
    dstPtr: NativePointer
): Boolean

@ExternalSymbolName("org_jetbrains_skia_PathUtils__1nFillPathWithPaintMatrix")
// Needs ForceNativeThreadState: applies the paint's path effect and stroke to the path, so its time grows with the path
// size.
// Native call path: FillPathWithPaint -> SkStrokeRec::applyToPath -> SkStroke::strokePath
@ForceNativeThreadState
private external fun _nFillPathWithPaintMatrix(
    srcPtr: NativePointer,
    paintPtr: NativePointer,
    dstPtr: NativePointer,
    matrix: InteropPointer
): Boolean

@ExternalSymbolName("org_jetbrains_skia_PathUtils__1nFillPathWithPaintCull")
// Needs ForceNativeThreadState: applies the paint's path effect and stroke to the path, so its time grows with the path
// size.
// Native call path: FillPathWithPaint -> SkStrokeRec::applyToPath -> SkStroke::strokePath
@ForceNativeThreadState
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
