package org.jetbrains.skiko

import org.jetbrains.skiko.redrawer.LinuxOpenGLRedrawer
import org.jetbrains.skiko.redrawer.MacOsOpenGLRedrawer
import org.jetbrains.skiko.redrawer.SoftwareRedrawer
import org.jetbrains.skiko.redrawer.Redrawer
import org.jetbrains.skiko.redrawer.WindowsOpenGLRedrawer
import org.jetbrains.skiko.redrawer.Direct3DRedrawer
import org.jetbrains.skiko.redrawer.MetalRedrawer
import java.awt.Component
import java.awt.Window
import javax.swing.SwingUtilities

internal interface PlatformOperations {
    fun isFullscreen(component: Component): Boolean
    fun setFullscreen(component: Component, value: Boolean)
    fun disableTitleBar(platformInfo: Long)
    fun orderEmojiAndSymbolsPopup()
    fun getDpiScale(component: Component): Float
    fun createRedrawer(layer: SkiaLayer, renderApi: GraphicsApi, properties: SkiaLayerProperties): Redrawer
}

internal val platformOperations: PlatformOperations by lazy {
    when (hostOs) {
        OS.MacOS -> object: PlatformOperations {
                override fun isFullscreen(component: Component): Boolean {
                    return osxIsFullscreenNative(component)
                }

                override fun setFullscreen(component: Component, value: Boolean) {
                    osxSetFullscreenNative(component, value)
                }

                override fun getDpiScale(component: Component): Float {
                    return component.graphicsConfiguration.defaultTransform.scaleX.toFloat()
                }

                override fun disableTitleBar(platformInfo: Long) {
                    osxDisableTitleBar(platformInfo)
                }

                override fun orderEmojiAndSymbolsPopup() {
                    osxOrderEmojiAndSymbolsPopup()
                }

                override fun createRedrawer(
                    layer: SkiaLayer,
                    renderApi: GraphicsApi,
                    properties: SkiaLayerProperties
                ) = when(renderApi) {
                    GraphicsApi.SOFTWARE -> SoftwareRedrawer(layer)
                    GraphicsApi.METAL -> MetalRedrawer(layer, properties)
                    else -> MacOsOpenGLRedrawer(layer, properties)
                }
        }
        OS.Windows -> {
            object: PlatformOperations {
                override fun isFullscreen(component: Component): Boolean {
                    val window = SwingUtilities.getRoot(component) as Window
                    val device = window.graphicsConfiguration.device
                    return device.getFullScreenWindow() == window
                }

                override fun setFullscreen(component: Component, value: Boolean) {
                    val window = SwingUtilities.getRoot(component) as Window
                    val device = window.graphicsConfiguration.device
                    device.setFullScreenWindow(if (value) window else null)
                }

                override fun disableTitleBar(platformInfo: Long) {
                }

                override fun orderEmojiAndSymbolsPopup() {
                }

                override fun getDpiScale(component: Component): Float {
                    return component.graphicsConfiguration.defaultTransform.scaleX.toFloat()
                }

                override fun createRedrawer(
                    layer: SkiaLayer,
                    renderApi: GraphicsApi,
                    properties: SkiaLayerProperties
                ) = when(renderApi) {
                    GraphicsApi.SOFTWARE -> SoftwareRedrawer(layer)
                    GraphicsApi.DIRECT3D -> Direct3DRedrawer(layer, properties)
                    else -> WindowsOpenGLRedrawer(layer, properties)
                }
            }
        }
        OS.Linux -> {
            object: PlatformOperations {
                override fun isFullscreen(component: Component): Boolean {
                    val window = SwingUtilities.getRoot(component) as Window
                    val device = window.graphicsConfiguration.device
                    return device.getFullScreenWindow() == window
                }

                override fun setFullscreen(component: Component, value: Boolean) {
                    val window = SwingUtilities.getRoot(component) as Window
                    val device = window.graphicsConfiguration.device
                    device.setFullScreenWindow(if (value) window else null)
                }

                override fun disableTitleBar(platformInfo: Long) {
                }

                override fun orderEmojiAndSymbolsPopup() {
                }

                override fun getDpiScale(component: Component): Float {
                    return component.graphicsConfiguration.defaultTransform.scaleX.toFloat()
                    // TODO doesn't work well because java doesn't scale windows (content has offset with 200% scale)
                    //
                    // Two solutions:
                    // 1. dynamically change sun.java2d.uiScale (it is global property,
                    // so we have to be careful) and update all windows
                    //
                    // 2. apply contentScale manually to all windows
                    // (it is not good, because on different platform windows will have different size.
                    // Maybe we will apply contentScale manually on all platforms?)

                    // see also comment for HardwareLayer.checkContentScale
                    // return component.useDrawingSurfacePlatformInfo(::linuxGetDpiScaleNative)
                }

                override fun createRedrawer(
                    layer: SkiaLayer,
                    renderApi: GraphicsApi,
                    properties: SkiaLayerProperties
                ) = when(renderApi) {
                    GraphicsApi.SOFTWARE -> SoftwareRedrawer(layer)
                    else -> LinuxOpenGLRedrawer(layer, properties)
                }
            }
        }
    }
}

// OSX
external private fun osxIsFullscreenNative(component: Component): Boolean
external private fun osxSetFullscreenNative(component: Component, value: Boolean)
external private fun osxDisableTitleBar(platformInfo: Long)
external private fun osxOrderEmojiAndSymbolsPopup()

// Linux
external private fun linuxGetDpiScaleNative(platformInfo: Long): Float
