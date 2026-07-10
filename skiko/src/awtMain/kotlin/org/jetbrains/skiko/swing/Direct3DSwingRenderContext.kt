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
import org.jetbrains.skiko.RenderContext
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.SkikoProperties
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.alignedTextureWidth
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.chooseAdapter
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.createDirectXOffscreenDevice
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.disposeDevice
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.disposeDirectXTexture
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.makeDirectXContext
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.makeDirectXRenderTargetOffScreen
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.makeDirectXTexture
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.waitForCompletion

/**
 * A genuinely view-less **offscreen** Direct3D 12 [RenderContext]: it renders into an offscreen DirectX texture
 * created from a headless offscreen device ([createDirectXOffscreenDevice], no `HWND`) and hands back the
 * backing Skia [Surface]. The AWT counterpart of the other platforms' view-less GPU factories.
 *
 * It backs both the public `RenderContext.createOffscreen(w, h, GraphicsApi.DIRECT3D)` factory and, via
 * [SwingRenderer], [SkiaSwingLayer]'s Swing-interop pull model (which blits the surface onto a `Graphics2D`
 * with a [SoftwareSwingPainter] — DirectX has no zero-copy shared-texture path here, so [texturePtr] stays 0).
 *
 * DirectX requires row alignment, so the backing texture — and therefore [acquireSurface]'s returned surface —
 * is widened to [alignedTextureWidth]; the extra columns are right-side padding. This matches the existing
 * [org.jetbrains.skiko.graphicapi.DirectXOffscreenContext] semantics.
 *
 * Not thread-safe — drive it from a single render thread, mirroring [RenderContext]'s contract.
 */
@OptIn(ExperimentalSkikoApi::class)
internal class Direct3DSwingRenderContext(
    adapterPriority: GpuPriority = SkikoProperties.gpuPriority,
    gpuResourceCacheLimit: Long = SkikoProperties.gpuResourceCacheLimit,
) : SwingRenderContext {
    companion object {
        init {
            Library.load()
        }
    }

    private val adapter = chooseAdapter(adapterPriority)

    private val device = createDirectXOffscreenDevice(adapter).also {
        if (it == 0L) {
            throw RenderException("Failed to create DirectX12 device.")
        }
    }

    private val context: DirectContext = DirectContext(makeDirectXContext(device)).also {
        if (gpuResourceCacheLimit >= 0) {
            it.resourceCacheLimit = gpuResourceCacheLimit
        }
    }

    private var renderTarget: BackendRenderTarget? = null
    private var surface: Surface? = null
    private var surfaceWidth = 0
    private var surfaceHeight = 0
    private var currentTexturePtr: Long = 0
    private var closed = false

    override val graphicsApi: GraphicsApi get() = GraphicsApi.DIRECT3D
    override val deviceName: String get() = "DirectX12" // TODO: properly get name
    override val directContext: DirectContext get() = context

    override fun acquireSurface(width: Int, height: Int): Surface {
        check(!closed) { "Direct3DSwingRenderContext is disposed" }
        require(width > 0 && height > 0) { "Surface size must be positive, was ${width}x$height" }
        // The [Surface] has width == [alignedWidth], but the content is drawn at the logical [width].
        val alignedWidth = alignedTextureWidth(width)
        if (surface == null || alignedWidth != surfaceWidth || height != surfaceHeight) {
            createSurface(alignedWidth, height)
        }
        return surface!!
    }

    override fun present() {
        if (closed) return
        surface?.flushAndSubmit(syncCpu = false)
        waitForCompletion(device, currentTexturePtr)
    }

    override fun close() {
        if (closed) return
        closed = true
        disposeSurface()
        context.close()
        disposeDirectXTexture(currentTexturePtr)
        currentTexturePtr = 0
        disposeDevice(device)
    }

    private fun createSurface(alignedWidth: Int, height: Int) {
        disposeSurface()
        currentTexturePtr = makeDirectXTexture(device, currentTexturePtr, alignedWidth, height)
        if (currentTexturePtr == 0L) {
            throw RenderException("Can't allocate DirectX resources")
        }
        renderTarget = BackendRenderTarget(makeDirectXRenderTargetOffScreen(currentTexturePtr))
        surface = Surface.makeFromBackendRenderTarget(
            context,
            renderTarget!!,
            SurfaceOrigin.TOP_LEFT,
            SurfaceColorFormat.BGRA_8888,
            ColorSpace.sRGB,
            SurfaceProps(pixelGeometry = PixelGeometry.UNKNOWN)
        ) ?: throw RenderException("Cannot create surface")
        surfaceWidth = alignedWidth
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
}
