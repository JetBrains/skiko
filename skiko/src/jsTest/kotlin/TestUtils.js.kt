package org.jetbrains.skiko.tests

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import org.jetbrains.skia.Data
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.wasm.await
import org.jetbrains.skiko.wasm.wasmSetup

actual fun runTest(block: suspend () -> Unit): dynamic = GlobalScope.promise {
    wasmSetup.await()
    block()
}

internal actual fun InteropScope.allocateBytesForPixels(size: Int): NativePointer {
    return toInterop(ByteArray(size))
}

actual typealias SkipJsTarget = kotlin.test.Ignore

actual annotation class SkipJvmTarget

actual annotation class SkipNativeTarget

actual fun makeFromFileName(path: String?): Data = Data(0)
