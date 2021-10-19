package org.jetbrains.skiko

import javax.swing.JFrame

@Deprecated("Will be removed soon")
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

fun SkiaLayer.disableTitleBar() {
    backedLayer.useDrawingSurfacePlatformInfo {
        platformOperations.disableTitleBar(it)
    }
}

fun orderEmojiAndSymbolsPopup() {
    platformOperations.orderEmojiAndSymbolsPopup()
}