package org.jetbrains.skiko.native

import org.jetbrains.skiko.native.*
import org.jetbrains.skiko.native.SkikoProperties.renderApi
import org.jetbrains.skiko.native.redrawer.*

internal interface PlatformOperations {
    fun createRedrawer(layer: HardwareLayer, properties: SkiaLayerProperties): Redrawer
}

internal val platformOperations: PlatformOperations by lazy {
        object: PlatformOperations {
                override fun createRedrawer(layer: HardwareLayer, properties: SkiaLayerProperties) = when(renderApi) {
                    GraphicsApi.OPENGL -> MacOsOpenGLRedrawer(layer, properties)
                    else -> error("No software rendering for native yet")
                }
        }
}
