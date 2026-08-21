package org.jetbrains.skiko

import org.jetbrains.skiko.renderer.*
import javax.swing.UIManager

actual fun setSystemLookAndFeel() = UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

internal fun makeDefaultRenderFactory(): RenderFactory =
    RenderFactory { layer, renderApi, analytics, properties ->
        val renderer = createRenderer(layer, renderApi, analytics, properties)
        try {
            FrameDriver(layer, renderer, scheduleInBatchFor(renderer))
        } catch (e: Throwable) {
            renderer.close()
            throw e
        }
    }

// The OpenGL backends draw every window from a cross-window batch, so their drivers schedule into
// the family batch instead of running a per-window dispatcher.
private fun scheduleInBatchFor(renderer: AwtRenderer): FrameScheduler? = when (renderer) {
    is LinuxOpenGLRenderer -> FrameScheduler { driver -> LinuxGLFrameBatch.scheduleFrame(driver, renderer) }
    is WindowsOpenGLRenderer -> FrameScheduler { driver -> WindowsGLFrameBatch.scheduleFrame(driver, renderer) }
    else -> null
}

private fun createRenderer(
    layer: SkiaLayer,
    renderApi: GraphicsApi,
    analytics: SkiaLayerAnalytics,
    properties: SkiaLayerProperties,
): AwtRenderer = when (hostOs) {
    OS.MacOS -> when (renderApi) {
        GraphicsApi.SOFTWARE_COMPAT, GraphicsApi.SOFTWARE_FAST -> SoftwareRenderer(layer, analytics, properties)
        else -> MetalRenderer(layer, analytics, properties)
    }
    OS.Windows -> when (renderApi) {
        GraphicsApi.SOFTWARE_COMPAT -> SoftwareRenderer(layer, analytics, properties)
        GraphicsApi.SOFTWARE_FAST -> WindowsSoftwareRenderer(layer, analytics, properties)
        GraphicsApi.OPENGL -> WindowsOpenGLRenderer(layer, analytics, properties)
        GraphicsApi.ANGLE -> AngleRenderer(layer, analytics, properties)
        else -> Direct3DRenderer(layer, analytics, properties)
    }
    OS.Linux -> when (renderApi) {
        GraphicsApi.SOFTWARE_COMPAT -> SoftwareRenderer(layer, analytics, properties)
        GraphicsApi.SOFTWARE_FAST -> LinuxSoftwareRenderer(layer, analytics, properties)
        else -> LinuxOpenGLRenderer(layer, analytics, properties)
    }
    else -> throw UnsupportedOperationException("AWT doesn't support $hostOs")
}
