package org.jetbrains.skiko.swing

import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import java.awt.Graphics2D

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

    private val adapter = chooseAdapter(swingLayerProperties.adapterPriority.ordinal).also {
        onDeviceChosen("DirectX12") // TODO: properly get name
    }

    private val device = createDirectXOffscreenDevice(adapter)

    private val swingOffscreenDrawer = SwingOffscreenDrawer(swingLayerProperties)

    private val context = DirectContext(
        makeDirectXContext(device)
    )

    private var texturePtr: Long = 0
    private var bytesToDraw = ByteArray(0)
    private val rowBytesAlignment = getAlignment().toInt()
    private val widthSizeAlignment = rowBytesAlignment / 4

    init {
        onContextInit()
    }

    override fun dispose() {
        bytesToDraw = ByteArray(0)
        context.close()
        disposeDirectXTexture(texturePtr)
        disposeDevice(device)
        super.dispose()
    }

    override fun onRender(g: Graphics2D, width: Int, height: Int, nanoTime: Long) {
        autoCloseScope {
            // Calculate aligned width that is needed for performance optimization,
            // since DirectX uses aligned bytebuffer.
            // So we will have [Surface] with width == [alignedWidth],
            // but imitate (for SkikoView and Swing) like it has width == [width].
            val alignedWidth = if (width % widthSizeAlignment != 0) {
                width + widthSizeAlignment - (width % widthSizeAlignment);
            } else {
                width
            }

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

        val bytesArraySize = surface.width * surface.height * 4
        if (bytesToDraw.size != bytesArraySize) {
            bytesToDraw = ByteArray(bytesArraySize)
        }

        if(!readPixels(device, texturePtr, bytesToDraw)) {
            throw RenderException("Couldn't read pixels")
        }

        swingOffscreenDrawer.draw(g, bytesToDraw, surface.width, surface.height)
    }

    private fun makeRenderTarget() = BackendRenderTarget(
        makeDirectXRenderTargetOffScreen(texturePtr)
    )

    // Called from native code
    private fun isAdapterSupported(name: String) = isVideoCardSupported(GraphicsApi.DIRECT3D, hostOs, name)

    private external fun chooseAdapter(adapterPriority: Int): Long
    private external fun createDirectXOffscreenDevice(adapter: Long): Long
    private external fun makeDirectXContext(device: Long): Long

    private external fun readPixels(device: Long, texturePtr: Long, byteArray: ByteArray): Boolean

    private external fun getAlignment(): Long

    /**
     * Provides ID3D12Resource texture taking given [oldTexturePtr] into account
     * since it can be reused if width and height are not changed,
     * or the new one will be created.
     */
    private external fun makeDirectXTexture(device: Long, oldTexturePtr: Long, width: Int, height: Int): Long
    private external fun disposeDirectXTexture(texturePtr: Long)

    private external fun makeDirectXRenderTargetOffScreen(texturePtr: Long): Long

    private external fun disposeDevice(device: Long)
}