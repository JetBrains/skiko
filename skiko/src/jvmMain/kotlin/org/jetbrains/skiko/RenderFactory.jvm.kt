package org.jetbrains.skiko

import org.jetbrains.skiko.redrawer.*

internal actual fun makeDefaultRenderFactory(): RenderFactory {
    return object : RenderFactory {
        override fun createRedrawer(
            layer: SkiaLayer,
            renderApi: GraphicsApi,
            properties: SkiaLayerProperties
        ): Redrawer = when (hostOs) {
            OS.MacOS -> when (renderApi) {
                GraphicsApi.AWT_SOFTWARE, GraphicsApi.SOFTWARE -> SoftwareRedrawer(layer, properties)
                else -> MetalRedrawer(layer, properties)
            }
            OS.Windows -> when (renderApi) {
                GraphicsApi.AWT_SOFTWARE -> SoftwareRedrawer(layer, properties)
                GraphicsApi.SOFTWARE -> WindowsSoftwareRedrawer(layer, properties)
                GraphicsApi.OPENGL -> WindowsOpenGLRedrawer(layer, properties)
                else -> Direct3DRedrawer(layer, properties)
            }
            OS.Linux -> when (renderApi) {
                GraphicsApi.AWT_SOFTWARE -> SoftwareRedrawer(layer, properties)
                GraphicsApi.SOFTWARE -> LinuxSoftwareRedrawer(layer, properties)
                else -> LinuxOpenGLRedrawer(layer, properties)
            }
            OS.JS, OS.Ios -> {
                TODO("Commonize me")
            }
        }
    }
}
