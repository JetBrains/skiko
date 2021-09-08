package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer

actual fun FilterMipmap._actualPack(): NativePointer {
    return filterMode.ordinal shl 32 or mipmapMode.ordinal
}