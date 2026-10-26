package org.jetbrains.skia

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.objcPtr
import org.jetbrains.skia.gpu.ganesh.adoptTextureFrom
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import platform.Metal.MTLCreateSystemDefaultDevice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class GaneshImageTestDarwin {
    @OptIn(ExperimentalForeignApi::class)
    @Test
    fun cannotAdoptGlTextureWithMetalContext() {
        if (hostOs == OS.Tvos) return

        val device = assertNotNull(MTLCreateSystemDefaultDevice())
        val queue = assertNotNull(device.newCommandQueue())
        DirectContext.makeMetal(device.objcPtr(), queue.objcPtr()).use { context ->
            BackendTexture.makeGL(
                width = 1,
                height = 1,
                isMipmapped = false,
                textureId = 1,
                textureTarget = 0x0DE1,
                textureFormat = 0x8058
            ).use { texture ->
                val opaqueFailure = assertFailsWith<RuntimeException> {
                    Image.adoptTextureFrom(context, texture, SurfaceOrigin.TOP_LEFT, ColorType.RGBA_8888)
                }
                assertEquals("Failed to Image::makeFromTexture", opaqueFailure.message)
                val premulFailure = assertFailsWith<RuntimeException> {
                    Image.adoptTextureFrom(
                        context, texture, SurfaceOrigin.TOP_LEFT, ColorType.RGBA_8888, ColorAlphaType.PREMUL
                    )
                }
                assertEquals("Failed to Image::makeFromTexture", premulFailure.message)
            }
        }
    }
}
