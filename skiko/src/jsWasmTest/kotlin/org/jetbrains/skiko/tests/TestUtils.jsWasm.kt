package org.jetbrains.skiko.tests

import org.jetbrains.skia.Data
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.NativePointer

internal actual fun InteropScope.allocateBytesForPixels(size: Int): NativePointer {
    return toInterop(ByteArray(size))
}

actual annotation class SkipJvmTarget

actual annotation class SkipNativeTarget

actual fun makeFromFileName(path: String?): Data = Data(0)

actual val isDebugModeOnJvm: Boolean = false