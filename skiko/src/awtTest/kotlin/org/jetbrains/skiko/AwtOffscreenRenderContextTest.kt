@file:OptIn(ExperimentalSkikoApi::class)

package org.jetbrains.skiko

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.junit.Assume.assumeTrue
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * End-to-end coverage for the genuinely view-less public AWT offscreen factory:
 * `RenderContext.createOffscreen(...)` → [RenderContext.acquireSurface] → caller draws → [RenderContext.present]
 * → read pixels back.
 *
 * Runs **headless** (no AWT window, no UI harness): the offscreen contexts have no on-screen peer, which is the
 * whole point of the view-less path — the GPU backends render into an offscreen texture built from an
 * adapter/offscreen device, not from an AWT window/JAWT surface. The software round-trip runs on every host; the
 * Metal round-trip is gated to macOS (the locally-runnable GPU backend here).
 */
class AwtOffscreenRenderContextTest {

    @Test
    fun `software offscreen context renders content that reads back correctly`() {
        val width = 64
        val height = 48
        val ctx = RenderContext.createOffscreen(width, height, GraphicsApi.SOFTWARE_FAST)
        try {
            assertEquals(GraphicsApi.SOFTWARE_FAST, ctx.graphicsApi)
            assertNull(ctx.directContext, "a software (raster) offscreen context has no Ganesh DirectContext")

            assertRendersQuadrant(ctx, width, height)
        } finally {
            ctx.close()
        }
    }

    @Test
    fun `metal offscreen context renders content that reads back correctly`() {
        assumeTrue("Metal offscreen rendering is macOS-only", hostOs == OS.MacOS)
        val width = 64
        val height = 48
        val ctx = RenderContext.createOffscreen(width, height, GraphicsApi.METAL)
        try {
            assertEquals(GraphicsApi.METAL, ctx.graphicsApi)
            assertNotNull(ctx.directContext, "a Metal offscreen context has a live Ganesh DirectContext")

            assertRendersQuadrant(ctx, width, height)
        } finally {
            ctx.close()
        }
    }

    @Test
    fun `apiPreference falls through a backend unavailable on this host to software`() {
        // A GPU api for a different OS cannot initialise here, so the chain must fall through to software.
        val foreignGpuApi = if (hostOs == OS.MacOS) GraphicsApi.DIRECT3D else GraphicsApi.METAL
        val ctx = RenderContext.createOffscreen(32, 32, apiPreference = listOf(foreignGpuApi, GraphicsApi.SOFTWARE_FAST))
        try {
            assertEquals(GraphicsApi.SOFTWARE_FAST, ctx.graphicsApi)
        } finally {
            ctx.close()
        }
    }

    @Test
    fun `requesting a backend unavailable on this host directly is rejected`() {
        val foreignGpuApi = if (hostOs == OS.MacOS) GraphicsApi.DIRECT3D else GraphicsApi.METAL
        assertFailsWith<RenderException> {
            RenderContext.createOffscreen(32, 32, foreignGpuApi)
        }
    }

    @Test
    fun `acquireSurface resizes the offscreen surface`() {
        val ctx = RenderContext.createOffscreen(16, 16, GraphicsApi.SOFTWARE_FAST)
        try {
            val small = ctx.acquireSurface(16, 16)
            assertEquals(16, small.width)
            assertEquals(16, small.height)
            val large = ctx.acquireSurface(128, 64)
            assertEquals(128, large.width)
            assertEquals(64, large.height)
        } finally {
            ctx.close()
        }
    }

    /**
     * Draws a green top-left quadrant over a red clear, presents, and asserts the two colors read back off the
     * surface. Shared by the software and Metal round-trips so both drive the identical acquire→draw→present→read
     * contract.
     */
    private fun assertRendersQuadrant(ctx: RenderContext, width: Int, height: Int) {
        val surface = ctx.acquireSurface(width, height)
        surface.canvas.clear(Color.RED)
        surface.canvas.drawRect(
            Rect.makeXYWH(0f, 0f, width / 2f, height / 2f),
            Paint().apply { color = Color.GREEN }
        )
        ctx.present()

        val bitmap = Bitmap().apply { allocN32Pixels(width, height, opaque = false) }
        assertTrue(surface.readPixels(bitmap, 0, 0), "readPixels off the offscreen surface should succeed")

        assertEquals(Color.GREEN, bitmap.getColor(4, 4), "top-left quadrant should be the drawn green")
        assertEquals(Color.RED, bitmap.getColor(width - 4, height - 4), "bottom-right should be the red clear")
    }
}
