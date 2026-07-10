package org.jetbrains.skiko.swing

import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.GpuPriority
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.Library
import org.jetbrains.skiko.Logger
import org.jetbrains.skiko.MetalAdapter
import org.jetbrains.skiko.RenderContext
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.SkikoProperties
import org.jetbrains.skiko.chooseMetalAdapter
import org.jetbrains.skiko.dispose
import org.jetbrains.skiko.hostArch
import org.jetbrains.skiko.hostOs

/**
 * A genuinely view-less **offscreen** Metal [RenderContext]: it renders into an offscreen `MTLTexture`
 * (created from a [MetalAdapter], **not** from an AWT window/JAWT surface) and hands back the backing Skia
 * [Surface]. There is no on-screen peer, no `CAMetalLayer`, and no AWT component — this is the AWT counterpart
 * of the view-less GPU factories on darwin/android.
 *
 * It backs both:
 *  * the public `RenderContext.createOffscreen(w, h, GraphicsApi.METAL)` factory (a caller drives
 *    `acquireSurface` → draw → `present`, then reads pixels back), and
 *  * [SkiaSwingLayer]'s Swing-interop pull model, where a [SwingRenderer] drives the same acquire→draw→present
 *    and then blits the surface onto a `Graphics2D` via a [SwingPainter] (zero-copy when the JBR shared-texture
 *    path is available — see [texturePtr] / [AcceleratedSwingPainter]).
 *
 * Not thread-safe — drive it from a single render thread, mirroring [RenderContext]'s contract.
 */
@OptIn(ExperimentalSkikoApi::class)
internal class MetalSwingRedrawer(
    adapterPriority: GpuPriority = SkikoProperties.gpuPriority,
    private val gpuResourceCacheLimit: Long = SkikoProperties.gpuResourceCacheLimit,
) : SwingRenderContext {
    companion object {
        init {
            Library.load()
        }
    }

    private val adapter: MetalAdapter = chooseMetalAdapter(adapterPriority)

    private val context: DirectContext = DirectContext(makeMetalContext(adapter.ptr)).also {
        if (System.getProperty("skiko.hardwareInfo.enabled") == "true") {
            Logger.info { "Renderer info:\n ${rendererInfo()}" }
        }
        if (gpuResourceCacheLimit >= 0) {
            it.resourceCacheLimit = gpuResourceCacheLimit
        }
    }

    private var renderTarget: BackendRenderTarget? = null
    private var surface: Surface? = null
    private var surfaceWidth = 0
    private var surfaceHeight = 0
    private var closed = false

    override var texturePtr: Long = 0
        private set

    override val graphicsApi: GraphicsApi get() = GraphicsApi.METAL
    override val deviceName: String get() = adapter.name
    override val directContext: DirectContext get() = context

    override fun acquireSurface(width: Int, height: Int): Surface {
        check(!closed) { "MetalSwingRedrawer is disposed" }
        require(width > 0 && height > 0) { "Surface size must be positive, was ${width}x$height" }
        require(width <= adapter.maxTextureSize && height <= adapter.maxTextureSize) {
            "Texture dimensions must be less than maximum allowed size: ${adapter.maxTextureSize}, got $width x $height"
        }
        if (surface == null || width != surfaceWidth || height != surfaceHeight) {
            createSurface(width, height)
        }
        return surface!!
    }

    override fun present() {
        if (closed) return
        surface?.flushAndSubmit(syncCpu = true)
    }

    override fun close() {
        if (closed) return
        closed = true
        disposeSurface()
        disposeMetalTexture(texturePtr)
        texturePtr = 0
        context.close()
        adapter.dispose()
    }

    private fun createSurface(width: Int, height: Int) {
        disposeSurface()
        texturePtr = makeMetalTexture(adapter.ptr, texturePtr, width, height)
        renderTarget = BackendRenderTarget(makeMetalRenderTargetOffScreen(texturePtr))
        surface = Surface.makeFromBackendRenderTarget(
            context,
            renderTarget!!,
            SurfaceOrigin.TOP_LEFT,
            SurfaceColorFormat.BGRA_8888,
            ColorSpace.sRGB,
            SurfaceProps(pixelGeometry = PixelGeometry.UNKNOWN)
        ) ?: throw RenderException("Cannot create surface")
        surfaceWidth = width
        surfaceHeight = height
    }

    private fun disposeSurface() {
        surface?.close()
        renderTarget?.close()
        surface = null
        renderTarget = null
        surfaceWidth = 0
        surfaceHeight = 0
    }

    private fun rendererInfo(): String =
        "GraphicsApi: ${GraphicsApi.METAL}\n" +
            "OS: ${hostOs.id} ${hostArch.id}\n" +
            "Video card: ${adapter.name}\n" +
            "Total VRAM: ${adapter.memorySize / 1024 / 1024} MB\n"

    private external fun makeMetalContext(adapter: Long): Long

    private external fun makeMetalRenderTargetOffScreen(texture: Long): Long

    /**
     * Provides Metal texture taking given [oldTexture] into account since it
     * can be reused if width and height are not changed, or the new one will
     * be created.
     */
    private external fun makeMetalTexture(adapter: Long, oldTexture: Long, width: Int, height: Int): Long
    private external fun disposeMetalTexture(texture: Long): Long
}
