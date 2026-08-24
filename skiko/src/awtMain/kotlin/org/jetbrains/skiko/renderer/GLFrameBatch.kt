package org.jetbrains.skiko.renderer

import kotlinx.coroutines.CancellationException
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.MainUIDispatcher

/**
 * The OpenGL backends draw every on-screen window from one cross-window batch per family, waiting
 * for vsync once per tick instead of once per window: per-window vsync waits would serialize, so
 * N windows would run at 1/N of the refresh rate. A window enters the batch each time its
 * [FrameDriver] schedules a frame and leaves it when the tick completes; disposed and hidden
 * windows are skipped at draw time.
 */
internal abstract class GLFrameBatch<R : AbstractOpenGLRenderer> {
    private val toRedraw = mutableSetOf<Pair<FrameDriver, R>>()
    private val toRedrawCopy = mutableSetOf<Pair<FrameDriver, R>>()
    protected val toRedrawVisible: Sequence<Pair<FrameDriver, R>> = toRedrawCopy
        .asSequence()
        .filterNot { (_, renderer) -> renderer.isDisposed }
        .filter { (_, renderer) -> renderer.layer.isShowing }

    fun scheduleFrame(driver: FrameDriver, renderer: R) {
        toRedraw.add(driver to renderer)
        frameDispatcher.scheduleFrame()
    }

    /** Runs before the tick records anything; the place to wait out a frame limit. */
    protected open suspend fun awaitFrameLimit() {}

    /** Records and draws every window of [toRedrawVisible]; each frame binds its own GL context. */
    protected fun drawAll() {
        for ((driver, renderer) in toRedrawVisible) {
            driver.inFrame { scope -> renderer.drawFrame(scope) }
        }
    }

    /** Draws and presents every window of [toRedrawVisible], with this family's one vsync wait. */
    protected abstract suspend fun drawAndPresent()

    private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
        toRedrawCopy.addAll(toRedraw)
        toRedraw.clear()

        awaitFrameLimit()

        val nanoTime = System.nanoTime()
        for ((driver, _) in toRedrawVisible) {
            try {
                driver.updateIfRequested(nanoTime)
            } catch (e: CancellationException) {
                // continue
            }
        }

        drawAndPresent()

        // Without clearing we will have a memory leak
        toRedrawCopy.clear()
    }
}
