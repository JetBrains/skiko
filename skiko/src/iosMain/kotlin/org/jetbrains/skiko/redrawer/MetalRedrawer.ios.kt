package org.jetbrains.skiko.redrawer

import kotlinx.cinterop.*
import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.DirectContext
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.context.ContextHandler
import org.jetbrains.skiko.context.MetalContextHandler
import platform.CoreGraphics.CGColorCreate
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGContextRef
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSRunLoop
import platform.Foundation.NSSelectorFromString
import platform.Metal.MTLCreateSystemDefaultDevice
import platform.Metal.MTLDeviceProtocol
import platform.Metal.MTLPixelFormatBGRA8Unorm
import platform.QuartzCore.*
import platform.darwin.*

internal class MetalRedrawer(
    private val layer: SkiaLayer
) : Redrawer {
    private val contextHandler = MetalContextHandler(layer)
    override val renderInfo: String get() = contextHandler.rendererInfo()

    private var isDisposed = false
    internal val device = MTLCreateSystemDefaultDevice() ?: throw IllegalStateException("Metal is not supported on this system")
    private val queue = device.newCommandQueue() ?: throw IllegalStateException("Couldn't create Metal command queue")
    private var currentDrawable: CAMetalDrawableProtocol? = null
    private val metalLayer = MetalLayer()

    // Semaphore for preventing command buffers count more than swapchain size to be scheduled/executed at the same time
    private val inflightSemaphore = dispatch_semaphore_create(metalLayer.maximumDrawableCount.toLong())

    /*
     * Indicates that scene is invalidated and next display link callback will draw
     */
    private var hasScheduledDrawOnNextVSync = false

    /**
     * Needs scheduling displayLink for forcing UITouch events to come at the fastest possible cadence.
     * Otherwise, touch events can come at rate lower than actual display refresh rate.
     */
    var needsProactiveDisplayLink = false
        set(value) {
            field = value

            if (value) {
                caDisplayLink.setPaused(false)
            }
        }

    private val frameListener: NSObject = FrameTickListener {
        if (hasScheduledDrawOnNextVSync) {
            hasScheduledDrawOnNextVSync = false

            drawIfLayerIsShowing()
        }

        if (!needsProactiveDisplayLink) {
            caDisplayLink.setPaused(true)
        }
    }

    private val caDisplayLink = CADisplayLink.displayLinkWithTarget(
        target = frameListener,
        selector = NSSelectorFromString(FrameTickListener::onDisplayLinkTick.name)
    )
    init {
        metalLayer.init(this.layer, contextHandler, device)
        caDisplayLink.setPaused(true)
        caDisplayLink.addToRunLoop(NSRunLoop.mainRunLoop, NSRunLoop.mainRunLoop.currentMode)
    }

    fun makeContext() = DirectContext.makeMetal(device.objcPtr(), queue.objcPtr())

    fun makeRenderTarget(width: Int, height: Int): BackendRenderTarget {
        // If more than swapchain size count of command buffers are inflight
        // wait until one finishes work
        dispatch_semaphore_wait(inflightSemaphore, DISPATCH_TIME_FOREVER)
        currentDrawable = metalLayer.nextDrawable()!!
        return BackendRenderTarget.makeMetal(width, height, currentDrawable!!.texture.objcPtr())
    }

    override fun dispose() {
        if (!isDisposed) {
            caDisplayLink.invalidate()
            contextHandler.dispose()
            metalLayer.dispose()
            isDisposed = true
        }
    }

    override fun syncSize() {
        metalLayer.contentsScale = layer.contentScale.toDouble()
        val osView = layer.view!!
        val (w, h) = osView.frame.useContents {
            size.width to size.height
        }
        metalLayer.frame = osView.frame
        metalLayer.init(layer, contextHandler, device)
        metalLayer.drawableSize = CGSizeMake(w * metalLayer.contentsScale, h * metalLayer.contentsScale)

        osView.window?.screen?.maximumFramesPerSecond?.let {
            caDisplayLink.preferredFramesPerSecond = it
        }
    }

    override fun needRedraw() {
        check(!isDisposed) { "MetalRedrawer is disposed" }

        hasScheduledDrawOnNextVSync = true

        // If caDisplayLink is proactive (touches tracking), this does nothing (already unpaused)
        caDisplayLink.setPaused(false)
    }

    override fun redrawImmediately() {
        // TODO: separate iOS MetalRedrawer from Redrawer, it's a false abstraction, iOS only uses MetalRedrawer explicitly
        throw UnsupportedOperationException("This should never be called on iOS")
    }

    private fun drawIfLayerIsShowing() {
        if (layer.isShowing()) {
            draw()
        }
    }

    private fun draw() {
        // TODO: maybe make flush async as in JVM version.
        autoreleasepool { //todo measure performance without autoreleasepool
            if (!isDisposed) {
                contextHandler.draw()
            }
        }
    }

    fun finishFrame() {
        autoreleasepool {
            currentDrawable?.let {
                val commandBuffer = queue.commandBuffer()!!
                commandBuffer.label = "Present"
                commandBuffer.presentDrawable(it)
                commandBuffer.addCompletedHandler {
                    // Signal work finish, allow a new command buffer to be scheduled
                    dispatch_semaphore_signal(inflightSemaphore)
                }
                commandBuffer.commit()
                currentDrawable = null
            }
        }
    }
}

internal class MetalLayer : CAMetalLayer {
    private lateinit var skiaLayer: SkiaLayer
    private lateinit var contextHandler: ContextHandler

    @OverrideInit
    constructor() : super()

    @OverrideInit
    constructor(layer: Any) : super(layer)

    fun init(
        skiaLayer: SkiaLayer,
        contextHandler: ContextHandler,
        theDevice: MTLDeviceProtocol
    ) {
        this.skiaLayer = skiaLayer
        this.contextHandler = contextHandler
        this.setNeedsDisplayOnBoundsChange(true)
        this.removeAllAnimations()
        // TODO: looks like a bug in K/N interop.
        this.device = theDevice as objcnames.protocols.MTLDeviceProtocol?
        this.pixelFormat = MTLPixelFormatBGRA8Unorm
        this.contentsGravity = kCAGravityTopLeft
        doubleArrayOf(0.0, 0.0, 0.0, 0.0).usePinned {
            this.backgroundColor =
                CGColorCreate(CGColorSpaceCreateDeviceRGB(), it.addressOf(0))
        }
        this.framebufferOnly = false
        this.opaque = false // For UIKit interop through a "Hole"
        skiaLayer.view?.let {
            this.frame = it.frame
            it.layer.addSublayer(this)
        }
    }

    fun dispose() {
        this.removeFromSuperlayer()
        // TODO: anything else to dispose the layer?
    }

    override fun drawInContext(ctx: CGContextRef?) {
        contextHandler.draw()
        super.drawInContext(ctx)
    }
}

private class FrameTickListener(val onFrameTick: () -> Unit) : NSObject() {
    @ObjCAction
    fun onDisplayLinkTick() {
        onFrameTick()
    }
}
