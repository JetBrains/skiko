package org.jetbrains.skiko

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Canvas
import java.awt.Component
import java.awt.Graphics
import java.awt.event.InputMethodEvent
import javax.accessibility.Accessible
import javax.accessibility.AccessibleContext
import kotlin.time.ExperimentalTime

internal open class HardwareLayer(
    externalAccessibleFactory: ((Component) -> Accessible)? = null
) : Canvas() {
    companion object {
        init {
            Library.load()
        }
    }

    // getDpiScale is expensive operation on some platforms, so we cache it
    private var _contentScale: Float? = null

    fun defineContentScale() {
        _contentScale = getDpiScale()
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

    // TODO checkContentScale is called before init. it is ok, but when we fix getDpiScale on Linux we should check [isInit]
    fun checkContentScale(): Boolean {
        val contentScale = getDpiScale()
        if (contentScale != _contentScale) {
            _contentScale = contentScale
            return true
        }
        return false
    }

    private fun getDpiScale(): Float {
        val scale = platformOperations.getDpiScale(this)
        check(scale > 0) { "HardwareLayer.contentScale isn't positive: $contentScale"}
        return scale
    }

    val contentHandle: Long
        get() = useDrawingSurfacePlatformInfo(::getContentHandle)

    val windowHandle: Long
        get() = useDrawingSurfacePlatformInfo(::getWindowHandle)

    val contentScale: Float
        get() = _contentScale!!

    var fullscreen: Boolean
        get() = platformOperations.isFullscreen(this)
        set(value) = platformOperations.setFullscreen(this, value)

    fun disableTitleBar(customHeaderHeight: Float) {
        platformOperations.disableTitleBar(this, customHeaderHeight)
    }

    private external fun getContentHandle(platformInfo: Long): Long
    private external fun getWindowHandle(platformInfo: Long): Long

    private val _externalAccessible = externalAccessibleFactory?.invoke(this)
    override fun getAccessibleContext(): AccessibleContext {
        val res = _externalAccessible?.accessibleContext
        return res ?: super.getAccessibleContext()
    }
}

/**
 * HardwareLayer should not dispose native resources while [scope] is active.
 *
 * So wait for scope cancellation in dispose method:
 * ```
 *  runBlocking {
 *      frameJob.cancelAndJoin()
 *  }
 * ```
 *
 * Can be accessed from multiple threads.
 */
@OptIn(ExperimentalTime::class)
@Suppress("UNUSED_PARAMETER")
internal fun FrameLimiter(
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

    scope.launch {
        while (true) {
            frames.receive()
            // TODO will lockLinuxDrawingSurface inside getDisplayRefreshRate can cause draw lock too?
            // it takes 2ms on my machine on Linux (0.01ms on macOs, 0.1ms on Windows)
            state.frameLimit = component.getDisplayRefreshRate()
            onNewFrameLimit(state.frameLimit)
            delay(1000)
        }
    }

    return FrameLimiter(
        scope,
        frameMillis = {
            frames.trySend(Unit)
            (1000 / state.frameLimit).toLong()
        }
    )
}