package org.jetbrains.skiko.native

import org.jetbrains.skiko.redrawer.Redrawer

internal interface PlatformOperations {
    fun createRedrawer(layer: HardwareLayer, properties: SkiaLayerProperties): Redrawer
}

// TODO: commonize more.
internal val platformOperations: PlatformOperations by lazy {
    makePlatformOperations()
}

internal expect fun makePlatformOperations(): PlatformOperations

