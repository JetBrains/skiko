package org.jetbrains.skiko

import org.jetbrains.skiko.redrawer.AWTRedrawer
import org.jetbrains.skiko.redrawer.AngleRedrawer
import org.jetbrains.skiko.redrawer.Direct3DRedrawer
import org.jetbrains.skiko.redrawer.LinuxOpenGLRedrawer
import org.jetbrains.skiko.redrawer.LinuxSoftwareRedrawer
import org.jetbrains.skiko.redrawer.MetalRedrawer
import org.jetbrains.skiko.redrawer.OnScreenRedrawer
import org.jetbrains.skiko.redrawer.SoftwareRedrawer
import org.jetbrains.skiko.redrawer.WindowsOpenGLRedrawer
import org.jetbrains.skiko.redrawer.WindowsSoftwareRedrawer
import javax.swing.UIManager

actual fun setSystemLookAndFeel() = UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

// AWT builds a per-API render context ([createRedrawer]) and drives it through the single generic
// [OnScreenRedrawer] loop, which is the Redrawer the SkiaLayer path selects and paces.
internal actual fun makeDefaultRenderFactory(): RenderFactory =
    RenderFactory { layer, renderApi, analytics, properties ->
        OnScreenRedrawer(layer, createRedrawer(layer, renderApi, properties), analytics)
    }

/**
 * Builds the [AWTRedrawer] for [renderApi] on the current OS, or throws [RenderException] if that API cannot
 * be initialised. The single place the per-OS/API backend table lives.
 */
private fun createRedrawer(
    layer: SkiaLayer,
    renderApi: GraphicsApi,
    properties: SkiaLayerProperties,
): AWTRedrawer = when (hostOs) {
    OS.MacOS -> when (renderApi) {
        GraphicsApi.SOFTWARE_COMPAT, GraphicsApi.SOFTWARE_FAST -> SoftwareRedrawer(layer, properties)
        else -> MetalRedrawer(layer, properties)
    }
    OS.Windows -> when (renderApi) {
        GraphicsApi.SOFTWARE_COMPAT -> SoftwareRedrawer(layer, properties)
        GraphicsApi.SOFTWARE_FAST -> WindowsSoftwareRedrawer(layer, properties)
        GraphicsApi.OPENGL -> WindowsOpenGLRedrawer(layer, properties)
        GraphicsApi.ANGLE -> AngleRedrawer(layer, properties)
        else -> Direct3DRedrawer(layer, properties)
    }
    OS.Linux -> when (renderApi) {
        GraphicsApi.SOFTWARE_COMPAT -> SoftwareRedrawer(layer, properties)
        GraphicsApi.SOFTWARE_FAST -> LinuxSoftwareRedrawer(layer, properties)
        else -> LinuxOpenGLRedrawer(layer, properties)
    }
    else -> throw UnsupportedOperationException("AWT doesn't support $hostOs")
}
