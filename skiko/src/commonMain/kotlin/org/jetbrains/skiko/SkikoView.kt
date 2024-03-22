package org.jetbrains.skiko

@Deprecated(
    message = "Replaced with SkikoRenderDelegate",
    replaceWith = ReplaceWith("SkikoRenderDelegate")
)
typealias SkikoView = SkikoRenderDelegate

@Deprecated(
    message = "Replaced with SkiaLayerRenderDelegate",
    replaceWith = ReplaceWith("SkiaLayerRenderDelegate")
)
typealias GenericSkikoView = SkiaLayerRenderDelegate
