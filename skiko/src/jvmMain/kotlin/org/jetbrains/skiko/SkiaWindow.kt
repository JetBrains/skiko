package org.jetbrains.skiko

import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JFrame

open class SkiaWindow : JFrame() {
    val layer = SkiaLayer()

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
}
