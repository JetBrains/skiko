package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.HardwareLayer

internal class SoftwareRedrawer(
    private val layer: HardwareLayer
) : Redrawer {

    private val frameDispatcher = FrameDispatcher(Dispatchers.Swing) {
        layer.update(System.nanoTime())
        layer.draw()
    }

    override fun dispose() {
        frameDispatcher.cancel()
    }

    override fun needRedraw() {
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() {
        layer.update(System.nanoTime())
        layer.draw()
    }
}