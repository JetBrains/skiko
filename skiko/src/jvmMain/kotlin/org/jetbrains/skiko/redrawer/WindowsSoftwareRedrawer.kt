package org.jetbrains.skiko.redrawer

import org.jetbrains.skia.Surface
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.FrameLimiter
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerProperties

internal class WindowsSoftwareRedrawer(
    private val layer: SkiaLayer,
    private val properties: SkiaLayerProperties
) : Redrawer {
    private val frameJob = Job()
    private val frameLimiter = FrameLimiter(CoroutineScope(Dispatchers.IO + frameJob), layer.backedLayer)
    private val frameDispatcher = FrameDispatcher(Dispatchers.Swing) {
        if (properties.isVsyncEnabled && properties.isVsyncFramelimitFallbackEnabled) {
            frameLimiter.awaitNextFrame()
        }

        if (layer.isShowing) {
            layer.update(System.nanoTime())
            layer.inDrawScope(layer::draw)
        }
    }

    private val device = createDevice(layer.contentHandle).also {
        if (it == 0L) {
            throw IllegalArgumentException("Failed to create Software device.")
        }
    }

    override fun needRedraw() {
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() {
        layer.update(System.nanoTime())
        layer.inDrawScope(layer::draw)
    }

    fun resize(width: Int, height: Int) = resize(device, width, height)
    fun getSurface() = Surface(getSurface(device))
    fun finishFrame() = finishFrame(device)
    override fun dispose() {
        disposeDevice(device)
        frameDispatcher.cancel()
        runBlocking {
            frameJob.cancelAndJoin()
        }  
    }

    private external fun createDevice(contentHandle: Long): Long
    private external fun resize(devicePtr: Long, width: Int, height: Int)
    private external fun getSurface(devicePtr: Long): Long
    private external fun finishFrame(devicePtr: Long)
    private external fun disposeDevice(devicePtr: Long)
}