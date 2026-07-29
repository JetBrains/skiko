package org.jetbrains.skiko.rendercontext

import kotlinx.coroutines.*
import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Every GLX call runs inside [org.jetbrains.skiko.lockLinuxDrawingSurface]. The per-frame body runs off the
 * EDT on [dispatcherToBlockOn], with [withCurrentContext] binding and releasing the context around it, which
 * is what makes running on shared pooled threads safe.
 *
 * On X11 the JAWT drawing-surface lock is the process-wide AWT lock, so a swap waiting for vblank holds it
 * whichever thread runs it. Only one window per process waits per frame; see [swapIntervalForFrame].
 */
internal class LinuxOpenGLRenderContext(
    host: AwtSurfaceHost,
    analytics: SkiaLayerAnalytics,
    private val properties: SkiaLayerProperties
) : AbstractOpenGLRenderContext(host, analytics, properties) {
    init {
        loadOpenGLLibrary()
    }

    /**
     * Guards every native/JNI touch point: the GLX context lifetime, the Skia [DirectContext]/surface, and
     * presentation. Frames run on the EDT, but [acquireSurface] and [present] are public entry points a
     * caller drives from its own render thread, and [releaseResources] can arrive on the EDT while one of those is in
     * flight. Each takes this lock and re-checks [isDisposed] *inside* it before any native call, mirroring
     * [MetalRenderContext]'s and [Direct3DRenderContext]'s discipline, so [releaseResources] cannot free the GLX
     * context out from under a running JNI call.
     */
    private val drawLock = Any()

    private var context = 0L

    init {
    	host.backedLayer.lockLinuxDrawingSurface {
            context = it.createContext(host.transparency)
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
        }
        onContextInit()
        liveContexts.add(this)
    }

    private val frameJob = Job()
    @Volatile
    private var frameLimit = 0.0
    private val frameLimiter = layerFrameLimiter(
        CoroutineScope(frameJob),
        host.backedLayer,
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

    override suspend fun runFrame(frame: suspend () -> Unit) {
        // Gate the software frame limiter on visibility: pace only while the window is showing.
        if (host.isShowing) {
            limitFramesIfNeeded()
        }
        frame()
    }

    override fun releaseResources() {
        liveContexts.remove(this)
        frameJob.cancel()
        synchronized(drawLock) {
            // makeCurrent is mandatory to destroy context, otherwise, OpenGL will destroy wrong context (from another window).
            // see the official example: https://www.khronos.org/opengl/wiki/Tutorial:_OpenGL_3.0_Context_Creation_(GLX)
            withCurrentContext { disposeGlResources() }
            host.backedLayer.lockLinuxDrawingSurface { it.destroyContext(context) }
        }
    }

    /**
     * Locks the window's drawing surface, binds the GLX context to the calling thread for [body], and
     * releases it again before returning.
     *
     * The release is what lets frames run on pooled threads: a context left bound stays current on whichever
     * thread last used it, and binding it from a second thread while the first still holds it is an error.
     * Releasing here means no thread can inherit another's binding, so which pooled thread runs which
     * window's frame stops mattering.
     */
    private inline fun <T> withCurrentContext(body: (LinuxDrawingSurface) -> T): T =
        host.backedLayer.lockLinuxDrawingSurface {
            it.makeCurrent(context)
            try {
                body(it)
            } finally {
                it.releaseCurrent()
            }
        }

    override suspend fun renderFrame(scope: LayerDrawScope, immediate: Boolean) {
        if (immediate) {
            drawAndSwap(scope, immediate = true)
        } else {
            // GLX fuses the vblank wait into glXSwapBuffers, so the wait cannot be hoisted out on its own
            // the way Windows does it with dwmFlush; the whole frame body moves off the EDT instead.
            withContext(dispatcherToBlockOn) {
                drawAndSwap(scope, immediate = false)
            }
        }
    }

    private fun drawAndSwap(scope: LayerDrawScope, immediate: Boolean) = synchronized(drawLock) {
        // Re-check inside the lock (not just at the call site): this is what makes `dispose` and an
        // in-flight frame mutually exclusive rather than merely racing on `isDisposed`.
        if (isDisposed) {
            return
        }
        withCurrentContext {
            with(scope) { drawFrame() }
            it.setSwapInterval(swapIntervalForFrame(immediate))
            it.swapBuffers()
            OpenGLApi.instance.glFlush()
        }
    }

    override fun acquireSurface(width: Int, height: Int): Surface = synchronized(drawLock) {
        check(!isDisposed) { "LinuxOpenGLRenderContext is disposed" }
        withCurrentContext {
            if (!ensureContext()) {
                throw RenderException("Cannot init graphic context")
            }
            createSurface(width, height, host.pixelGeometry)
            glSurface ?: throw RenderException("Cannot create surface for ${width}x$height")
        }
    }

    override fun present() {
        if (isDisposed) return
        synchronized(drawLock) {
            if (isDisposed) return
            withCurrentContext {
                flushGl()
                it.swapBuffers()
                OpenGLApi.instance.glFlush()
            }
        }
    }

    /**
     * The GLX swap interval for the frame about to be presented: 1 blocks [swapBuffers] until the next
     * vblank, 0 returns immediately.
     *
     * At most one window in the process may block. Every GLX call holds its JAWT drawing-surface lock, which
     * on X11 is the process-wide AWT lock, so a swap that waits for vblank holds the whole toolkit for that
     * vblank regardless of which thread runs it; N blocking windows would cost N vblanks per frame. The
     * window with the highest frame limit is the one that waits, so a window on a slower monitor cannot pace
     * down the rest; the others swap unblocked and are paced by [frameLimiter].
     */
    private fun swapIntervalForFrame(immediate: Boolean): Int = when {
        !properties.isVsyncEnabled -> 0
        immediate -> if (SkikoProperties.linuxWaitForVsyncOnRedrawImmediately) 1 else 0
        else -> if (this === vsyncPacedContext()) 1 else 0
    }

    private companion object {
        /**
         * Every live on-screen GLX context in the process, in creation order. Windows pace themselves
         * independently, so electing the one that waits for vblank ([swapIntervalForFrame]) needs a view of
         * all of them rather than of one window's frame.
         *
         * Mutated from the EDT alongside context creation and destruction, but read per frame from
         * [dispatcherToBlockOn], so it has to tolerate iteration concurrent with those mutations.
         */
        val liveContexts = CopyOnWriteArrayList<LinuxOpenGLRenderContext>()

        fun vsyncPacedContext(): LinuxOpenGLRenderContext? = liveContexts
            .filter { !it.isDisposed && it.host.isShowing && it.properties.isVsyncEnabled }
            .maxByOrNull { it.frameLimit }
    }
}

private fun LinuxDrawingSurface.createContext(transparency: Boolean) = createContext(display, transparency)
private fun LinuxDrawingSurface.destroyContext(context: Long) = destroyContext(display, context)
private fun LinuxDrawingSurface.makeCurrent(context: Long) = makeCurrent(display, window, context)
private fun LinuxDrawingSurface.releaseCurrent() = releaseCurrent(display)
private fun LinuxDrawingSurface.swapBuffers() = swapBuffers(display, window)
private fun LinuxDrawingSurface.setSwapInterval(interval: Int) = setSwapInterval(display, window, interval)

private external fun makeCurrent(display: Long, window: Long, context: Long)
private external fun releaseCurrent(display: Long)
private external fun createContext(display: Long, transparency: Boolean): Long
private external fun destroyContext(display: Long, context: Long)
private external fun setSwapInterval(display: Long, window: Long, interval: Int)
private external fun swapBuffers(display: Long, window: Long)