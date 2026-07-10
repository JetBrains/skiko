package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.*
import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import org.jetbrains.skiko.layerFrameLimiter
import java.awt.Color
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.*

/**
 * Single per-window Software render context: owns the CPU-backed Skia [Bitmap] surface used to render each
 * frame, and the frame-loop plumbing that drives it. Owning both the CPU surface and the frame loop in one
 * type mirrors [MetalRedrawer].
 */
internal class SoftwareRedrawer(
    private val layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    properties: SkiaLayerProperties
) : AWTRedrawer(layer, analytics, GraphicsApi.SOFTWARE_FAST) {
    init {
        onDeviceChosen("Software")
    }

    /**
     * Guards the CPU-backed [storage]/[canvas] surface. Frame loop and [dispose] both run on the EDT here, so
     * they can't actually race; the lock is kept for uniformity with the off-EDT backends like [MetalRedrawer].
     */
    private val drawLock = Any()

    private val colorModel = ComponentColorModel(
        ColorSpace.getInstance(ColorSpace.CS_sRGB),
        true,
        false,
        Transparency.TRANSLUCENT,
        DataBuffer.TYPE_BYTE
    )
    private val storage = Bitmap()
    private var canvas: Canvas? = null

    override val renderInfo: String
        get() = renderInfoHeader(layer.renderApi)

    private val frameJob = if (properties.isVsyncEnabled && properties.isVsyncFramelimitFallbackEnabled) Job() else null
    private val frameLimiter = frameJob?.let {
        layerFrameLimiter(CoroutineScope(it), layer.backedLayer)
    }

    private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
        frameLimiter?.awaitNextFrame()

        if (layer.isShowing) {
            update()
            inDrawScope { performDraw() }
        }
    }

    init {
        onContextInit()
    }

    override fun dispose() = synchronized(drawLock) {
        frameJob?.cancel()
        frameDispatcher.cancel()
        canvas?.close()
        canvas = null
        storage.close()
        super.dispose()
    }

    override fun needRender(throttledToVsync: Boolean) {
        frameDispatcher.scheduleFrame()
    }

    override fun renderImmediately() {
        checkDisposed()
        update()
        inDrawScope {
            if (!isDisposed) { // Redrawer may be disposed in user code, during `update`
                performDraw()
            }
        }
    }

    override fun isTransparentBackgroundSupported(): Boolean {
        // TODO: why Software rendering has another transparency logic from the beginning
        return hostOs == OS.MacOS
    }

    private fun LayerDrawScope.performDraw() = synchronized(drawLock) {
        if (!isDisposed) {
            drawFrame()
        }
    }

    private fun LayerDrawScope.drawFrame() {
        ensureContext()
        initCanvas()
        canvas?.runRestoringState {
            clear(org.jetbrains.skia.Color.TRANSPARENT)
            layer.draw(this)
        }
        flushFrame()
    }

    private var isContextInitialized = false

    /** Logs the renderer summary once, on the first frame, mirroring the other AWT backends. */
    private fun ensureContext() {
        if (!isContextInitialized) {
            isContextInitialized = true
            logRendererInfo { renderInfo }
        }
    }

    private fun LayerDrawScope.initCanvas() {
        val w = scaledLayerWidth
        val h = scaledLayerHeight

        if (storage.width != w || storage.height != h) {
            storage.allocPixelsFlags(ImageInfo.makeS32(w, h, ColorAlphaType.PREMUL), false)
        }

        canvas = Canvas(storage, SurfaceProps(pixelGeometry = pixelGeometry))
    }

    private fun LayerDrawScope.flushFrame() {
        // Size from the bitmap, not the draw scope: the two diverge when a frame is recorded at a forced size.
        val w = storage.width
        val h = storage.height

        val bytes = storage.readPixels(storage.imageInfo, dstRowBytes = (w * 4), srcX = 0, srcY = 0)
        if (bytes != null) {
            val buffer = DataBufferByte(bytes, bytes.size)
            val raster = Raster.createInterleavedRaster(
                buffer,
                w,
                h,
                w * 4, 4,
                intArrayOf(2, 1, 0, 3), // BGRA order
                null
            )
            val image = BufferedImage(colorModel, raster, false, null)
            val graphics = layer.backedLayer.graphics
            if (!layer.fullscreen && layer.transparency && hostOs == OS.MacOS) {
                graphics?.color = Color(0, 0, 0, 0)
                graphics?.clearRect(0, 0, w, h)
            }
            graphics?.drawImage(image, 0, 0, layer.width, layer.height, null)
        }
    }
}
