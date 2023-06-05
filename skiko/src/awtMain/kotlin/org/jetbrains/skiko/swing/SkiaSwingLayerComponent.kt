package org.jetbrains.skiko.swing

import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skiko.ClipRectangle
import org.jetbrains.skiko.GraphicsApi
import javax.accessibility.Accessible
import javax.swing.JComponent

abstract class SkiaSwingLayerComponent : JComponent() {
    abstract val clipComponents: MutableList<ClipRectangle>

    abstract val renderApi: GraphicsApi

    abstract val pixelGeometry: PixelGeometry

    abstract fun dispose()

    abstract fun requestNativeFocusOnAccessible(accessible: Accessible?)
}