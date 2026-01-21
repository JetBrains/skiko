package org.jetbrains.skia.util

import org.jetbrains.skia.impl.NativePointer

internal actual val NativePointer.isNullPointer: Boolean
    get() = this == 0L
