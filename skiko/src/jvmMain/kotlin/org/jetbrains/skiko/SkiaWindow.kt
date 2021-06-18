package org.jetbrains.skiko

import javax.swing.JFrame

open class SkiaWindow(
    properties: SkiaLayerProperties = SkiaLayerProperties()
) : JFrame() {
    val layer = SkiaLayer(properties)

    init {
        contentPane.add(layer)
    }

    override fun dispose() {
        layer.dispose()
        super.dispose()
    }

    fun disableTitleBar() {
        layer.backedLayer.useDrawingSurfacePlatformInfo {
            platformOperations.disableTitleBar(it)
        }
    }
}
