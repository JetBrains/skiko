package org.jetbrains.skiko

import org.jetbrains.skia.DirectContext
import org.jetbrains.skiko.util.joinThreadCatching
import kotlin.test.*

class DirectContextTest {
    @Test
    fun makeGL() {
        // use a new thread to be sure that there is no OpenGL context in it
        joinThreadCatching {
            // Test only that the method doesn't crash VM, and only throw an exception.
            // To properly test it, we have to use third-party created OpenGL context.
            // Testing with our own created OpenGL context won't be fair, as these functions can do additional
            // initialization (i.e. if we call our own glMakeCurrent, we have to call `loadOpenGLLibrary`)
            var actualException: RenderException? = null
            try {
                DirectContext.makeGL()
            } catch (e: RenderException) {
                actualException = e
            }
            assertTrue(actualException != null, "makeGL didn't fail")
        }
    }
}

