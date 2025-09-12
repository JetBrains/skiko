package org.jetbrains.skiko.context

import org.jetbrains.skiko.Logger
import org.jetbrains.skiko.SkiaLayer

internal abstract class JvmContextHandler(layer: SkiaLayer) : ContextHandler(layer, layer::draw) {
    protected fun onContextInitialized() {
        if (System.getProperty("skiko.hardwareInfo.enabled") == "true") {
            Logger.info { "Renderer info:\n ${rendererInfo()}" }
        }
        context?.run {
            val gpuResourceCacheLimit = layer.properties.gpuResourceCacheLimit
            if (gpuResourceCacheLimit >= 0) {
                resourceCacheLimit = gpuResourceCacheLimit
            }
        }
    }
}
