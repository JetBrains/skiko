package org.jetbrains.skiko.renderer

import org.jetbrains.skia.DirectContext
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.Logger
import org.jetbrains.skiko.hostArch
import org.jetbrains.skiko.hostOs

internal fun renderInfoHeader(renderApi: GraphicsApi): String =
    "GraphicsApi: $renderApi\n" +
    "OS: ${hostOs.id} ${hostArch.id}\n"

internal inline fun logRendererInfo(renderInfo: () -> String) {
    if (System.getProperty("skiko.hardwareInfo.enabled") == "true") {
        Logger.info { "Renderer info:\n ${renderInfo()}" }
    }
}

internal inline fun onContextInitialized(context: DirectContext, gpuResourceCacheLimit: Long, renderInfo: () -> String) {
    logRendererInfo(renderInfo)
    if (gpuResourceCacheLimit >= 0) {
        context.resourceCacheLimit = gpuResourceCacheLimit
    }
}
