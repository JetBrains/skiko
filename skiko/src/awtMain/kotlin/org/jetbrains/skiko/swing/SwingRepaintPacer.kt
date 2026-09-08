package org.jetbrains.skiko.swing

import java.awt.Component

/**
 * The repaint entry point used by [SkiaSwingLayer.needRender].
 *
 * This initial implementation deliberately preserves Swing's existing repaint behavior. Pacing
 * can be added behind this small boundary without changing the public API again.
 */
internal class SwingRepaintPacer(
    private val component: Component
) {
    fun requestRepaint() = component.repaint()

    fun dispose() = Unit
}
