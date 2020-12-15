package org.jetbrains.skiko

import java.awt.Color
import java.awt.Component
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JLayeredPane
import org.jetbrains.skija.Rect

open class SkiaPanel: JLayeredPane {
    val layer = SkiaLayer()

    constructor() : super() {
        setLayout(null)
        setBackground(Color.white)
    }

    override fun add(component: Component): Component {
        layer.clipComponets.add(component)
        return super.add(component, Integer.valueOf(0))
    }

    override fun addNotify() {
        super.addNotify()
        super.add(layer, Integer.valueOf(10))

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                layer.reinit()
                layer.setSize(width, height)
            }
        })
    }

     override fun removeNotify() {
        super.removeNotify()
     }
}
