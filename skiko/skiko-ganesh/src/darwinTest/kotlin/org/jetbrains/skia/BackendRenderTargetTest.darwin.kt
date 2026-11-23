package org.jetbrains.skia

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.objcPtr
import org.jetbrains.skia.impl.Native.Companion.NullPointer
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.gpu.ganesh.makeFromBackendRenderTarget
import platform.Metal.MTLDeviceProtocol
import platform.Metal.MTLPixelFormatBGRA8Unorm
import platform.Metal.MTLTextureDescriptor
import platform.Metal.MTLTextureUsageRenderTarget
import platform.Metal.MTLTextureUsageShaderRead
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalForeignApi::class)
class BackendRenderTargetDarwinTest {
    @Test
    fun metalRenderTargetCanBackASurface() {
        withTestMetalContext { device, context ->
            withTestMetalRenderTarget(device) { target ->
                assertFalse(target.isClosed)
                assertNotNull(Surface.makeFromBackendRenderTarget(
                    context, target, SurfaceOrigin.TOP_LEFT, SurfaceColorFormat.BGRA_8888, null
                )).use { surface ->
                    assertEquals(8, surface.width)
                    assertEquals(8, surface.height)
                    surface.canvas.clear(Color.RED)
                    context.flushAndSubmit(surface, true)
                    Bitmap().use { bitmap ->
                        assertTrue(bitmap.allocN32Pixels(8, 8))
                        assertTrue(surface.readPixels(bitmap, 0, 0))
                        assertEquals(Color.RED, bitmap.getColor(4, 4))
                    }
                }
            }
        }
    }

    @Test
    fun direct3DRenderTargetIsUnavailableOnDarwin() {
        val failure = assertFailsWith<RuntimeException> {
            BackendRenderTarget.makeDirect3D(8, 8, NullPointer, 0, 1, 1)
        }
        assertEquals("Can't wrap nullptr", failure.message)
    }

}

@OptIn(ExperimentalForeignApi::class)
internal fun withTestMetalRenderTarget(device: MTLDeviceProtocol, block: (BackendRenderTarget) -> Unit) {
    val descriptor = MTLTextureDescriptor.texture2DDescriptorWithPixelFormat(
        MTLPixelFormatBGRA8Unorm, 8uL, 8uL, false
    )
    descriptor.usage = MTLTextureUsageRenderTarget or MTLTextureUsageShaderRead
    val texture = checkNotNull(device.newTextureWithDescriptor(descriptor))
    val target = BackendRenderTarget.makeMetal(8, 8, texture.objcPtr())
    target.use(block)
    assertTrue(target.isClosed)
}
