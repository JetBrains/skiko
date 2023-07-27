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
    private var texturePtr: Long = 0
    private var renderTarget: BackendRenderTarget? = null
    private var surface: Surface? = null

    private var bytesToDraw = ByteArray(0)

    init {
        onContextInit()
    }

    override fun dispose() {
        surface?.close()
        renderTarget?.close()
        bytesToDraw = ByteArray(0)
        storage.close()
        context.close()
        disposeDevice(device)
        super.dispose()
    }

    override fun onRender(g: Graphics2D, width: Int, height: Int, nanoTime: Long) {
        val newTexturePtr = getRenderTargetTexture(device, width, height)

        if (newTexturePtr != texturePtr) {
            texturePtr = newTexturePtr

            val format = 28 // DXGI_FORMAT_R8G8B8A8_UNORM
            val sampleCnt = 1
            val levelCnt = 1

            renderTarget = BackendRenderTarget.makeDirect3D(
                width,
                height,
                texturePtr,
                format,
                sampleCnt,
                levelCnt
            )

            surface = Surface.makeFromBackendRenderTarget(
                context,
                renderTarget ?: throw RenderException("renderTarget is null"),
                SurfaceOrigin.TOP_LEFT,
                SurfaceColorFormat.RGBA_8888,
                ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = PixelGeometry.UNKNOWN)
            )
        }

        val surface = surface ?: throw RenderException("surface is null")

        val canvas = surface.canvas
        canvas.clear(Color.BLUE)
        skikoView.onRender(canvas, width, height, nanoTime)
        flush(surface, g)
    }

    fun flush(surface: Surface, g: Graphics2D) {
        surface.flushAndSubmit(syncCpu = true)
        context.flush()

        val expectedSize = surface.width * surface.height * 4
        if (bytesToDraw.size != expectedSize) {
            bytesToDraw = ByteArray(expectedSize)
        }
        // TODO: it copies pixels from GPU to CPU, so it is really slow
        readPixels(device, bytesToDraw)

        swingOffscreenDrawer.draw(g, bytesToDraw, surface.width, surface.height)
    }

    // Called from native code
    private fun isAdapterSupported(name: String) = isVideoCardSupported(GraphicsApi.DIRECT3D, hostOs, name)

    private external fun chooseAdapter(adapterPriority: Int): Long
    private external fun createDirectXOffscreenDevice(adapter: Long): Long
    private external fun makeDirectXContext(device: Long): Long

    private external fun readPixels(device: Long, byteArray: ByteArray)

    // creates ID3D12Resource
    private external fun getRenderTargetTexture(device: Long, width: Int, height: Int): Long

    private external fun disposeDevice(device: Long)
}