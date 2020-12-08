package org.jetbrains.skiko

import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JFrame
import org.jetbrains.skiko.layer.SkiaLayer

open class SkiaWindow : JFrame() {
    val layer: SkiaLayer = SkiaLayer()

    init {
        contentPane.add(layer)

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                layer.reinit()
            }
        })
    }

    fun display() {
        layer.display()
    }

    override fun setVisible(value: Boolean) {
        super.setVisible(value)
        display()
    }
}
