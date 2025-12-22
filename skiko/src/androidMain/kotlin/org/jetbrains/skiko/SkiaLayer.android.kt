package org.jetbrains.skiko

import android.content.Context
import android.view.*
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skia.Color

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

    actual var transparency: Boolean
        get() = false
        set(value) {
            if (value) throw IllegalArgumentException("transparency unsupported")
        }

    /**
     * The background color of the layer.
     */
    actual var backgroundColor: Int = Color.WHITE
        set(value) {
            field = value
            needRender()
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

        val view = SkikoSurfaceView(container.context, this)
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