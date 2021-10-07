package org.jetbrains.skiko

import javax.swing.JFrame

open class SkiaWindow(
    properties: SkiaLayerProperties = makeDefaultSkiaLayerProperties(),
    layerFactory: () -> SkiaLayer = { SkiaLayer(properties) }
) : JFrame() {
    val layer = layerFactory()

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

fun orderEmojiAndSymbolsPopup() {
    platformOperations.orderEmojiAndSymbolsPopup()
}