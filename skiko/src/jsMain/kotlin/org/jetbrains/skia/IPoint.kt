package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer

actual fun toIPoint(p: NativePointer): IPoint = IPoint((p ushr 32), (p and -1))
