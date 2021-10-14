package org.jetbrains.skiko.redrawer

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.*
import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.DirectContext
import org.jetbrains.skiko.AppDispatcher
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerProperties
import platform.CoreGraphics.CGColorCreate
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGContextRef
import platform.Metal.MTLCreateSystemDefaultDevice
import platform.Metal.MTLDeviceProtocol
import platform.Metal.MTLPixelFormatBGRA8Unorm
import platform.QuartzCore.CAMetalDrawableProtocol
import platform.QuartzCore.CAMetalLayer
import platform.QuartzCore.kCAGravityTopLeft
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_t
import kotlin.coroutines.CoroutineContext
import kotlin.system.getTimeNanos

internal class MetalRedrawer(
    private val layer: SkiaLayer,
    private val properties: SkiaLayerProperties
) : Redrawer {
    private var isDisposed = false
    internal val device = MTLCreateSystemDefaultDevice()!!
    private val queue = device.newCommandQueue()!!
    private var currentDrawable: CAMetalDrawableProtocol? = null
    private val metalLayer = MetalLayer(this.layer, device)

    private val frameDispatcher = FrameDispatcher(AppDispatcher) {
        if (layer.isShowing()) {
            update(getTimeNanos())
            draw()
        }
    }

    fun makeContext() = DirectContext.makeMetal(device.objcPtr(), queue.objcPtr())

    fun makeRenderTarget(width: Int, height: Int): BackendRenderTarget {
        currentDrawable = metalLayer.nextDrawable()!!
        return BackendRenderTarget.makeMetal(width, height, currentDrawable!!.texture.objcPtr())
    }

    override fun dispose() {
        frameDispatcher.cancel()
        isDisposed = true
    }

    override fun syncSize() {
        println("TODO: implement syncSize()")
    }

    override fun needRedraw() {
        check(!isDisposed) { "MetalRedrawer is disposed" }
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() {
        check(!isDisposed) { "MetalRedrawer is disposed" }
        metalLayer.setNeedsDisplay()
        update(getTimeNanos())
        performDraw()
    }

    private fun performDraw() {
        if (!isDisposed) {
            layer.draw()
        }
    }

    private fun update(nanoTime: Long) {
        layer.update(nanoTime)
    }

    private fun draw() {
        // TODO: maybe make flush async as in JVM version.
        autoreleasepool {
            performDraw()
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

class MetalLayer(
    private val skiaLayer: SkiaLayer,
    theDevice: MTLDeviceProtocol
) : CAMetalLayer() {
    init {
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
        this.opaque = true
        this.frame = skiaLayer.view.frame
        skiaLayer.view.layer.addSublayer(this)
    }

    fun draw()  {
        skiaLayer.update(getTimeNanos())
        skiaLayer.draw()
    }

    fun dispose() {
        this.removeFromSuperlayer()
        // TODO: anything else to dispose the layer?
    }

    @Suppress("unused")
    private fun performDraw() {
        try {
            draw()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun drawInContext(ctx: CGContextRef?) {
        performDraw()
        super.drawInContext(ctx)
    }
}
