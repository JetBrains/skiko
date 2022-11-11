package org.jetbrains.skiko

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class GraphicsApiTest {
    @Test
    fun `check that device is supported`() {
        assertTrue { isVideoCardSupported(GraphicsApi.METAL, OS.MacOS, "AMD Radeon Pro 5500M") }
    }

    @Test
    fun `check that device is not supported`() {
        assertFalse { isVideoCardSupported(GraphicsApi.DIRECT3D, OS.Windows, "NVIDIA Quadro M2000M") }
    }

    @Test
    fun `parseNotSupportedAdapter test`() {
        assertEquals(NotSupportedAdapter(os = OS.Windows,
                                         api = GraphicsApi.OPENGL,
                                         name = "Intel(R) HD Graphics 2000"),
                     parseNotSupportedAdapter("windows:opengl:Intel(R) HD Graphics 2000"))
    }
}