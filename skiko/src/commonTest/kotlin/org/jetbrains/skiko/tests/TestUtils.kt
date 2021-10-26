package org.jetbrains.skiko.tests

import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.NativePointer

expect fun runTest(block: suspend () -> Unit)

expect fun InteropScope.allocateBytesForPixels(size: Int): NativePointer
