package org.jetbrains.skia.skottie

import org.jetbrains.skia.impl.NativePointer

internal actual fun Logger.doInit(ptr: NativePointer) {
    _nInit(this, ptr)
}

private external fun _nInit(thisPtr: Logger, ptr: NativePointer)