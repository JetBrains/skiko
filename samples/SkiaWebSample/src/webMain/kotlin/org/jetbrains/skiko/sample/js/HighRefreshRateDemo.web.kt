@file:OptIn(org.jetbrains.skiko.ExperimentalSkikoApi::class, kotlin.js.ExperimentalWasmJsInterop::class)

package org.jetbrains.skiko.sample.js

import kotlinx.browser.document
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoRenderDelegate
import org.w3c.dom.HTMLCanvasElement
import kotlin.js.js

internal fun runHighRefreshRateDemo() {
    val layers = mutableListOf<SkiaLayer>()
    for (highRefresh in listOf(false, true)) {
        val id = if (highRefresh) "high" else "normal"
        val element = document.getElementById(id) as HTMLCanvasElement
        val status = document.getElementById("$id-status")!!
        val layer = SkiaLayer().apply { experimentalHighRefreshRateEnabled = highRefresh }
        val paint = Paint().apply { color = if (highRefresh) Color.RED else Color.BLUE }
        var start = 0L
        var frames = 0
        layer.renderDelegate = SkikoRenderDelegate { canvas, width, height, nanos ->
            canvas.clear(Color.WHITE)
            val x = ((nanos / 1_000_000.0 % 2000) / 2000 * width).toFloat()
            canvas.drawCircle(x, height / 2f, 20 * layer.contentScale, paint)
            if (start == 0L) start = nanos else frames++
            val elapsed = (nanos - start) / 1_000_000.0
            if (elapsed >= 1000) {
                status.textContent = "${(frames * 1000 / elapsed).toInt()} render callbacks/s"
                frames = 0
                start = nanos
            }
            if (animationEnabled()) layer.needRender()
        }
        layer.attachTo(element)
        observeSize(element) { width, height ->
            layer.resize(width, height)
            layer.needRender()
        }
        layer.needRender()
        layers.add(layer)
    }
    onAnimationToggle { layers.forEach { it.needRender() } }
    document.body!!.setAttribute("data-skiko-ready", "true")
}

private fun animationEnabled(): Boolean = js("document.getElementById('animate').checked")
private fun onAnimationToggle(callback: () -> Unit): Unit = js("document.getElementById('animate').addEventListener('change', callback)")
private fun observeSize(canvas: HTMLCanvasElement, callback: (Int, Int) -> Unit): Unit = js("""{
    new ResizeObserver(function() {
        callback(Math.round(canvas.clientWidth * devicePixelRatio), Math.round(canvas.clientHeight * devicePixelRatio));
    }).observe(canvas);
}""")
