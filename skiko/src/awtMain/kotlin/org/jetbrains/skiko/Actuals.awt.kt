package org.jetbrains.skiko

import org.jetbrains.skiko.renderer.*
import javax.swing.UIManager

actual fun setSystemLookAndFeel() = UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

internal actual fun makeDefaultRenderFactory(): RenderFactory =
    RenderFactory { layer, renderApi, analytics, properties ->
        when (hostOs) {
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
    }
