package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.HardwareLayer
import org.jetbrains.skiko.OpenGLApi

internal class WindowsRedrawer(
    private val layer: HardwareLayer
) : Redrawer {
    private val device = getDevice(layer)
    private val context = createContext(device)
    private var isDisposed = false
    private val job = Job()

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
        job.cancel()
    }

    override fun needRedraw() {
        check(!isDisposed)
        toRedraw.add(this)
        frameDispatcher.scheduleFrame()
    }

    private suspend fun update(nanoTime: Long) {
        withContext(job) {
            layer.update(nanoTime)
        }
    }

    private fun draw() {
        layer.draw()
    }

    private fun makeCurrent() = makeCurrent(device, context)
    private fun swapBuffers() = swapBuffers(device)

    companion object {
        private val toRedraw = mutableSetOf<WindowsRedrawer>()
        private val toRedrawCopy = mutableSetOf<WindowsRedrawer>()
        private val toRedrawAlive = toRedrawCopy.asSequence().filterNot(WindowsRedrawer::isDisposed)

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

            withContext(Dispatchers.IO) {
                dwmFlush() // wait for vsync
            }
        }
    }
}

private external fun makeCurrent(device: Long, context: Long)
private external fun getDevice(layer: HardwareLayer): Long
private external fun createContext(device: Long): Long
private external fun deleteContext(context: Long)
private external fun setSwapInterval(interval: Int)
private external fun swapBuffers(device: Long)

// TODO according to https://bugs.chromium.org/p/chromium/issues/detail?id=467617 dwmFlush has lag 3 ms after vsync.
//  Maybe we should use D3DKMTWaitForVerticalBlankEvent? See also https://www.vsynctester.com/chromeisbroken.html
// TODO should we support Windows 7? DWM can be disabled on Windows 7.
//  it that case there will be a crash or just no frame limit (I don't know exactly).
private external fun dwmFlush()