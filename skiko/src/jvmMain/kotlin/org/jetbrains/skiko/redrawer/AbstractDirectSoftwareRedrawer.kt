package org.jetbrains.skiko.redrawer

import org.jetbrains.skia.Surface
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.FrameLimiter
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerProperties
import org.jetbrains.skiko.context.DirectSoftwareContextHandler

internal abstract class AbstractDirectSoftwareRedrawer(
    private val layer: SkiaLayer,
    private val properties: SkiaLayerProperties
) : Redrawer {
    private val contextHandler = DirectSoftwareContextHandler(layer)
    override val renderInfo: String get() = contextHandler.rendererInfo()

    private val frameJob = Job()
    private val frameLimiter = FrameLimiter(CoroutineScope(Dispatchers.IO + frameJob), layer.backedLayer)
    private val frameDispatcher = FrameDispatcher(Dispatchers.Swing) {
        if (properties.isVsyncEnabled && properties.isVsyncFramelimitFallbackEnabled) {
            frameLimiter.awaitNextFrame()
        }

        if (layer.isShowing) {
            layer.update(System.nanoTime())
            layer.inDrawScope(contextHandler::draw)
        }
    }

    protected var device = 0L

    override fun needRedraw() {
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() {
        layer.update(System.nanoTime())
        layer.inDrawScope(contextHandler::draw)
    }

    open fun resize(width: Int, height: Int) = resize(device, width, height)
    fun getSurface(): Surface {
        val surface = getSurface(device)
        if (surface == 0L) {
            throw RenderException("Failed to create Surface")
        }
        return Surface(surface)
    }
    open fun finishFrame() = finishFrame(device)
    override fun dispose() {
        frameDispatcher.cancel()
        contextHandler.dispose()
        disposeDevice(device)
        runBlocking {
            frameJob.cancelAndJoin()
        }  
    }

    private external fun resize(devicePtr: Long, width: Int, height: Int)
    private external fun getSurface(devicePtr: Long): Long
    private external fun finishFrame(devicePtr: Long)
    private external fun disposeDevice(devicePtr: Long)
}