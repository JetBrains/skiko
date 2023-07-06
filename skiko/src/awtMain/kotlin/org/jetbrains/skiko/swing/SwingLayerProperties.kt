package org.jetbrains.skiko.swing

import org.jetbrains.skiko.GpuPriority
import java.awt.GraphicsConfiguration

internal interface SwingLayerProperties {
    val width: Int

    val height: Int

    val graphicsConfiguration: GraphicsConfiguration

    val adapterPriority: GpuPriority
}

internal val SwingLayerProperties.scale: Float get() = graphicsConfiguration.defaultTransform.scaleX.toFloat()