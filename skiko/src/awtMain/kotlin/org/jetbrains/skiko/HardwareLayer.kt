package org.jetbrains.skiko

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.awt.*
import java.awt.event.FocusEvent
import java.awt.event.InputMethodEvent
import java.beans.PropertyChangeEvent
import javax.accessibility.Accessible
import javax.accessibility.AccessibleContext

internal open class HardwareLayer(
    externalAccessibleFactory: ((Component) -> Accessible)? = null
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
        resetFocusAccessibleJob?.cancel()
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

    private val _externalAccessible = externalAccessibleFactory?.invoke(this)
    private var _focusedAccessible: Accessible? = null
    override fun getAccessibleContext(): AccessibleContext {
        val res = (_focusedAccessible ?: _externalAccessible)?.accessibleContext
        return res ?: super.getAccessibleContext()
    }

    private var resetFocusAccessibleJob: Job? = null

    fun requestNativeFocusOnAccessible(accessible: Accessible?) {
        _focusedAccessible = accessible

        when (hostOs) {
            OS.Windows -> requestAccessBridgeFocusOnAccessible()
            OS.MacOS -> requestMacOSFocusOnAccessible(accessible)
            else -> {
                _focusedAccessible = null
                return
            }
        }

        // Listener spawns asynchronous notification post procedure, reading current focus owner
        // and its accessibility context. This timeout is used to deal with concurrency
        // TODO Find more reliable procedure
        resetFocusAccessibleJob?.cancel()
        resetFocusAccessibleJob = GlobalScope.launch(MainUIDispatcher) {
            delay(100)
            _focusedAccessible = null
        }
    }

    private fun requestAccessBridgeFocusOnAccessible() {
        val focusEvent = FocusEvent(this, FocusEvent.FOCUS_GAINED)
        focusListeners.forEach { it.focusGained(focusEvent) }
    }

    private fun requestMacOSFocusOnAccessible(accessible: Accessible?) {
        val focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager()
        val listeners = focusManager.getPropertyChangeListeners("focusOwner")
        val event = PropertyChangeEvent(focusManager, "focusOwner", null, accessible)
        listeners.forEach { it.propertyChange(event) }
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
        scope + Dispatchers.IO,
        frameMillis = {
            frames.trySend(Unit)
            (1000 / state.frameLimit).toLong()
        }
    )
}