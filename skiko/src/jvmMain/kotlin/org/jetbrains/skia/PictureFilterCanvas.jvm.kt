package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer

internal actual fun PictureFilterCanvas.doInit(ptr: NativePointer) {
    PictureFilterCanvas_nInit(this, ptr)
}

private external fun PictureFilterCanvas_nInit(thisPtr: PictureFilterCanvas, canvasPtr: NativePointer)
