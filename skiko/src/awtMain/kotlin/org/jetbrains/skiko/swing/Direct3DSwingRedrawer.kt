package org.jetbrains.skiko.swing

import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.alignedTextureWidth
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.createDirectXOffscreenDevice
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.disposeDirectXTexture
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.chooseAdapter
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.disposeDevice
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.makeDirectXContext
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.makeDirectXRenderTargetOffScreen
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.makeDirectXTexture
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.waitForCompletion
import java.awt.Graphics2D

// TODO reuse DirectXOffscreenContext
internal class Direct3DSwingRedrawer(
    swingLayerProperties: SwingLayerProperties,
    private val renderDelegate: SkikoRenderDelegate,
    analytics: SkiaLayerAnalytics
) : SwingRedrawerBase(swingLayerProperties, analytics, GraphicsApi.DIRECT3D) {
    companion object {
        init {
            Library.load()
        }
    }

    private val adapter = chooseAdapter(swingLayerProperties.adapterPriority).also {
        onDeviceChosen("DirectX12") // TODO: properly get name
    }

    private val device = createDirectXOffscreenDevice(adapter)

    private val painter: SwingPainter = SoftwareSwingPainter(swingLayerProperties)

    private val context = if (device == 0L) {
        throw RenderException("Failed to create DirectX12 device.")
    } else {
        DirectContext(
            makeDirectXContext(device)
        )
    }

    private var texturePtr: Long = 0
    private var bytesToDraw = ByteArray(0)

    init {
        onContextInit()
    }

    override fun dispose() {
        bytesToDraw = ByteArray(0)
        context.close()
        disposeDirectXTexture(texturePtr)
        disposeDevice(device)
        painter.dispose()
        super.dispose()
    }

    override fun onRender(g: Graphics2D, width: Int, height: Int, nanoTime: Long) {
        autoCloseScope {
            // We will have [Surface] with width == [alignedWidth],
            // but imitate (for SkikoRenderDelegate and Swing) like it has width == [width].
            val alignedWidth = alignedTextureWidth(width)

            texturePtr = makeDirectXTexture(device, texturePtr, alignedWidth, height)
            if (texturePtr == 0L) {
                throw RenderException("Can't allocate DirectX resources")
            }
            val renderTarget = makeRenderTarget().autoClose()

            val surface = Surface.makeFromBackendRenderTarget(
                context,
                renderTarget,
                SurfaceOrigin.TOP_LEFT,
                SurfaceColorFormat.BGRA_8888,
                ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = PixelGeometry.UNKNOWN)
            )?.autoClose() ?: throw RenderException("Cannot create surface")

            val canvas = surface.canvas
            canvas.clear(Color.TRANSPARENT)
            renderDelegate.onRender(canvas, width, height, nanoTime)
            flush(surface, g)
        }
    }

    fun flush(surface: Surface, g: Graphics2D) {
        surface.flushAndSubmit(syncCpu = false)
        waitForCompletion(device, texturePtr)

        painter.paint(g, surface, texturePtr)
    }

    private fun makeRenderTarget() = BackendRenderTarget(
        makeDirectXRenderTargetOffScreen(texturePtr)
    )
}