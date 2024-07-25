package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.*
import org.jetbrains.skiko.*
import org.jetbrains.skiko.layerFrameLimiter
import org.jetbrains.skiko.context.SoftwareContextHandler

internal class SoftwareRedrawer(
    private val layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    private val properties: SkiaLayerProperties
) : AWTRedrawer(layer, analytics, GraphicsApi.SOFTWARE_FAST) {
    init {
        onDeviceChosen("Software")
    }

    private val contextHandler = SoftwareContextHandler(layer)
    override val renderInfo: String get() = contextHandler.rendererInfo()

    private val frameJob = Job()
    private val frameLimiter = layerFrameLimiter(CoroutineScope(frameJob), layer.backedLayer)

    private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
        if (properties.isVsyncEnabled && properties.isVsyncFramelimitFallbackEnabled) {
            frameLimiter.awaitNextFrame()
        }

        if (layer.isShowing) {
            update(System.nanoTime())
            inDrawScope(contextHandler::draw)
        }
    }

    init {
        onContextInit()
    }

    override fun dispose() {
        frameJob.cancel()
        frameDispatcher.cancel()
        contextHandler.dispose()
        super.dispose()
    }

    override fun needRedraw() {
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately(waitForVsync: Boolean) {
        update(System.nanoTime())
        inDrawScope(contextHandler::draw)
    }
}