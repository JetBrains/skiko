package org.jetbrains.skiko

import org.jetbrains.skiko.redrawer.LinuxRedrawer
import org.jetbrains.skiko.redrawer.MacOsRedrawer
import org.jetbrains.skiko.redrawer.Redrawer
import org.jetbrains.skiko.redrawer.WindowsRedrawer
import java.awt.Component
import java.awt.Window
import javax.swing.SwingUtilities

internal interface PlatformOperations {
    fun isFullscreen(component: Component): Boolean
    fun setFullscreen(component: Component, value: Boolean)
    fun getDpiScale(component: Component): Float
    fun createHardwareRedrawer(layer: HardwareLayer): Redrawer
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

                override fun createHardwareRedrawer(layer: HardwareLayer) = MacOsRedrawer(layer)
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

                override fun getDpiScale(component: Component): Float {
                    return component.graphicsConfiguration.defaultTransform.scaleX.toFloat()
                }

                override fun createHardwareRedrawer(layer: HardwareLayer) = WindowsRedrawer(layer)
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

                override fun getDpiScale(component: Component): Float {
                    return component.graphicsConfiguration.defaultTransform.scaleX.toFloat()
                    // TODO doesn't work well because java doesn't scale windows (content has offset with 200% scale)
                    //
                    // Two solutions:
                    // 1. dynamically change sun.java2d.uiScale (it is global property, so we have to be careful) and update all windows
                    //
                    // 2. apply contentScale manually to all windows
                    // (it is not good, because on different platform windows will have different size.
                    // Maybe we will apply contentScale manually on all platforms?)

                    // see also comment for HardwareLayer.checkContentScale

                    // return linuxGetDpiScaleNative(component)
                }

                override fun createHardwareRedrawer(layer: HardwareLayer) = LinuxRedrawer(layer)
            }
        }
    }
}

// OSX
external private fun osxIsFullscreenNative(component: Component): Boolean
external private fun osxSetFullscreenNative(component: Component, value: Boolean)

// Linux
external private fun linuxGetDpiScaleNative(component: Component): Float
