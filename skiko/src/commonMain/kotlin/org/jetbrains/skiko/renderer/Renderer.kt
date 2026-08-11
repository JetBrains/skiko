package org.jetbrains.skiko.renderer

import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import kotlin.time.TimeSource

private val initialTime = TimeSource.Monotonic.markNow()
internal fun renderTime() = initialTime.elapsedNow().inWholeNanoseconds

/**
 * Draws the content of [layer] on the screen, using a specific graphics API.
 *
 * A [Renderer] owns both the frame loop (when to render and present a frame) and the Skia objects the content is
 * drawn into: the [context], the [surface] and its [canvas].
 */
internal abstract class Renderer(
    protected val layer: SkiaLayer
) {
    protected var context: DirectContext? = null
    protected var renderTarget: BackendRenderTarget? = null
    protected var surface: Surface? = null
    protected var canvas: Canvas? = null

    /**
     * Human-readable information about this renderer and the device it draws on.
     */
    open val renderInfo: String
        get() = "GraphicsApi: ${layer.renderApi}\n" +
                "OS: ${hostOs.id} ${hostArch.id}\n"

    /**
     * Schedules the rendering and presentation of a frame at an appropriate moment.
     *
     * @param throttledToVsync Whether to throttle updating the content to at most once between vsync signals
     * (if vsync is enabled).
     */
    abstract fun needRender(throttledToVsync: Boolean)

    /**
     * Renders and presents a frame synchronously.
     */
    abstract fun renderImmediately()

    /**
     * Updates the content to be drawn; The actual drawing happens in [draw].
     */
    abstract fun update(nanoTime: Long = renderTime())

    /**
     * Synchronizes the size and position of the drawing surface with the platform component of [layer].
     */
    open fun syncBoundsFromPlatformComponent() = Unit

    open fun setVisible(isVisible: Boolean) = Unit

    /**
     * Invoked by AWT [SkiaLayer] when the underlying Swing component is resized. Unused in other source-sets.
     */
    open fun onLayerComponentResized() = Unit

    open fun isTransparentBackgroundSupported(): Boolean {
        if (hostOs == OS.MacOS) {
            // macOS transparency is always supported
            return true
        }

        // for non-macOS in fullscreen transparency is not supported
        return !layer.fullscreen
    }

    /**
     * Initializes the graphics context, if needed; Returns whether the context is usable.
     */
    protected abstract fun initContext(): Boolean

    /**
     * Prepares [canvas] (and, depending on the graphics API, [surface] and [renderTarget]) for drawing a frame.
     */
    protected abstract fun LayerDrawScope.initCanvas()

    protected fun drawContent(canvas: Canvas) {
        layer.draw(canvas)
    }

    protected open fun flush() {
        context?.flush()
    }

    // throws RenderException if initialization of graphic context was not successful
    fun LayerDrawScope.draw(flush: Boolean = true) {
        if (!initContext()) {
            throw RenderException("Cannot init graphic context")
        }
        initCanvas()
        canvas?.runRestoringState {
            clear(Color.TRANSPARENT)
            drawContent(this)
        }
        if (flush) {
            flush()
        }
    }

    open fun dispose() {
        disposeCanvas()
        context?.close()
    }

    protected open fun disposeCanvas() {
        surface?.close()
        renderTarget?.close()
    }
}
