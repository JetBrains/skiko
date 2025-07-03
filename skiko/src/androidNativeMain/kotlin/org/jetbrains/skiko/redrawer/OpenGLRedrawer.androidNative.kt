package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.newFixedThreadPoolContext
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.context.AndroidNativeOpenGLContextHandler

internal class AndroidNativeOpenGLRedrawer(
    private val skiaLayer: SkiaLayer,
) : Redrawer {

    private val contextHandler = AndroidNativeOpenGLContextHandler(skiaLayer)

    private val frameDispatcher = FrameDispatcher(newFixedThreadPoolContext(1, "OpenGLFrameDispatcher")) {
        redrawImmediately()
    }

    override fun dispose() {
        frameDispatcher.cancel()
        contextHandler.dispose()
    }

    override fun needRedraw() {
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() {
        val currentTime = kotlin.system.getTimeNanos()
        skiaLayer.update(currentTime)
        contextHandler.draw()
    }

    override val renderInfo: String
        get() = contextHandler.rendererInfo()
}
