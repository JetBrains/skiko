package org.jetbrains.skiko

import java.awt.Component
import java.awt.Window
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.SwingUtilities

/**
 * A proxy between [SkiaLayer] and [HardwareLayer] that keeps the assigned "fullscreen" state while the layer doesn't
 * exist, and applies it once it does.
 */
internal class FullscreenAdapter(
    val backedLayer: HardwareLayer
): ComponentAdapter() {

    // Keep a `localFullscreen` flag which stores the virtual fullscreen state when the window is not actually
    // visible, and apply it when the window does become visible.
    private var localFullscreen = false

    // Additionally, keep an `isWindowShown` flag that says whether `backedLayer` or `localFullscreen` is currently
    // the source of truth. This flag must be used, rather than the real state (e.g. with `window.isVisible)
    // because otherwise the code becomes dependent on the order of listener calls. For example, `componentResized`
    // can be called when the window is already visible but before `componentShown` has been called. If the test
    // in `componentResized` was on `window.isVisible`, it would reset the value of `localFullscreen` before it had
    // applied in `componentShown`.
    private var isWindowShown = false

    var fullscreen: Boolean
        // If window is shown, return backedLayer.fullscreen; localFullscreen may not have updated yet
        get() = if (isWindowShown) backedLayer.fullscreen else localFullscreen
        set(value) {
            localFullscreen = value
            if (isWindowShown) {
                backedLayer.fullscreen = value
            }
        }

    override fun componentShown(e: ComponentEvent) {
        isWindowShown = true
        backedLayer.fullscreen = localFullscreen
    }

    override fun componentHidden(e: ComponentEvent?) {
        isWindowShown = false
        localFullscreen = backedLayer.fullscreen
    }

    override fun componentResized(e: ComponentEvent) {
        if (isWindowShown) {
            localFullscreen = backedLayer.fullscreen
        }
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
        else -> throw UnsupportedOperationException()
    }
}

// OSX
private external fun osxIsFullscreenNative(component: Component): Boolean
private external fun osxSetFullscreenNative(component: Component, value: Boolean)
private external fun osxDisableTitleBar(component: Component, headerHeight: Float)
private external fun osxOrderEmojiAndSymbolsPopup()

// Transparent window hack
private val TRANSPARENT_COLOR = java.awt.Color(0, 0, 0, 0)

/**
 * Returns the color that should be set as the [Window.background] for transparent windows.
 *
 * Note that this is a workaround for an implementation detail of OpenGL and Software
 * (and possibly other) renderers on Windows. As such, it may be removed in the future.
 */
@DelicateSkikoApi
fun transparentWindowBackgroundHack(renderApi: GraphicsApi): java.awt.Color? {
    // There is a hack inside OpenGL and Software redrawers for Windows that makes the
    // window transparent without setting the `background` of the AWT window. It is done
    // by getting the native component parent and calling `DwmEnableBlurBehindWindow`.
    //
    // FIXME: Make OpenGL work inside transparent window (background == Color(0, 0, 0, 0)) without this hack.
    // See `enableTransparentWindow` (skiko/src/awtMain/cpp/windows/window_util.cc)
    val skikoTransparentWindowHack = hostOs == OS.Windows && renderApi != GraphicsApi.DIRECT3D
    return if (skikoTransparentWindowHack) null else TRANSPARENT_COLOR
}
