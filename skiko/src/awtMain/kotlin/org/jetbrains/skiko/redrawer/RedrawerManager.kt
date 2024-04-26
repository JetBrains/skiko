package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.Logger
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.SkikoProperties

internal class RedrawerManager<R>(
    defaultRenderApi: GraphicsApi,
    private val redrawerFactory: (renderApi: GraphicsApi, oldRedrawer: R?) -> R
) {
    private var _redrawer: R? = null
    private val fallbackRenderApiQueue = SkikoProperties.fallbackRenderApiQueue(defaultRenderApi).toMutableList()
    private var _renderApi = fallbackRenderApiQueue[0]

    val redrawer: R?
        get() = _redrawer

    val renderApi: GraphicsApi
        get() = _renderApi

    fun findNextWorkingRenderApi(recreation: Boolean = false) {
        if (recreation) {
            fallbackRenderApiQueue.add(0, renderApi)
        }
        var thrown: Boolean
        do {
            thrown = false
            try {
                _renderApi = fallbackRenderApiQueue.removeAt(0)
                _redrawer = redrawerFactory(_renderApi, redrawer)
            } catch (e: RenderException) {
                _redrawer = null
                Logger.warn(e) { "Fallback to next API" }
                thrown = true
            }
        } while (thrown && fallbackRenderApiQueue.isNotEmpty())

        if (thrown) {
            throw RenderException("Cannot fallback to any render API")
        }
    }

    fun forceRenderApi(renderApi: GraphicsApi) {
        _renderApi = renderApi
    }

    fun dispose() {
        _redrawer = null
    }
}