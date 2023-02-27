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
import platform.QuartzCore.CADisplayLink
import platform.QuartzCore.CAMetalDrawableProtocol
import platform.QuartzCore.CAMetalLayer
import platform.QuartzCore.kCAGravityTopLeft
import platform.darwin.NSObject

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
    private val activeFrameSubscription: Boolean = false
    private val frameListener = object : NSObject() {
        @ObjCAction
        fun onDisplayLinkTick() {
            if (layer.isShowing()) {
                draw()
            }
        }
    }
    private val caDisplayLink = CADisplayLink.displayLinkWithTarget(
        target = frameListener,
        selector = NSSelectorFromString(frameListener::onDisplayLinkTick.name)
    )

    init {
        metalLayer.init(this.layer, contextHandler, device)
    }

    private inline fun addFrameSubscription() {
        if (!activeFrameSubscription) {
            activeFrameSubscription = true
            caDisplayLink.addToRunLoop(NSRunLoop.mainRunLoop, NSRunLoop.mainRunLoop.currentMode)
        }
    }

    private inline fun removeFrameSubscription() {
        if (activeFrameSubscription) {
            caDisplayLink.removeFromRunLoop(NSRunLoop.mainRunLoop, NSRunLoop.mainRunLoop.currentMode)
        }
    }

    fun makeContext() = DirectContext.makeMetal(device.objcPtr(), queue.objcPtr())

    fun makeRenderTarget(width: Int, height: Int): BackendRenderTarget {
        currentDrawable = metalLayer.nextDrawable()!!
        return BackendRenderTarget.makeMetal(width, height, currentDrawable!!.texture.objcPtr())
    }

    override fun dispose() {
        if (!isDisposed) {
            removeFrameSubscription()
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
        addFrameSubscription()
    }

    override fun redrawImmediately() {
        check(!isDisposed) { "MetalRedrawer is disposed" }
        draw()
    }

    private fun draw() {
        // TODO: maybe make flush async as in JVM version.
        autoreleasepool { //todo maybe autoreleasepool is redundant, make measurements and talk with Kotlin Native team
            if (!isDisposed) {
                contextHandler.draw()
            }
        }
    }

    fun finishFrame() {
        removeFrameSubscription()
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
