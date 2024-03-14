package org.jetbrains.skiko.sample

import kotlinx.browser.window
import org.jetbrains.skiko.SkiaLayer
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.TouchEvent
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.get

class WebClocks(skiaLayer: SkiaLayer, canvas: HTMLCanvasElement) : Clocks(skiaLayer.renderApi) {
    init {
        val scale = window.devicePixelRatio.toFloat()
        val bounds = canvas.getBoundingClientRect()
        canvas.addTypedEvent<TouchEvent>("touchmove") { event ->
            event.preventDefault()
            event.touches[0]?.let {
                xpos = (it.clientX - bounds.left) / scale
                ypos = (it.clientY - bounds.top) / scale
            }
        }
        canvas.addTypedEvent<MouseEvent>("mousemove") { event ->
            xpos = event.offsetX / scale
            ypos = event.offsetY / scale
        }
        skiaLayer.attachTo(canvas)
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T : Event> HTMLCanvasElement.addTypedEvent(
    type: String,
    handler: (event: T) -> Unit
) {
    this.addEventListener(type, { event -> handler(event as T) })
}
