package org.jetbrains.skiko.sample

import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.skiko.GenericSkikoView
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.notifications.Notification
import org.jetbrains.skiko.onContentScaleChanged
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.HTMLCanvasElement

fun main() {
    onWasmReady {
        val skiaLayer = SkiaLayer()
        onContentScaleChanged = { scale -> println(scale) }
        val game = Clocks(skiaLayer)
        skiaLayer.skikoView = GenericSkikoView(skiaLayer, game)
        val canvas = document.getElementById("SkikoTarget") as HTMLCanvasElement
        canvas.setAttribute("tabindex", "0")
        skiaLayer.attachTo(canvas)
        skiaLayer.needRedraw()

        GlobalScope.launch {
            Notification(
                title = "Hello",
                body = "It works",
            ).send()
        }
    }
}
