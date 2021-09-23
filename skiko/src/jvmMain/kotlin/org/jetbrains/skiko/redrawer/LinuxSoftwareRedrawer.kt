package org.jetbrains.skiko.redrawer

import org.jetbrains.skia.Surface
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skiko.*

internal class LinuxSoftwareRedrawer(
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
            if (layer.prepareDrawContext()) {
                layer.draw()
            }
        }
    }

    private var device = 0L

    init {
        layer.backedLayer.lockLinuxDrawingSurface {
            device = createDevice(it.display, it.window).also {
                if (it == 0L) {
                    throw IllegalArgumentException("Failed to create Software device.")
                }
            }
        }
    }

    override fun needRedraw() {
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() = layer.backedLayer.lockLinuxDrawingSurface {
        layer.update(System.nanoTime())
        if (layer.prepareDrawContext()) {
            layer.draw()
        }
    }

    fun resize(width: Int, height: Int) = layer.backedLayer.lockLinuxDrawingSurface {
        resize(device, width, height)
    }
    fun getSurface() = Surface(getSurface(device))
    fun finishFrame() = layer.backedLayer.lockLinuxDrawingSurface {
        finishFrame(device)
    }
    override fun dispose() {
        disposeDevice(device)
        frameDispatcher.cancel()
        runBlocking {
            frameJob.cancelAndJoin()
        }  
    }

    private external fun createDevice(display: Long, window: Long): Long
    private external fun resize(devicePtr: Long, width: Int, height: Int)
    private external fun getSurface(devicePtr: Long): Long
    private external fun finishFrame(devicePtr: Long)
    private external fun disposeDevice(devicePtr: Long)
}