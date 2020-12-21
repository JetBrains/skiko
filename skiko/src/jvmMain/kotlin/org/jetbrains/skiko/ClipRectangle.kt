package org.jetbrains.skiko

import java.awt.Component

interface ClipRectangle {
    val x: Float
    val y: Float
    val width: Float
    val height: Float
}

class ClipComponent(val component: Component) : ClipRectangle {
    override val x: Float
        get() = component.x.toFloat()
    override val y: Float
        get() = component.y.toFloat()
    override val width: Float
        get() = component.width.toFloat()
    override val height: Float
        get() = component.height.toFloat()
}