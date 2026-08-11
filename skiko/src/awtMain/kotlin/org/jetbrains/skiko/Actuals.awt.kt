package org.jetbrains.skiko

import org.jetbrains.skiko.redrawer.*
import javax.swing.UIManager

actual fun setSystemLookAndFeel() = UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

internal actual fun makeDefaultRenderFactory(): RenderFactory =
    RenderFactory { layer, renderApi, analytics, properties ->
        when (hostOs) {
            OS.MacOS -> when (renderApi) {
                GraphicsApi.SOFTWARE_COMPAT, GraphicsApi.SOFTWARE_FAST -> SoftwareRedrawer(layer, analytics, properties)
                else -> MetalRedrawer(layer, analytics, properties)
            }
            OS.Windows -> when (renderApi) {
                GraphicsApi.SOFTWARE_COMPAT -> SoftwareRedrawer(layer, analytics, properties)
                GraphicsApi.SOFTWARE_FAST -> WindowsSoftwareRedrawer(layer, analytics, properties)
                GraphicsApi.OPENGL -> WindowsOpenGLRedrawer(layer, analytics, properties)
                GraphicsApi.ANGLE -> AngleRedrawer(layer, analytics, properties)
                else -> Direct3DRedrawer(layer, analytics, properties)
            }
            OS.Linux -> when (renderApi) {
                GraphicsApi.SOFTWARE_COMPAT -> SoftwareRedrawer(layer, analytics, properties)
                GraphicsApi.SOFTWARE_FAST -> LinuxSoftwareRedrawer(layer, analytics, properties)
                else -> LinuxOpenGLRedrawer(layer, analytics, properties)
            }
            else -> throw UnsupportedOperationException("AWT doesn't support $hostOs")
        }
    }
