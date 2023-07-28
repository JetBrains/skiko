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

    private var texturePtr: Long = 0
    private var renderTarget: BackendRenderTarget? = null
    private var surface: Surface? = null

    private var bytesToDraw = ByteArray(0)
    private val rowBytesAlignment = getAlignment().toInt()
    private val widthSizeAlignment = rowBytesAlignment / 4

    init {
        onContextInit()
    }

    override fun dispose() {
        surface?.close()
        renderTarget?.close()
        bytesToDraw = ByteArray(0)
        context.close()
        disposeDevice(device)
        super.dispose()
    }

    override fun onRender(g: Graphics2D, width: Int, height: Int, nanoTime: Long) {
        val alignedWidth = if (width % widthSizeAlignment != 0) {
            width + widthSizeAlignment - (width % widthSizeAlignment);
        } else {
            width
        }

        val newTexturePtr = getRenderTargetTexture(device, alignedWidth, height)

        if (newTexturePtr != texturePtr) {
            texturePtr = newTexturePtr

            val format = 28 // DXGI_FORMAT_R8G8B8A8_UNORM
            val sampleCnt = 1
            val levelCnt = 1

            renderTarget = BackendRenderTarget.makeDirect3D(
                alignedWidth,
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
        canvas.clear(Color.TRANSPARENT)
        skikoView.onRender(canvas, surface.width, surface.height, nanoTime)
        flush(surface, g)
    }

    fun flush(surface: Surface, g: Graphics2D) {
        surface.flushAndSubmit(syncCpu = false)

        val bytesArraySize = surface.width * surface.height * 4
        if (bytesToDraw.size != bytesArraySize) {
            bytesToDraw = ByteArray(bytesArraySize)
        }

        readPixels(device, bytesToDraw)

        swingOffscreenDrawer.draw(g, bytesToDraw, surface.width, surface.height)
    }

    // Called from native code
    private fun isAdapterSupported(name: String) = isVideoCardSupported(GraphicsApi.DIRECT3D, hostOs, name)

    private external fun chooseAdapter(adapterPriority: Int): Long
    private external fun createDirectXOffscreenDevice(adapter: Long): Long
    private external fun makeDirectXContext(device: Long): Long

    private external fun readPixels(device: Long, byteArray: ByteArray)

    private external fun getAlignment(): Long

    // creates ID3D12Resource
    private external fun getRenderTargetTexture(device: Long, width: Int, height: Int): Long

    private external fun disposeDevice(device: Long)
}