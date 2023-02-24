package org.jetbrains.skiko.sample

import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoUIView
import org.jetbrains.skiko.SkikoInput

class IosClocks(layer: SkiaLayer) : Clocks(layer) {
    override var inputText: String = "Hello, IosClocks"

    override fun handleBackspace() {
        if (inputText.isNotEmpty()) {
            inputText = inputText.dropLast(1)
        }
    }

    override val input: SkikoInput = object : SkikoInput by SkikoInput.Empty {
        override fun insertText(text: String) {
            inputText += text
        }

        override fun deleteBackward() {
            handleBackspace()
        }
    }
}
