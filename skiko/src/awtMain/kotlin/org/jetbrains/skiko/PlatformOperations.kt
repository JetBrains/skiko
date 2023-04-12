package org.jetbrains.skiko

import java.awt.Component
import java.awt.Window
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.SwingUtilities

internal open class FullscreenAdapter(
    val backedLayer: HardwareLayer
): ComponentAdapter() {
    private var _isFullscreenDispatched = false
    private var _isFullscreen: Boolean = false
    var fullscreen: Boolean
        get() = _isFullscreen
        set(value) {
            _isFullscreen = value
            val window = SwingUtilities.getRoot(backedLayer)
            if ( window == null || !window.isVisible) {
                _isFullscreenDispatched = value
            } else {
                backedLayer.fullscreen = value
            }
        }

    override fun componentShown(e: ComponentEvent) {
        backedLayer.fullscreen = _isFullscreenDispatched
    }

    override fun componentHidden(e: ComponentEvent) {
        _isFullscreenDispatched = _isFullscreen
    }

    override fun componentResized(e: ComponentEvent) {
        _isFullscreen = backedLayer.fullscreen
    }
}

internal interface PlatformOperations {
    fun isFullscreen(component: Component): Boolean
    fun setFullscreen(component: Component, value: Boolean)
    fun disableTitleBar(component: Component, headerHeight: Float)
    fun orderEmojiAndSymbolsPopup()
}

internal val platformOperations: PlatformOperations by lazy {
    when (hostOs) {
        OS.MacOS -> {
            object: PlatformOperations {
                override fun isFullscreen(component: Component): Boolean {
                    return osxIsFullscreenNative(component)
                }

                override fun setFullscreen(component: Component, value: Boolean) {
                    osxSetFullscreenNative(component, value)
                }

                override fun disableTitleBar(component: Component, headerHeight: Float) {
                    osxDisableTitleBar(component, headerHeight)
                }

                override fun orderEmojiAndSymbolsPopup() {
                    osxOrderEmojiAndSymbolsPopup()
                }
            }
        }
        OS.Windows -> {
            object: PlatformOperations {
                override fun isFullscreen(component: Component): Boolean {
                    val window = SwingUtilities.getRoot(component) as Window
                    val device = window.graphicsConfiguration.device
                    return device.fullScreenWindow == window
                }

                override fun setFullscreen(component: Component, value: Boolean) {
                    val window = SwingUtilities.getRoot(component) as Window
                    val device = window.graphicsConfiguration.device
                    device.fullScreenWindow = if (value) window else null
                }

                override fun disableTitleBar(component: Component, headerHeight: Float) {
                }

                override fun orderEmojiAndSymbolsPopup() {
                }
            }
        }
        OS.Linux -> {
            object: PlatformOperations {
                override fun isFullscreen(component: Component): Boolean {
                    val window = SwingUtilities.getRoot(component) as Window
                    val device = window.graphicsConfiguration.device
                    return device.fullScreenWindow == window
                }

                override fun setFullscreen(component: Component, value: Boolean) {
                    val window = SwingUtilities.getRoot(component) as Window
                    val device = window.graphicsConfiguration.device
                    device.fullScreenWindow = if (value) window else null
                }

                override fun disableTitleBar(component: Component, headerHeight: Float) {
                }

                override fun orderEmojiAndSymbolsPopup() {
                }
            }
        }
        OS.Android -> TODO()
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
