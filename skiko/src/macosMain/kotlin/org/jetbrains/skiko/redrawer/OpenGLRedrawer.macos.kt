@file:OptIn(BetaInteropApi::class)

package org.jetbrains.skiko.redrawer

import kotlinx.cinterop.*
import org.jetbrains.skia.*
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.LayerDrawScope
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoDispatchers
import org.jetbrains.skiko.currentNanoTime
import platform.CoreFoundation.CFTimeInterval
import platform.CoreGraphics.CGRectMake
import platform.CoreVideo.CVTimeStamp
import platform.OpenGL.GL_DRAW_FRAMEBUFFER_BINDING
import platform.OpenGL.glGetIntegerv
import platform.OpenGLCommon.CGLContextObj
import platform.OpenGLCommon.CGLPixelFormatObj
import platform.OpenGLCommon.CGLSetCurrentContext
import platform.OpenGLCommon.GLenum
import platform.QuartzCore.CAOpenGLLayer
import platform.QuartzCore.*

/**
 * OpenGL [Redrawer] implementation for MacOs.
 *
 * Not actually used, unless the corresponding [GraphicsApi] is hardcoded in [SkiaLayer].
 * See [SkiaLayer.renderApi] and [MacOsMetalRedrawer] instead.
 */
internal class MacOsOpenGLRedrawer(
    layer: SkiaLayer
) : Redrawer(layer) {
    private val glLayer = MacosGLLayer()

    init {
        glLayer.init(layer, this)
    }

    private val frameDispatcher = FrameDispatcher(SkikoDispatchers.Main) {
        renderImmediately()
    }

    override fun dispose() {
        super.dispose()
        glLayer.dispose()
    }

    override fun syncBoundsFromPlatformComponent() {
        syncContentScale()
        layer.nsView.frame.useContents {
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
        glLayer.contentsScale = layer.nsView.window!!.backingScaleFactor
        CATransaction.commit()
        CATransaction.flush()
    }

    override fun update(nanoTime: Long) {
        layer.update(nanoTime)
    }

    override fun needRender(throttledToVsync: Boolean) {
        frameDispatcher.scheduleFrame()
    }

    override fun renderImmediately() {
        glLayer.setNeedsDisplay()
        layer.nsView.setNeedsDisplay(true)
    }


    override fun initContext(): Boolean {
        try {
            if (context == null) {
                context = DirectContext.makeGL()
            }
        } catch (_: Exception) {
            println("Failed to create Skia OpenGL context!")
            return false
        }
        return true
    }

    @ExperimentalUnsignedTypes
    private fun openglGetIntegerv(pname: GLenum): UInt {
        var result = 0U
        memScoped {
            val data = alloc<IntVar>()
            glGetIntegerv(pname, data.ptr)
            result = data.value.toUInt()
        }
        return result
    }

    private var currentWidth = 0
    private var currentHeight = 0
    private fun isSizeChanged(width: Int, height: Int): Boolean {
        if (width != currentWidth || height != currentHeight) {
            currentWidth = width
            currentHeight = height
            return true
        }
        return false
    }

    override fun LayerDrawScope.initCanvas() {
        val w = scaledLayerWidth
        val h = scaledLayerHeight
        if (isSizeChanged(w, h)) {
            val fbId = openglGetIntegerv(GL_DRAW_FRAMEBUFFER_BINDING.toUInt())
            renderTarget = BackendRenderTarget.makeGL(
                    w,
                    h,
                    0,
                    8,
                    fbId.toInt(),
                    FramebufferFormat.GR_GL_RGBA8
                )
            surface = Surface.makeFromBackendRenderTarget(
                    context!!,
                    renderTarget!!,
                    SurfaceOrigin.BOTTOM_LEFT,
                    SurfaceColorFormat.RGBA_8888,
                    ColorSpace.sRGB,
                    SurfaceProps(pixelGeometry = layer.pixelGeometry)
                ) ?: throw RenderException("Cannot create surface")

            canvas = surface?.canvas
                ?: error("Could not obtain Canvas from Surface")
        }
    }
}

internal class MacosGLLayer : CAOpenGLLayer {
    private lateinit var skiaLayer: SkiaLayer
    private lateinit var redrawer: Redrawer

    @OverrideInit
    constructor(): super()
    @OverrideInit
    constructor(layer: Any): super(layer)

    fun init(layer: SkiaLayer, redrawer: Redrawer) {
        skiaLayer = layer
        this.redrawer = redrawer
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
            skiaLayer.inDrawScope {
                redrawer.draw()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }

        super.drawInCGLContext(ctx, pixelFormat,forLayerTime, displayTime)
    }
}