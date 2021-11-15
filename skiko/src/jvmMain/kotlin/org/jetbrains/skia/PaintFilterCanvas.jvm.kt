package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer

internal actual fun PaintFilterCanvas.doInit(ptr: NativePointer) {
    PaintFilterCanvas_nInit(this, ptr)
}

private external fun PaintFilterCanvas_nInit(thisPtr: PaintFilterCanvas, canvasPtr: NativePointer)
