package org.jetbrains.skiko.properties

import java.awt.Component
import java.awt.Window
import javax.swing.SwingUtilities
import org.jetbrains.skiko.properties.PlatformProperties

internal class WindowsProperties : PlatformProperties {
    override fun isFullscreen(component: Component): Boolean {
        val window = SwingUtilities.getRoot(component) as Window
        val device = window.graphicsConfiguration.device
        val result = device.getFullScreenWindow()
        if (result == null) {
            return false
        }
        if (result == window) {
            return true
        } else {
            return false
        }
    }
    
    override fun makeFullscreen(component: Component, value: Boolean) {
        val window = SwingUtilities.getRoot(component) as Window
        val device = window.graphicsConfiguration.device
        device.setFullScreenWindow(if (value) window else null)
    }
}