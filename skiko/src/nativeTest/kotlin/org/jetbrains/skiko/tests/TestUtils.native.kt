package org.jetbrains.skiko.tests

import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.NativePointer

actual fun runTest(block: suspend () -> Unit) {
    runBlocking { block() }
}

actual fun InteropScope.allocateBytesForPixels(size: Int): NativePointer {
    return toInterop(ByteArray(size))
}

actual annotation class SkipJsTarget

actual annotation class SkipJvmTarget

actual typealias SkipNativeTarget = kotlin.test.Ignore
