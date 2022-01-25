package org.jetbrains.skiko

import android.content.Context
import android.opengl.GLSurfaceView
import android.opengl.GLU
import android.util.Log
import android.widget.LinearLayout
import kotlinx.coroutines.Dispatchers
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SkikoSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer = SkikoSurfaceRender()
    init {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        setRenderer(renderer)
    }

    private val frameDispatcher = FrameDispatcher(Dispatchers.Main) {
        // draw()
        println("dispatch frame")
    }
}
class SkikoSurfaceRender : GLSurfaceView.Renderer {
    var width: Int = 0
    var height: Int = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        gl!!
        Log.d("GL","SkikoSurfaceRender.onSurfaceCreated: $gl")
        gl.glClearColor(0.0f, 1.0f, 0.0f, 0.0f)
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        gl!!
        Log.d("GL", "SkikoSurfaceRender.onSurfaceChanged: $width x $height")
        this.width = width
        this.height = height

        gl.glViewport(0, 0, width, height)
        gl.glMatrixMode(GL10.GL_PROJECTION)
        gl.glLoadIdentity()
        GLU.gluPerspective(
            gl, 45.0f,
            width.toFloat() / height.toFloat(),
            0.1f, 100.0f
        )
        gl.glMatrixMode(GL10.GL_MODELVIEW)
    }

    override fun onDrawFrame(gl: GL10?) {
        gl!!
        Log.d("GL", "SkikoSurfaceRender.onDrawFrame: XXX 3: $width x $height")
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT)
    }
}
