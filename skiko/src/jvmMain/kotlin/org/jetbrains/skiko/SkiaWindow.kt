package org.jetbrains.skiko

import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JFrame
import org.jetbrains.skija.BackendRenderTarget
import org.jetbrains.skija.Canvas
import org.jetbrains.skija.ColorSpace
import org.jetbrains.skija.Context
import org.jetbrains.skija.FramebufferFormat
import org.jetbrains.skija.Surface
import org.jetbrains.skija.SurfaceColorFormat
import org.jetbrains.skija.SurfaceOrigin

private class SkijaState {
    var context: Context? = null
    var renderTarget: BackendRenderTarget? = null
    var surface: Surface? = null
    var canvas: Canvas? = null

    fun clear() {
        surface?.close()
        renderTarget?.close()
    }
}

interface SkiaRenderer {
    fun onInit()
    fun onRender(canvas: Canvas, width: Int, height: Int)
    fun onReshape(width: Int, height: Int)
    fun onDispose()
}

open class SkiaLayer : HardwareLayer() {
    var renderer: SkiaRenderer? = null

    private val skijaState = SkijaState()
    protected var inited: Boolean = false

    fun reinit() {
        inited = false
    }

    override fun disposeLayer() {
        renderer?.onDispose()
    }

    override fun draw() {
        if (!inited) {
            if (skijaState.context == null) {
                skijaState.context = Context.makeGL()
            }
            initSkija()
            renderer?.onInit()
            inited = true
            renderer?.onReshape(width, height)
        }

        skijaState.apply {
            val gl = OpenGLApi.instance
            gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
            gl.glClear(gl.GL_COLOR_BUFFER_BIT)

            canvas!!.clear(-1)
            renderer?.onRender(canvas!!, width, height)
            context!!.flush()

            gl.glFinish()
        }
    }

    private fun initSkija() {
        val dpi = contentScale
        skijaState.clear()
        val gl: OpenGLApi = OpenGLApi.instance
        val fbId = gl.glGetIntegerv(gl.GL_DRAW_FRAMEBUFFER_BINDING)
        skijaState.renderTarget = BackendRenderTarget.makeGL(
            (width * dpi).toInt(),
            (height * dpi).toInt(),
            0,
            8,
            fbId,
            FramebufferFormat.GR_GL_RGBA8
        )
        skijaState.surface = Surface.makeFromBackendRenderTarget(
            skijaState.context,
            skijaState.renderTarget,
            SurfaceOrigin.BOTTOM_LEFT,
            SurfaceColorFormat.RGBA_8888,
            ColorSpace.getSRGB()
        )
        skijaState.canvas = skijaState.surface!!.canvas
        skijaState.canvas!!.scale(dpi, dpi)
    }
}

open class SkiaWindow : JFrame() {
    companion object {
        init {
            Library.load("/", "skiko")
        }
    }

    val layer: SkiaLayer = SkiaLayer()

    init {
        contentPane.add(layer)

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                layer.reinit()
            }
        })
    }

    fun display() {
        layer.display()
    }
}
