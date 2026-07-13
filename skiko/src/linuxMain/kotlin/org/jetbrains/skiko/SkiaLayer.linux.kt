package org.jetbrains.skiko

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.PictureRecorder
import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skiko.redrawer.Redrawer

actual open class SkiaLayer {
    actual var renderApi: GraphicsApi = GraphicsApi.OPENGL
        set(value) {
            if (value != GraphicsApi.OPENGL) {
                throw IllegalArgumentException("$field is not supported in Linux native")
            }
            field = value
        }

    actual val contentScale: Float
        get() = if (this::window.isInitialized) window.contentScale else 1.0f

    actual var fullscreen: Boolean = false

    internal lateinit var window: LinuxWindow
        private set

    actual val component: Any?
        get() = if (this::window.isInitialized) window else null

    actual var renderDelegate: SkikoRenderDelegate? = null

    internal var redrawer: Redrawer? = null

    actual fun attachTo(container: Any) {
        check(!this::window.isInitialized) { "Already attached to another window" }
        check(container is LinuxWindow) { "container should be an instance of LinuxWindow (e.g. X11Window)" }
        window = container
        redrawer = createNativeRedrawer(this, renderApi).apply {
            syncBounds()
            needRender()
        }
    }

    actual fun detach() {
        redrawer?.dispose()
        redrawer = null
    }

    actual fun needRender(throttledToVsync: Boolean) {
        redrawer?.needRender(throttledToVsync)
    }

    @Deprecated(
        message = "Use needRender() instead",
        replaceWith = ReplaceWith("needRender()"),
    )
    actual fun needRedraw() = needRender()

    private var picture: PictureHolder? = null
    private val pictureRecorder = PictureRecorder()

    internal fun update(nanoTime: Long) {
        // LinuxWindow geometry is physical pixels (unlike macOS points), so the surface
        // size is the window size as-is; contentScale only informs UI density.
        val width = window.width.coerceAtLeast(0)
        val height = window.height.coerceAtLeast(0)

        val canvas = pictureRecorder.beginRecording(0f, 0f, width.toFloat(), height.toFloat()).apply {
            clear(Color.WHITE)
        }
        renderDelegate?.onRender(canvas, width, height, nanoTime)

        val picture = pictureRecorder.finishRecordingAsPicture()
        this.picture = PictureHolder(picture, width, height)
    }

    internal actual fun draw(canvas: Canvas) {
        picture?.also {
            canvas.drawPicture(it.instance)
        }
    }

    actual val pixelGeometry: PixelGeometry
        get() = PixelGeometry.UNKNOWN

    private fun createDrawScope() = LayerDrawScope(
        pixelGeometry = pixelGeometry,
        scaledLayerWidth = window.width.coerceAtLeast(0),
        scaledLayerHeight = window.height.coerceAtLeast(0),
    )

    internal fun inDrawScope(block: LayerDrawScope.() -> Unit) {
        createDrawScope().block()
    }
}

actual val currentSystemTheme: SystemTheme
    get() = SystemTheme.UNKNOWN
