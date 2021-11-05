package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skiko.*
import org.jetbrains.skiko.context.OpenGLContextHandler
import java.util.concurrent.Executors

internal class LinuxOpenGLRedrawer(
    private val layer: SkiaLayer,
    private val properties: SkiaLayerProperties
) : Redrawer {
    private var isDisposed = false

    private var context = 0L
    private val defaultSwapInterval = if (properties.isVsyncEnabled) 1 else 0
    private var swapInterval = -1

    private fun LinuxDrawingSurface.setSwapIntervalFast(swapInterval: Int) {
        if (this@LinuxOpenGLRedrawer.swapInterval != swapInterval) {
            setSwapInterval(swapInterval)
        }
    }

    init {
        runBlocking {
            inDrawThread {
                context = it.createContext(layer.transparency)
                if (context == 0L) {
                    throw RenderException("Cannot create Linux GL context")
                }
                it.makeCurrent(context)
                if (!isVideoCardSupported(layer.renderApi)) {
                    throw RenderException("Cannot create Linux GL context")
                }
                it.setSwapIntervalFast(defaultSwapInterval)
            }
        }
    }

    private val frameJob = Job()
    @Volatile
    private var frameLimit = 0.0
    private val frameLimiter = FrameLimiter(
        CoroutineScope(Dispatchers.IO + frameJob),
        layer.backedLayer,
        onNewFrameLimit = { frameLimit = it }
    )

    private suspend fun limitFramesIfNeeded() {
        // Some Linuxes don't turn vsync on, so we apply additional frame limit (which should be no longer than enabled vsync)
        if (properties.isVsyncEnabled) {
            try {
                frameLimiter.awaitNextFrame()
            } catch (e: CancellationException) {
                // ignore
            }
        }
    }

    override fun dispose() {
        check(!isDisposed) { "LinuxOpenGLRedrawer is disposed" }

        runBlocking {
            inDrawThread {
                // makeCurrent is mandatory to destroy context, otherwise, OpenGL will destroy wrong context (from another window).
                // see the official example: https://www.khronos.org/opengl/wiki/Tutorial:_OpenGL_3.0_Context_Creation_(GLX)
                it.makeCurrent(context)
                // TODO remove in https://github.com/JetBrains/skiko/pull/300
                (layer.contextHandler as OpenGLContextHandler).disposeInOpenGLContext()
                it.destroyContext(context)
            }
        }
        runBlocking {
            frameJob.cancelAndJoin()
        }
        isDisposed = true
    }

    override fun needRedraw() {
        check(!isDisposed) { "LinuxOpenGLRedrawer is disposed" }
        toRedraw.add(this)
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() {
        check(!isDisposed) { "LinuxOpenGLRedrawer is disposed" }
        update(System.nanoTime())
        runBlocking {
            draw(withVsync = false)
        }
    }

    private fun update(nanoTime: Long) {
        layer.update(nanoTime)
    }

    private suspend fun draw(withVsync: Boolean) {
        layer.inDrawScope {
            inDrawThread {
                it.makeCurrent(context)
                layer.draw()
                it.setSwapIntervalFast(if (withVsync) defaultSwapInterval else 0)
                it.swapBuffers()
                OpenGLApi.instance.glFinish()
            }
        }
    }

    private suspend fun inDrawThread(body: (LinuxDrawingSurface) -> Unit) {
        withContext(drawDispatcher) {
            if (!isDisposed) {
                layer.backedLayer.lockLinuxDrawingSurface {
                    body(it)
                }
            }
        }
    }

    companion object {
        private val toRedraw = mutableSetOf<LinuxOpenGLRedrawer>()
        private val toRedrawCopy = mutableSetOf<LinuxOpenGLRedrawer>()
        private val toRedrawVisible = toRedrawCopy
            .asSequence()
            .filterNot(LinuxOpenGLRedrawer::isDisposed)
            .filter { it.layer.isShowing }

        private val drawDispatcher = Executors.newSingleThreadExecutor {
            Thread(it).apply {
                isDaemon = true
            }
        }.asCoroutineDispatcher()

        private val frameDispatcher = FrameDispatcher(Dispatchers.Swing) {
            toRedrawCopy.addAll(toRedraw)
            toRedraw.clear()

            // we should wait for the window with the maximum frame limit to avoid bottleneck when there is a window on a slower monitor
            toRedrawVisible.maxByOrNull { it.frameLimit }?.limitFramesIfNeeded()

            val nanoTime = System.nanoTime()

            for (redrawer in toRedrawVisible) {
                redrawer.update(nanoTime)
            }

            // TODO(demin): How can we properly synchronize multiple windows with multiple displays?
            //  I checked, and without vsync there is no tearing. Is it only my case (Ubuntu, Nvidia, X11),
            //  or Ubuntu write all the screen content into an intermediate buffer? If so, then we probably only
            //  need a frame limiter.

            // Synchronize with vsync only for the fastest monitor, for the single window.
            // Otherwise, 5 windows will wait for vsync 5 times.
            val vsyncRedrawer = toRedrawVisible
                .filter { it.properties.isVsyncEnabled }
                .maxByOrNull { it.frameLimit }

            for (redrawer in toRedrawVisible.filter { it != vsyncRedrawer }) {
                redrawer.draw(withVsync = false)
            }
            if (vsyncRedrawer?.isDisposed != true && vsyncRedrawer?.layer?.isShowing == true) {
                vsyncRedrawer.draw(withVsync = true)
            }

            // Without clearing we will have a memory leak
            toRedrawCopy.clear()
        }
    }
}

private fun LinuxDrawingSurface.createContext(transparency: Boolean) = createContext(display, transparency)
private fun LinuxDrawingSurface.destroyContext(context: Long) = destroyContext(display, context)
private fun LinuxDrawingSurface.makeCurrent(context: Long) = makeCurrent(display, window, context)
private fun LinuxDrawingSurface.swapBuffers() = swapBuffers(display, window)
private fun LinuxDrawingSurface.setSwapInterval(interval: Int) = setSwapInterval(display, window, interval)

private external fun makeCurrent(display: Long, window: Long, context: Long)
private external fun createContext(display: Long, transparency: Boolean): Long
private external fun destroyContext(display: Long, context: Long)
private external fun setSwapInterval(display: Long, window: Long, interval: Int)
private external fun swapBuffers(display: Long, window: Long)