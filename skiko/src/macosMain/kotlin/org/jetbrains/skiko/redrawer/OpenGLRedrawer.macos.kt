package org.jetbrains.skiko.redrawer

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.useContents
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerProperties
import org.jetbrains.skiko.SkikoDispatchers
import platform.CoreFoundation.CFTimeInterval
import platform.CoreGraphics.CGRectMake
import platform.CoreVideo.CVTimeStamp
import platform.OpenGLCommon.CGLContextObj
import platform.OpenGLCommon.CGLPixelFormatObj
import platform.OpenGLCommon.CGLSetCurrentContext
import platform.QuartzCore.CAOpenGLLayer
import platform.QuartzCore.*
import kotlin.system.getTimeNanos

internal class MacOsOpenGLRedrawer(
    private val skiaLayer: SkiaLayer,
    private val properties: SkiaLayerProperties
) : Redrawer {
    private val glLayer = MacosGLLayer()

    init {
        glLayer.init(skiaLayer)
    }

    private val frameDispatcher = FrameDispatcher(SkikoDispatchers.Main) {
        redrawImmediately()
    }

    override fun dispose() { 
        glLayer.dispose()
    }

    override fun syncSize() {
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

    override fun needRedraw() {
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() {
        glLayer.setNeedsDisplay()
        skiaLayer.nsView.setNeedsDisplay(true)
    }
}

class MacosGLLayer : CAOpenGLLayer {
    private lateinit var layer: SkiaLayer
    @OverrideInit
    constructor(): super()
    @OverrideInit
    constructor(layer: Any): super(layer)

    fun init(layer: SkiaLayer) {
        this.layer = layer
        this.setNeedsDisplayOnBoundsChange(true)
        this.removeAllAnimations()
        this.setAutoresizingMask(kCALayerWidthSizable or kCALayerHeightSizable )
        layer.nsView.layer = this
        layer.nsView.wantsLayer = true
        this.contentsGravity = kCAGravityTopLeft;
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
        CGLSetCurrentContext(ctx);
        try {
            layer.update(getTimeNanos())
            layer.draw()
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }

        super.drawInCGLContext(ctx, pixelFormat,forLayerTime, displayTime)
    }
}

