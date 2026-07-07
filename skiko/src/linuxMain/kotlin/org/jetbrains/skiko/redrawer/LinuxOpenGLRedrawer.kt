package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoDispatchers
import org.jetbrains.skiko.context.LinuxOpenGLContextHandler
import kotlin.time.TimeSource

internal class LinuxOpenGLRedrawer(
    private val skiaLayer: SkiaLayer,
) : Redrawer {
    private val contextHandler = LinuxOpenGLContextHandler(skiaLayer)
    override val renderInfo: String get() = contextHandler.rendererInfo()

    private val win = skiaLayer.window
    private val gl =
        win.createGlContext().apply {
            makeCurrent()
            setSwapInterval(1) // Vsync enabled
        }

    private val initialTime = TimeSource.Monotonic.markNow()

    private val frameDispatcher =
        FrameDispatcher(SkikoDispatchers.Main) {
            renderImmediately()
        }

    override fun dispose() {
        frameDispatcher.cancel()
        gl.makeCurrent()
        contextHandler.dispose()
        gl.close()
    }

    override fun needRender(throttledToVsync: Boolean) {
        frameDispatcher.scheduleFrame()
    }

    override fun update(nanoTime: Long) {
        skiaLayer.update(nanoTime)
    }

    override fun renderImmediately() {
        gl.makeCurrent()
        skiaLayer.update(initialTime.elapsedNow().inWholeNanoseconds)
        skiaLayer.inDrawScope {
            contextHandler.draw()
        }
        gl.swapBuffers()
    }

    override fun isTransparentBackgroundSupported() = false
}
