package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BackendTextureTest {
    @Test
    fun glTextureWrapperCanInvalidateParametersAndClose() = runTest {
        val texture = BackendTexture.makeGL(8, 8, false, 1, 0x0DE1, 0x8058)
        texture.use {
            assertFalse(it.isClosed)
            it.glTextureParametersModified()
            assertFalse(it.isClosed)
        }
        assertTrue(texture.isClosed)
    }
}
