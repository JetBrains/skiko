@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class, ExperimentalSkikoApi::class)

package org.jetbrains.skiko

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Color
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.BackendTexture
import org.jetbrains.skia.SurfaceOrigin
import platform.CoreGraphics.CGSizeMake
import platform.Metal.MTLCreateSystemDefaultDevice
import platform.Metal.MTLPixelFormatBGRA8Unorm
import platform.Metal.MTLRegionMake2D
import platform.Metal.MTLStorageModeShared
import platform.Metal.MTLTextureDescriptor
import platform.Metal.MTLTextureUsageShaderRead
import platform.QuartzCore.CAMetalLayer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * A genuine external-texture GPU round-trip for the Metal interop seam, on the darwin /
 * Kotlin-Native path — the only path where a raw `MTLTexture` can be allocated and filled directly from
 * Kotlin (via `platform.Metal` cinterop), with no extra JNI. This is a round-trip the pure-JVM `awtTest`
 * harness cannot do (you can't allocate an `MTLTexture` from the JVM).
 *
 * The flow, end to end:
 *  1. Create a live Metal [RenderContext] via the darwin factory [RenderContext.createFromMetalLayer] over an
 *     offscreen [CAMetalLayer] — this owns a real [org.jetbrains.skia.DirectContext] and exposes the
 *     `MTLDevice` skiko renders on via [RenderContext.metalDevice].
 *  2. Allocate a small `MTLTexture` on **that same device** and fill it, on the CPU, with a known solid
 *     color (opaque red) via `replaceRegion`.
 *  3. Wrap it with [BackendTexture.makeMetal] (passing the raw `id<MTLTexture>` from [objcPtr]) and
 *     [Image.adoptTextureFrom] against the context's `directContext`.
 *  4. Draw the adopted image onto a Surface from the context ([RenderContext.acquireSurface]) and read the
 *     pixels back, asserting the sampled pixel equals the color the texture was filled with — a real
 *     pixel-check, not a `pointer != 0` assertion.
 *
 * **What runs:** this needs a functioning Metal GPU. If [MTLCreateSystemDefaultDevice] returns `null`
 * (no Metal device in the environment), or the layer cannot vend a drawable (`acquireSurface` throws),
 * the test skips with a printed reason instead of failing, so an environment without Metal still passes.
 */
class RenderContextMetalInteropTest {

    private companion object {
        const val WIDTH = 4
        const val HEIGHT = 4
        // MTLPixelFormatBGRA8Unorm stores bytes as B, G, R, A. Opaque red = R=255, A=255.
        val RED_BGRA: UByteArray = UByteArray(WIDTH * HEIGHT * 4).also { buf ->
            for (px in 0 until WIDTH * HEIGHT) {
                val o = px * 4
                buf[o + 0] = 0u   // B
                buf[o + 1] = 0u   // G
                buf[o + 2] = 255u // R
                buf[o + 3] = 255u // A
            }
        }
    }

    @Test
    fun metalExternalTextureRoundTripSamplesTheFilledColor() {
        if (MTLCreateSystemDefaultDevice() == null) {
            println(
                "SKIP metalExternalTextureRoundTripSamplesTheFilledColor: " +
                    "MTLCreateSystemDefaultDevice() returned null (no Metal device in this environment)."
            )
            return
        }

        autoreleasepool {
            val layer = CAMetalLayer().apply {
                drawableSize = CGSizeMake(WIDTH.toDouble(), HEIGHT.toDouble())
            }
            // createFromMetalLayer sets layer.device and owns the MTLDevice/queue + Skia DirectContext.
            val ctx = RenderContext.createFromMetalLayer(layer)
            var image: Image? = null
            var backendTexture: BackendTexture? = null
            try {
                val device = assertNotNull(ctx.metalDevice, "Metal RenderContext must expose its MTLDevice")
                val directContext =
                    assertNotNull(ctx.directContext, "Metal RenderContext must expose a live DirectContext")

                // 1. Allocate a small BGRA8 texture on skiko's own device and fill it red on the CPU.
                val descriptor = MTLTextureDescriptor.texture2DDescriptorWithPixelFormat(
                    pixelFormat = MTLPixelFormatBGRA8Unorm,
                    width = WIDTH.toULong(),
                    height = HEIGHT.toULong(),
                    mipmapped = false
                ).apply {
                    usage = MTLTextureUsageShaderRead
                    storageMode = MTLStorageModeShared // CPU-writable via replaceRegion on Apple Silicon
                }
                val texture = assertNotNull(
                    device.newTextureWithDescriptor(descriptor),
                    "device could not allocate an MTLTexture"
                )
                RED_BGRA.usePinned { pinned ->
                    texture.replaceRegion(
                        region = MTLRegionMake2D(0u, 0u, WIDTH.toULong(), HEIGHT.toULong()),
                        mipmapLevel = 0u,
                        withBytes = pinned.addressOf(0),
                        bytesPerRow = (WIDTH * 4).toULong()
                    )
                }

                // 2. Wrap + adopt the external texture for zero-copy sampling against the context.
                val wrapped = BackendTexture.makeMetal(WIDTH, HEIGHT, texture.objcPtr())
                backendTexture = wrapped
                val adopted = Image.adoptTextureFrom(
                    directContext,
                    wrapped,
                    SurfaceOrigin.TOP_LEFT,
                    ColorType.BGRA_8888
                )
                image = adopted

                // 3. Draw the adopted image onto a real surface from the context and read it back.
                val surface = try {
                    ctx.acquireSurface(WIDTH, HEIGHT)
                } catch (e: RenderException) {
                    println(
                        "SKIP metalExternalTextureRoundTripSamplesTheFilledColor: " +
                            "the CAMetalLayer could not vend a drawable in this environment (${e.message})."
                    )
                    return@autoreleasepool
                }
                surface.canvas.clear(Color.BLACK)
                surface.canvas.drawImage(adopted, 0f, 0f)
                surface.flushAndSubmit()

                val bitmap = Bitmap().apply { allocN32Pixels(WIDTH, HEIGHT, opaque = false) }
                val read = surface.readPixels(bitmap, 0, 0)
                assertTrue(read, "surface.readPixels must succeed for the round-trip pixel check")

                // 4. The whole point: the sampled pixel must equal the color the texture was filled with.
                val sampled = bitmap.getColor(WIDTH / 2, HEIGHT / 2)
                assertEquals(
                    Color.RED,
                    sampled,
                    "Adopted external MTLTexture should sample as the opaque red it was filled with, " +
                        "got 0x${sampled.toUInt().toString(16)}"
                )
                bitmap.close()

                // Keep the MTLTexture alive across all the GPU work above; skiko took its own retain in
                // makeMetal, so releasing our Kotlin reference now would not free it out from under skiko.
                texture.objcPtr()
            } finally {
                // Close in adopt -> wrap order, then the context. makeMetal takes an independent retain on
                // the MTLTexture and releases it when the BackendTexture closes, so there is no double free.
                image?.close()
                backendTexture?.close()
                ctx.close()
            }
        }
    }
}
