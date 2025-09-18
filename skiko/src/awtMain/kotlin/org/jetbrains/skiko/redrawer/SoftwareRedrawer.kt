package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.*
import org.jetbrains.skiko.*
import org.jetbrains.skiko.layerFrameLimiter
import org.jetbrains.skiko.context.SoftwareContextHandler

internal class SoftwareRedrawer(
    private val layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    properties: SkiaLayerProperties
) : AWTRedrawer(layer, analytics, GraphicsApi.SOFTWARE_FAST) {
    init {
        onDeviceChosen("Software")
    }

    private val contextHandler = SoftwareContextHandler(layer)
    override val renderInfo: String get() = contextHandler.rendererInfo()

    private val frameJob = if (properties.isVsyncEnabled && properties.isVsyncFramelimitFallbackEnabled) Job() else null
    private val frameLimiter = frameJob?.let {
        layerFrameLimiter(CoroutineScope(it), layer.backedLayer)
    }

    private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
        frameLimiter?.awaitNextFrame()

        if (layer.isShowing) {
            update()
            inDrawScope(contextHandler::draw)
        }
    }

    init {
        onContextInit()
    }

    override fun dispose() {
        frameJob?.cancel()
        frameDispatcher.cancel()
        contextHandler.dispose()
        super.dispose()
    }

    override fun needRedraw(throttledToVsync: Boolean) {
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() {
        checkDisposed()
        update()
        inDrawScope {
            if (!isDisposed) { // Redrawer may be disposed in user code, during `update`
                contextHandler.draw()
            }
        }
    }
}