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
import platform.Foundation.NSThread
import platform.Metal.MTLCreateSystemDefaultDevice
import platform.Metal.MTLDeviceProtocol
import platform.Metal.MTLPixelFormatBGRA8Unorm
import platform.QuartzCore.*
import platform.UIKit.window
import platform.darwin.NSObject

fun logEventTime(eventName: String) {
    println("$eventName : ${CACurrentMediaTime()}")
}

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

    /**
     * Indicates whether draw was already dispatched during current vsync interval,
     * so any extra work should be scheduled on next frame
     */
    private var canDrawOnCurrentVsync = false

    /**
     * UITouch events are dispatched right before next CADisplayLink callback by iOS.
     * It's too late to encode any work for this frame after this happens.
     * Any work dispatched before the next CADisplayLink callback should be scheduled after that callback.
     */
    fun preventDrawDispatchDuringCurrentVsync() {
        canDrawOnCurrentVsync = false
    }

    /**
     * Needs scheduling displayLink for forcing UITouch events to come at the fastest possible cadence.
     * Otherwise touch events can come at rate lower than actual display refresh rate.
     */
    var needsProactiveDisplayLink = false
        set(value) {
            field = value

            if (value) {
                caDisplayLink.setPaused(false)
            }
        }

    /**
     * Indicates that there is update that needs redraw, which will be executed once new vsync is coming
     */
    private var hasScheduledDrawForNextVsync = false

    private val frameListener: NSObject = FrameTickListener {
        logEventTime("displayLink callback")

        canDrawOnCurrentVsync = true

        if (!needsProactiveDisplayLink) {
            caDisplayLink.setPaused(true)
        }

        if (hasScheduledDrawForNextVsync) {
            if (drawIfNeededDuringCurrentVsyncInterval()) {
                hasScheduledDrawForNextVsync = false
            } else {
                logEventTime("discard draw")

                caDisplayLink.setPaused(false)
            }
        } else {
            logEventTime("skip")
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

        logEventTime("needRedraw ${NSThread.currentThread}")

        /**
         * If we can't perform redraw during current vsync,
         * displayLink will fire at least one time more after this vsync
         */
        if (!drawIfNeededDuringCurrentVsyncInterval()) {
            logEventTime("schedule for next frame")

            hasScheduledDrawForNextVsync = true

            caDisplayLink.setPaused(false)
        }
    }

    override fun redrawImmediately() {
        check(!isDisposed) { "MetalRedrawer is disposed" }
        draw()
    }

    /**
     * Dispatch redraw immediately during current vsync, if no frames were previously dispatched during it.
     * Returns true if dispatch can happen, false otherwise.
     */
    private fun drawIfNeededDuringCurrentVsyncInterval(): Boolean {
        return canDrawOnCurrentVsync.also {
            if (!it) return@also

            logEventTime("dispatch draw")

            if (layer.isShowing()) {
                draw()
            }

            canDrawOnCurrentVsync = false
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
