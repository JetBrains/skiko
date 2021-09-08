package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer

actual fun toIRange(p: NativePointer): IRange = IRange((p ushr 32), (p and -1))
