package org.jetbrains.skiko.sample

import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoInputEvent

class MacosClocks(layer: SkiaLayer): Clocks(layer) {
    override var inputText: String = ""

    override fun handleBackspace() {
        if (inputText.isNotEmpty()) {
            inputText = inputText.dropLast(1)
        }
    }

    override fun onInputEvent(event: SkikoInputEvent) {
        inputText += event.input
    }
}
