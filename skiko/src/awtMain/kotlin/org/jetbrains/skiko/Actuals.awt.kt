package org.jetbrains.skiko

import org.jetbrains.skiko.redrawer.*
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.UnsupportedFlavorException
import java.net.URI
import javax.swing.UIManager

actual fun setSystemLookAndFeel() = UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

internal actual fun makeDefaultRenderFactory(): RenderFactory {
    return object : RenderFactory {
        override fun createRedrawer(
            layer: SkiaLayer,
            renderApi: GraphicsApi,
            properties: SkiaLayerProperties
        ): Redrawer = when (hostOs) {
            OS.MacOS -> when (renderApi) {
                GraphicsApi.SOFTWARE_COMPAT, GraphicsApi.SOFTWARE_FAST -> SoftwareRedrawer(layer, properties)
                else -> MetalRedrawer(layer, properties)
            }
            OS.Windows -> when (renderApi) {
                GraphicsApi.SOFTWARE_COMPAT -> SoftwareRedrawer(layer, properties)
                GraphicsApi.SOFTWARE_FAST -> WindowsSoftwareRedrawer(layer, properties)
                GraphicsApi.OPENGL -> WindowsOpenGLRedrawer(layer, properties)
                else -> Direct3DRedrawer(layer, properties)
            }
            OS.Linux -> when (renderApi) {
                GraphicsApi.SOFTWARE_COMPAT -> SoftwareRedrawer(layer, properties)
                GraphicsApi.SOFTWARE_FAST -> LinuxSoftwareRedrawer(layer, properties)
                else -> LinuxOpenGLRedrawer(layer, properties)
            }
            OS.Android -> TODO()
            OS.JS, OS.Ios -> {
                TODO("Commonize me")
            }
        }
    }
}

internal actual fun URIHandler_openUri(uri: String) {
    Desktop.getDesktop().browse(URI(uri))
}

private val systemClipboard by lazy {
    try {
        Toolkit.getDefaultToolkit().getSystemClipboard()
    } catch (e: java.awt.HeadlessException) {
        null
    }
}

internal actual fun ClipboardManager_setText(text: String) {
    systemClipboard?.setContents(StringSelection(text), null)
}
internal actual fun ClipboardManager_getText(): String? {
    return try {
        systemClipboard?.getData(DataFlavor.stringFlavor) as String?
    } catch (_: UnsupportedFlavorException) {
        null
    }
}
