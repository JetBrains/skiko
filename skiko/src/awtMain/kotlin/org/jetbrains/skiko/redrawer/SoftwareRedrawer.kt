package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.*
import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import org.jetbrains.skiko.layerFrameLimiter
import java.awt.Color
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.*

internal class SoftwareRedrawer(
    host: AwtSurfaceHost,
    analytics: SkiaLayerAnalytics,
    properties: SkiaLayerProperties
) : AWTRedrawer(host, analytics, GraphicsApi.SOFTWARE_FAST) {
    init {
        onDeviceChosen("Software")
    }

    /** [acquireSurface] and [present] are public API, so unlike the frame loop they are not EDT-confined. */
    private val drawLock = Any()

    // Software rasterizes on the CPU, so there is no Ganesh DirectContext.
    override val directContext: DirectContext? get() = null

    private val colorModel = ComponentColorModel(
        ColorSpace.getInstance(ColorSpace.CS_sRGB),
        true,
        false,
        Transparency.TRANSLUCENT,
        DataBuffer.TYPE_BYTE
    )
    private val storage = Bitmap()
    private var canvas: Canvas? = null

    // Standalone RenderContext surface (public acquireSurface/present path). Kept separate from the
    // on-screen `storage`/`canvas` above; present() reads it back into `storage` and reuses the same blit.
    private var standaloneSurface: Surface? = null
    private var standaloneWidth = 0
    private var standaloneHeight = 0

    override val renderInfo: String
        get() = renderInfoHeader(host.renderApi)

    private val frameJob = if (properties.isVsyncEnabled && properties.isVsyncFramelimitFallbackEnabled) Job() else null
    private val frameLimiter = frameJob?.let {
        layerFrameLimiter(CoroutineScope(it), host.backedLayer)
    }

    init {
        onContextInit()
    }

    override suspend fun runFrame(frame: suspend () -> Unit) {
        frameLimiter?.awaitNextFrame()
        frame()
    }

    override fun releaseResources() {
        frameJob?.cancel()
        canvas?.close()
        canvas = null
        standaloneSurface?.close()
        standaloneSurface = null
        storage.close()
    }

    override fun acquireSurface(width: Int, height: Int): Surface = synchronized(drawLock) {
        check(!isDisposed) { "SoftwareRedrawer is disposed" }
        if (standaloneSurface == null || width != standaloneWidth || height != standaloneHeight) {
            standaloneSurface?.close()
            standaloneSurface = Surface.makeRaster(ImageInfo.makeS32(width, height, ColorAlphaType.PREMUL))
            standaloneWidth = width
            standaloneHeight = height
        }
        standaloneSurface!!
    }

    override fun present() = synchronized(drawLock) {
        if (!isDisposed) {
            val surface = standaloneSurface
            if (surface != null) {
                val w = standaloneWidth
                val h = standaloneHeight
                if (storage.width != w || storage.height != h) {
                    storage.allocPixelsFlags(ImageInfo.makeS32(w, h, ColorAlphaType.PREMUL), false)
                }
                surface.flushAndSubmit()
                surface.readPixels(storage, 0, 0)
                blitStorage(w, h)
            }
        }
    }

    override fun isTransparentBackgroundSupported(): Boolean {
        // TODO: why Software rendering has another transparency logic from the beginning
        return hostOs == OS.MacOS
    }

    // renderImmediately() doesn't help with software renderer and actually makes it look worse
    // TODO: Implement a special solution for the software renderer
    override val presentsBeforeShown: Boolean get() = false

    override suspend fun renderFrame(scope: LayerDrawScope, immediate: Boolean) {
        performDraw(scope)
    }

    private fun performDraw(scope: LayerDrawScope) = synchronized(drawLock) {
        // Re-check inside the lock (not just at the call site), matching MetalRedrawer: this is what makes
        // `dispose` and an in-flight frame mutually exclusive.
        if (!isDisposed) {
            with(scope) { drawFrame() }
        }
    }

    private fun LayerDrawScope.drawFrame() {
        ensureContext()
        initCanvas()
        canvas?.runRestoringState {
            clear(org.jetbrains.skia.Color.TRANSPARENT)
            host.draw(this)
        }
        flushFrame()
    }

    private var isContextInitialized = false

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

        canvas?.close()
        canvas = Canvas(storage, SurfaceProps(pixelGeometry = pixelGeometry))
    }

    private fun LayerDrawScope.flushFrame() = blitStorage(storage.width, storage.height)

    /** Reads [storage] into a [BufferedImage] and blits it to the AWT peer. Shared by the on-screen loop
     * ([flushFrame]) and the standalone [present]. */
    private fun blitStorage(w: Int, h: Int) {
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
            val graphics = host.backedLayer.graphics
            if (!host.fullscreen && host.transparency && hostOs == OS.MacOS) {
                graphics?.color = Color(0, 0, 0, 0)
                graphics?.clearRect(0, 0, w, h)
            }
            graphics?.drawImage(image, 0, 0, host.width, host.height, null)
        }
    }
}