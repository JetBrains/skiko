package org.jetbrains.skiko

import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JFrame
import org.jetbrains.awthrl.Components.HardwareLayer
import org.jetbrains.awthrl.DriverApi.Engine
import org.jetbrains.awthrl.DriverApi.OpenGLApi
import org.jetbrains.skija.BackendRenderTarget
import org.jetbrains.skija.Canvas
import org.jetbrains.skija.ColorSpace
import org.jetbrains.skija.Context
import org.jetbrains.skija.FramebufferFormat
import org.jetbrains.skija.Library
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

class SkiaLayer() : HardwareLayer() {

    var renderer: SkiaRenderer? = null
    private val skijaState = SkijaState()
    var init: Boolean = false

    public override fun draw() {
        if (!init) {
            if (skijaState.context == null) {
                skijaState.context = Context.makeGL()
            }
            initSkija()
            init = true
        }

        skijaState.apply {
            val gl = OpenGLApi.get()
            gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
            gl.glClear(gl.GL_COLOR_BUFFER_BIT)

            canvas!!.clear(0xFFFFFFF)
            renderer!!.onRender(canvas!!, width, height)
            context!!.flush()

            gl.glFinish()
        }
    }

    private fun initSkija() {
        val dpi = contentScale
        skijaState.clear()
        val gl: OpenGLApi = OpenGLApi.get()
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

class SkiaWindow() : JFrame() {
    companion object {
        init {
            Library.load("/", "skiko")
        }
    }

    val layer: SkiaLayer

    init {
        layer = SkiaLayer()
        setLayout(null);
        add(layer)

        addComponentListener(object : ComponentAdapter() {
            public override fun componentResized(e: ComponentEvent) {
                layer.init = false
                layer.setSize(width, height)
                Engine.get().render(layer)
            }
        })
    }
}
