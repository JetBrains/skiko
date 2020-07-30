package org.jetbrains.awthrl.Components

import org.jetbrains.awthrl.DriverApi.Engine
import java.awt.Graphics
import javax.swing.JFrame


open class Window : JFrame(), Drawable {
    override fun paint(g: Graphics) {
        Engine.get().render(this)
    }

    open fun draw() {}
    external override fun redrawLayer()
    external override fun updateLayer()
    external override fun disposeLayer()
    override val contentScale: Float
        external get
}