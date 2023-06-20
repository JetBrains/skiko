package org.jetbrains.skiko.swing

import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import java.awt.Graphics2D

internal class Direct3DSwingRedrawer(
        private val swingLayerProperties: SwingLayerProperties,
        skikoView: SkikoView,
        analytics: SkiaLayerAnalytics
) : SwingRedrawerBase(swingLayerProperties, skikoView, analytics, GraphicsApi.DIRECT3D) {
    companion object {
        init {
            Library.load()
        }
    }

    private val adapter = chooseAdapter(swingLayerProperties.adapterPriority.ordinal).also {
        onDeviceChosen("DirectX12") // TODO: properly get name
    }

    private val device = createDirectXOffscreenDevice(adapter)

    private val swingOffscreenDrawer = SwingOffscreenDrawer(swingLayerProperties)

    override fun dispose() {
        disposeDevice(device)
        super.dispose()
    }

    override fun createDirectContext(): DirectContext {
        return DirectContext(
                makeDirectXContext(device)
        )
    }

    override fun initCanvas(context: DirectContext?): DrawingSurfaceData {
        context ?: error("DirectContext should be initialized")

        val scale = swingLayerProperties.graphicsConfiguration.defaultTransform.scaleX.toFloat()
        val width = (swingLayerProperties.width * scale).toInt().coerceAtLeast(0)
        val height = (swingLayerProperties.height * scale).toInt().coerceAtLeast(0)

        val renderTarget = createBackendRenderTarget(width, height)

        val surface = Surface.makeFromBackendRenderTarget(
                context,
                renderTarget,
                SurfaceOrigin.TOP_LEFT,
                SurfaceColorFormat.RGBA_8888,
                ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = PixelGeometry.UNKNOWN)
        ) ?: throw RenderException("Cannot create surface")

        return DrawingSurfaceData(renderTarget, surface, surface.canvas)
    }

    override fun flush(drawingSurfaceData: DrawingSurfaceData, g: Graphics2D) {
        val surface = drawingSurfaceData.surface ?: error("Surface should be initialized")
        surface.flushAndSubmit(syncCpu = false)

        val width = surface.width
        val height = surface.height

        val storage = Bitmap()
        storage.setImageInfo(ImageInfo.makeN32Premul(width, height))
        storage.allocPixels()
        // TODO: it copies pixels from GPU to CPU, so it is really slow
        surface.readPixels(storage, 0, 0)

        val bytes = storage.readPixels(storage.imageInfo, (width * 4), 0, 0)
        if (bytes != null) {
            swingOffscreenDrawer.draw(g, bytes, width, height)
        }
    }

    // TODO: memory leak for texture?
    // TODO: create native method that creates backendRenderTarget?
    private fun createBackendRenderTarget(
            width: Int,
            height: Int
    ): BackendRenderTarget {
        val format = 28 // DXGI_FORMAT_R8G8B8A8_UNORM
        val sampleCnt = 1
        val levelCnt = 1
        return BackendRenderTarget.makeDirect3D(width, height, createDirectXTexture(device, width, height), format, sampleCnt, levelCnt)
    }

    // Called from native code
    private fun isAdapterSupported(name: String) = isVideoCardSupported(GraphicsApi.DIRECT3D, hostOs, name)

    private external fun chooseAdapter(adapterPriority: Int): Long
    private external fun createDirectXOffscreenDevice(adapter: Long): Long
    private external fun makeDirectXContext(device: Long): Long

    // creates ID3D12Resource
    private external fun createDirectXTexture(device: Long, width: Int, height: Int): Long

    private external fun disposeDevice(device: Long)
}