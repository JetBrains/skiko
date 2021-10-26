package org.jetbrains.skiko.tests

import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.NativePointer
import java.nio.ByteBuffer

actual fun runTest(block: suspend () -> Unit) {
    runBlocking { block() }
}

actual fun InteropScope.allocateBytesForPixels(size: Int): NativePointer {
    return TestHelpers().getPointerFromByteBuffer(ByteBuffer.allocateDirect(size))
}
