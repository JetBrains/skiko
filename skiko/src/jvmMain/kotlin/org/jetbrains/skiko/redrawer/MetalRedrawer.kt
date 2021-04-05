package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import org.jetbrains.skija.BackendRenderTarget
import org.jetbrains.skija.DirectContext
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerProperties
import org.jetbrains.skiko.useDrawingSurfacePlatformInfo
import javax.swing.SwingUtilities.convertPoint
import javax.swing.SwingUtilities.getRootPane

internal class MetalRedrawer(
    private val layer: SkiaLayer,
    private val properties: SkiaLayerProperties
) : Redrawer {
    private var isDisposed = false
    private var disposeLock = Any()
    private val device = layer.backedLayer.useDrawingSurfacePlatformInfo(::createMetalDevice)

    private val frameDispatcher = FrameDispatcher(Dispatchers.Swing) {
        update(System.nanoTime())
        draw()
    }

    override fun dispose() = synchronized(disposeLock) {
        frameDispatcher.cancel()
        disposeDevice(device)
        isDisposed = true
    }

    override fun needRedraw() {
        check(!isDisposed)
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() {
        check(!isDisposed)
        // TODO: now we wait until previous `layer.draw` is finished. it ends only on the next vsync.
        //  Because of that we lose one frame on resize and can theoretically see very small white bars on the sides
        //  of the window to avoid this we should be able to draw in two modes: with vsync and without.
        frameDispatcher.scheduleFrame()
    }

    private fun update(nanoTime: Long) {
        layer.update(nanoTime)
    }

    private suspend fun draw() {
        if (layer.prepareDrawContext()) {
            // 2,3 GHz 8-Core Intel Core i9
            //
            // Test1. 8 windows, multiple clocks, 800x600
            //
            // Executors.newSingleThreadExecutor().asCoroutineDispatcher(): 20 FPS, 130% CPU
            // Dispatchers.IO: 58 FPS, 460% CPU
            //
            // Test2. 60 windows, single clock, 800x600
            //
            // Executors.newSingleThreadExecutor().asCoroutineDispatcher(): 50 FPS, 150% CPU
            // Dispatchers.IO: 50 FPS, 200% CPU
            withContext(Dispatchers.IO) {
                synchronized(disposeLock) {
                    if (!isDisposed) {
                        layer.draw()
                    }
                }
            }
        }
    }

    override fun syncSize() {
        val rootPane = getRootPane(layer)
        val globalPosition = convertPoint(layer, layer.x, layer.y, rootPane)
        setContentScale(device, layer.contentScale)
        resizeLayers(device,
            globalPosition.x,
            rootPane.height - globalPosition.y - layer.height,
            layer.width.coerceAtLeast(0),
            layer.height.coerceAtLeast(0)
        )
    }

    fun makeContext() = DirectContext(
        makeMetalContext(device)
    )

    fun makeRenderTarget(width: Int, height: Int) = BackendRenderTarget(
        makeMetalRenderTarget(device, width, height)
    )
    
    fun finishFrame() = finishFrame(device)

    private external fun createMetalDevice(platformInfo: Long): Long
    private external fun makeMetalContext(device: Long): Long
    private external fun makeMetalRenderTarget(device: Long, width: Int, height: Int): Long
    private external fun disposeDevice(device: Long)
    private external fun finishFrame(device: Long)
    private external fun resizeLayers(device: Long, x: Int, y: Int, width: Int, height: Int)
    private external fun setContentScale(device: Long, contentScale: Float)
}
