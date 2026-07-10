@file:OptIn(ExperimentalSkikoApi::class)

package SkiaAwtSample

import org.jetbrains.skiko.ClipComponent
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.SkiaPanel
import org.jetbrains.skiko.SkikoRenderDelegate
import java.awt.Color
import java.awt.Component
import javax.swing.JLayeredPane

/**
 * The lightweight Swing-interop demo: a [SkiaPanel] in [SkiaPanel.RenderMode.SwingComposited] mode (the
 * push-only successor of `SkiaSwingLayer`) rasterizes offscreen and blits into Swing, so z-order and
 * double-buffering are respected. Overlay children are registered as clip cutouts.
 *
 * Driven by a [SwingCompositedDriver] (a Swing timer) rather than `DisplayFrameTicker(window)`: a
 * SwingComposited panel has no heavyweight surface, which the AWT ticker factory requires.
 */
class SkiaSwingPanel(delegate: SkikoRenderDelegate) : JLayeredPane() {
    val skiaPanel = SkiaPanel(renderMode = SkiaPanel.RenderMode.SwingComposited)
    private val driver = SwingCompositedDriver(skiaPanel, delegate)

    init {
        layout = null
        background = Color.white
    }

    override fun add(component: Component): Component {
        skiaPanel.clipComponents.add(ClipComponent(component))
        return super.add(component, Integer.valueOf(0))
    }

    override fun doLayout() {
        skiaPanel.setBounds(0, 0, width, height)
    }

    override fun addNotify() {
        super.addNotify()
        super.add(skiaPanel, Integer.valueOf(10))
        driver.start()
    }

    override fun removeNotify() {
        driver.dispose()
        skiaPanel.dispose()
        super.removeNotify()
    }
}
