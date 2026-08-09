@file:OptIn(
    kotlinx.cinterop.ExperimentalForeignApi::class,
    org.jetbrains.skiko.InternalSkikoApi::class,
)

package org.jetbrains.skiko

import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.pointed
import kotlinx.cinterop.value
import org.jetbrains.skia.Color
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skia.Surface
import org.jetbrains.skia.impl.Native.Companion.NullPointer
import org.jetbrains.skia.impl.NativePointer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SkiaLayerLinuxTest {
    @Test
    fun rendersAndSnapshotsWithSoftwareRenderer() {
        val host = SoftwareTestHost()
        val layer =
            SkiaLayer(
                SkiaLayerProperties(
                    isVsyncEnabled = false,
                    renderApi = GraphicsApi.SOFTWARE_FAST,
                )
            )
        var renderCount = 0
        layer.renderDelegate = SkikoRenderDelegate { canvas, width, height, _ ->
            assertEquals(host.drawableWidth, width)
            assertEquals(host.drawableHeight, height)
            canvas.clear(Color.RED)
            renderCount += 1
        }

        layer.attachTo(host)

        assertTrue(layer.hasPendingRender)
        assertTrue(layer.render())
        assertEquals(1, renderCount)
        assertEquals(1, host.presentedFrames)
        assertEquals(GraphicsApi.SOFTWARE_FAST, layer.renderApi)
        assertEquals(PixelGeometry.RGB_H, layer.pixelGeometry)
        assertEquals(1, layer.diagnostics.renderedFrameCount)
        assertFalse(layer.hasPendingRender)
        assertNotNull(layer.snapshot(host.drawableWidth, host.drawableHeight))?.close()

        layer.detach()
        assertEquals(1, host.softwareBeginCount)
        assertEquals(1, host.softwareEndCount)
    }

    @Test
    fun fallsBackToSoftwareWhenOpenGlContextCannotBeCreated() {
        val host = SoftwareTestHost()
        val layer =
            SkiaLayer(
                SkiaLayerProperties(
                    isVsyncEnabled = false,
                    renderApi = GraphicsApi.OPENGL,
                )
            )
        layer.renderDelegate = SkikoRenderDelegate { _, _, _, _ -> }

        layer.attachTo(host)

        assertEquals(GraphicsApi.SOFTWARE_FAST, layer.renderApi)
        assertEquals(1, layer.diagnostics.fallbackCount)
        assertTrue(layer.render())
        assertEquals(1, host.presentedFrames)
        layer.detach()
    }

    @Test
    fun parsesLinuxRendererConfiguration() {
        assertEquals(GraphicsApi.SOFTWARE_FAST, SkikoProperties.parseRenderApi("SOFTWARE"))
        assertEquals(GraphicsApi.SOFTWARE_COMPAT, SkikoProperties.parseRenderApi("software_compat"))
        assertEquals(16L * 1024L * 1024L, SkikoProperties.parseSize("16M"))
        assertEquals(
            listOf(GraphicsApi.SOFTWARE_FAST, GraphicsApi.SOFTWARE_COMPAT),
            SkikoProperties.fallbackRenderApiQueue(GraphicsApi.SOFTWARE_FAST),
        )
    }

    @Test
    fun platformHostCanPublishCurrentSystemTheme() {
        try {
            updateLinuxSystemTheme(SystemTheme.DARK)
            assertEquals(SystemTheme.DARK, currentSystemTheme)
            updateLinuxSystemTheme(SystemTheme.LIGHT)
            assertEquals(SystemTheme.LIGHT, currentSystemTheme)
        } finally {
            updateLinuxSystemTheme(SystemTheme.UNKNOWN)
        }
    }

    @Test
    fun recreatesLostOpenGlContextBeforeRendering() {
        val host = SoftwareTestHost()
        val createdRenderers = mutableListOf<FakeRenderer>()
        linuxLayerRendererFactoryOverride = { api, _, _ ->
            FakeRenderer(api).also(createdRenderers::add)
        }
        try {
            val layer =
                SkiaLayer(
                    SkiaLayerProperties(isVsyncEnabled = false, renderApi = GraphicsApi.OPENGL)
                )
            var delegateCalls = 0
            layer.renderDelegate = SkikoRenderDelegate { _, _, _, _ -> delegateCalls += 1 }
            layer.attachTo(host)
            createdRenderers.single().contextLost = true

            assertTrue(layer.render())

            assertEquals(2, createdRenderers.size)
            assertTrue(createdRenderers.first().closedAfterContextLoss)
            assertEquals(1, createdRenderers.last().renderCount)
            assertEquals(1, delegateCalls)
            assertEquals(1, layer.diagnostics.contextRecoveryCount)
            layer.detach()
        } finally {
            linuxLayerRendererFactoryOverride = null
            createdRenderers.forEach { it.disposeSurface() }
        }
    }

    @Test
    fun persistentOpenGlFailureFallsBackToSoftware() {
        val host = SoftwareTestHost()
        val createdApis = mutableListOf<GraphicsApi>()
        val createdRenderers = mutableListOf<FakeRenderer>()
        linuxLayerRendererFactoryOverride = { api, _, _ ->
            FakeRenderer(api, failRendering = api == GraphicsApi.OPENGL).also {
                createdApis += api
                createdRenderers += it
            }
        }
        try {
            val layer =
                SkiaLayer(
                    SkiaLayerProperties(isVsyncEnabled = false, renderApi = GraphicsApi.OPENGL)
                )
            var delegateCalls = 0
            layer.renderDelegate = SkikoRenderDelegate { _, _, _, _ -> delegateCalls += 1 }
            layer.attachTo(host)

            assertTrue(layer.render())

            assertEquals(
                listOf(GraphicsApi.OPENGL, GraphicsApi.OPENGL, GraphicsApi.SOFTWARE_FAST),
                createdApis,
            )
            assertEquals(GraphicsApi.SOFTWARE_FAST, layer.renderApi)
            assertEquals(1, delegateCalls)
            assertEquals(1, layer.diagnostics.contextRecoveryCount)
            assertEquals(1, layer.diagnostics.fallbackCount)
            layer.detach()
        } finally {
            linuxLayerRendererFactoryOverride = null
            createdRenderers.forEach { it.disposeSurface() }
        }
    }

    @Test
    fun unthrottledRequestWinsWhenRenderRequestsAreCoalesced() {
        val host = SoftwareTestHost()
        val fakeRenderer = FakeRenderer(GraphicsApi.OPENGL)
        linuxLayerRendererFactoryOverride = { _, _, _ -> fakeRenderer }
        try {
            val layer =
                SkiaLayer(
                    SkiaLayerProperties(isVsyncEnabled = true, renderApi = GraphicsApi.OPENGL)
                )
            layer.renderDelegate = SkikoRenderDelegate { _, _, _, _ -> }
            layer.attachTo(host)
            layer.render()
            fakeRenderer.waitForVsyncValues.clear()

            layer.needRender(throttledToVsync = true)
            layer.needRender(throttledToVsync = false)
            layer.render()

            assertEquals(listOf(false), fakeRenderer.waitForVsyncValues)
            layer.detach()
        } finally {
            linuxLayerRendererFactoryOverride = null
            fakeRenderer.disposeSurface()
        }
    }

    @Test
    fun transparentSoftwareLayerPresentsTransparentPixels() {
        var firstPixel: UInt? = null
        val host =
            SoftwareTestHost(
                transparency = true,
                inspectPixels = { pixels ->
                    firstPixel = interpretCPointer<UIntVar>(pixels)?.pointed?.value
                },
            )
        val layer =
            SkiaLayer(
                SkiaLayerProperties(isVsyncEnabled = false, renderApi = GraphicsApi.SOFTWARE_FAST)
            )
        layer.renderDelegate = SkikoRenderDelegate { _, _, _, _ -> }
        layer.attachTo(host)

        assertTrue(layer.render())

        assertEquals(0u, firstPixel)
        layer.detach()
    }

    @Test
    fun publishesRendererAnalyticsAndFrameDiagnostics() {
        val host = SoftwareTestHost()
        val analytics = RecordingAnalytics()
        val fakeRenderer = FakeRenderer(GraphicsApi.OPENGL)
        linuxLayerRendererFactoryOverride = { _, _, _ -> fakeRenderer }
        try {
            val layer =
                SkiaLayer(
                    properties =
                        SkiaLayerProperties(
                            isVsyncEnabled = false,
                            renderApi = GraphicsApi.OPENGL,
                        ),
                    analytics = analytics,
                )
            layer.renderDelegate = SkikoRenderDelegate { _, _, _, _ -> }
            layer.attachTo(host)
            layer.render()

            assertEquals(
                listOf(
                    "renderer.init",
                    "renderer.deviceChosen",
                    "device.init",
                    "device.contextInit",
                    "device.beforeFirstFrame",
                    "device.beforeFrame",
                    "device.afterFirstFrame",
                    "device.afterFrame",
                ),
                analytics.events,
            )
            assertEquals(1, layer.diagnostics.renderedFrameCount)
            layer.detach()
        } finally {
            linuxLayerRendererFactoryOverride = null
            fakeRenderer.disposeSurface()
        }
    }

    @Test
    fun publishesEffectiveWindowCapabilities() {
        val host =
            SoftwareTestHost(
                transparency = true,
                transparencySupported = true,
                effectiveFrameBufferCount = 2,
            )
        val layer =
            SkiaLayer(
                SkiaLayerProperties(
                    isVsyncEnabled = false,
                    frameBuffering = FrameBuffering.TRIPLE,
                    renderApi = GraphicsApi.SOFTWARE_FAST,
                )
            )

        layer.attachTo(host)

        assertTrue(layer.diagnostics.transparencyRequested)
        assertTrue(layer.diagnostics.hasTransparentWindowBuffer)
        assertEquals(FrameBuffering.TRIPLE, layer.diagnostics.frameBuffering)
        assertEquals(2, layer.diagnostics.effectiveFrameBufferCount)
        layer.detach()
    }
}

private class FakeRenderer(
    override val renderApi: GraphicsApi,
    private val failRendering: Boolean = false,
) : LinuxLayerRenderer {
    override val description: String = "Fake $renderApi"
    override val deviceName: String = "Fake device"
    private val rasterSurface = Surface.makeRasterN32Premul(32, 24)
    var contextLost = false
    var closedAfterContextLoss = false
    var renderCount = 0
    val waitForVsyncValues = mutableListOf<Boolean>()

    override fun render(
        width: Int,
        height: Int,
        waitForVsync: Boolean,
        block: (Canvas) -> Unit,
    ) {
        if (failRendering) throw RenderException("simulated renderer failure")
        renderCount += 1
        waitForVsyncValues += waitForVsync
        block(rasterSurface.canvas)
    }

    override fun snapshot(width: Int, height: Int): Bitmap = Bitmap().also {
        check(it.allocN32Pixels(width, height))
    }

    override fun isContextLost(): Boolean = contextLost

    override fun close(contextLost: Boolean) {
        closedAfterContextLoss = contextLost
    }

    fun disposeSurface() = rasterSurface.close()
}

private class SoftwareTestHost(
    override val transparency: Boolean = false,
    override val transparencySupported: Boolean = transparency,
    override val effectiveFrameBufferCount: Int? = null,
    private val inspectPixels: (NativePointer) -> Unit = {},
) : LinuxSkiaLayerComponent {
    override val windowHandle: Any = this
    override val drawableWidth: Int = 32
    override val drawableHeight: Int = 24
    override val contentScale: Float = 1.5f
    override val pixelGeometry: PixelGeometry = PixelGeometry.RGB_H
    override var fullscreen: Boolean = false
    override val openGlResolverContext: NativePointer = NullPointer
    override val openGlResolver: NativePointer = NullPointer

    var softwareBeginCount = 0
    var softwareEndCount = 0
    var presentedFrames = 0

    override fun createOpenGlContext(): NativePointer = NullPointer

    override fun makeOpenGlContextCurrent(context: NativePointer) = Unit

    override fun setOpenGlSwapInterval(interval: Int): Boolean = false

    override fun swapOpenGlBuffers() = Unit

    override fun deleteOpenGlContext(context: NativePointer) = Unit

    override fun beginSoftwareRendering() {
        softwareBeginCount += 1
    }

    override fun presentSoftwareFrame(
        pixels: NativePointer,
        width: Int,
        height: Int,
        rowBytes: Int,
    ) {
        assertTrue(pixels != NullPointer)
        assertEquals(drawableWidth, width)
        assertEquals(drawableHeight, height)
        assertTrue(rowBytes >= width * 4)
        inspectPixels(pixels)
        presentedFrames += 1
    }

    override fun endSoftwareRendering() {
        softwareEndCount += 1
    }

    override fun requestRender() = Unit
}

@OptIn(ExperimentalSkikoApi::class)
private class RecordingAnalytics : SkiaLayerAnalytics {
    val events = mutableListOf<String>()

    override fun renderer(
        skikoVersion: String,
        os: OS,
        api: GraphicsApi,
    ): SkiaLayerAnalytics.RendererAnalytics =
        object : SkiaLayerAnalytics.RendererAnalytics {
            override fun init() {
                events += "renderer.init"
            }

            override fun deviceChosen() {
                events += "renderer.deviceChosen"
            }
        }

    override fun device(
        skikoVersion: String,
        os: OS,
        api: GraphicsApi,
        deviceName: String?,
    ): SkiaLayerAnalytics.DeviceAnalytics =
        object : SkiaLayerAnalytics.DeviceAnalytics {
            override fun init() {
                events += "device.init"
            }

            override fun contextInit() {
                events += "device.contextInit"
            }

            override fun beforeFirstFrameRender() {
                events += "device.beforeFirstFrame"
            }

            override fun beforeFrameRender() {
                events += "device.beforeFrame"
            }

            override fun afterFirstFrameRender() {
                events += "device.afterFirstFrame"
            }

            override fun afterFrameRender() {
                events += "device.afterFrame"
            }
        }
}
