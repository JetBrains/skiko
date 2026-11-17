package org.jetbrains.skia

import kotlinx.cinterop.*
import org.jetbrains.skia.impl.Native.Companion.NullPointer
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import platform.Metal.MTLCreateSystemDefaultDevice
import platform.Metal.MTLDeviceProtocol
import platform.posix.RTLD_NOW
import platform.posix.dlclose
import platform.posix.dlopen
import platform.posix.dlsym
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalForeignApi::class)
class DirectContextDarwinTest {
    @Test
    fun metalContextFlushSubmitAndResetPreservePixels() {
        withTestMetalContext { _, context ->
            context.resourceCacheLimit = 4L * 1024 * 1024
            assertEquals(4L * 1024 * 1024, context.resourceCacheLimit)
            Surface.makeRenderTarget(context, false, ImageInfo.makeN32Premul(8, 8)).use { surface ->
                surface.canvas.clear(Color.RED)
                assertSame(context, context.flush(surface))
                context.submit(true)
                assertSurfaceColor(surface, Color.RED)

                assertSame(context, context.resetAll())
                assertSame(context, context.resetGLAll())
                surface.canvas.clear(Color.BLUE)
                assertSame(context, context.flush())
                context.submit(true)
                assertSurfaceColor(surface, Color.BLUE)

                surface.canvas.clear(Color.GREEN)
                context.flushAndSubmit(surface, true)
                assertSurfaceColor(surface, Color.GREEN)
            }
            context.abandon()
            assertFailsWith<IllegalArgumentException> {
                Surface.makeRenderTarget(context, false, ImageInfo.makeN32Premul(8, 8))
            }
        }
    }

    @Test
    fun glContextWithCurrentNativeContextPreservesPixels() {
        if (hostOs != OS.MacOS) return
        withTestGlContext {
            DirectContext.makeGL().use { context ->
                Surface.makeRenderTarget(context, false, ImageInfo.makeN32Premul(8, 8)).use { surface ->
                    surface.canvas.clear(Color.RED)
                    context.flushAndSubmit(surface, true)
                    assertSurfaceColor(surface, Color.RED)
                }
            }
        }
    }

    @Test
    fun direct3DIsUnavailableOnDarwin() {
        val failure = assertFailsWith<RuntimeException> {
            DirectContext.makeDirect3D(NullPointer, NullPointer, NullPointer)
        }
        assertEquals("Can't wrap nullptr", failure.message)
    }

    private fun assertSurfaceColor(surface: Surface, color: Int) {
        Bitmap().use { bitmap ->
            assertTrue(bitmap.allocN32Pixels(8, 8))
            assertTrue(surface.readPixels(bitmap, 0, 0))
            assertEquals(color, bitmap.getColor(4, 4))
        }
    }

    private fun withTestGlContext(block: () -> Unit) {
        val library = checkNotNull(dlopen("/System/Library/Frameworks/OpenGL.framework/OpenGL", RTLD_NOW))
        try {
            val choosePixelFormat = checkNotNull(dlsym(library, "CGLChoosePixelFormat"))
                .reinterpret<CFunction<(CPointer<IntVar>?, CPointer<COpaquePointerVar>?, CPointer<IntVar>?) -> Int>>()
            val createContext = checkNotNull(dlsym(library, "CGLCreateContext"))
                .reinterpret<CFunction<(COpaquePointer?, COpaquePointer?, CPointer<COpaquePointerVar>?) -> Int>>()
            val getCurrentContext = checkNotNull(dlsym(library, "CGLGetCurrentContext"))
                .reinterpret<CFunction<() -> COpaquePointer?>>()
            val setCurrentContext = checkNotNull(dlsym(library, "CGLSetCurrentContext"))
                .reinterpret<CFunction<(COpaquePointer?) -> Int>>()
            val destroyContext = checkNotNull(dlsym(library, "CGLDestroyContext"))
                .reinterpret<CFunction<(COpaquePointer?) -> Int>>()
            val destroyPixelFormat = checkNotNull(dlsym(library, "CGLDestroyPixelFormat"))
                .reinterpret<CFunction<(COpaquePointer?) -> Int>>()
            memScoped {
                val pixelFormat = alloc<COpaquePointerVar>()
                val pixelFormatCount = alloc<IntVar>()
                val attributes = allocArray<IntVar>(5)
                val rendererId = 70
                val genericFloatRenderer = 0x00020400
                val openGlProfile = 99
                val version32Core = 0x3200
                attributes[0] = rendererId
                attributes[1] = genericFloatRenderer
                attributes[2] = openGlProfile
                attributes[3] = version32Core
                attributes[4] = 0
                assertEquals(0, choosePixelFormat(attributes, pixelFormat.ptr, pixelFormatCount.ptr))
                val format = checkNotNull(pixelFormat.value)
                try {
                    val nativeContext = alloc<COpaquePointerVar>()
                    assertEquals(0, createContext(format, null, nativeContext.ptr))
                    val context = checkNotNull(nativeContext.value)
                    try {
                        val previousContext = getCurrentContext()
                        try {
                            assertEquals(0, setCurrentContext(context))
                            block()
                        } finally {
                            assertEquals(0, setCurrentContext(previousContext))
                        }
                    } finally {
                        assertEquals(0, destroyContext(context))
                    }
                } finally {
                    assertEquals(0, destroyPixelFormat(format))
                }
            }
        } finally {
            dlclose(library)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun withTestMetalContext(block: (MTLDeviceProtocol, DirectContext) -> Unit) {
    if (hostOs == OS.Tvos) return

    val device = checkNotNull(MTLCreateSystemDefaultDevice()) { "Metal is not supported on this system" }
    val queue = checkNotNull(device.newCommandQueue()) { "Failed to create a Metal command queue" }
    DirectContext.makeMetal(device.objcPtr(), queue.objcPtr()).use { context ->
        block(device, context)
    }
}
