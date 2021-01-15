package org.jetbrains.skiko

import javax.swing.JFrame

open class SkiaWindow : JFrame() {
    val layer = SkiaLayer()

    init {
        contentPane.add(layer)
    }

    override fun dispose() {
        layer.dispose()
        super.dispose()
    }
}
