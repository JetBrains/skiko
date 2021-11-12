package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats

internal actual fun Drawable.doInit(ptr: NativePointer) {
    Stats.onNativeCall()
    Drawable_nInit(this, ptr)
}

private external fun Drawable_nInit(thisPtr: Drawable, ptr: NativePointer)