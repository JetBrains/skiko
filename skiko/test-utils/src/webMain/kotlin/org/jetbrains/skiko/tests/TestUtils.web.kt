package org.jetbrains.skiko.tests

import org.jetbrains.skia.Data
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.InternalSkikoApi

@OptIn(InternalSkikoApi::class)
actual fun InteropScope.allocateBytesForPixels(size: Int): NativePointer {
    return toInterop(ByteArray(size))
}

actual annotation class SkipJvmTarget

actual annotation class SkipNativeTarget

@OptIn(InternalSkikoApi::class)
actual fun makeFromFileName(path: String?): Data = Data(0)

actual val isDebugModeOnJvm: Boolean = false

actual typealias TestReturnType = Any
