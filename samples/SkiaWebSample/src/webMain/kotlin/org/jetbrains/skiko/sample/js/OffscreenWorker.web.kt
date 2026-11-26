@file:OptIn(org.jetbrains.skiko.ExperimentalSkikoApi::class, kotlin.js.ExperimentalWasmJsInterop::class)

package org.jetbrains.skiko.sample.js

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoRenderDelegate
import org.jetbrains.skiko.wasm.onWasmReady
import kotlin.js.JsAny
import kotlin.js.js

/** The same sample bundle can be loaded as a dedicated worker by offscreen.html. */
internal fun runOffscreenWorker(): Boolean {
    if (!isWorker()) return false
    onWasmReady { initializeOffscreenWorker() }
    return true
}

private fun initializeOffscreenWorker() {
    val layer = SkiaLayer()
    val paint = Paint().apply { color = Color.RED }
    var canvas: JsAny? = null
    var scale = 1f
    var frames = 0
    var reportStart = 0L
    layer.renderDelegate = SkikoRenderDelegate { target: Canvas, width, height, nanos ->
        target.clear(Color.WHITE)
        val x = ((nanos / 1_000_000.0 % 2000) / 2000 * width).toFloat()
        target.drawCircle(x, height / 2f, 20 * scale, paint)
        if (reportStart == 0L) reportStart = nanos else frames++
        val elapsed = (nanos - reportStart) / 1_000_000.0
        if (elapsed >= 1000) {
            reportFrames(frames, elapsed)
            frames = 0
            reportStart = nanos
        }
        layer.needRender()
    }
    onWorkerMessage { message ->
        when (messageType(message)) {
            "init" -> {
                canvas = messageCanvas(message)
                scale = messageScale(message).toFloat()
                layer.attachToOffscreenCanvas(canvas!!, scale)
                layer.needRender()
            }
            "resize" -> {
                canvas?.let {
                    scale = messageScale(message).toFloat()
                    layer.resize(messageWidth(message), messageHeight(message))
                    layer.attachToOffscreenCanvas(it, scale)
                    layer.needRender()
                }
            }
            "pause" -> layer.detach()
            "resume" -> canvas?.let {
                layer.attachToOffscreenCanvas(it, scale)
                frames = 0
                reportStart = 0L
                layer.needRender()
            }
        }
    }
    reportReady()
}

private fun isWorker(): Boolean = js("typeof document === 'undefined'")
private fun onWorkerMessage(callback: (JsAny) -> Unit): Unit =
    js("{ globalThis.onmessage = function(event) { callback(event.data); }; }")
private fun messageType(message: JsAny): String = js("message.type")
private fun messageCanvas(message: JsAny): JsAny = js("message.canvas")
private fun messageScale(message: JsAny): Double = js("message.scale")
private fun messageWidth(message: JsAny): Int = js("message.width")
private fun messageHeight(message: JsAny): Int = js("message.height")
private fun reportReady(): Unit = js("globalThis.postMessage({ type: 'ready' })")
private fun reportFrames(frames: Int, elapsed: Double): Unit =
    js("globalThis.postMessage({ type: 'frames', fps: frames * 1000 / elapsed })")
