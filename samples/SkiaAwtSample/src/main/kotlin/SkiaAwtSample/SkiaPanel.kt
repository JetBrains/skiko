package SkiaAwtSample

import org.jetbrains.skiko.ClipComponent
import org.jetbrains.skiko.SkiaLayer
import java.awt.Color
import java.awt.Component
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JLayeredPane

open class SkiaPanel: JLayeredPane {
    val layer = SkiaLayer()

    constructor() : super() {
        setLayout(null)
        setBackground(Color.white)
    }

    override fun add(component: Component): Component {
        layer.clipComponents.add(ClipComponent(component))
        return super.add(component, Integer.valueOf(0))
    }

    override fun addNotify() {
        super.addNotify()
        super.add(layer, Integer.valueOf(10))

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                layer.setSize(width, height)
            }
        })
    }

     override fun removeNotify() {
        layer.dispose()
        super.removeNotify()
     }
}
