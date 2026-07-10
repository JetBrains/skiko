@file:OptIn(ExperimentalSkikoApi::class)

package SkiaAwtSample

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Picture
import org.jetbrains.skia.PictureRecorder
import org.jetbrains.skia.Rect
import org.jetbrains.skiko.DisplayFrameTicker
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.FrameListener
import org.jetbrains.skiko.SkiaPanel
import org.jetbrains.skiko.SkikoRenderDelegate
import java.awt.Window
import javax.swing.Timer

/**
 * Records [delegate] into a fresh [Picture] sized to [panel]'s device pixels, matching the contract of
 * `SkiaLayerRenderDelegate`: the canvas is pre-scaled by the content scale and the
 * delegate is handed *logical* width/height. Background and clip cutouts are NOT recorded here — [SkiaPanel]
 * applies those on the present path.
 */
private fun record(panel: SkiaPanel, delegate: SkikoRenderDelegate, recorder: PictureRecorder, nanoTime: Long): Picture {
    val scale = panel.contentScale
    val deviceWidth = (panel.width * scale).toInt().coerceAtLeast(1)
    val deviceHeight = (panel.height * scale).toInt().coerceAtLeast(1)
    val canvas = recorder.beginRecording(Rect.makeWH(deviceWidth.toFloat(), deviceHeight.toFloat()))
    canvas.scale(scale, scale)
    delegate.onRender(canvas, (deviceWidth / scale).toInt(), (deviceHeight / scale).toInt(), nanoTime)
    return recorder.finishRecordingAsPicture()
}

/**
 * Drives a [SkiaPanel.RenderMode.DirectSurface] panel from a per-window vsync [DisplayFrameTicker]: on each
 * frame it records the delegate into a [Picture] and hands it to [SkiaPanel.present], then re-schedules for
 * continuous animation. This is the push-only replacement for the deprecated `SkiaLayer` pull loop.
 *
 * Construct it only after [window] is realized and hosts the panel's heavyweight surface (the
 * `DisplayFrameTicker(window)` factory derives the vsync source from that surface's native handle).
 */
class DirectSurfaceDriver(
    private val panel: SkiaPanel,
    private val delegate: SkikoRenderDelegate,
    window: Window,
) : FrameListener {
    private val ticker = DisplayFrameTicker(window)
    private val subscription = ticker.subscribe(this)
    private val recorder = PictureRecorder()

    fun start() = ticker.scheduleFrame()

    override suspend fun onFrame(targetTimeNanos: Long) {
        panel.present(record(panel, delegate, recorder, targetTimeNanos))
        ticker.scheduleFrame() // keep animating
    }

    fun dispose() {
        subscription.close()
        ticker.close()
        recorder.close()
    }

    /**
     * Rasterize the current frame straight to a [Bitmap]. [SkiaPanel] intentionally has no `screenshot()` (it
     * is push-only and does not retain a queryable frame), so the caller — which owns the content — renders it
     * itself.
     */
    fun screenshot(): Bitmap {
        val scale = panel.contentScale
        val w = (panel.width * scale).toInt().coerceAtLeast(1)
        val h = (panel.height * scale).toInt().coerceAtLeast(1)
        val bitmap = Bitmap().apply { allocN32Pixels(w, h) }
        val canvas = Canvas(bitmap)
        canvas.scale(scale, scale)
        delegate.onRender(canvas, (w / scale).toInt(), (h / scale).toInt(), System.nanoTime())
        bitmap.setImmutable()
        return bitmap
    }
}

/**
 * Drives a [SkiaPanel.RenderMode.SwingComposited] panel with a Swing [Timer].
 *
 * NOTE: this deliberately does NOT use `DisplayFrameTicker(window)` — that factory requires the window to host
 * a heavyweight skiko surface (a `HardwareLayer`), which a pure SwingComposited panel does not create.
 * Because that surface is absent, a ~60fps Swing timer drives this panel instead of a vsync ticker.
 */
class SwingCompositedDriver(
    private val panel: SkiaPanel,
    private val delegate: SkikoRenderDelegate,
) {
    private val recorder = PictureRecorder()
    private val timer = Timer(1000 / 60) {
        if (panel.isShowing) panel.present(record(panel, delegate, recorder, System.nanoTime()))
    }

    fun start() = timer.start()

    fun dispose() {
        timer.stop()
        recorder.close()
    }
}
