package org.jetbrains.skiko

import android.content.Context
import android.view.*
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skia.Color

@Deprecated(
    message = "SkiaLayer is superseded by the view-decoupled render-context APIs: acquire a RenderContext with " +
        "RenderContext.createFromCurrentGLContext(), drive frames with DisplayFrameTicker(), and present each frame yourself. " +
        "SkikoSurfaceView hosts the surface. The replacement is not a drop-in expression, so no automatic " +
        "ReplaceWith is offered."
)
actual open class SkiaLayer {
    private var glView: SkikoSurfaceView? = null
    private var container: ViewGroup? = null

    actual var renderApi: GraphicsApi = GraphicsApi.OPENGL
    actual val contentScale: Float
        get() = container?.context?.resources?.displayMetrics?.density?: 1.0f

    actual var fullscreen: Boolean
        get() = true
        set(value) {
            if (value) throw IllegalArgumentException("changing fullscreen is unsupported")
        }

    actual var renderDelegate: SkikoRenderDelegate? = null

    actual fun attachTo(container: Any) {
        when (container) {
            is ViewGroup -> {
                attachTo(container)
            }
            else -> error("Cannot attach to $container")
        }
    }

    fun attachTo(container: ViewGroup) {
        initDefaultContext(container.context)

        val view = SkikoSurfaceView(container.context) { renderDelegate }
        container.addView(view)

        this.container = container
        this.glView = view

        view.setFocusableInTouchMode(true)

        needRender()
    }

    actual fun detach() {
        this.container?.let {
            it.removeView(this.glView)
            this.glView = null
        }
    }

    actual fun needRender(throttledToVsync: Boolean) {
        glView?.apply {
            scheduleFrame()
        }
    }

    actual fun needRedraw() = needRender()

    actual val pixelGeometry: PixelGeometry
        get() = PixelGeometry.UNKNOWN

    actual val component: Any?
        get() = this.container

    internal actual fun draw(canvas: Canvas): Unit = TODO()
}