package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

/**
 * A utility proxy base class for implementing draw/paint filters.
 */
abstract class PaintFilterCanvas(private val canvas: Canvas, unrollDrawable: Boolean) :
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
        doInit(_ptr)
        Stats.onNativeCall()
        reachabilityBarrier(canvas)
    }
}

internal expect fun PaintFilterCanvas.doInit(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_PaintFilterCanvas__1nMake")
@ExternalCode("Module['asm']['org_jetbrains_skia_PaintFilterCanvas__1nMake']")
private external fun PaintFilterCanvas_nMake(canvasPtr: NativePointer, unrollDrawable: @InteropType Boolean): NativePointer

// Native/JS only

@ExternalSymbolName("org_jetbrains_skia_PaintFilterCanvas__1nInit")
@ExternalCode("Module['asm']['org_jetbrains_skia_PaintFilterCanvas__1nInit']")
internal external fun PaintFilterCanvas_nInit(ptr: NativePointer, onFilter: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_PaintFilterCanvas__1nGetOnFilterPaint")
@ExternalCode("Module['asm']['org_jetbrains_skia_PaintFilterCanvas__1nGetOnFilterPaint']")
internal external fun PaintFilterCanvas_nGetOnFilterPaint(ptr: NativePointer): NativePointer
