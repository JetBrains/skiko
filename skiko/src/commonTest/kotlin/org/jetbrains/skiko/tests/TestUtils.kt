package org.jetbrains.skiko.tests

import org.jetbrains.skia.Data
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.NativePointer

expect fun runTest(block: suspend () -> Unit)

internal expect fun InteropScope.allocateBytesForPixels(size: Int): NativePointer

expect annotation class SkipNativeTarget

expect annotation class SkipJsTarget

expect annotation class SkipJvmTarget

expect fun makeFromFileName(path: String?): Data

expect val isDebugModeOnJvm: Boolean
