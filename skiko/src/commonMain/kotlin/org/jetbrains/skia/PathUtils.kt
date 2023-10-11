package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier

object PathUtils {

    /**
     * Returns the filled equivalent of the stroked path using the provided paint attributes.
     *
     * @param src       Path to create a filled version of.
     * @param paint     Paint from which attributes such as stroke cap, width, miter, join, and
     *                  pathEffect will be used.
     * @param cull      Optional limit passed to the path effect.
     * @param resScale  If &gt; 1, increase precision, else if (0 &lt; resScale &lt; 1) reduce precision
     *                  to favor speed and size.
     * @return          A filled version of the source path.
     */
    fun fillPathWithPaint(src: Path, paint: Paint, cull: Rect?, resScale: Float): Path {
        return fillPathWithPaint(src, paint, cull, Matrix33.makeScale(resScale))
    }

    /**
     * Returns the filled equivalent of the stroked path using the provided paint attributes.
     *
     * @param src       Path to create a filled version of.
     * @param paint     Paint from which attributes such as stroke cap, width, miter, join, and
     *                  pathEffect will be used.
     * @param cull      Optional limit passed to the path effect.
     * @param matrix    Current transformation matrix.
     * @return          A filled version of the source path.
     */
    fun fillPathWithPaint(src: Path, paint: Paint, cull: Rect?, matrix: Matrix33): Path {
        return try {
            Stats.onNativeCall()
            if (cull == null) org.jetbrains.skia.Path(
                interopScope {
                    _nFillPathWithPaint(
                        getPtr(src),
                        getPtr(paint),
                        toInterop(matrix.mat)
                    )
                }
            ) else org.jetbrains.skia.Path(
                interopScope {
                    _nFillPathWithPaintCull(
                        getPtr(src),
                        getPtr(paint),
                        cull.left,
                        cull.top,
                        cull.right,
                        cull.bottom,
                        toInterop(matrix.mat)
                    )
                }
            )
        } finally {
            reachabilityBarrier(src)
            reachabilityBarrier(paint)
        }
    }

    /**
     * Returns the filled equivalent of the stroked path using the provided paint attributes.
     *
     * @param src   Path to create a filled version of.
     * @param paint Paint attributes such as stroke cap, width, miter, join, and pathEffect.
     * @return      A filled version of the source path.
     */
    fun fillPathWithPaint(src: Path, paint: Paint): Path {
        return fillPathWithPaint(src, paint, null, 1f)
    }

    init {
        staticLoad()
    }
}

@ExternalSymbolName("org_jetbrains_skia_PathUtils__1nFillPathWithPaint")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PathUtils__1nFillPathWithPaint")
private external fun _nFillPathWithPaint(
    srcPtr: NativePointer,
    paintPtr: NativePointer,
    matrix: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathUtils__1nFillPathWithPaintCull")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PathUtils__1nFillPathWithPaintCull")
private external fun _nFillPathWithPaintCull(
    srcPtr: NativePointer,
    paintPtr: NativePointer,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    matrix: InteropPointer
): NativePointer
