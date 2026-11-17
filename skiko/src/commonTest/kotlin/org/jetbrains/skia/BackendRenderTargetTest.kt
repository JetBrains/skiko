package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BackendRenderTargetTest {
    @Test
    fun glFramebufferWrapperCanBeCreatedWithoutCurrentContext() = runTest {
        val target = BackendRenderTarget.makeGL(8, 8, 1, 8, 0, 0x8058)
        target.use {
            assertFalse(it.isClosed)
        }
        assertTrue(target.isClosed)
    }
}
