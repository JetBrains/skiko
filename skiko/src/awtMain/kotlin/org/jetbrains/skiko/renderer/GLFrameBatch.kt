package org.jetbrains.skiko.renderer

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.MainUIDispatcher
import org.jetbrains.skiko.OpenGLApi
import org.jetbrains.skiko.lockLinuxDrawingSurface
import org.jetbrains.skiko.unlockLinuxDrawingSurface

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

/**
 * Draws every window under its drawing-surface lock, then swaps the fastest vsync-enabled window
 * with swap interval 1 and the rest unblocked.
 */
internal object LinuxGLFrameBatch : GLFrameBatch<LinuxOpenGLRenderer>() {
    override suspend fun awaitFrameLimit() {
        // we should wait for the window with the maximum frame limit to avoid bottleneck when there is a window on a slower monitor
        toRedrawVisible.maxByOrNull { (_, renderer) -> renderer.frameLimit }?.second?.limitFramesIfNeeded()
    }

    override suspend fun drawAndPresent() {
        val drawingSurfaces = toRedrawVisible.associateWith { (_, renderer) ->
            lockLinuxDrawingSurface(renderer.layer.backedLayer)
        }
        try {
            for (window in toRedrawVisible) {
                val (driver, renderer) = window
                drawingSurfaces[window]!!.makeCurrent(renderer.context)
                driver.inFrame { scope -> renderer.drawFrame(scope) }
            }

            // TODO(demin): How can we properly synchronize multiple windows with multiple displays?
            //  I checked, and without vsync there is no tearing. Is it only my case (Ubuntu, Nvidia, X11),
            //  or Ubuntu write all the screen content into an intermediate buffer? If so, then we probably only
            //  need a frame limiter.

            // Synchronize with vsync only for the fastest monitor, for the single window.
            // Otherwise, 5 windows will wait for vsync 5 times.
            val vsyncWindow = toRedrawVisible
                .filter { (_, renderer) -> renderer.properties.isVsyncEnabled }
                .maxByOrNull { (_, renderer) -> renderer.frameLimit }

            for (window in toRedrawVisible.filter { it != vsyncWindow }) {
                val (_, renderer) = window
                drawingSurfaces[window]!!.makeCurrent(renderer.context)
                drawingSurfaces[window]!!.setSwapInterval(0)
                drawingSurfaces[window]!!.swapBuffers()
                OpenGLApi.instance.glFlush()
            }

            if (vsyncWindow != null) {
                val (_, renderer) = vsyncWindow
                drawingSurfaces[vsyncWindow]!!.makeCurrent(renderer.context)
                drawingSurfaces[vsyncWindow]!!.setSwapInterval(1)
                drawingSurfaces[vsyncWindow]!!.swapBuffers()
                OpenGLApi.instance.glFlush()
            }
        } finally {
            drawingSurfaces.values.forEach(::unlockLinuxDrawingSurface)
        }
    }
}

/**
 * Draw all, swap all, then one dwmFlush for vsync.
 */
internal object WindowsGLFrameBatch : GLFrameBatch<WindowsOpenGLRenderer>() {
    override suspend fun drawAndPresent() {
        for ((driver, renderer) in toRedrawVisible) {
            renderer.makeCurrent()
            driver.inFrame { scope -> renderer.drawFrame(scope) }
        }

        for ((_, renderer) in toRedrawVisible) {
            renderer.swapBuffers()
        }

        for ((_, renderer) in toRedrawVisible) {
            renderer.makeCurrent()
            OpenGLApi.instance.glFinish()
        }

        val isVsyncEnabled = toRedrawVisible.all { (_, renderer) -> renderer.properties.isVsyncEnabled }
        if (isVsyncEnabled) {
            withContext(dispatcherToBlockOn) {
                dwmFlush() // wait for vsync
            }
        }
    }
}
