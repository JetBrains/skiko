package org.jetbrains.skiko

import android.content.Context
import android.opengl.GLSurfaceView
import android.widget.LinearLayout
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SkikoSurfaceView(context: Context, width: Int, height: Int) : GLSurfaceView(context) {
    private val renderer = SkikoSurfaceRender()
    init {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        holder.setFixedSize(width, height)
        setRenderer(renderer)
    }
}
class SkikoSurfaceRender : GLSurfaceView.Renderer {
    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        println("SkikoSurfaceRender.onSurfaceCreated")
    }

    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {
        println("SkikoSurfaceRender.onSurfaceChanged")
    }

    override fun onDrawFrame(p0: GL10?) {
        println("SkikoSurfaceRender.onDrawFrame")
    }
}
