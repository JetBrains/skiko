package org.jetbrains.skiko

import org.jetbrains.skiko.redrawer.*
import java.awt.Component
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.UnsupportedFlavorException
import java.net.URI
import javax.swing.UIManager

actual fun setSystemLookAndFeel() = UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

internal actual fun makeDefaultRenderFactory(): RenderFactory = RenderFactory { layer, renderApi, properties ->
    when (hostOs) {
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

actual typealias Cursor = java.awt.Cursor

internal actual fun CursorManager_setCursor(component: Any, cursor: Cursor) {
    if (component is Component) {
        component.cursor = cursor
    }
}

internal actual fun CursorManager_getCursor(component: Any): Cursor? {
    return if (component is Component) {
        component.cursor
    } else {
        null
    }
}

internal actual fun getCursorById(id: PredefinedCursorsId): Cursor =
    when (id) {
        PredefinedCursorsId.DEFAULT -> Cursor(Cursor.DEFAULT_CURSOR)
        PredefinedCursorsId.CROSSHAIR -> Cursor(Cursor.CROSSHAIR_CURSOR)
        PredefinedCursorsId.HAND -> Cursor(Cursor.HAND_CURSOR)
        PredefinedCursorsId.TEXT -> Cursor(Cursor.TEXT_CURSOR)
    }