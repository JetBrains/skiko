package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skiko.*

internal class LinuxOpenGLRedrawer(
    private val layer: SkiaLayer,
    private val properties: SkiaLayerProperties
) : Redrawer {
    private var isDisposed = false
    private var context = 0L
    private val swapInterval = if (properties.isVsyncEnabled) 1 else 0

    init {
    	layer.backedLayer.lockLinuxDrawingSurface {
            context = it.createContext()
            it.makeCurrent(context)
            if (context == 0L || !isVideoCardSupported(layer.renderApi)) {
                throw IllegalArgumentException("Cannot create Linux GL context")
            }
            it.setSwapInterval(swapInterval)
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
        layer.backedLayer.lockLinuxDrawingSurface {
            it.destroyContext(context)
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

    override fun redrawImmediately() = layer.backedLayer.lockLinuxDrawingSurface {
        check(!isDisposed) { "LinuxOpenGLRedrawer is disposed" }
        update(System.nanoTime())
        it.makeCurrent(context)
        draw()
        it.setSwapInterval(0)
        it.swapBuffers()
        OpenGLApi.instance.glFinish()
        it.setSwapInterval(swapInterval)
    }

    private fun update(nanoTime: Long) {
        layer.update(nanoTime)
    }

    private fun draw() {
        if (layer.prepareDrawContext()) {
            layer.draw()
        }
    }

    companion object {
        private val toRedraw = mutableSetOf<LinuxOpenGLRedrawer>()
        private val toRedrawCopy = mutableSetOf<LinuxOpenGLRedrawer>()
        private val toRedrawVisible = toRedrawCopy
            .asSequence()
            .filterNot(LinuxOpenGLRedrawer::isDisposed)
            .filter { it.layer.isShowing }

        private val frameDispatcher = FrameDispatcher(Dispatchers.Swing) {
            // we should wait for the window with the maximum frame limit to avoid bottleneck when there is a window on a slower monitor
            toRedrawVisible.maxByOrNull { it.frameLimit }?.limitFramesIfNeeded()

            toRedrawCopy.clear()
            toRedrawCopy.addAll(toRedraw)
            toRedraw.clear()

            val nanoTime = System.nanoTime()

            for (redrawer in toRedrawVisible) {
                try {
                    redrawer.update(nanoTime)
                } catch (e: CancellationException) {
                    // continue
                }
            }

            val drawingSurfaces = toRedrawVisible.associateWith { lockLinuxDrawingSurface(it.layer.backedLayer) }
            try {
                toRedrawVisible.forEach { redrawer ->
                    drawingSurfaces[redrawer]!!.makeCurrent(redrawer.context)
                    redrawer.draw()
                }

                // TODO(demin) it seems now vsync doesn't work as expected with two windows (we have fps = refreshRate / windowCount)
                //  perhaps we should create frameDispatcher for each display.
                //  Don't know what happened, but on 620547a commit everything was okay. maybe something changed in the code, maybe my system changed
                toRedrawVisible.forEach { redrawer ->
                    drawingSurfaces[redrawer]!!.swapBuffers()
                }

                toRedrawVisible.forEach { redrawer ->
                    drawingSurfaces[redrawer]!!.makeCurrent(redrawer.context)
                    OpenGLApi.instance.glFinish()
                }
            } finally {
                drawingSurfaces.values.forEach(::unlockLinuxDrawingSurface)
            }
        }
    }
}

private fun LinuxDrawingSurface.createContext() = createContext(display)
private fun LinuxDrawingSurface.destroyContext(context: Long) = destroyContext(display, context)
private fun LinuxDrawingSurface.makeCurrent(context: Long) = makeCurrent(display, window, context)
private fun LinuxDrawingSurface.swapBuffers() = swapBuffers(display, window)
private fun LinuxDrawingSurface.setSwapInterval(interval: Int) = setSwapInterval(display, window, interval)

private external fun makeCurrent(display: Long, window: Long, context: Long)
private external fun createContext(display: Long): Long
private external fun destroyContext(display: Long, context: Long)
private external fun setSwapInterval(display: Long, window: Long, interval: Int)
private external fun swapBuffers(display: Long, window: Long)