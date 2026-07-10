@file:OptIn(BetaInteropApi::class)

package org.jetbrains.skiko.redrawer

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.useContents
import kotlinx.cinterop.value
import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.FramebufferFormat
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skia.runRestoringState
import org.jetbrains.skiko.FrameDispatcher
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
 * OpenGL [Redrawer] implementation for the native (Kotlin/Native) macOS target.
 *
 * macOS [SkiaLayer.renderApi] selects Metal, so this context is reached only through the
 * hardcoded-OpenGL path.
 */
internal class MacOsOpenGLRedrawer(
    private val skiaLayer: SkiaLayer
) : Redrawer {
    private var context: DirectContext? = null
    private var renderTarget: BackendRenderTarget? = null
    private var surface: Surface? = null
    private var canvas: Canvas? = null

    private var currentWidth = 0
    private var currentHeight = 0

    private val glLayer = MacosGLLayer()

    init {
        glLayer.init(skiaLayer, this)
    }

    override val renderInfo: String
        get() = "GraphicsApi: ${skiaLayer.renderApi}\nOS: macOS (native OpenGL)\n"

    private val frameDispatcher = FrameDispatcher(SkikoDispatchers.Main) {
        renderImmediately()
    }

    override fun dispose() {
        disposeSurface()
        context?.close()
        context = null
        glLayer.dispose()
    }

    override fun syncBoundsFromPlatformComponent() {
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
        glLayer.contentsScale = skiaLayer.nsView.window?.backingScaleFactor ?: 1.0
        CATransaction.commit()
        CATransaction.flush()
    }

    override fun update(nanoTime: Long) {
        skiaLayer.update(nanoTime)
    }

    override fun needRender(throttledToVsync: Boolean) {
        frameDispatcher.scheduleFrame()
    }

    override fun renderImmediately() {
        glLayer.setNeedsDisplay()
        skiaLayer.nsView.setNeedsDisplay(true)
    }

    /**
     * Invoked from the [MacosGLLayer]'s own `drawInCGLContext:`, with the CGL context already made current.
     */
    internal fun drawInLayerContext() {
        skiaLayer.update(currentNanoTime())
        skiaLayer.inDrawScope {
            performDraw()
        }
    }

    private fun LayerDrawScope.performDraw() {
        if (!initContext()) {
            throw RenderException("Cannot init graphic OpenGL context")
        }
        initSurface()
        canvas?.runRestoringState {
            clear(Color.TRANSPARENT)
            skiaLayer.draw(this)
        }
        context?.flush()
    }

    private fun initContext(): Boolean {
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

    private fun isSizeChanged(width: Int, height: Int): Boolean {
        if (width != currentWidth || height != currentHeight) {
            currentWidth = width
            currentHeight = height
            return true
        }
        return false
    }

    private fun LayerDrawScope.initSurface() {
        val w = scaledLayerWidth
        val h = scaledLayerHeight
        if (isSizeChanged(w, h) || surface == null) {
            disposeSurface()
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
                SurfaceProps(pixelGeometry = skiaLayer.pixelGeometry)
            ) ?: throw RenderException("Cannot create surface")

            canvas = surface?.canvas
                ?: error("Could not obtain Canvas from Surface")
        }
    }

    private fun disposeSurface() {
        surface?.close()
        surface = null
        renderTarget?.close()
        renderTarget = null
        canvas = null
        currentWidth = 0
        currentHeight = 0
    }

    override fun isTransparentBackgroundSupported() = defaultIsTransparentBackgroundSupported(skiaLayer)
}

internal class MacosGLLayer : CAOpenGLLayer {
    private lateinit var skiaLayer: SkiaLayer
    private lateinit var redrawer: MacOsOpenGLRedrawer

    @OverrideInit
    constructor(): super()
    @OverrideInit
    constructor(layer: Any): super(layer)

    fun init(layer: SkiaLayer, redrawer: MacOsOpenGLRedrawer) {
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
        if (this::skiaLayer.isInitialized && skiaLayer.nsView.layer == this) {
            skiaLayer.nsView.layer = null
            skiaLayer.nsView.wantsLayer = false
        }
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
            redrawer.drawInLayerContext()
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }

        super.drawInCGLContext(ctx, pixelFormat, forLayerTime, displayTime)
    }
}
