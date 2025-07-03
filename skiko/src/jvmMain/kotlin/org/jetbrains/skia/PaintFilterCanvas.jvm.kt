package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer

internal actual fun PaintFilterCanvas.doInit(ptr: NativePointer) {
    PaintFilterCanvas_nInit(this, ptr)
}