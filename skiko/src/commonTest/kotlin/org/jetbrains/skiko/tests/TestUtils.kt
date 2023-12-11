package org.jetbrains.skiko.tests

import org.jetbrains.skia.Data
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.NativePointer

expect class TestReturnType

expect fun runTest(block: suspend () -> Unit): TestReturnType

internal expect fun InteropScope.allocateBytesForPixels(size: Int): NativePointer

expect annotation class SkipNativeTarget

expect annotation class SkipJsTarget

expect annotation class SkipWasmTarget

expect annotation class SkipJvmTarget

expect fun makeFromFileName(path: String?): Data

expect val isDebugModeOnJvm: Boolean
