package org.jetbrains.skia.gpu.graphite

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.get
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.reinterpret
import org.jetbrains.skia.Color
import org.jetbrains.skia.Surface
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import platform.Metal.MTLCommandBufferStatusCompleted
import platform.Metal.MTLCreateSystemDefaultDevice
import platform.Metal.MTLOriginMake
import platform.Metal.MTLPixelFormatBGRA8Unorm
import platform.Metal.MTLResourceStorageModeShared
import platform.Metal.MTLSizeMake
import platform.Metal.MTLTextureDescriptor
import platform.Metal.MTLTextureUsageRenderTarget
import platform.Metal.MTLTextureUsageShaderRead
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalForeignApi::class, ExperimentalSkikoApi::class)
class BackendTextureTest {
    @Test
    fun metalTextureCanBeWrappedInSurface() {
        if (hostOs == OS.Tvos) return
        val device = checkNotNull(MTLCreateSystemDefaultDevice()) { "Metal is not supported on this system" }
        val queue = checkNotNull(device.newCommandQueue())
        val descriptor = MTLTextureDescriptor.texture2DDescriptorWithPixelFormat(
            MTLPixelFormatBGRA8Unorm, 8uL, 8uL, false
        )
        descriptor.usage = MTLTextureUsageRenderTarget or MTLTextureUsageShaderRead
        val metalTexture = checkNotNull(device.newTextureWithDescriptor(descriptor))
        GraphiteContext.makeMetal(device.objcPtr(), queue.objcPtr()).use { context ->
            context.makeRecorder().use { recorder ->
                val backendTexture = BackendTexture.makeMetal(8, 8, metalTexture.objcPtr())
                backendTexture.use {
                    assertFalse(it.isClosed)
                    assertNotNull(Surface.wrapBackendTexture(recorder, it, null)).use { surface ->
                        assertEquals(8, surface.width)
                        assertEquals(8, surface.height)
                        surface.canvas.clear(Color.RED)
                        recorder.snap().use { recording ->
                            context.insertRecording(recording)
                            context.submit(syncCpu = true)
                        }
                        assertFalse(surface.isClosed)
                        val readback = checkNotNull(device.newBufferWithLength(2048uL, MTLResourceStorageModeShared))
                        val commandBuffer = checkNotNull(queue.commandBuffer())
                        val blit = checkNotNull(commandBuffer.blitCommandEncoder())
                        blit.copyFromTexture(
                            metalTexture, 0uL, 0uL, MTLOriginMake(0uL, 0uL, 0uL), MTLSizeMake(8uL, 8uL, 1uL),
                            readback, 0uL, 256uL, 2048uL
                        )
                        blit.endEncoding()
                        commandBuffer.commit()
                        commandBuffer.waitUntilCompleted()
                        assertEquals(MTLCommandBufferStatusCompleted, commandBuffer.status)
                        val pixels = checkNotNull(readback.contents()).reinterpret<UByteVar>()
                        val center = 4 * 256 + 4 * 4
                        assertEquals(0, pixels[center].toInt())
                        assertEquals(0, pixels[center + 1].toInt())
                        assertEquals(255, pixels[center + 2].toInt())
                        assertEquals(255, pixels[center + 3].toInt())
                    }
                }
                assertTrue(backendTexture.isClosed)
            }
        }
    }
}
