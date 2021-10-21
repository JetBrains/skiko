package org.jetbrains.skiko.redrawer

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.useContents
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerProperties
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
    private val layer: SkiaLayer,
    private val properties: SkiaLayerProperties
) : Redrawer {
    private val drawLayer = MacosGLLayer(layer, setNeedsDisplayOnBoundsChange = true)

    override fun dispose() { 
        drawLayer.dispose()
    }

    override fun syncSize() {
        // TODO: What do we really do here?
        layer.nsView.frame.useContents {
            drawLayer.setFrame(
                origin.x.toInt(),
                origin.y.toInt(),
                size.width.toInt().coerceAtLeast(0),
                size.height.toInt().coerceAtLeast(0)
            )
        }
    }

    override fun needRedraw() {
    }

    override fun redrawImmediately() {
        layer.update(getTimeNanos())
    }
}

class MacosGLLayer(val layer: SkiaLayer, setNeedsDisplayOnBoundsChange: Boolean) : CAOpenGLLayer() {
    init {
        this.setNeedsDisplayOnBoundsChange(setNeedsDisplayOnBoundsChange)
        this.removeAllAnimations()
        this.setAutoresizingMask(kCALayerWidthSizable or kCALayerHeightSizable )
        this.setAsynchronous(true)
        layer.nsView.layer = this
        layer.nsView.wantsLayer = true
    }

    fun draw()  { 
        layer.update(getTimeNanos())
        layer.draw()
    }

    fun setFrame(x: Int, y: Int, width: Int, height: Int) {
        val newY = layer.nsView.frame.useContents { size.height } - y - height

        CATransaction.begin()
        CATransaction.setDisableActions(true)
        this.frame = CGRectMake(x.toDouble(), newY, width.toDouble(), height.toDouble())
        CATransaction.commit()
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
        //display.finish()
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
        performDraw()

        //context.flush() // TODO: I thought the below should call context.flush().
        super.drawInCGLContext(ctx, pixelFormat,forLayerTime, displayTime)
    }
}

