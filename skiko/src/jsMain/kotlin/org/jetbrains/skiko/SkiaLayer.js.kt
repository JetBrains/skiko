package org.jetbrains.skiko

import org.jetbrains.skiko.wasm.api.CanvasRenderer
import org.w3c.dom.HTMLCanvasElement

actual open class SkiaLayer(properties: SkiaLayerProperties = makeDefaultSkiaLayerProperties()
) {
    private var state: CanvasRenderer? = null

    actual var renderApi: GraphicsApi = GraphicsApi.WEBGL
    actual val contentScale: Float
        get() = 1.0f
    actual var fullscreen: Boolean
        get() = false
        set(value) = throw Exception("Fullscreen is not supported!")
    actual var transparency: Boolean
        get() = false
        set(value) = throw Exception("Transparency is not supported!")

    actual fun needRedraw() {
        draw()
    }

    actual var renderer: SkiaRenderer? = null

    fun setCanvas(htmlCanvas: HTMLCanvasElement) {
        state = object: CanvasRenderer(htmlCanvas) {
            override fun drawFrame(currentTimestamp: Double) {
                // currentTimestamp is milliseconds.
                val currentNanos = currentTimestamp * 1000000
                renderer?.onRender(canvas, width, height, currentNanos.toLong())
            }
        }
    }

    fun draw() {
        state?.draw()
    }
}
