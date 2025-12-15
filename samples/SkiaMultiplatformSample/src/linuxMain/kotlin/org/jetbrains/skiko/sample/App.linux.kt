package org.jetbrains.skiko.sample

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerRenderDelegate
import org.jetbrains.skiko.X11SkikoWindow
import platform.posix.getenv

@OptIn(ExperimentalForeignApi::class)
fun main() {
    val renderApiEnv = getenv("SKIKO_RENDER_API")?.toKString()?.uppercase()
    val layer = SkiaLayer().apply {
        renderApi = when (renderApiEnv) {
            "VULKAN" -> GraphicsApi.VULKAN
            "SOFTWARE", "SOFTWARE_FAST" -> GraphicsApi.SOFTWARE_FAST
            "SOFTWARE_COMPAT" -> GraphicsApi.SOFTWARE_COMPAT
            else -> GraphicsApi.VULKAN
        }
    }
    val clocks = LinuxClocks(layer)
    layer.renderDelegate = SkiaLayerRenderDelegate(layer, clocks)

    X11SkikoWindow(
        layer = layer,
        onMouseMove = clocks::motion,
        onButtonPress = clocks::buttonPressed,
        onButtonRelease = clocks::buttonReleased,
    ).use { window ->
        layer.needRender()
        window.run()
    }
}
