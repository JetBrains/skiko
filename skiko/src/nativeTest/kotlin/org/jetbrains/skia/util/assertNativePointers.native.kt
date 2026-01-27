package org.jetbrains.skia.util

import kotlinx.cinterop.ExperimentalForeignApi
import org.jetbrains.skia.impl.NativePointer

@OptIn(ExperimentalForeignApi::class)
internal actual val NativePointer.isNullPointer: Boolean
    get() = this == NativePointer.NULL
