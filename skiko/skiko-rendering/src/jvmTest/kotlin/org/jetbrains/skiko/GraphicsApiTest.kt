package org.jetbrains.skiko

import kotlin.test.*


class GraphicsApiTest {
    @Test
    fun `device is supported test`() {
        assertTrue { isVideoCardSupported(GraphicsApi.METAL, OS.MacOS, "AMD Radeon Pro 5500M") }
    }

    @Test
    fun `device is not supported test`() {
        assertFalse { isVideoCardSupported(GraphicsApi.DIRECT3D, OS.Windows, "NVIDIA Quadro M2000M") }
    }

    @Test
    fun `device is not supported by pattern`() {
        assertFalse { isVideoCardSupported(GraphicsApi.OPENGL, OS.Linux, "llvmpipe") }
        assertFalse { isVideoCardSupported(GraphicsApi.OPENGL, OS.Linux, "llvmpipe (LLVM 5.0 256 bits)") }
        assertFalse { isVideoCardSupported(GraphicsApi.OPENGL, OS.Linux, "virgl (Apple M1 Max (Compat))") }
        assertTrue { isVideoCardSupported(GraphicsApi.OPENGL, OS.Linux, "Intel llvmpipe") }
    }
}