package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.interopScope

internal actual fun Drawable.doInit(ptr: NativePointer) {
    Stats.onNativeCall()
    interopScope {
        val onDrawCallback = virtual {
            onDraw(Canvas(Drawable_nGetOnDrawCanvas(_ptr), false, this))
        }
        val onGetBoundsCallback = virtual {
            val bounds = onGetBounds()
            Drawable_nSetBounds(_ptr, bounds.left, bounds.top, bounds.right, bounds.bottom)
        }
        Drawable_nInit(_ptr, onGetBoundsCallback, onDrawCallback)
    }
}
