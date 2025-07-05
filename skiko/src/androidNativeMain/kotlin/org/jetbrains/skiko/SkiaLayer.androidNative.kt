package org.jetbrains.skiko

import kotlinx.cinterop.*
import org.jetbrains.skia.*
import org.jetbrains.skiko.redrawer.Redrawer
import platform.android.*

actual open class SkiaLayer {
    actual var renderApi: GraphicsApi = GraphicsApi.OPENGL
        set(value) {
            if (value != GraphicsApi.OPENGL) {
                throw IllegalArgumentException("$field is not supported in Android Native")
            }
            field = value
        }

    actual val contentScale: Float
        get() = 1.0f

    actual var fullscreen: Boolean
        get() = false
        set(value) {
            if (value) throw IllegalArgumentException("fullscreen unsupported")
        }

    actual var transparency: Boolean = false

    internal var nativeWindow: CPointer<cnames.structs.ANativeWindow>? = null

    actual val component: Any?
        get() = this.nativeWindow

    /**
     * Implements rendering logic and events processing.
     */
    actual var renderDelegate: SkikoRenderDelegate? = null

    internal var redrawer: Redrawer? = null

    /**
     * Created/updated by recording in [update].
     * It's used as a source for drawing on canvas in [draw].
     */
    private var picture: PictureHolder? = null
    private val pictureRecorder = PictureRecorder()

    /**
     * @param container - should be an instance of [NSView]
     */
    actual fun attachTo(container: Any) {
        @Suppress("UNCHECKED_CAST")
        nativeWindow = container as CPointer<cnames.structs.ANativeWindow>
        redrawer = createNativeRedrawer(this, renderApi).apply {
            syncBounds()
            needRedraw()
        }
    }

    actual fun detach() {
        redrawer?.dispose()
        redrawer = null
    }

    /**
     * Schedules a frame to an appropriate moment.
     */
    actual fun needRedraw() {
        redrawer?.needRedraw()
    }

    /**
     * Updates the [picture] according to current [nanoTime]
     */
    internal fun update(nanoTime: Long) {
        val currentWindow = nativeWindow ?: return

        val width = ANativeWindow_getWidth(currentWindow)
        val height = ANativeWindow_getHeight(currentWindow)

        val bounds = Rect.makeWH(width.toFloat(), height.toFloat())
        val canvas = pictureRecorder.beginRecording(bounds)
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
}

actual val currentSystemTheme: SystemTheme
    get() = SystemTheme.UNKNOWN
