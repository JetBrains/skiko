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

    internal var context = 0L
        private set
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

    override suspend fun renderFrame(scope: LayerDrawScope, immediate: Boolean) {
        layer.backedLayer.lockLinuxDrawingSurface {
            it.makeCurrent(context)
            with(scope) { drawFrame() }
            val turnOfVsync = properties.isVsyncEnabled && !SkikoProperties.linuxWaitForVsyncOnRedrawImmediately
            if (turnOfVsync) {
                it.setSwapInterval(0)
            }
            it.swapBuffers()
            OpenGLApi.instance.glFlush()
            if (turnOfVsync) {
                it.setSwapInterval(swapInterval)
            }
        }
    }
}

private fun LinuxDrawingSurface.createContext(transparency: Boolean) = createContext(display, transparency)
private fun LinuxDrawingSurface.destroyContext(context: Long) = destroyContext(display, context)
internal fun LinuxDrawingSurface.makeCurrent(context: Long) = makeCurrent(display, window, context)
internal fun LinuxDrawingSurface.swapBuffers() = swapBuffers(display, window)
internal fun LinuxDrawingSurface.setSwapInterval(interval: Int) = setSwapInterval(display, window, interval)

private external fun makeCurrent(display: Long, window: Long, context: Long)
private external fun createContext(display: Long, transparency: Boolean): Long
private external fun destroyContext(display: Long, context: Long)
private external fun setSwapInterval(display: Long, window: Long, interval: Int)
private external fun swapBuffers(display: Long, window: Long)