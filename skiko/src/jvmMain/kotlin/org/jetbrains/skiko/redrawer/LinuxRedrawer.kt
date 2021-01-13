package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.HardwareLayer
import org.jetbrains.skiko.OpenGLApi

internal class LinuxRedrawer(
    private val layer: HardwareLayer
) : Redrawer {
    private val context = layer.lockDrawingSurface {
        val context = it.createContext()
        it.makeCurrent(context)
        it.setSwapInterval(1)
        context
    }
    private var isDisposed = false

    override fun dispose() {
        check(!isDisposed)
        layer.lockDrawingSurface {
            it.destroyContext(context)
        }
        isDisposed = true
    }

    override fun needRedraw() {
        toRedraw.add(this)
        frameDispatcher.scheduleFrame()
    }

    companion object {
        private val toRedraw = mutableSetOf<LinuxRedrawer>()
        private val toRedrawCopy = mutableSetOf<LinuxRedrawer>()
        private val toRedrawAlive = toRedrawCopy.asSequence().filterNot(LinuxRedrawer::isDisposed)

        private val frameDispatcher = FrameDispatcher(Dispatchers.Swing) {
            toRedrawCopy.clear()
            toRedrawCopy.addAll(toRedraw)
            toRedraw.clear()

            val nanoTime = System.nanoTime()

            for (redrawer in toRedrawAlive) {
                redrawer.layer.update(nanoTime)
            }

            val drawingSurfaces = toRedrawAlive.map { lockDrawingSurface(it.layer) }.toList()
            try {
                toRedrawAlive.forEachIndexed { index, redrawer ->
                    drawingSurfaces[index].makeCurrent(redrawer.context)
                    redrawer.layer.draw()
                }

                toRedrawAlive.forEachIndexed { index, _ ->
                    drawingSurfaces[index].swapBuffers()
                }

                toRedrawAlive.forEachIndexed { index, redrawer ->
                    drawingSurfaces[index].makeCurrent(redrawer.context)
                    OpenGLApi.instance.glFinish()
                }
            } finally {
                drawingSurfaces.forEach(::unlockDrawingSurface)
            }
        }
    }
}

private inline fun <T> HardwareLayer.lockDrawingSurface(action: (DrawingSurface) -> T): T {
    val drawingSurface = lockDrawingSurface(this)
    try {
        return action(drawingSurface)
    } finally {
        unlockDrawingSurface(drawingSurface)
    }
}

private fun lockDrawingSurface(layer: HardwareLayer): DrawingSurface {
    val ptr = lockDrawingSurfaceNative(layer)
    return DrawingSurface(ptr, getDisplay(ptr), getWindow(ptr))
}

private fun unlockDrawingSurface(drawingSurface: DrawingSurface) {
    unlockDrawingSurfaceNative(drawingSurface.ptr)
}

private class DrawingSurface(
    val ptr: Long,
    val display: Long,
    val window: Long
) {
    fun createContext() = createContext(display)
    fun destroyContext(context: Long) = destroyContext(display, context)
    fun makeCurrent(context: Long) = makeCurrent(display, window, context)
    fun swapBuffers() = swapBuffers(display, window)
    fun setSwapInterval(interval: Int) = setSwapInterval(display, window, interval)
}

private external fun lockDrawingSurfaceNative(layer: HardwareLayer): Long
private external fun unlockDrawingSurfaceNative(drawingSurface: Long)
private external fun getDisplay(drawingSurface: Long): Long
private external fun getWindow(drawingSurface: Long): Long

private external fun makeCurrent(display: Long, window: Long, context: Long)
private external fun createContext(display: Long): Long
private external fun destroyContext(display: Long, context: Long)
private external fun setSwapInterval(display: Long, window: Long, interval: Int)
private external fun swapBuffers(display: Long, window: Long)