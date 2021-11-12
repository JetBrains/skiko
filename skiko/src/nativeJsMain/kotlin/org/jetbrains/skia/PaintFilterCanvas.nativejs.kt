package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.interopScope

internal actual fun PaintFilterCanvas.doInit(ptr: NativePointer) {
    interopScope {
        val onFilter = virtualBoolean {
            val paint = Paint(PaintFilterCanvas_nGetOnFilterPaint(ptr), false)
            onFilter(paint)
        }
        PaintFilterCanvas_nInit(ptr, onFilter)
    }
}
