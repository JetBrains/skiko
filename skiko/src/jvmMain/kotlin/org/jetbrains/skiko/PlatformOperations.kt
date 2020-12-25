package org.jetbrains.skiko

import java.awt.Component
import java.awt.Window
import javax.swing.SwingUtilities

internal interface PlatformOperations {
    fun isFullscreen(component: Component): Boolean
    fun setFullscreen(component: Component, value: Boolean)
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
                    return linuxGetDpiScaleNative(component)
                }
            }
        }
    }
}

internal val renderApi: Int = getPlatformRenderApi()

internal fun getPlatformRenderApi(): Int {
        val environment = System.getenv("SKIKO_RENDER_API")
        val property = System.getProperty("skiko.renderApi")
        if (environment != null) {
            return parseRenderApi(environment)
        }
        return parseRenderApi(property)
}

private fun parseRenderApi(text: String): Int {
    when(text) {
        "RASTER" -> return GraphicsApi.RASTER
        "OPENGL" -> return GraphicsApi.OPENGL
        "VULKAN" -> return GraphicsApi.VULKAN
        "METAL" -> return GraphicsApi.METAL
        else -> return GraphicsApi.OPENGL
    }
}

// OSX
external private fun osxIsFullscreenNative(component: Component): Boolean
external private fun osxSetFullscreenNative(component: Component, value: Boolean)

// Linux
external private fun linuxGetDpiScaleNative(component: Component): Float
