package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

/**
 * A utility proxy base class for implementing draw/paint filters.
 */
abstract class PaintFilterCanvas(private val canvas: Canvas, unrollDrawable: Boolean) :
    Canvas(makePaintFilterCanvas(canvas, unrollDrawable), true, canvas) {
    private companion object {
        init {
            staticLoad()
        }
    }

    /**
     * Called with the paint that will be used to draw the specified type.
     * The implementation may modify the paint as they wish.
     *
     * The result boolean is used to determine whether the draw op is to be
     * executed (true) or skipped (false).
     *
     * Note: The base implementation calls onFilter() for top-level/explicit paints only.
     */
    protected abstract fun onFilter(paint: Paint): Boolean

    // For JNI call
    fun onFilter(paintPtr: NativePointer): Boolean {
        val paint = Paint(paintPtr, false)
        return onFilter(paint)
    }

    /**
     * @param unrollDrawable if needed to filter nested drawable content using this canvas (for drawables there is no paint to filter)
     */
    init {
        nativeCall { doInit(_ptr) }
    }
}

private fun makePaintFilterCanvas(canvas: Canvas, unrollDrawable: Boolean): NativePointer =
    nativeCallFromStatic(canvas) { canvas ->
        PaintFilterCanvas_nMake(canvas._ptr, unrollDrawable)
    }

internal expect fun PaintFilterCanvas.doInit(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_PaintFilterCanvas__1nMake")
// Needs ForceNativeThreadState: asks nested canvases for their image info recursively, so its time grows with the
// nesting depth.
// Native call path: SkPaintFilterCanvas::onImageInfo -> SkCanvas::imageInfo
@ForceNativeThreadState
private external fun PaintFilterCanvas_nMake(canvasPtr: NativePointer, unrollDrawable: Boolean): NativePointer
