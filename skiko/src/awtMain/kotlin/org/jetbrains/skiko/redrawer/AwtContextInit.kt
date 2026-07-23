package org.jetbrains.skiko.redrawer

import org.jetbrains.skia.DirectContext
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.Logger
import org.jetbrains.skiko.hostArch
import org.jetbrains.skiko.hostOs

/** The API + OS lines every AWT backend's `renderInfo` begins with, before its device-specific tail. */
internal fun renderInfoHeader(renderApi: GraphicsApi): String =
    "GraphicsApi: $renderApi\n" +
    "OS: ${hostOs.id} ${hostArch.id}\n"

/**
 * Logs the renderer summary once, when `skiko.hardwareInfo.enabled` is set. [renderInfo] is evaluated only
 * then, because some backends query the GPU driver to build it.
 */
internal inline fun logRendererInfo(renderInfo: () -> String) {
    if (System.getProperty("skiko.hardwareInfo.enabled") == "true") {
        Logger.info { "Renderer info:\n ${renderInfo()}" }
    }
}

/**
 * The setup every GPU backend runs on a freshly created [DirectContext]: log the renderer summary and apply
 * [gpuResourceCacheLimit] when non-negative. Software backends have no [DirectContext] and call
 * [logRendererInfo] alone.
 */
internal inline fun onContextInitialized(context: DirectContext, gpuResourceCacheLimit: Long, renderInfo: () -> String) {
    logRendererInfo(renderInfo)
    if (gpuResourceCacheLimit >= 0) {
        context.resourceCacheLimit = gpuResourceCacheLimit
    }
}
