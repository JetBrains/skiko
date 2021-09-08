package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer

actual fun toIPoint(p: NativePointer): IPoint = IPoint((p ushr 32).toInt(), (p and -1).toInt())
