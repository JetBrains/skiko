package org.jetbrains.skiko.redrawer

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.useContents
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoDispatchers
import org.jetbrains.skiko.context.ContextHandler
import org.jetbrains.skiko.context.MacOSOpenGLContextHandler
import org.jetbrains.skiko.currentNanoTime
import platform.CoreFoundation.CFTimeInterval
import platform.CoreGraphics.CGRectMake
import platform.CoreVideo.CVTimeStamp
import platform.OpenGLCommon.CGLContextObj
import platform.OpenGLCommon.CGLPixelFormatObj
import platform.OpenGLCommon.CGLSetCurrentContext
import platform.QuartzCore.CAOpenGLLayer
import platform.QuartzCore.*

/**
 * OpenGL [Redrawer] implementation for MacOs.
 *
 * Not actually used. See [SkiaLayer.renderApi]
 */
internal class MacOsOpenGLRedrawer(
    private val skiaLayer: SkiaLayer
) : Redrawer {
    private val contextHandler = MacOSOpenGLContextHandler(skiaLayer)
    override val renderInfo: String get() = contextHandler.rendererInfo()

    private val glLayer = MacosGLLayer()

    init {
        glLayer.init(skiaLayer, contextHandler)
    }

    private val frameDispatcher = FrameDispatcher(SkikoDispatchers.Main) {
        redrawImmediately(updateNeeded = true)
    }

    override fun dispose() {
        contextHandler.dispose()
        glLayer.dispose()
    }

    override fun syncBounds() {
        syncContentScale()
        skiaLayer.nsView.frame.useContents {
            glLayer.setFrame(
                origin.x.toInt(),
                origin.y.toInt(),
                size.width.toInt().coerceAtLeast(0),
                size.height.toInt().coerceAtLeast(0)
            )
        }
    }

    private fun syncContentScale() {
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        glLayer.contentsScale = skiaLayer.nsView.window!!.backingScaleFactor
        CATransaction.commit()
        CATransaction.flush()
    }

    override fun update(nanoTime: Long) {
        skiaLayer.update(nanoTime)
    }

    override fun needRedraw(throttledToVsync: Boolean) {
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately(updateNeeded: Boolean) {
        glLayer.setNeedsDisplay()
        skiaLayer.nsView.setNeedsDisplay(true)
    }
}

internal class MacosGLLayer : CAOpenGLLayer {
    private lateinit var skiaLayer: SkiaLayer
    private lateinit var contextHandler: ContextHandler

    @OverrideInit
    constructor(): super()
    @OverrideInit
    constructor(layer: Any): super(layer)

    fun init(layer: SkiaLayer, contextHandler: ContextHandler) {
        skiaLayer = layer
        this.contextHandler = contextHandler
        this.setNeedsDisplayOnBoundsChange(true)
        this.removeAllAnimations()
        this.setAutoresizingMask(kCALayerWidthSizable or kCALayerHeightSizable )
        skiaLayer.nsView.layer = this
        skiaLayer.nsView.wantsLayer = true
        this.contentsGravity = kCAGravityTopLeft
    }

    fun setFrame(x: Int, y: Int, width: Int, height: Int) {
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        this.frame = CGRectMake(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
        CATransaction.commit()
        CATransaction.flush()
    }

    fun dispose() {
        this.removeFromSuperlayer()
        // TODO: anything else to dispose the layer?
    }

    override fun canDrawInCGLContext(
        ctx: CGLContextObj?,
        pixelFormat: CGLPixelFormatObj?,
        forLayerTime: CFTimeInterval,
        displayTime: CPointer<CVTimeStamp>?
    ): Boolean {
        return true
    }

    override fun drawInCGLContext(
        ctx: CGLContextObj?,
        pixelFormat: CGLPixelFormatObj?,
        forLayerTime: CFTimeInterval,
        displayTime: CPointer<CVTimeStamp>?
    ) {
        CGLSetCurrentContext(ctx)
        try {
            skiaLayer.update(currentNanoTime())
            contextHandler.draw()
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }

        super.drawInCGLContext(ctx, pixelFormat,forLayerTime, displayTime)
    }
}

