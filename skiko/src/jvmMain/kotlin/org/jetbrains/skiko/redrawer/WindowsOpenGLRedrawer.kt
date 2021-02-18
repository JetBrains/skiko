package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.HardwareLayer
import org.jetbrains.skiko.OpenGLApi
import org.jetbrains.skiko.SkiaLayerProperties
import org.jetbrains.skiko.useDrawingSurfacePlatformInfo

internal class WindowsOpenGLRedrawer(
    private val layer: HardwareLayer,
    private val properties: SkiaLayerProperties
) : Redrawer {
    private val device = layer.useDrawingSurfacePlatformInfo(::getDevice)
    private val context = createContext(device)
    private var isDisposed = false

    init {
        makeCurrent()
        // For vsync we will use dwmFlush instead of swapInterval,
        // because it isn't reliable with DWM (Desktop Windows Manager): interval between frames isn't stable (14-19ms).
        // With dwmFlush it is stable (16.6-16.8 ms)
        // GLFW also uses dwmFlush (https://www.glfw.org/docs/3.0/window.html#window_swap)
        setSwapInterval(0)
    }

    override fun dispose() {
        check(!isDisposed)
        deleteContext(context)
        isDisposed = true
    }

    override fun needRedraw() {
        check(!isDisposed)
        toRedraw.add(this)
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() {
        check(!isDisposed)
        update(System.nanoTime())
        makeCurrent()
        draw()
        swapBuffers()
        OpenGLApi.instance.glFinish()
    }

    private fun update(nanoTime: Long) {
        layer.update(nanoTime)
    }

    private fun draw() {
        layer.draw()
    }

    private fun makeCurrent() = makeCurrent(device, context)
    private fun swapBuffers() = swapBuffers(device)

    companion object {
        private val toRedraw = mutableSetOf<WindowsOpenGLRedrawer>()
        private val toRedrawCopy = mutableSetOf<WindowsOpenGLRedrawer>()
        private val toRedrawAlive = toRedrawCopy.asSequence().filterNot(WindowsOpenGLRedrawer::isDisposed)

        private val frameDispatcher = FrameDispatcher(Dispatchers.Swing) {
            toRedrawCopy.clear()
            toRedrawCopy.addAll(toRedraw)
            toRedraw.clear()

            val nanoTime = System.nanoTime()

            for (redrawer in toRedrawAlive) {
                try {
                    redrawer.update(nanoTime)
                } catch (e: CancellationException) {
                    // continue
                }
            }

            for (redrawer in toRedrawAlive) {
                redrawer.makeCurrent()
                redrawer.draw()
            }

            for (redrawer in toRedrawAlive) {
                redrawer.swapBuffers()
            }

            for (redrawer in toRedrawAlive) {
                redrawer.makeCurrent()
                OpenGLApi.instance.glFinish()
            }

            val isVsyncEnabled = toRedrawAlive.all { it.properties.isVsyncEnabled }
            if (isVsyncEnabled) {
                withContext(Dispatchers.IO) {
                    dwmFlush() // wait for vsync
                }
            }
        }
    }
}

private external fun makeCurrent(device: Long, context: Long)
private external fun getDevice(platformInfo: Long): Long
private external fun createContext(device: Long): Long
private external fun deleteContext(context: Long)
private external fun setSwapInterval(interval: Int)
private external fun swapBuffers(device: Long)

// TODO according to https://bugs.chromium.org/p/chromium/issues/detail?id=467617 dwmFlush has lag 3 ms after vsync.
//  Maybe we should use D3DKMTWaitForVerticalBlankEvent? See also https://www.vsynctester.com/chromeisbroken.html
// TODO should we support Windows 7? DWM can be disabled on Windows 7.
//  it that case there will be a crash or just no frame limit (I don't know exactly).
private external fun dwmFlush()