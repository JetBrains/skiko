@file:OptIn(BetaInteropApi::class)

package org.jetbrains.skiko.redrawer

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skia.runRestoringState
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.LayerDrawScope
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.SkikoDispatchers
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.currentNanoTime
import platform.AppKit.NSWindowDidChangeOcclusionStateNotification
import platform.AppKit.NSWindowOcclusionStateVisible
import platform.CoreGraphics.CGColorCreate
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGContextRef
import platform.Metal.MTLCreateSystemDefaultDevice
import platform.Metal.MTLDeviceProtocol
import platform.Metal.MTLPixelFormatBGRA8Unorm
import platform.QuartzCore.CAMetalDrawableProtocol
import platform.QuartzCore.CAMetalLayer
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCAGravityTopLeft
import platform.QuartzCore.kCALayerHeightSizable
import platform.QuartzCore.kCALayerWidthSizable
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.darwin.NSObjectProtocol
import kotlin.concurrent.Volatile

/**
 * Metal [Redrawer] implementation for the native (Kotlin/Native) macOS target.
 */
internal class MacOsMetalRedrawer(
    private val skiaLayer: SkiaLayer
) : Redrawer {
    private var isDisposed = false
    internal val device = MTLCreateSystemDefaultDevice() ?: throw IllegalStateException("Metal is not supported on this system")
    private val queue = device.newCommandQueue() ?: throw IllegalStateException("Couldn't create Metal command queue")

    private var context: DirectContext? = null
    private var renderTarget: BackendRenderTarget? = null
    private var surface: Surface? = null
    private var canvas: Canvas? = null

    private var currentDrawable: CAMetalDrawableProtocol? = null

    private val metalLayer = MetalLayer()
    private val occlusionObserver: NSObjectProtocol
    private val windowOcclusionStateChannel = Channel<Boolean>(Channel.CONFLATED)
    @Volatile private var isWindowOccluded = false

    init {
        metalLayer.init(skiaLayer, this, device)

        val window = skiaLayer.nsView.window!!
        occlusionObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = NSWindowDidChangeOcclusionStateNotification,
            `object` = window,
            queue = NSOperationQueue.mainQueue,
            usingBlock = { notification: NSNotification? ->
                val isOccluded = window.occlusionState and NSWindowOcclusionStateVisible == 0uL
                isWindowOccluded = isOccluded
                windowOcclusionStateChannel.trySend(isOccluded)
            }
        )
    }

    override val renderInfo: String get() = "Native Metal: device ${device.name}"

    private val frameDispatcher = FrameDispatcher(SkikoDispatchers.Main) {
        if (skiaLayer.isShowing()) {
            draw()
        }
    }

    override fun dispose() {
        if (!isDisposed) {
            isDisposed = true
            releasePendingDrawable()
            disposeSurface()
            context?.close()
            context = null
            metalLayer.dispose()
            NSNotificationCenter.defaultCenter.removeObserver(occlusionObserver)
        }
    }

    /**
     * Synchronizes the [metalLayer] size with the size of the underlying nsView.
     */
    override fun syncBoundsFromPlatformComponent() {
        syncContentScale()
        val osFrame = skiaLayer.nsView.frame
        val (w, h) = osFrame.useContents {
            size.width to size.height
        }
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        metalLayer.frame = osFrame
        metalLayer.init(skiaLayer, this, device)
        metalLayer.drawableSize = CGSizeMake(w * metalLayer.contentsScale, h * metalLayer.contentsScale)
        CATransaction.commit()
        CATransaction.flush()
    }

    private fun syncContentScale() {
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        metalLayer.contentsScale = skiaLayer.nsView.window?.backingScaleFactor ?: 1.0
        CATransaction.commit()
        CATransaction.flush()
    }

    private fun checkDisposed() {
        check(!isDisposed) { "MetalRedrawer is disposed" }
    }

    /**
     * Schedules a frame [draw] to an appropriate moment.
     */
    override fun needRender(throttledToVsync: Boolean) {
        checkDisposed()
        frameDispatcher.scheduleFrame()
    }

    override fun update(nanoTime: Long) {
        checkDisposed()
        skiaLayer.update(nanoTime)
    }

    /**
     * Invokes [draw] right away.
     */
    override fun renderImmediately() {
        checkDisposed()
        autoreleasepool {
            if (!isDisposed) {
                update()
            }
            if (!isDisposed) { // Redrawer may be disposed in user code, during `update`
                skiaLayer.inDrawScope {
                    performDraw()
                }
            }
        }
    }

    private suspend fun draw() {
        autoreleasepool {
            if (!isDisposed) {
                update()
                skiaLayer.inDrawScope {
                    performDraw()
                }
            }
        }

        // When window is not visible - it doesn't make sense to redraw fast to avoid battery drain.
        if (isWindowOccluded) {
            withTimeoutOrNull(300) {
                // If the window becomes non-occluded, stop waiting immediately
                @Suppress("ControlFlowWithEmptyBody")
                while (windowOcclusionStateChannel.receive()) { }
            }
        }
    }

    internal fun drawInLayerContext() {
        if (isDisposed) return
        skiaLayer.update(currentNanoTime())
        skiaLayer.inDrawScope {
            performDraw()
        }
    }

    private fun LayerDrawScope.performDraw() {
        if (isDisposed) return
        try {
            if (!initContext()) {
                throw RenderException("Cannot init graphic Metal context")
            }
            initSurface()
            canvas?.runRestoringState {
                clear(Color.TRANSPARENT)
                skiaLayer.draw(this)
            }
            context?.flush()
            surface?.flushAndSubmit()
            finishFrame()
        } catch (t: Throwable) {
            releasePendingDrawable()
            throw t
        }
    }

    private fun initContext(): Boolean {
        try {
            if (context == null) {
                context = DirectContext.makeMetal(device.objcPtr(), queue.objcPtr())
            }
        } catch (e: Exception) {
            println("${e.message}\nFailed to create Skia Metal context!")
            return false
        }
        return true
    }

    private fun LayerDrawScope.initSurface() {
        disposeSurface()

        val w = scaledLayerWidth
        val h = scaledLayerHeight

        if (w > 0 && h > 0) {
            currentDrawable = metalLayer.nextDrawable()
                ?: throw RenderException("No Metal drawable available")
            renderTarget = BackendRenderTarget.makeMetal(w, h, currentDrawable!!.texture.objcPtr())

            surface = Surface.makeFromBackendRenderTarget(
                context!!,
                renderTarget!!,
                SurfaceOrigin.TOP_LEFT,
                SurfaceColorFormat.BGRA_8888,
                ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = skiaLayer.pixelGeometry)
            ) ?: throw RenderException("Cannot create surface")

            canvas = surface!!.canvas
        } else {
            renderTarget = null
            surface = null
            canvas = null
        }
    }

    private fun disposeSurface() {
        surface?.close()
        surface = null
        renderTarget?.close()
        renderTarget = null
        canvas = null
    }

    private fun finishFrame() {
        autoreleasepool {
            currentDrawable?.let {
                val commandBuffer = queue.commandBuffer()!!
                commandBuffer.label = "Present"
                commandBuffer.presentDrawable(it)
                commandBuffer.commit()
                currentDrawable = null
            }
        }
    }

    private fun releasePendingDrawable() {
        currentDrawable = null
    }
}

internal class MetalLayer : CAMetalLayer {
    private lateinit var skiaLayer: SkiaLayer
    private lateinit var redrawer: MacOsMetalRedrawer

    @OverrideInit
    constructor(): super()
    @OverrideInit
    constructor(layer: Any): super(layer)

    fun init(
        layer: SkiaLayer,
        redrawer: MacOsMetalRedrawer,
        theDevice: MTLDeviceProtocol
    ) {
        this.skiaLayer = layer
        this.redrawer = redrawer
        this.setNeedsDisplayOnBoundsChange(true)
        this.removeAllAnimations()
        this.setAutoresizingMask(kCALayerWidthSizable or kCALayerHeightSizable )
        this.device = theDevice as objcnames.protocols.MTLDeviceProtocol?
        this.pixelFormat = MTLPixelFormatBGRA8Unorm
        this.opaque = false
        doubleArrayOf(0.0, 0.0, 0.0, 0.0).usePinned {
            this.backgroundColor =
                CGColorCreate(CGColorSpaceCreateDeviceRGB(), it.addressOf(0))
        }
        this.framebufferOnly = false
        skiaLayer.nsView.layer = this
        skiaLayer.nsView.wantsLayer = true
        this.contentsGravity = kCAGravityTopLeft
    }

    fun dispose() {
        this.removeFromSuperlayer()
        if (this::skiaLayer.isInitialized && skiaLayer.nsView.layer == this) {
            skiaLayer.nsView.layer = null
            skiaLayer.nsView.wantsLayer = false
        }
    }

    override fun drawInContext(ctx: CGContextRef?) {
        redrawer.drawInLayerContext()
    }
}
