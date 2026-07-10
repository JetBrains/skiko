@file:OptIn(ExperimentalSkikoApi::class)

package org.jetbrains.skiko

import kotlinx.coroutines.delay
import org.jetbrains.skia.BackendTexture
import org.jetbrains.skiko.redrawer.AWTRedrawer
import org.jetbrains.skiko.redrawer.OnScreenRedrawer
import org.jetbrains.skiko.util.UiTestWindow
import org.jetbrains.skiko.util.uiTest
import org.junit.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * End-to-end coverage for the GPU texture-interop seam on [RenderContext]:
 *
 *  * [BackendTexture.makeMetal] / [BackendTexture.makeDirect3D] argument validation — runs everywhere,
 *    including headless, because the `require(...)` checks fire before any native call.
 *  * The per-backend GPU-handle accessors ([metalDevicePointer], [metalCommandQueuePointer],
 *    [direct3DDevicePointer], …) against a **live** on-screen [RenderContext] — a real GPU test, gated on the
 *    `skiko.test.ui.enabled` UI harness like the other on-screen tests.
 *
 * A full external-texture round-trip (allocate an `MTLTexture`, fill it, wrap with [BackendTexture.makeMetal],
 * [org.jetbrains.skia.Image.adoptTextureFrom], draw, pixel-check) needs to *allocate* a Metal/D3D texture on
 * the exposed device. That requires calling the platform graphics API directly, for which there is no
 * pure-JVM binding in skiko — it can only be driven from the Kotlin/Native (`darwinMain`) side. That genuine
 * round-trip lives in `RenderContextMetalInteropTest` in the `macosTest` source set (Kotlin/Native), not here.
 * On the JVM we therefore exercise the wrap path up to native entry (validation) and assert the live
 * device/queue the round-trip would target are real, non-null handles.
 */
class RenderContextGpuInteropTest {

    // --- Argument validation (headless-safe): the require(...) checks precede any native call. ---

    @Test
    fun `makeMetal rejects a null texture pointer`() {
        assertFailsWith<IllegalArgumentException> {
            BackendTexture.makeMetal(width = 64, height = 64, mtlTexturePtr = 0L)
        }
    }

    @Test
    fun `makeMetal rejects non-positive dimensions`() {
        // A non-null placeholder pointer so the width/height checks are what fire (native is never reached).
        val placeholder = 1L
        assertFailsWith<IllegalArgumentException> {
            BackendTexture.makeMetal(width = 0, height = 64, mtlTexturePtr = placeholder)
        }
        assertFailsWith<IllegalArgumentException> {
            BackendTexture.makeMetal(width = 64, height = -1, mtlTexturePtr = placeholder)
        }
    }

    @Test
    fun `makeDirect3D rejects a null texture pointer`() {
        assertFailsWith<IllegalArgumentException> {
            BackendTexture.makeDirect3D(width = 64, height = 64, texturePtr = 0L, format = 0)
        }
    }

    @Test
    fun `makeDirect3D rejects non-positive dimensions`() {
        val placeholder = 1L
        assertFailsWith<IllegalArgumentException> {
            BackendTexture.makeDirect3D(width = -1, height = 64, texturePtr = placeholder, format = 0)
        }
    }

    // --- Live GPU-handle accessors against an on-screen RenderContext (real GPU; UI harness gated). ---

    @Test(timeout = 60_000)
    fun `on-screen RenderContext exposes live per-backend GPU handles`() = uiTest {
        // Software rendering has no GPU device/queue to expose.
        if (renderApi == GraphicsApi.SOFTWARE_COMPAT || renderApi == GraphicsApi.SOFTWARE_FAST) return@uiTest

        val window = UiTestWindow(properties = SkiaLayerProperties(renderApi = renderApi))
        try {
            window.layer.renderDelegate = SkikoRenderDelegate { canvas, _, _, _ ->
                canvas.clear(org.jetbrains.skia.Color.RED)
            }
            window.setLocation(60, 60)
            window.setSize(220, 220)
            window.isVisible = true

            // Drive frames until the lazily-created GPU context is live.
            var ctx: RenderContext? = null
            for (i in 0 until 60) {
                window.layer.needRedraw()
                delay(50)
                val candidate = (window.layer.redrawer as? OnScreenRedrawer)?.renderer
                if (candidate is AWTRedrawer && candidate.directContext != null) {
                    ctx = candidate
                    break
                }
            }
            val renderContext = assertNotNull(ctx, "on-screen SkiaLayer never exposed a live DirectContext")
            assertTrue(
                renderContext.graphicsApi == renderApi,
                "graphicsApi should match the layer's render API"
            )
            assertNotNull(renderContext.directContext, "a GPU RenderContext should expose a DirectContext")

            when (renderApi) {
                GraphicsApi.METAL -> {
                    val device = renderContext.metalDevicePointer
                    assertTrue(device != null && device != 0L, "metalDevicePointer should be a real id<MTLDevice>")
                    val queue = renderContext.metalCommandQueuePointer
                    assertTrue(
                        queue != null && queue != 0L,
                        "metalCommandQueuePointer should be a real id<MTLCommandQueue>"
                    )
                    // Non-Metal accessors must return null (never throw / never a stale handle).
                    assertTrue(renderContext.direct3DDevicePointer == null, "D3D device pointer must be null on Metal")
                }
                GraphicsApi.DIRECT3D -> {
                    // Reached only when the UI harness selects Direct3D: a Windows host with a live D3D12 device.
                    val device = renderContext.direct3DDevicePointer
                    assertTrue(device != null && device != 0L, "direct3DDevicePointer should be a real ID3D12Device")
                    val adapter = renderContext.direct3DAdapterPointer
                    assertTrue(adapter != null && adapter != 0L, "direct3DAdapterPointer should be a real IDXGIAdapter1")
                    val queue = renderContext.direct3DQueuePointer
                    assertTrue(queue != null && queue != 0L, "direct3DQueuePointer should be a real ID3D12CommandQueue")
                    assertTrue(renderContext.metalDevicePointer == null, "Metal device pointer must be null on D3D")
                }
                else -> {
                    // OpenGL/ANGLE on the JVM: no separately-passable device/queue; only directContext matters.
                    assertTrue(renderContext.metalDevicePointer == null, "Metal device pointer must be null off-Metal")
                    assertTrue(renderContext.direct3DDevicePointer == null, "D3D device pointer must be null off-D3D")
                }
            }
        } finally {
            window.isVisible = false
            window.dispose()
        }
    }
}
