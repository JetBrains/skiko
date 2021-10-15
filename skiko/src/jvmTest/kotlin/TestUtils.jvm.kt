package org.jetbrains.skiko.tests

import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.SkikoByteBuffer
import java.nio.ByteBuffer

actual typealias IgnoreTestOnJvm = org.junit.Ignore

actual fun runTest(block: suspend () -> Unit) {
    runBlocking { block() }
}

actual fun InteropScope.allocateBytesForPixels(size: Int): NativePointer {
    return TestHelpers().getPointerFromByteBuffer(SkikoByteBuffer(ByteBuffer.allocateDirect(size)))
}
