package org.jetbrains.skiko.swing

import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import java.awt.Graphics2D

internal class Direct3DSwingRedrawer(
    swingLayerProperties: SwingLayerProperties,
    private val skikoView: SkikoView,
    analytics: SkiaLayerAnalytics
) : SwingRedrawerBase(swingLayerProperties, analytics, GraphicsApi.DIRECT3D) {
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

    private val context = DirectContext(
        makeDirectXContext(device)
    )

    private val storage = Bitmap()

    private var bytesToDraw = ByteArray(0)

    init {
        onContextInit()
    }

    override fun dispose() {
        bytesToDraw = ByteArray(0)
        storage.close()
        context.close()
        disposeDevice(device)
        super.dispose()
    }

    override fun onRender(g: Graphics2D, width: Int, height: Int, nanoTime: Long) {
        autoCloseScope {
            val renderTarget = createBackendRenderTarget(width, height).autoClose()
            val surface = Surface.makeFromBackendRenderTarget(
                context,
                renderTarget,
                SurfaceOrigin.TOP_LEFT,
                SurfaceColorFormat.RGBA_8888,
                ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = PixelGeometry.UNKNOWN)
            )?.autoClose() ?: throw RenderException("Cannot create surface")

            val canvas = surface.canvas
            canvas.clear(Color.TRANSPARENT)
            skikoView.onRender(canvas, width, height, nanoTime)
            flush(surface, g)
        }
    }

    fun flush(surface: Surface, g: Graphics2D) {
        surface.flushAndSubmit(syncCpu = false)

        val width = surface.width
        val height = surface.height

        val dstRowBytes = width * 4
        if (storage.width != width || storage.height != height) {
            storage.allocPixelsFlags(ImageInfo.makeS32(width, height, ColorAlphaType.PREMUL), false)
            bytesToDraw = ByteArray(storage.getReadPixelsArraySize(dstRowBytes = dstRowBytes))
        }
        // TODO: it copies pixels from GPU to CPU, so it is really slow
        surface.readPixels(storage, 0, 0)

        val successfulRead = storage.readPixels(bytesToDraw, dstRowBytes = dstRowBytes)
        if (successfulRead) {
            swingOffscreenDrawer.draw(g, bytesToDraw, width, height)
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
        return BackendRenderTarget.makeDirect3D(
            width,
            height,
            createDirectXTexture(device, width, height),
            format,
            sampleCnt,
            levelCnt
        )
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