package org.jetbrains.skiko

import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.wasm.GL

internal actual fun makeGLContextCurrent(contextPointer: NativePointer) {
    GL.makeContextCurrent(contextPointer)
}