package org.jetbrains.skiko.rendercontext

import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.Logger
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.SkikoProperties

internal class RenderApiFallbackManager<R>(
    defaultRenderApi: GraphicsApi,
    private val factory: (renderApi: GraphicsApi, previous: R?) -> R,
    private val onRenderApiChanged: ((GraphicsApi) -> Unit)? = null
) {
    private val fallbackRenderApiQueue = SkikoProperties.fallbackRenderApiQueue(defaultRenderApi).toMutableList()

    var current: R? = null
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
                current = factory(renderApi, current)
            } catch (e: RenderException) {
                current = null
                Logger.warn(e) { "Fallback to next API" }
                thrown = true
            }
        } while (thrown && fallbackRenderApiQueue.isNotEmpty())

        if (thrown) {
            throw RenderException("Cannot fallback to any render API")
        }
    }

    fun dispose() {
        current = null
    }
}