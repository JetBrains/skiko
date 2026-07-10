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
 * The single per-window Software on-screen render context ([AWTRedrawer]): owns the CPU-backed Skia
 * [Bitmap] surface used to render each frame and blits it to the AWT peer. The frame loop itself lives in
 * the generic [OnScreenRedrawer]; this type owns only the surface.
 * An optional software frame limiter runs in [paceBeforeFrame].
 */
internal class SoftwareRedrawer(
    private val layer: SkiaLayer,
    properties: SkiaLayerProperties
) : AWTRedrawer {

    /**
     * Guards the CPU-backed [storage]/[canvas] surface. Frame loop and [dispose] both run on the EDT here, so
     * they can't actually race; the lock is kept for uniformity with the off-EDT backends like [MetalRedrawer].
     */
    private val drawLock = Any()

    @Volatile
    private var isDisposed = false

    override val graphicsApi: GraphicsApi get() = GraphicsApi.SOFTWARE_FAST
    override val deviceName: String? get() = "Software"

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

    override suspend fun paceBeforeFrame() {
        frameLimiter?.awaitNextFrame()
    }

    override fun dispose() = synchronized(drawLock) {
        isDisposed = true
        frameJob?.cancel()
        canvas?.close()
        canvas = null
        storage.close()
    }

    override fun isTransparentBackgroundSupported(): Boolean {
        // TODO: why Software rendering has another transparency logic from the beginning
        return hostOs == OS.MacOS
    }

    override suspend fun renderFrame(scope: LayerDrawScope, immediate: Boolean) {
        performDraw(scope)
    }

    private fun performDraw(scope: LayerDrawScope) = synchronized(drawLock) {
        if (!isDisposed) {
            with(scope) { drawFrame() }
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
