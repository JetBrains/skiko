package org.jetbrains.skiko

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.PictureRecorder
import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skia.Rect
import org.jetbrains.skiko.Logger
import org.jetbrains.skiko.redrawer.Redrawer

/**
 * SkiaLayer implementation for Kotlin/Native Linux.
 *
 * Rendering is driven by a platform [Redrawer] (OpenGL/software) created in [attachTo].
 * Content is recorded into a [org.jetbrains.skia.Picture] in [update] and then drawn by the redrawer via [draw],
 */
actual open class SkiaLayer {
    internal var x11Window: X11SkikoWindow? = null
        private set

    internal var redrawer: Redrawer? = null
        private set

    actual var renderApi: GraphicsApi = GraphicsApi.OPENGL

    actual val contentScale: Float
        get() = x11Window?.contentScale ?: 1f

    actual var fullscreen: Boolean = false
    actual var transparency: Boolean = false

    actual val component: Any?
        get() = x11Window

    actual fun needRender(throttledToVsync: Boolean) {
        redrawer?.needRender(throttledToVsync)
    }

    @Deprecated(
        message = "Use needRender() instead",
        replaceWith = ReplaceWith("needRender()")
    )
    actual fun needRedraw() = needRender()

    actual var renderDelegate: SkikoRenderDelegate? = null

    internal var picture: PictureHolder? = null
        private set
    private val pictureRecorder = PictureRecorder()

    actual fun attachTo(container: Any) {
        check(redrawer == null) { "SkiaLayer is already attached" }
        check(container is X11SkikoWindow) { "Linux SkiaLayer expects X11SkikoWindow, got: ${container::class}" }
        x11Window = container

        redrawer = createNativeRedrawer(this, renderApi).apply {
            syncBounds()
            needRender(throttledToVsync = true)
        }
    }

    actual fun detach() {
        redrawer?.dispose()
        redrawer = null

        picture?.instance?.close()
        picture = null
        x11Window = null
        renderDelegate = null
    }

    internal fun tryFallbackFromRenderFailure(cause: Throwable): Boolean {
        val currentApi = renderApi
        val nextApi = when (currentApi) {
            GraphicsApi.OPENGL -> GraphicsApi.SOFTWARE_FAST
            GraphicsApi.SOFTWARE_FAST, GraphicsApi.SOFTWARE_COMPAT -> return false
            else -> GraphicsApi.OPENGL
        }

        Logger.warn(cause) { "Render failed with $currentApi, falling back to $nextApi" }

        val old = redrawer
        return try {
            renderApi = nextApi
            val replacement = createNativeRedrawer(this, nextApi).also { it.syncBounds() }
            old?.dispose()
            redrawer = replacement
            redrawer?.needRender(throttledToVsync = true)
            true
        } catch (fallbackError: Throwable) {
            renderApi = currentApi
            Logger.error(fallbackError) { "Failed to fallback from $currentApi to $nextApi" }
            false
        }
    }

    internal fun update(nanoTime: Long) {
        val x11Window = x11Window ?: return

        val pictureWidth = (x11Window.width * contentScale).toInt().coerceAtLeast(0)
        val pictureHeight = (x11Window.height * contentScale).toInt().coerceAtLeast(0)

        val bounds = Rect.makeWH(pictureWidth.toFloat(), pictureHeight.toFloat())
        val canvas = pictureRecorder.beginRecording(bounds)
        renderDelegate?.onRender(canvas, pictureWidth, pictureHeight, nanoTime)

        picture?.instance?.close()
        val picture = pictureRecorder.finishRecordingAsPicture()
        this.picture = PictureHolder(picture, pictureWidth, pictureHeight)
    }

    internal actual fun draw(canvas: Canvas) {
        picture?.also {
            canvas.drawPicture(it.instance)
        }
    }

    actual val pixelGeometry: PixelGeometry
        get() = PixelGeometry.UNKNOWN
}
