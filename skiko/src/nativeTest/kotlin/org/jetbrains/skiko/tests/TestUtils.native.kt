package org.jetbrains.skiko.tests

import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.NativePointer

annotation class DoNothing
actual typealias IgnoreTestOnJvm = DoNothing

actual fun runTest(block: suspend () -> Unit) {
    runBlocking { block() }
}

actual fun InteropScope.allocateBytesForPixels(size: Int): NativePointer {
    return toInterop(ByteArray(size))
}
