@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier

/**
 * A utility proxy base class for implementing draw/paint filters.
 */
abstract class PaintFilterCanvas(canvas: Canvas, unrollDrawable: Boolean) :
    Canvas(PaintFilterCanvas_nMake(getPtr(canvas), unrollDrawable), true, canvas) {
    companion object {
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
    fun onFilter(paintPtr: NativePointer): Boolean {
        val paint = Paint(paintPtr, false)
        return onFilter(paint)
    }

    /**
     * @param unrollDrawable if needed to filter nested drawable content using this canvas (for drawables there is no paint to filter)
     */
    init {
        Stats.onNativeCall()
        _nAttachToJava(_ptr)
        Stats.onNativeCall()
        reachabilityBarrier(canvas)
    }
}

@ExternalSymbolName("org_jetbrains_skia_PaintFilterCanvas__1nMake")
private external fun PaintFilterCanvas_nMake(canvasPtr: NativePointer, unrollDrawable: Boolean): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PaintFilterCanvas__1nAttachToJava")
external fun _nAttachToJava(canvasPtr: NativePointer)

