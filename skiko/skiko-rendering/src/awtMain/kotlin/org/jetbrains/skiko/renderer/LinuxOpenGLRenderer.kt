package org.jetbrains.skiko.renderer

import kotlinx.coroutines.*
import org.jetbrains.skia.*
import org.jetbrains.skiko.*

internal class LinuxOpenGLRenderer(
    layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    internal val properties: SkiaLayerProperties
) : AbstractOpenGLRenderer(layer, analytics) {
    init {
        loadOpenGLLibrary()
    }

    private var context = 0L
    private val swapInterval = if (properties.isVsyncEnabled) 1 else 0

    private var lockedDrawingSurface: LinuxDrawingSurface? = null

    /**
     * Locks this window's drawing surface; [makeCurrent] and [swapBuffers] use it until
     * [unlockDrawingSurface].
     */
    internal fun lockDrawingSurface(): LinuxDrawingSurface {
        val surface = lockLinuxDrawingSurface(layer.backedLayer)
        lockedDrawingSurface = surface
        return surface
    }

    internal fun unlockDrawingSurface() {
        val surface = lockedDrawingSurface ?: return
        lockedDrawingSurface = null
        unlockLinuxDrawingSurface(surface)
    }

    override fun makeCurrent() {
        checkNotNull(lockedDrawingSurface).makeCurrent(context)
    }

    internal fun swapBuffers(interval: Int) {
        makeCurrent()
        val surface = checkNotNull(lockedDrawingSurface)
        surface.setSwapInterval(interval)
        surface.swapBuffers()
        OpenGLApi.instance.glFlush()
    }

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

    private val frameJob = Job()
    @Volatile
    internal var frameLimit = 0.0
        private set
    private val frameLimiter = layerFrameLimiter(
        CoroutineScope(frameJob),
        layer.backedLayer,
        onNewFrameLimit = { frameLimit = it }
    )

    internal suspend fun limitFramesIfNeeded() {
        // Some Linuxes don't turn vsync on, so we apply additional frame limit (which should be no longer than enabled vsync)
        if (properties.isVsyncEnabled) {
            try {
                frameLimiter.awaitNextFrame()
            } catch (e: CancellationException) {
                // ignore
            }
        }
    }

    override fun releaseResources() {
        frameJob.cancel()
        layer.backedLayer.lockLinuxDrawingSurface {
            // makeCurrent is mandatory to destroy context, otherwise, OpenGL will destroy wrong context (from another window).
            // see the official example: https://www.khronos.org/opengl/wiki/Tutorial:_OpenGL_3.0_Context_Creation_(GLX)
            it.makeCurrent(context)
            disposeGlResources()
            it.destroyContext(context)
        }
    }

    override suspend fun LayerDrawScope.renderFrame(immediate: Boolean) {
        val surface = lockDrawingSurface()
        try {
            drawFrame()
            val turnOfVsync = properties.isVsyncEnabled && !SkikoProperties.linuxWaitForVsyncOnRedrawImmediately
            if (turnOfVsync) {
                surface.setSwapInterval(0)
            }
            surface.swapBuffers()
            OpenGLApi.instance.glFlush()
            if (turnOfVsync) {
                surface.setSwapInterval(swapInterval)
            }
        } finally {
            unlockDrawingSurface()
        }
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
        // Unlock from a snapshot: user code inside the draw can hide or dispose a layer, which
        // removes it from toRedrawVisible while its surface still needs to be unlocked.
        val windows = toRedrawVisible.toList()
        windows.forEach { (_, renderer) -> renderer.lockDrawingSurface() }
        try {
            drawAll()

            // TODO(demin): How can we properly synchronize multiple windows with multiple displays?
            //  I checked, and without vsync there is no tearing. Is it only my case (Ubuntu, Nvidia, X11),
            //  or Ubuntu write all the screen content into an intermediate buffer? If so, then we probably only
            //  need a frame limiter.

            // Synchronize with vsync only for the fastest monitor, for the single window.
            // Otherwise, 5 windows will wait for vsync 5 times.
            val vsyncWindow = toRedrawVisible
                .filter { (_, renderer) -> renderer.properties.isVsyncEnabled }
                .maxByOrNull { (_, renderer) -> renderer.frameLimit }

            for ((_, renderer) in toRedrawVisible.filter { it != vsyncWindow }) {
                renderer.swapBuffers(interval = 0)
            }
            vsyncWindow?.second?.swapBuffers(interval = 1)
        } finally {
            windows.forEach { (_, renderer) -> renderer.unlockDrawingSurface() }
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