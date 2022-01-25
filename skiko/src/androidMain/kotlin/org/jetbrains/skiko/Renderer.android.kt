package org.jetbrains.skiko

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.Log
import android.widget.LinearLayout
import kotlinx.coroutines.Dispatchers
import org.jetbrains.skia.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SkikoSurfaceView(context: Context, layer: SkiaLayer) : GLSurfaceView(context) {
    private val renderer = SkikoSurfaceRender(layer)
    init {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    private val frameDispatcher = FrameDispatcher(Dispatchers.Main) {
        // draw()
        println("dispatch frame")
        renderer.update()
        requestRender()
    }

    fun scheduleFrame() {
        frameDispatcher.scheduleFrame()
    }
}

class SkikoSurfaceRender(private val layer: SkiaLayer) : GLSurfaceView.Renderer {
    private var width: Int = 0
    private var height: Int = 0

    @Volatile
    private var picture: PictureHolder? = null
    private var pictureRecorder: PictureRecorder = PictureRecorder()
    private val pictureLock = Any()

    private fun <T : Any> lockPicture(action: (PictureHolder) -> T): T? {
        return synchronized(pictureLock) {
            val picture = picture
            if (picture != null) {
                action(picture)
            } else {
                null
            }
        }
    }

    // This method is called from the main thread.
    fun update() {
        layer.skikoView?.let {
            val bounds = Rect.makeWH(width.toFloat(), width.toFloat())
            val canvas = pictureRecorder.beginRecording(bounds)
            try {
                it.onRender(canvas, width, height, System.nanoTime())
            } finally {
                synchronized(pictureLock) {
                    picture?.instance?.close()
                    val picture = pictureRecorder.finishRecordingAsPicture()
                    this.picture = PictureHolder(picture, width, height)
                }
            }
        }
    }

    // This method is called from GL rendering thread.
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        gl!!
        Log.d("GL","SkikoSurfaceRender.onSurfaceCreated: $gl")
        gl.glClearColor(0.0f, 1.0f, 0.0f, 0.0f)
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT)
    }

    // This method is called from GL rendering thread.
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        gl!!
        Log.d("GL", "SkikoSurfaceRender.onSurfaceChanged: $width x $height")
        this.width = width
        this.height = height
/*
        gl.glViewport(0, 0, width, height)
        gl.glMatrixMode(GL10.GL_PROJECTION)
        gl.glLoadIdentity()
        GLU.gluPerspective(
            gl, 45.0f,
            width.toFloat() / height.toFloat(),
            0.1f, 100.0f
        )
        gl.glMatrixMode(GL10.GL_MODELVIEW)
 */
        initCanvas()
    }

    // This method is called from GL rendering thread, it shall render Skia picture.
    override fun onDrawFrame(gl: GL10?) {
        gl!!
        Log.d("GL", "SkikoSurfaceRender.onDrawFrame: XXX 4: $width x $height")
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT)

        lockPicture {
            canvas?.drawPicture(it.instance)
            Unit
        }
    }

    private var context: DirectContext? = null
    private var renderTarget: BackendRenderTarget? = null
    private var surface: Surface? = null
    private var canvas: Canvas? = null

    private fun initCanvas() {
        disposeCanvas()
        val gl = OpenGLApi.instance
        val fbId = gl.glGetIntegerv(gl.GL_DRAW_FRAMEBUFFER_BINDING)
        renderTarget = makeGLRenderTarget(
            width,
            height,
            0,
            8,
            fbId,
            FramebufferFormat.GR_GL_RGBA8
        )
        context = makeGLContext()
        surface = Surface.makeFromBackendRenderTarget(
            context!!,
            renderTarget!!,
            SurfaceOrigin.BOTTOM_LEFT,
            SurfaceColorFormat.RGBA_8888,
            ColorSpace.sRGB
        )
        canvas = surface!!.canvas
    }

    private fun disposeCanvas() {
        surface?.close()
        renderTarget?.close()
    }
}
