package org.jetbrains.skiko.redrawer

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.useContents
import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.DirectContext
import org.jetbrains.skiko.*
import org.jetbrains.skiko.context.ContextHandler
import org.jetbrains.skiko.context.MetalContextHandler
import platform.CoreGraphics.CGColorCreate
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGContextRef
import platform.Metal.MTLCreateSystemDefaultDevice
import platform.Metal.MTLDeviceProtocol
import platform.Metal.MTLPixelFormatBGRA8Unorm
import platform.QuartzCore.CAMetalDrawableProtocol
import platform.QuartzCore.CAMetalLayer
import platform.QuartzCore.kCAGravityTopLeft
import kotlin.system.getTimeNanos
import platform.CoreGraphics.CGSizeMake

internal class MetalRedrawer(
    private val layer: SkiaLayer
) : Redrawer {
    internal val contextHandler = MetalContextHandler(layer)
    override val renderInfo: String get() = contextHandler.rendererInfo()

    private var isDisposed = false
    internal val device = MTLCreateSystemDefaultDevice()!!
    private val queue = device.newCommandQueue()!!
    private var currentDrawable: CAMetalDrawableProtocol? = null
    private val metalLayer = MetalLayer()

    init {
        metalLayer.init(this.layer, contextHandler, device)
    }

    private val frameDispatcher = FrameDispatcher(SkikoDispatchers.Main) {
        if (layer.isShowing()) {
            draw()
        }
    }

    fun makeContext() = DirectContext.makeMetal(device.objcPtr(), queue.objcPtr())

    fun makeRenderTarget(width: Int, height: Int): BackendRenderTarget {
        currentDrawable = metalLayer.nextDrawable()!!
        return BackendRenderTarget.makeMetal(width, height, currentDrawable!!.texture.objcPtr())
    }

    override fun dispose() {
        if (!isDisposed) {
            frameDispatcher.cancel()
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
    }

    override fun needRedraw() {
        check(!isDisposed) { "MetalRedrawer is disposed" }
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() {
        check(!isDisposed) { "MetalRedrawer is disposed" }
        draw()
    }

    private fun draw() {
        // TODO: maybe make flush async as in JVM version.
        autoreleasepool {
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
    constructor(): super()
    @OverrideInit
    constructor(layer: Any): super(layer)

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
        this.opaque = false
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
