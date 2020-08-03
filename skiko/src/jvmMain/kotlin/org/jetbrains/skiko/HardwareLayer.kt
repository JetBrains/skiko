package org.jetbrains.skiko

import java.awt.Graphics
import java.awt.Canvas

open class HardwareLayer : Canvas(), Drawable {
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