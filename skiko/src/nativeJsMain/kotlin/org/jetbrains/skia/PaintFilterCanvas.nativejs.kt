package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.interopScope

internal actual fun PaintFilterCanvas.doInit(ptr: NativePointer) {
    interopScope {
        val onFilter = virtualBoolean {
            onFilter(PaintFilterCanvas_nGetOnFilterPaint(ptr))
        }
        PaintFilterCanvas_nInit(ptr, onFilter)
    }
}
