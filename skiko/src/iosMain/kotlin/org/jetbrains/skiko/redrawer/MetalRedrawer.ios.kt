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
import platform.UIKit.window
import platform.darwin.NSObject

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
     * Property used for bookkeeping at least certain amount of frames (set in [topUpRundownCounter]) scheduled
     * after last [needRedraw] to avoid reentry situation where two frames were asked to be drawn and
     * scheduled in the frame _after_ next and merge into one causing no frame scheduled for next vsync
     * during current vsync window, tick for which could happen slighly before input processing handling and corresponding
     * [needRedraw] call
     *
     * TODO: still needs a little investigation regarding current problem
     *       being a symptom caused by an anomaly of redraw callback
     *       being dispatched *strictly* after next vsync + some time after
     *
     **/
    private var rundownCounter = 0

    private val frameListener: NSObject = FrameTickListener {
        rundownCounter -= 1

        if (rundownCounter == 0) {
            caDisplayLink.setPaused(true)
        }

        if (layer.isShowing()) {
            draw()
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

    private fun topUpRundownCounter() {
        rundownCounter = RUNDOWN_COUNTER_TOP_VALUE
    }

    override fun needRedraw() {
        check(!isDisposed) { "MetalRedrawer is disposed" }

        topUpRundownCounter()

        caDisplayLink.setPaused(false)
    }

    override fun redrawImmediately() {
        check(!isDisposed) { "MetalRedrawer is disposed" }
        draw()
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

    companion object {
        /**
         * This value indicates how many frames CADisplayLink will continue to draw after the last [needRedraw] call
         * until it pauses itself. The value is arbitrarily chosen. Must be at least 2 to avoid incorrect scheduling.
         * It doesn't affect correctness, and the actual optimal value (if this machinery is needed at all)
         * will be set once [rundownCounter] _todo_ investigation is finished.
         */
        private const val RUNDOWN_COUNTER_TOP_VALUE = 3
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
