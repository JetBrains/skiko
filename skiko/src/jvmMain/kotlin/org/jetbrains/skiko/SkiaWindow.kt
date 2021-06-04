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
        useDrawingSurfacePlatformInfo {
            platformOperations.disableTitleBar(it)
        }
    }

    fun reparentTo(pid: Long, winId: Long) {
        useDrawingSurfacePlatformInfo {
            platformOperations.reparentTo(it, pid, winId)
        }
    }
}
