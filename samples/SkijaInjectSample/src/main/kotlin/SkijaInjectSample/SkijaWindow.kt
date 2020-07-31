package SkijaInjectSample

import java.nio.IntBuffer
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent

import org.jetbrains.awthrl.Components.Drawable
import org.jetbrains.awthrl.Components.Window
import org.jetbrains.awthrl.DriverApi.Engine
import org.jetbrains.awthrl.DriverApi.OpenGLApi

import org.jetbrains.skija.BackendRenderTarget
import org.jetbrains.skija.FramebufferFormat
import org.jetbrains.skija.Canvas
import org.jetbrains.skija.Context
import org.jetbrains.skija.ColorSpace
import org.jetbrains.skija.Library
import org.jetbrains.skija.Surface
import org.jetbrains.skija.SurfaceOrigin
import org.jetbrains.skija.SurfaceColorFormat

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

class SkijaWindow() : Window() {
    companion object {
        init {
            Library.load("/", "skiko")
        }
    }

    var drawer: SkiaRenderer? = null
    private val skijaState = SkijaState()
    private var init: Boolean = false;

    init {
        val window = this;
        addComponentListener(object: ComponentAdapter() {
            public override fun componentResized(e: ComponentEvent) {
                init = false
                Engine.get().render(window);
            }
        })
    }

    public override fun draw() {
        if (!init) {
            if (skijaState.context == null) {
                skijaState.context = Context.makeGL()
            }
            initSkija(this)
            init = true;
        }

        skijaState.apply {
            canvas!!.clear(0xFFFFFFF)
            drawer!!.onRender(canvas!!, width, height)
            context!!.flush()
        }
    }

    private fun initSkija(glCanvas: Window) {
        val width = glCanvas.width
        val height = glCanvas.height
        val dpi = 2f
        skijaState.clear()
        val gl: OpenGLApi = OpenGLApi.get();
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