package org.jetbrains.skiko

import java.awt.Component
import java.awt.Window
import javax.swing.SwingUtilities

internal interface PlatformOperations {
    fun isFullscreen(component: Component): Boolean
    fun setFullscreen(component: Component, value: Boolean)
    fun disableTitleBar(component: Component, headerHeight: Float)
    fun orderEmojiAndSymbolsPopup()
    fun getDpiScale(component: Component): Float
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

                override fun disableTitleBar(component: Component, headerHeight: Float) {
                    osxDisableTitleBar(component, headerHeight)
                }

                override fun orderEmojiAndSymbolsPopup() {
                    osxOrderEmojiAndSymbolsPopup()
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

                override fun disableTitleBar(component: Component, headerHeight: Float) {
                }

                override fun orderEmojiAndSymbolsPopup() {
                }

                override fun getDpiScale(component: Component): Float {
                    return component.graphicsConfiguration.defaultTransform.scaleX.toFloat()
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

                override fun disableTitleBar(component: Component, headerHeight: Float) {
                }

                override fun orderEmojiAndSymbolsPopup() {
                }

                override fun getDpiScale(component: Component): Float {
                    return component.graphicsConfiguration.defaultTransform.scaleX.toFloat()
                }
            }
        }
        OS.JS, OS.Ios -> {
            TODO("Commonize me")
        }
    }
}

// OSX
external private fun osxIsFullscreenNative(component: Component): Boolean
external private fun osxSetFullscreenNative(component: Component, value: Boolean)
external private fun osxDisableTitleBar(component: Component, headerHeight: Float)
external private fun osxOrderEmojiAndSymbolsPopup()

// Linux
external private fun linuxGetDpiScaleNative(platformInfo: Long): Float
