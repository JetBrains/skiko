package org.jetbrains.skiko.sample

import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoInput
import org.jetbrains.skiko.SkikoInputEvent

class JsClocks(layer: SkiaLayer) : Clocks(layer) {
    override var inputText: String = "Hello, JsClocks"

    override fun handleBackspace() {
        if (inputText.isNotEmpty()) {
            inputText = inputText.dropLast(1)
        }
    }

    override val input: SkikoInput = object : SkikoInput {
        override fun onInputEvent(event: SkikoInputEvent) {
            inputText += event.input
        }
    }

}
