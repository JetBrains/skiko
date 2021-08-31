package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skiko.*

internal class LinuxOpenGLRedrawer(
    private val layer: SkiaLayer,
    private val properties: SkiaLayerProperties
) : Redrawer {
    private var isDisposed = false
    private var context = 0L
    private val swapInterval = if (properties.isVsyncEnabled) 1 else 0
    private val frameLimiter by lazy { FrameLimiter(layer) }

    private suspend fun limitFramesIfNeeded() {
        // Some Linuxes don't turn vsync on, so we apply additional frame limit (which should be no longer than enabled vsync)
        if (properties.isVsyncEnabled) {
            frameLimiter.awaitNextFrame()
        }
    }

    init {
    	layer.backedLayer.lockDrawingSurface {
            context = it.createContext()
            it.makeCurrent(context)
            if (context == 0L || !isVideoCardSupported(layer.renderApi)) {
                throw IllegalArgumentException("Cannot create Linux GL context")
            }
            it.setSwapInterval(swapInterval)
        }
    }
    

    override fun dispose() {
        check(!isDisposed) { "LinuxOpenGLRedrawer is disposed" }
        layer.backedLayer.lockDrawingSurface {
            it.destroyContext(context)
        }
        isDisposed = true
    }

    override fun needRedraw() {
        check(!isDisposed) { "LinuxOpenGLRedrawer is disposed" }
        toRedraw.add(this)
        frameDispatcher.scheduleFrame()
    }

    override suspend fun awaitRedraw(): Boolean {
        return frameDispatcher.awaitFrame()
    }

    override fun redrawImmediately() = layer.backedLayer.lockDrawingSurface {
        check(!isDisposed) { "LinuxOpenGLRedrawer is disposed" }
        update(System.nanoTime())
        it.makeCurrent(context)
        draw()
        it.setSwapInterval(0)
        it.swapBuffers()
        OpenGLApi.instance.glFinish()
        it.setSwapInterval(swapInterval)
    }

    private fun update(nanoTime: Long) {
        layer.update(nanoTime)
    }

    private fun draw() {
        if (layer.prepareDrawContext()) {
            layer.draw()
        }
    }

    companion object {
        private val toRedraw = mutableSetOf<LinuxOpenGLRedrawer>()
        private val toRedrawCopy = mutableSetOf<LinuxOpenGLRedrawer>()
        private val toRedrawAlive = toRedrawCopy.asSequence().filterNot(LinuxOpenGLRedrawer::isDisposed)

        private val frameDispatcher = FrameDispatcher(Dispatchers.Swing) {
            toRedrawAlive.forEach {
                it.limitFramesIfNeeded()
            }

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

            val drawingSurfaces = toRedrawAlive.map { lockDrawingSurface(it.layer.backedLayer) }.toList()
            try {
                toRedrawAlive.forEachIndexed { index, redrawer ->
                    drawingSurfaces[index].makeCurrent(redrawer.context)
                    redrawer.draw()
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

private inline fun <T> HardwareLayer.lockDrawingSurface(action: (LinuxDrawingSurface) -> T): T {
    val drawingSurface = lockDrawingSurface(this)
    try {
        return action(drawingSurface)
    } finally {
        unlockDrawingSurface(drawingSurface)
    }
}

private fun lockDrawingSurface(layer: HardwareLayer): LinuxDrawingSurface {
    val drawingSurface = layer.getDrawingSurface()
    drawingSurface.lock()
    return drawingSurface.getInfo().use {
        LinuxDrawingSurface(drawingSurface, getDisplay(it.platformInfo), getWindow(it.platformInfo))
    }
}

private fun unlockDrawingSurface(drawingSurface: LinuxDrawingSurface) {
    drawingSurface.common.unlock()
    drawingSurface.common.close()
}

private class LinuxDrawingSurface(
    val common: DrawingSurface,
    val display: Long,
    val window: Long
) {
    fun createContext() = createContext(display)
    fun destroyContext(context: Long) = destroyContext(display, context)
    fun makeCurrent(context: Long) = makeCurrent(display, window, context)
    fun swapBuffers() = swapBuffers(display, window)
    fun setSwapInterval(interval: Int) = setSwapInterval(display, window, interval)
}

private external fun getDisplay(platformInfo: Long): Long
private external fun getWindow(platformInfo: Long): Long

private external fun makeCurrent(display: Long, window: Long, context: Long)
private external fun createContext(display: Long): Long
private external fun destroyContext(display: Long, context: Long)
private external fun setSwapInterval(display: Long, window: Long, interval: Int)
private external fun swapBuffers(display: Long, window: Long)