package org.jetbrains.skiko.tests

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jetbrains.skia.Data
import org.jetbrains.skia.impl.BufferUtil
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.makeFromFileName
import java.nio.ByteBuffer

@OptIn(ExperimentalCoroutinesApi::class)
actual fun runTest(block: suspend () -> Unit) = kotlinx.coroutines.test.runTest { block() }

internal actual fun InteropScope.allocateBytesForPixels(size: Int): NativePointer {
    return BufferUtil.getPointerFromByteBuffer(ByteBuffer.allocateDirect(size))
}

actual annotation class SkipJsTarget

actual typealias SkipJvmTarget = org.junit.Ignore

actual annotation class SkipNativeTarget

actual fun makeFromFileName(path: String?): Data = Data.Companion.makeFromFileName(path)
