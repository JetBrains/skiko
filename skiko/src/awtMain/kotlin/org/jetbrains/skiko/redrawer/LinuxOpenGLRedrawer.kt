package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.*
import org.jetbrains.skiko.*
import org.jetbrains.skiko.context.OpenGLContextHandler

internal class LinuxOpenGLRedrawer(
    private val layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    private val properties: SkiaLayerProperties
) : AWTRedrawer(layer, analytics, GraphicsApi.OPENGL) {
    init {
        loadOpenGLLibrary()
    }

    private val contextHandler = OpenGLContextHandler(layer)
    override val renderInfo: String get() = contextHandler.rendererInfo()

    private var context = 0L
    private val swapInterval = if (properties.isVsyncEnabled) 1 else 0

    init {
    	layer.backedLayer.lockLinuxDrawingSurface {
            context = it.createContext(layer.transparency)
            if (context == 0L) {
                throw RenderException("Cannot create Linux GL context")
            }
            it.makeCurrent(context)
            adapterName.also { adapterName ->
                if (adapterName != null && !isVideoCardSupported(GraphicsApi.OPENGL, hostOs, adapterName)) {
                    throw RenderException("Cannot create Linux GL context")
                }
            }
            onDeviceChosen(adapterName)
            it.setSwapInterval(swapInterval)
        }
        onContextInit()
    }

    private val adapterName get() = OpenGLApi.instance.glGetString(OpenGLApi.instance.GL_RENDERER)

    private val frameJob = Job()
    @Volatile
    private var frameLimit = 0.0
    private val frameLimiter = layerFrameLimiter(
        CoroutineScope(frameJob),
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
        frameJob.cancel()
        layer.backedLayer.lockLinuxDrawingSurface {
            // makeCurrent is mandatory to destroy context, otherwise, OpenGL will destroy wrong context (from another window).
            // see the official example: https://www.khronos.org/opengl/wiki/Tutorial:_OpenGL_3.0_Context_Creation_(GLX)
            it.makeCurrent(context)
            contextHandler.dispose()
            it.destroyContext(context)
        }
        super.dispose()
    }

    override fun needRedraw() {
        check(!isDisposed) { "LinuxOpenGLRedrawer is disposed" }
        toRedraw.add(this)
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() = layer.backedLayer.lockLinuxDrawingSurface {
        check(!isDisposed) { "LinuxOpenGLRedrawer is disposed" }
        update(System.nanoTime())
        inDrawScope {
            it.makeCurrent(context)
            contextHandler.draw()
            it.setSwapInterval(0)
            it.swapBuffers()
            OpenGLApi.instance.glFinish()
            it.setSwapInterval(swapInterval)
        }
    }

    private fun draw() {
        inDrawScope(contextHandler::draw)
    }

    companion object {
        private val toRedraw = mutableSetOf<LinuxOpenGLRedrawer>()
        private val toRedrawCopy = mutableSetOf<LinuxOpenGLRedrawer>()
        private val toRedrawVisible = toRedrawCopy
            .asSequence()
            .filterNot(LinuxOpenGLRedrawer::isDisposed)
            .filter { it.layer.isShowing }

        private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
            toRedrawCopy.addAll(toRedraw)
            toRedraw.clear()

            // we should wait for the window with the maximum frame limit to avoid bottleneck when there is a window on a slower monitor
            toRedrawVisible.maxByOrNull { it.frameLimit }?.limitFramesIfNeeded()

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
                for (redrawer in toRedrawVisible) {
                    drawingSurfaces[redrawer]!!.makeCurrent(redrawer.context)
                    redrawer.draw()
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
                    drawingSurfaces[redrawer]!!.makeCurrent(redrawer.context)
                    drawingSurfaces[redrawer]!!.setSwapInterval(0)
                    drawingSurfaces[redrawer]!!.swapBuffers()
                    OpenGLApi.instance.glFinish()
                }

                if (vsyncRedrawer != null) {
                    drawingSurfaces[vsyncRedrawer]!!.makeCurrent(vsyncRedrawer.context)
                    drawingSurfaces[vsyncRedrawer]!!.setSwapInterval(1)
                    drawingSurfaces[vsyncRedrawer]!!.swapBuffers()
                    OpenGLApi.instance.glFinish()
                }
            } finally {
                drawingSurfaces.values.forEach(::unlockLinuxDrawingSurface)
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