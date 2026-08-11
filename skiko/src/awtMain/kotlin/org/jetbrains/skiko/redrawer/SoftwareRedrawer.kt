package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.*
import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import org.jetbrains.skiko.context.ContextFreeContextHandler
import org.jetbrains.skiko.layerFrameLimiter
import java.awt.Color
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.*

internal class SoftwareRedrawer(
    layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    properties: SkiaLayerProperties
) : ContextFreeContextHandler(layer, analytics, GraphicsApi.SOFTWARE_FAST) {
    init {
        onDeviceChosen("Software")
    }

    private val colorModel = ComponentColorModel(
        ColorSpace.getInstance(ColorSpace.CS_sRGB),
        true,
        false,
        Transparency.TRANSLUCENT,
        DataBuffer.TYPE_BYTE
    )
    private val storage = Bitmap()
    private var image: BufferedImage? = null
    private var raster: WritableRaster? = null

    private val frameJob = if (properties.isVsyncEnabled && properties.isVsyncFramelimitFallbackEnabled) Job() else null
    private val frameLimiter = frameJob?.let {
        layerFrameLimiter(CoroutineScope(it), layer.backedLayer)
    }

    private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
        frameLimiter?.awaitNextFrame()

        if (layer.isShowing) {
            update()
            inDrawScope { draw() }
        }
    }

    init {
        onContextInit()
    }

    override fun dispose() {
        frameJob?.cancel()
        frameDispatcher.cancel()
        super.dispose()
    }

    override fun needRender(throttledToVsync: Boolean) {
        frameDispatcher.scheduleFrame()
    }

    override fun renderBeforeShown(): Boolean {
        // renderImmediately() doesn't help with software renderer and actually makes it look worse
        // TODO: Implement a special solution for the software renderer
        return false
    }

    override fun renderImmediately() {
        checkDisposed()
        update()
        inDrawScope {
            if (!isDisposed) { // Redrawer may be disposed in user code, during `update`
                draw()
            }
        }
    }

    override fun isTransparentBackgroundSupported(): Boolean {
        // TODO: why Software rendering has another transparency logic from the beginning
        return hostOs == OS.MacOS
    }

    override fun LayerDrawScope.initCanvas() {
        disposeCanvas()

        val w = scaledLayerWidth
        val h = scaledLayerHeight

        if (storage.width != w || storage.height != h) {
            storage.allocPixelsFlags(ImageInfo.makeS32(w, h, ColorAlphaType.PREMUL), false)
        }

        canvas = Canvas(storage, SurfaceProps(pixelGeometry = pixelGeometry))
    }

    override fun flush() {
        val w = storage.width
        val h = storage.height

        val bytes = storage.readPixels(storage.imageInfo, dstRowBytes = (w * 4), srcX = 0, srcY = 0)
        if (bytes != null) {
            val buffer = DataBufferByte(bytes, bytes.size)
            raster = Raster.createInterleavedRaster(
                buffer,
                w,
                h,
                w * 4, 4,
                intArrayOf(2, 1, 0, 3), // BGRA order
                null
            )
            image = BufferedImage(colorModel, raster!!, false, null)
            val graphics = layer.backedLayer.graphics
            if (!layer.fullscreen && layer.transparency && hostOs == OS.MacOS) {
                graphics?.color = Color(0, 0, 0, 0)
                graphics?.clearRect(0, 0, w, h)
            }
            graphics?.drawImage(image!!, 0, 0, layer.width, layer.height, null)
        }
    }
}
