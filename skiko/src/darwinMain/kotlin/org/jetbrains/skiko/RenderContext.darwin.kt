@file:OptIn(BetaInteropApi::class, ExperimentalSkikoApi::class)

package org.jetbrains.skiko

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.objcPtr
import org.jetbrains.skia.*
import platform.Metal.MTLCommandQueueProtocol
import platform.Metal.MTLCreateSystemDefaultDevice
import platform.Metal.MTLDeviceProtocol
import platform.QuartzCore.CAMetalDrawableProtocol
import platform.QuartzCore.CAMetalLayer

/**
 * Create a [RenderContext] that rasterizes onto a **caller-owned** [CAMetalLayer] (darwin: macOS + iOS).
 *
 * The non-AWT entry point of the render-context API on Apple platforms: skiko does not own a view here — the
 * consumer provides its `CAMetalLayer` (e.g. the backing layer of its `NSView`/`UIView`), and skiko binds a
 * Metal GPU context + per-frame drawable to it. Each frame:
 * [acquireSurface][RenderContext.acquireSurface], draw onto it (a picture or direct ops), then
 * [present][RenderContext.present]; [close][RenderContext.close] releases the GPU resources. Drive it from a
 * single render thread. Metal's swap-chain rotates the drawable every frame, so a fresh surface per frame is
 * expected here (not wasteful).
 */
fun RenderContext.Companion.createFromMetalLayer(layer: CAMetalLayer): RenderContext = MetalRenderContext(layer)

/**
 * The standalone (view-less) Metal render context for darwin. It owns the `MTLDevice`/command queue, the
 * Skia [DirectContext] and the per-frame [CAMetalDrawable][CAMetalDrawableProtocol]/[Surface] for a
 * caller-owned [CAMetalLayer]. It is deliberately independent of skiko's on-screen `MacOsMetalRedrawer` /
 * uikit redrawer (those own their own view + frame loop); this is the primitive a consumer drives itself.
 */
internal class MetalRenderContext(private val metalLayer: CAMetalLayer) : RenderContext {
    private val deviceHandle = MTLCreateSystemDefaultDevice()
        ?: throw RenderException("Metal is not supported on this system")
    private val queueHandle = deviceHandle.newCommandQueue()
        ?: throw RenderException("Couldn't create Metal command queue")
    private val context = DirectContext.makeMetal(deviceHandle.objcPtr(), queueHandle.objcPtr())
    private var closed = false

    override val graphicsApi: GraphicsApi get() = GraphicsApi.METAL
    override val directContext: DirectContext get() = context

    /**
     * The `MTLDevice` skiko renders on. Backs the public [org.jetbrains.skiko.metalDevice] GPU-interop
     * accessor.
     *
     * @throws IllegalStateException if this context has been closed.
     */
    internal val device: MTLDeviceProtocol
        get() {
            check(!closed) { "MetalRenderContext is closed" }
            return deviceHandle
        }

    /**
     * The `MTLCommandQueue` skiko submits its frames on. Backs the public
     * [org.jetbrains.skiko.metalCommandQueue] GPU-interop accessor.
     *
     * @throws IllegalStateException if this context has been closed.
     */
    internal val queue: MTLCommandQueueProtocol
        get() {
            check(!closed) { "MetalRenderContext is closed" }
            return queueHandle
        }

    private var drawable: CAMetalDrawableProtocol? = null
    private var renderTarget: BackendRenderTarget? = null
    private var surface: Surface? = null

    init {
        @Suppress("UNCHECKED_CAST")
        metalLayer.device = device as objcnames.protocols.MTLDeviceProtocol?
        metalLayer.framebufferOnly = false
    }

    override fun acquireSurface(width: Int, height: Int): Surface {
        disposeSurface()
        val nextDrawable = metalLayer.nextDrawable()
            ?: throw RenderException("No Metal drawable available")
        drawable = nextDrawable
        val target = BackendRenderTarget.makeMetal(width, height, nextDrawable.texture.objcPtr())
        renderTarget = target
        val newSurface = Surface.makeFromBackendRenderTarget(
            context,
            target,
            SurfaceOrigin.TOP_LEFT,
            SurfaceColorFormat.BGRA_8888,
            ColorSpace.sRGB,
            SurfaceProps(pixelGeometry = PixelGeometry.UNKNOWN)
        ) ?: throw RenderException("Cannot create surface")
        surface = newSurface
        return newSurface
    }

    override fun present() {
        surface?.flushAndSubmit()
        autoreleasepool {
            drawable?.let {
                val commandBuffer = queue.commandBuffer()!!
                commandBuffer.label = "Present"
                commandBuffer.presentDrawable(it)
                commandBuffer.commit()
                drawable = null
            }
        }
    }

    override fun close() {
        disposeSurface()
        context.close()
        closed = true
    }

    private fun disposeSurface() {
        surface?.close()
        surface = null
        renderTarget?.close()
        renderTarget = null
    }
}
