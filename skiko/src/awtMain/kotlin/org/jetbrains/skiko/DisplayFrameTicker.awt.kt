package org.jetbrains.skiko

import kotlinx.coroutines.channels.Channel
import org.jetbrains.skiko.linux.LinuxVsyncTicker
import org.jetbrains.skiko.rendercontext.MetalVSyncer
import org.jetbrains.skiko.windows.WindowsVsyncTicker
import java.awt.Component
import java.awt.Container
import java.awt.Window
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Creates a per-window [DisplayFrameTicker] for AWT / desktop JVM, wiring the host OS's native vsync
 * source. CREATE only &mdash; the caller owns sharing and lifecycle; [close][DisplayFrameTicker.close] it
 * when done. Callbacks are delivered on the AWT event dispatch thread ([MainUIDispatcher]).
 *
 * The per-OS backing (the honest per-platform reality):
 * - **macOS** &mdash; `CVDisplayLink` via the internal `MetalVSyncer`; [FrameListener.onFrame] carries the
 *   display link's predicted present time (host clock).
 * - **Windows** &mdash; `DwmGetCompositionTimingInfo` (falling back to a fixed-interval poll) via the
 *   internal `WindowsVsyncTicker`; the timestamp is the predicted next compositor vblank.
 * - **Linux** &mdash; the GLX vblank counter via the internal `LinuxVsyncTicker` (preferring
 *   `GLX_OML_sync_control`'s real UST, falling back to `GLX_SGI_video_sync`). A driver exposing neither GLX
 *   extension (or no usable direct `GLXContext`) makes construction throw rather than yield a silently
 *   inert ticker &mdash; see `LinuxVsyncTicker`.
 *
 * Idle behaviour differs by OS: the macOS source parks its blocking thread when no frame is scheduled,
 * while the Windows/Linux native tick threads keep running between frames (frames are simply not delivered
 * while idle); after a long idle, the first frame on Windows/Linux may carry a slightly stale timestamp.
 * All of them stop for good on [close][DisplayFrameTicker.close].
 *
 * Requires the window to host a skiko rendering surface (a [HardwareLayer], as `SkiaLayer`/`SkiaPanel`
 * provide) &mdash; the per-window native handle is derived from it. A window without a skiko surface throws.
 */
fun DisplayFrameTicker(window: Window): DisplayFrameTicker {
    val hardwareLayer = findHardwareLayer(window)
        ?: error(
            "DisplayFrameTicker(window) requires the window to host a skiko rendering surface " +
                "(a HardwareLayer, as SkiaLayer/SkiaPanel provide); none was found in $window."
        )
    val driver = when {
        hostOs.isMacOS -> MetalAwtVsyncDriver(hardwareLayer.windowHandle)
        hostOs.isWindows -> createWindowsAwtVsyncDriver(hardwareLayer)
        hostOs.isLinux -> createLinuxAwtVsyncDriver(hardwareLayer)
        else -> error("DisplayFrameTicker(window) is not supported on $hostOs")
    }
    return AwtDisplayFrameTicker(driver)
}

private fun findHardwareLayer(component: Component): HardwareLayer? = when (component) {
    is HardwareLayer -> component
    is Container -> component.components.firstNotNullOfOrNull(::findHardwareLayer)
    else -> null
}

/**
 * Unifies the two shapes of internal AWT vsync source (macOS's pull-based [MetalVSyncer] and the
 * Windows/Linux push-based tickers) behind one suspending "await the next vsync" call.
 */
private interface AwtVsyncDriver : AutoCloseable {
    /** Suspends until the next vsync and returns its predicted present time, in nanoseconds. */
    suspend fun awaitVsync(): Long
}

private class AwtDisplayFrameTicker(private val driver: AwtVsyncDriver) : DisplayFrameTicker {
    private val listeners = CopyOnWriteArrayList<FrameListener>()

    @Volatile
    private var closed = false

    // Coalescing + idle pause/resume is provided by the internal FrameDispatcher; the frame body just awaits
    // the next vsync and fans it out. It runs on the EDT: awaitVsync only *suspends* the coroutine (the real
    // blocking, for macOS, happens on MetalVSyncer's own thread), so onFrame is delivered on the UI thread.
    private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
        val targetTimeNanos = driver.awaitVsync()
        if (!closed) {
            for (listener in listeners) dispatchFrame(listener, targetTimeNanos)
        }
    }

    override fun subscribe(listener: FrameListener): AutoCloseable {
        listeners.add(listener)
        return AutoCloseable { listeners.remove(listener) }
    }

    override fun scheduleFrame() {
        if (!closed) frameDispatcher.scheduleFrame()
    }

    override fun close() {
        if (closed) return
        closed = true
        frameDispatcher.cancel()
        listeners.clear()
        driver.close()
    }
}

/** macOS: `CVDisplayLink` via [MetalVSyncer] (pull model: [MetalVSyncer.waitForVSync] suspends). */
private class MetalAwtVsyncDriver(windowHandle: Long) : AwtVsyncDriver {
    private val vsyncer = MetalVSyncer(windowHandle)

    override suspend fun awaitVsync(): Long {
        vsyncer.waitForVSync()
        val predicted = vsyncer.lastVSyncOutputTimeNanos
        // 0 means no prediction was available (e.g. no display link yet); fall back to a sane clock.
        return if (predicted != 0L) predicted else currentNanoTime()
    }

    override fun close() = vsyncer.dispose()
}

/**
 * Windows/Linux: the push-based native tickers report ticks on their own thread; we funnel those into a
 * conflated channel that [awaitVsync] receives from. The native thread keeps running until [close].
 */
private class ChannelAwtVsyncDriver(
    private val channel: Channel<Long>,
    private val stopTicker: () -> Unit,
) : AwtVsyncDriver {
    override suspend fun awaitVsync(): Long = channel.receive()

    override fun close() {
        stopTicker()
        channel.close()
    }
}

private fun createWindowsAwtVsyncDriver(hardwareLayer: HardwareLayer): AwtVsyncDriver {
    val channel = Channel<Long>(Channel.CONFLATED)
    val ticker = WindowsVsyncTicker(hardwareLayer.windowHandle) { nanos -> channel.trySend(nanos) }
    ticker.start()
    return ChannelAwtVsyncDriver(channel) { ticker.stop() }
}

private fun createLinuxAwtVsyncDriver(hardwareLayer: HardwareLayer): AwtVsyncDriver {
    val channel = Channel<Long>(Channel.CONFLATED)
    // LinuxVsyncTicker.start() reads the X11 Display* on the calling thread, so it must run while the JAWT
    // drawing surface is locked (that keeps the connection valid); the native tick thread then uses its own
    // dedicated connection afterwards.
    val ticker = hardwareLayer.lockLinuxDrawingSurface { drawingSurface ->
        LinuxVsyncTicker(drawingSurface.display, drawingSurface.window) { nanos -> channel.trySend(nanos) }
            .also { it.start() }
    }
    return ChannelAwtVsyncDriver(channel) { ticker.stop() }
}
