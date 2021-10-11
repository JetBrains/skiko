package org.jetbrains.skiko

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.UIKit.*

actual open class SkiaLayer {
    actual var renderApi: GraphicsApi = GraphicsApi.OPENGL

    actual val contentScale: Float
        get() = 1.0f
}