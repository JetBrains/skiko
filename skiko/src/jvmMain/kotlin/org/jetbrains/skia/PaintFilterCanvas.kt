package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import java.lang.ref.Reference

/**
 * A utility proxy base class for implementing draw/paint filters.
 */
abstract class PaintFilterCanvas(canvas: Canvas, unrollDrawable: Boolean) :
    Canvas(_nMake(Native.Companion.getPtr(canvas), unrollDrawable), true, canvas) {
    companion object {
        external fun _nMake(canvasPtr: Long, unrollDrawable: Boolean): Long

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
    abstract fun onFilter(paint: Paint): Boolean
    fun onFilter(paintPtr: Long): Boolean {
        val paint = org.jetbrains.skia.Paint(paintPtr, false)
        return onFilter(paint)
    }

    external fun _nAttachToJava(canvasPtr: Long)

    /**
     * @param unrollDrawable if needed to filter nested drawable content using this canvas (for drawables there is no paint to filter)
     */
    init {
        Stats.onNativeCall()
        _nAttachToJava(_ptr)
        Stats.onNativeCall()
        Reference.reachabilityFence(canvas)
    }
}