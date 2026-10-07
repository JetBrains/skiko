package org.jetbrains.skiko.renderer

import kotlinx.coroutines.CancellationException
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.MainUIDispatcher

/**
 * The OpenGL backends draw every on-screen window from one cross-window batch per family, waiting
 * for vsync once per tick instead of once per window: per-window vsync waits would serialize, so
 * N windows would run at 1/N of the refresh rate. A window enters the batch each time its
 * [forWindow] scheduler schedules a frame and leaves it when the tick completes; disposed and
 * hidden windows are skipped at draw time.
 */
internal abstract class GLFrameBatch<R : AbstractOpenGLRenderer> {
    private val toRedraw = mutableSetOf<Pair<FrameProducer, R>>()
    private val toRedrawCopy = mutableSetOf<Pair<FrameProducer, R>>()
    protected val toRedrawVisible: Sequence<Pair<FrameProducer, R>> = toRedrawCopy
        .asSequence()
        .filterNot { (_, renderer) -> renderer.isDisposed }
        .filter { (_, renderer) -> renderer.layer.isShowing }

    /** One window's [FrameScheduler], scheduling into this family batch. */
    fun forWindow(producer: FrameProducer, renderer: R): FrameScheduler = object : FrameScheduler {
        override fun scheduleFrame(throttledToVsync: Boolean) {
            toRedraw.add(producer to renderer)
            frameDispatcher.scheduleFrame()
        }
    }

    /** Runs before the tick records anything; the place to wait out a frame limit. */
    protected open suspend fun awaitFrameLimit() {}

    /** Records and draws every window of [toRedrawVisible]; each frame binds its own GL context. */
    protected fun drawAll() {
        for ((producer, renderer) in toRedrawVisible) {
            producer.inFrame { with(renderer) { drawFrame() } }
        }
    }

    /** Draws and presents every window of [toRedrawVisible], with this family's one vsync wait. */
    protected abstract suspend fun drawAndPresent()

    private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
        toRedrawCopy.addAll(toRedraw)
        toRedraw.clear()

        awaitFrameLimit()

        val nanoTime = System.nanoTime()
        for ((producer, _) in toRedrawVisible) {
            try {
                producer.update(nanoTime)
            } catch (e: CancellationException) {
                // continue
            }
        }

        drawAndPresent()

        // Without clearing we will have a memory leak
        toRedrawCopy.clear()
    }
}
