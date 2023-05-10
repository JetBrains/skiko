package org.jetbrains.skiko.redrawer

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.useContents
import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.DirectContext
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.SkikoDispatchers
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.context.ContextHandler
import org.jetbrains.skiko.context.MacOsMetalContextHandler
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
import kotlin.system.getTimeNanos
import platform.CoreGraphics.CGSizeMake
import platform.Metal.MTLCommandQueueProtocol

/**
 * Metal [Redrawer] implementation for MacOs.
 *
 * See [MacOsMetalContextHandler]
 */
internal class MacOsMetalRedrawer(
    private val skiaLayer: SkiaLayer
) : Redrawer {
    private val contextHandler = MacOsMetalContextHandler(skiaLayer)
    override val renderInfo: String get() = contextHandler.rendererInfo()

    private var isDisposed = false
    internal val device = MTLCreateSystemDefaultDevice() ?: throw IllegalStateException("Metal is not supported on this system")
    private val queue = device.newCommandQueue() ?: throw IllegalStateException("Couldn't create Metal command queue")
    private var currentDrawable: CAMetalDrawableProtocol? = null
    private val metalLayer = MetalLayer()

    init {
        val device =
            MTLCreateSystemDefaultDevice() ?: throw IllegalStateException("Metal is not supported on this system")

        val queue =
            device.newCommandQueue() ?: throw IllegalStateException("Couldn't create Metal command queue")

        this.device = device
        this.queue = queue

        metalLayer.init(skiaLayer, contextHandler, device)
    }

    private val frameDispatcher = FrameDispatcher(SkikoDispatchers.Main) {
        if (skiaLayer.isShowing()) {
            draw()
        }
    }

    /**
     * Creates and returns an instances of [DirectContext]
     */
    fun makeContext(): DirectContext = DirectContext.makeMetal(device.objcPtr(), queue.objcPtr())

    /**
     * Creates and returns an instances of [BackendRenderTarget] ready for rendering.
     *
     * https://developer.apple.com/documentation/quartzcore/cametallayer/1478172-nextdrawable
     */
    fun makeRenderTarget(width: Int, height: Int): BackendRenderTarget {
        currentDrawable = metalLayer.nextDrawable()!!
        return BackendRenderTarget.makeMetal(width, height, currentDrawable!!.texture.objcPtr())
    }

    override fun dispose() {
        if (!isDisposed) {
            metalLayer.dispose()
            isDisposed = true
        }
    }

    /**
     * Synchronizes the [metalLayer] size with the size of underlying nsView
     */
    override fun syncSize() {
        syncContentScale()
        val osFrame = skiaLayer.nsView.frame
        val (w, h) = osFrame.useContents {
            size.width to size.height
        }
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        metalLayer.frame = osFrame
        metalLayer.init(skiaLayer, contextHandler, device)
        metalLayer.drawableSize = CGSizeMake(w * metalLayer.contentsScale, h * metalLayer.contentsScale)
        CATransaction.commit()
        CATransaction.flush()
    }

    private fun syncContentScale() {
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        metalLayer.contentsScale = skiaLayer.nsView.window!!.backingScaleFactor
        CATransaction.commit()
        CATransaction.flush()
    }

    /**
     * Schedules a frame [draw] to an appropriate moment.
     */
    override fun needRedraw() {
        check(!isDisposed) { "MetalRedrawer is disposed" }
        frameDispatcher.scheduleFrame()
    }

    /**
     * Invokes [draw] right away.
     */
    override fun redrawImmediately() {
        check(!isDisposed) { "MetalRedrawer is disposed" }
        draw()
    }

    private fun draw() {
        // TODO: maybe make flush async as in JVM version.
        autoreleasepool {
            if (!isDisposed) {
                skiaLayer.update(getTimeNanos())
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
    constructor(): super()
    @OverrideInit
    constructor(layer: Any): super(layer)

    fun init(
        layer: SkiaLayer,
        contextHandler: ContextHandler,
        theDevice: MTLDeviceProtocol
    ) {
        this.skiaLayer = layer
        this.contextHandler = contextHandler
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
        skiaLayer.nsView.layer = this
        skiaLayer.nsView.wantsLayer = true
        this.contentsGravity = kCAGravityTopLeft;
    }

    fun dispose() {
        this.removeFromSuperlayer()
    }

    override fun drawInContext(ctx: CGContextRef?) {
        skiaLayer.update(getTimeNanos())
        contextHandler.draw()
    }
}
