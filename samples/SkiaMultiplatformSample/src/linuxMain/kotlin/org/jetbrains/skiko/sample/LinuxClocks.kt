package org.jetbrains.skiko.sample

import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skia.Canvas

/**
 * Linux implementation of Clocks that tracks mouse position from LinuxWindow
 */
class LinuxClocks(
    private val skiaLayer: SkiaLayer,
    private val window: LinuxWindow
) : Clocks(skiaLayer::renderApi) {

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        // Update mouse position from window
        xpos = window.mouseX
        ypos = window.mouseY

        // Call parent implementation to render clocks
        super.onRender(canvas, width, height, nanoTime)
    }
}
