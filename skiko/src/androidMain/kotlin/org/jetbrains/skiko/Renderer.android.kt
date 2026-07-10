package org.jetbrains.skiko

import android.content.Context
import android.opengl.GLSurfaceView
import android.widget.LinearLayout
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.jetbrains.skia.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

internal interface FrameManager {
    fun onFrameCompleted()
}

/**
 * A [GLSurfaceView] that renders a [SkikoRenderDelegate] through the internal Android GLES render context.
 *
 * The render loop is **demand-driven**: the view uses [GLSurfaceView.RENDERMODE_WHEN_DIRTY] and renders
 * exactly one frame per [scheduleFrame], gated by [frameAck]. It does NOT animate continuously at the
 * display refresh rate. The [frameDispatcher] lives for the view instance's lifetime (it is not cancelled on
 * detach), so a recycled/reattached view keeps rendering.
 *
 * Construction is decoupled from [SkiaLayer]: it takes a render-delegate provider. The
 * `(Context, SkiaLayer)` constructor and the [layer] accessor remain for one deprecation cycle.
 */
class SkikoSurfaceView internal constructor(
    context: Context,
    renderDelegateProvider: () -> SkikoRenderDelegate?,
) : GLSurfaceView(context), FrameManager {

    /**
     * Renders [layer]'s delegate into this view.
     */
    @Deprecated(
        message = "SkikoSurfaceView is being decoupled from SkiaLayer. Construct it from a " +
            "SkikoRenderDelegate provider instead; this constructor (and the `layer` property) will be " +
            "removed in a future release.",
        level = DeprecationLevel.WARNING,
    )
    constructor(context: Context, layer: SkiaLayer) : this(context, { layer.renderDelegate }) {
        deprecatedLayer = layer
    }

    private var deprecatedLayer: SkiaLayer? = null

    /**
     * The [SkiaLayer] this view was constructed with, if the deprecated `(Context, SkiaLayer)` constructor
     * was used. Kept for one deprecation cycle only.
     */
    @Deprecated(
        message = "SkikoSurfaceView is being decoupled from SkiaLayer; this accessor will be removed in a " +
            "future release.",
        level = DeprecationLevel.WARNING,
    )
    val layer: SkiaLayer
        get() = deprecatedLayer
            ?: error("This SkikoSurfaceView was not constructed with a SkiaLayer")

    private val renderer = SkikoSurfaceRender(renderDelegateProvider, this)

    init {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        setEGLConfigChooser(8, 8, 8, 0, 24, 8)
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        setRenderMode(RENDERMODE_WHEN_DIRTY)
    }

    private val frameAck = Channel<Unit>(Channel.CONFLATED)

    override fun onFrameCompleted() {
        frameAck.trySend(Unit)
    }

    private val frameDispatcher = FrameDispatcher(Dispatchers.Main) {
        renderer.update()
        requestRender()
        frameAck.receive()
    }

    fun scheduleFrame() {
        frameDispatcher.scheduleFrame()
    }

    override fun onDetachedFromWindow() {
        // Tear the GLES render context down on the GL thread, while its EGL context is still current, so we
        // never free Skia GPU objects concurrently with an in-flight frame. The queued event runs before the
        // GL thread stops in super.onDetachedFromWindow(). The frameDispatcher is intentionally left alive so
        // a recycled/reattached view keeps working (its render context is rebuilt lazily on the next frame).
        queueEvent { renderer.dispose() }
        super.onDetachedFromWindow()
    }
}

private class SkikoSurfaceRender(
    private val renderDelegateProvider: () -> SkikoRenderDelegate?,
    private val manager: FrameManager,
) : GLSurfaceView.Renderer {
    private var width: Int = 0
    private var height: Int = 0

    @Volatile
    private var picture: PictureHolder? = null
    private var pictureRecorder: PictureRecorder = PictureRecorder()
    private val pictureLock = Any()

    // Owned and driven exclusively on the GL thread.
    private var renderContext: AndroidGLRenderContext? = null

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
        renderDelegateProvider()?.let {
            val bounds = Rect.makeWH(width.toFloat(), height.toFloat())
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

    // This method is called from the GL rendering thread whenever the EGL context is (re)created. On resume
    // after an onPause the previous EGL context is gone, so the cached render context references dead GL
    // objects; drop it (abandoning its GPU resources without issuing GL calls) so it is rebuilt lazily.
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        renderContext?.abandon()
        renderContext = null
    }

    // This method is called from the GL rendering thread.
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    // This method is called from the GL rendering thread; it rasterizes the recorded Skia picture through
    // the render context. It always acknowledges the frame so the demand-driven loop never stalls.
    override fun onDrawFrame(gl: GL10?) {
        try {
            if (width == 0 || height == 0) return
            val context = renderContext ?: AndroidGLRenderContext().also { renderContext = it }
            // A null surface means the context was closed after this frame was requested: safely no-op.
            val surface = context.acquireSurface(width, height) ?: return
            surface.canvas.clear(Color.WHITE)
            lockPicture { surface.canvas.drawPicture(it.instance) }
            context.present()
        } finally {
            manager.onFrameCompleted()
        }
    }

    // Called on the GL thread (via GLSurfaceView.queueEvent) during detach. The render context is closed but
    // its reference is intentionally kept: if a late onDrawFrame still fires, it observes a closed context
    // and no-ops instead of recreating one during teardown.
    fun dispose() {
        renderContext?.close()
    }
}
