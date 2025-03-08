package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.Logger
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.SkikoProperties

internal class RedrawerManager<R>(
    defaultRenderApi: GraphicsApi,
    private val redrawerFactory: (renderApi: GraphicsApi, oldRedrawer: R?) -> R,
    private val onRenderApiChanged: ((GraphicsApi) -> Unit)? = null
) {
    private val fallbackRenderApiQueue = SkikoProperties.fallbackRenderApiQueue(defaultRenderApi).toMutableList()

    var redrawer: R? = null
        private set

    var renderApi: GraphicsApi = fallbackRenderApiQueue[0]
        set(value) {
            field = value
            onRenderApiChanged?.invoke(value)
        }

    fun findNextWorkingRenderApi(recreation: Boolean = false) {
        if (recreation) {
            fallbackRenderApiQueue.add(0, renderApi)
        }
        var thrown: Boolean
        do {
            thrown = false
            try {
                renderApi = fallbackRenderApiQueue.removeAt(0)
                redrawer = redrawerFactory(renderApi, redrawer)
            } catch (e: RenderException) {
                redrawer = null
                Logger.warn(e) { "Fallback to next API" }
                thrown = true
            }
        } while (thrown && fallbackRenderApiQueue.isNotEmpty())

        if (thrown) {
            throw RenderException("Cannot fallback to any render API")
        }
    }

    fun dispose() {
        redrawer = null
    }
}