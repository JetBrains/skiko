package org.jetbrains.skiko

import java.awt.Component
import java.awt.Window
import javax.swing.SwingUtilities

internal interface PlatformOperations {
    fun isFullscreen(component: Component): Boolean
    fun setFullscreen(component: Component, value: Boolean)
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
        }
        else -> {
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
            }
        }
    }
}

external private fun osxIsFullscreenNative(component: Component): Boolean
external private fun osxSetFullscreenNative(component: Component, value: Boolean)