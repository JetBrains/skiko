package org.jetbrains.skiko.native

import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.native.redrawer.MacOsOpenGLRedrawer

internal actual fun makePlatformOperations(): PlatformOperations {
    return object: PlatformOperations {
        override fun createRedrawer(layer: HardwareLayer, properties: SkiaLayerProperties) =
            when (SkikoProperties.renderApi) {
                GraphicsApi.OPENGL -> MacOsOpenGLRedrawer(layer, properties)
                else -> error("No other renderings for native yet")
            }
    }
}