package org.jetbrains.skiko

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.jetbrains.skiko.redrawer.dispatcherToBlockOn
import java.awt.Canvas
import java.awt.Component
import java.awt.Graphics
import java.awt.event.InputMethodEvent
import javax.accessibility.AccessibleContext

internal open class HardwareLayer(
    private val accessibleContextProvider: ((Component) -> AccessibleContext)? = null
) : Canvas() {
    companion object {
        init {
            Library.load()
        }
    }

    override fun paint(g: Graphics) {}

    open fun init() {
        useDrawingSurfacePlatformInfo(::nativeInit)
    }

    open fun dispose() {
        nativeDispose()
    }

    fun doProcessInputMethodEvent(e: InputMethodEvent?) {
        processInputMethodEvent(e)
    }

    private external fun nativeInit(platformInfo: Long)
    private external fun nativeDispose()

    val contentHandle: Long
        get() = useDrawingSurfacePlatformInfo(::getContentHandle)

    val windowHandle: Long
        get() = useDrawingSurfacePlatformInfo(::getWindowHandle)

    val currentDPI: Int
        get() = useDrawingSurfacePlatformInfo(::getCurrentDPI)

    var fullscreen: Boolean
        get() = platformOperations.isFullscreen(this)
        set(value) = platformOperations.setFullscreen(this, value)

    fun disableTitleBar(customHeaderHeight: Float) {
        platformOperations.disableTitleBar(this, customHeaderHeight)
    }

    private external fun getContentHandle(platformInfo: Long): Long
    private external fun getWindowHandle(platformInfo: Long): Long
    private external fun getCurrentDPI(platformInfo: Long): Int

    override fun getAccessibleContext(): AccessibleContext {
        return accessibleContextProvider?.invoke(this) ?: super.getAccessibleContext()
    }
}

internal fun layerFrameLimiter(
    scope: CoroutineScope,
    component: HardwareLayer,
    onNewFrameLimit: (frameLimit: Double) -> Unit = {}
): FrameLimiter {
    val state = object {
        @Volatile
        var frameLimit = MinMainstreamMonitorRefreshRate
    }

    val frames = Channel<Unit>(Channel.CONFLATED)
    frames.trySend(Unit)

    // Use UI thread, because getDisplayRefreshRate uses UI lock anyway, and there is no point to call it in
    // a separate thread. Besides that, if we call it in a separate thread, we can have deadlocks
    scope.launch(MainUIDispatcher) {
        while (true) {
            frames.receive()
            //  on my machine it takes 0.2ms on Linux, 0.01ms on macOs, 0.1ms on Windows
            state.frameLimit = component.getDisplayRefreshRate()
            onNewFrameLimit(state.frameLimit)
            delay(1000)
        }
    }

    return FrameLimiter(
        scope + dispatcherToBlockOn,
        frameMillis = {
            frames.trySend(Unit)
            (1000 / state.frameLimit).toLong()
        }
    )
}
