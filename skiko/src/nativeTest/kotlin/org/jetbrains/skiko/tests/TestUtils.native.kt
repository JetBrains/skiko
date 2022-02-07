package org.jetbrains.skiko.tests

import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.Data
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.makeFromFileName

actual fun runTest(block: suspend () -> Unit) {
    runBlocking { block() }
}

internal actual fun InteropScope.allocateBytesForPixels(size: Int): NativePointer {
    return toInterop(ByteArray(size))
}

actual annotation class SkipJsTarget

actual annotation class SkipJvmTarget

actual typealias SkipNativeTarget = kotlin.test.Ignore

actual fun makeFromFileName(path: String?): Data = Data.Companion.makeFromFileName(path)