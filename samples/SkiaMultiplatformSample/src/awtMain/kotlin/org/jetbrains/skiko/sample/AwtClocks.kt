package org.jetbrains.skiko.sample

import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoInputEvent

class AwtClocks(layer: SkiaLayer) : Clocks(layer) {
    override var inputText: String = "Hello, AwtClocks"

    override fun handleBackspace() {
        if (inputText.isNotEmpty()) {
            inputText = inputText.dropLast(1)
        }
    }

    override fun onInputEvent(event: SkikoInputEvent) {
        if (event.input != "\b") {
            inputText += event.input
        }
    }

}