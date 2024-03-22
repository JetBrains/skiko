package org.jetbrains.skiko

import org.jetbrains.skia.Canvas

interface SkikoRenderDelegate {
    fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long)
}

open class SkiaLayerRenderDelegate(
    val layer: SkiaLayer,
    val renderDelegate: SkikoRenderDelegate
): SkikoRenderDelegate {
    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
        renderDelegate.onRender(canvas, (width / contentScale).toInt(), (height / contentScale).toInt(), nanoTime)
        // Request next frame immediately.
        layer.needRedraw()
    }
}
