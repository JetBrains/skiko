package org.jetbrains.skiko.redrawer

import org.jetbrains.skia.Surface
import kotlinx.coroutines.*
import org.jetbrains.skiko.*
import org.jetbrains.skiko.FrameLimiter
import org.jetbrains.skiko.context.DirectSoftwareContextHandler

internal abstract class AbstractDirectSoftwareRedrawer(
    private val layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    private val properties: SkiaLayerProperties
) : AWTRedrawer(layer, analytics, GraphicsApi.SOFTWARE_FAST) {
    private val contextHandler = DirectSoftwareContextHandler(layer)
    override val renderInfo: String get() = contextHandler.rendererInfo()

    private val frameJob = Job()
    private val frameLimiter = FrameLimiter(CoroutineScope(Dispatchers.IO + frameJob), layer.backedLayer)
    private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
        if (properties.isVsyncEnabled && properties.isVsyncFramelimitFallbackEnabled) {
            frameLimiter.awaitNextFrame()
        }

        if (layer.isShowing) {
            update(System.nanoTime())
            draw()
        }
    }

    protected var device = 0L

    override fun needRedraw() {
        frameDispatcher.scheduleFrame()
    }

    protected open fun draw() = inDrawScope(contextHandler::draw)

    override fun redrawImmediately() {
        update(System.nanoTime())
        draw()
    }

    open fun resize(width: Int, height: Int) = resize(device, width, height)
    fun acquireSurface(): Surface {
        val surface = acquireSurface(device)
        if (surface == 0L) {
            throw RenderException("Failed to create Surface")
        }
        return Surface(surface)
    }
    open fun finishFrame(surface: Long) = finishFrame(device, surface)
    override fun dispose() {
        frameDispatcher.cancel()
        contextHandler.dispose()
        disposeDevice(device)
        runBlocking {
            frameJob.cancelAndJoin()
        }  
    }

    private external fun resize(devicePtr: Long, width: Int, height: Int)
    private external fun acquireSurface(devicePtr: Long): Long
    private external fun finishFrame(devicePtr: Long, surfacePtr: Long)
    private external fun disposeDevice(devicePtr: Long)
}