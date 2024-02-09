package org.jetbrains.skiko

import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skiko.util.joinThreadCatching
import kotlin.test.*

class BackendRenderTargetTest {
    @Test
    fun makeGL() {
        // use a new thread to be sure that there is no OpenGL context in it
        joinThreadCatching {
            try {
                // if OpenGL is supported, this method doesn't fail, even if there is no OpenGL Context in the thread
                BackendRenderTarget.makeGL(100, 100, 1, 8, 0, 0x8058)
            } catch (e: RenderException) {
                // it fails if OpenGL isn't supported at all
            }
        }
    }
}