package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer

actual fun CubicResampler._actualPack(): NativePointer {
    return ((b.toBits() shl 32) or c.toBits())
}
