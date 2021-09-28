package org.jetbrains.skiko

import org.jetbrains.skiko.context.*
import org.jetbrains.skiko.redrawer.*

internal interface RenderFactory {
    fun createContextHandler(layer: SkiaLayer, renderApi: GraphicsApi): ContextHandler
    fun createRedrawer(layer: SkiaLayer, renderApi: GraphicsApi, properties: SkiaLayerProperties): Redrawer

    companion object {
        val Default = object : RenderFactory {
            override fun createContextHandler(layer: SkiaLayer, renderApi: GraphicsApi): ContextHandler {
                return when (renderApi) {
                    GraphicsApi.SOFTWARE -> return when (hostOs) {
                        OS.Windows -> WindowsSoftwareContextHandler(layer)
                        OS.Linux -> LinuxSoftwareContextHandler(layer)
                        OS.MacOS -> AWTSoftwareContextHandler(layer)
                        else -> AWTSoftwareContextHandler(layer)
                    }
                    GraphicsApi.AWTSOFTWARE -> AWTSoftwareContextHandler(layer)
                    GraphicsApi.OPENGL -> OpenGLContextHandler(layer)
                    GraphicsApi.DIRECT3D -> Direct3DContextHandler(layer)
                    GraphicsApi.METAL -> MetalContextHandler(layer)
                    else -> TODO("Unsupported yet.")
                }
            }

            override fun createRedrawer(
                layer: SkiaLayer,
                renderApi: GraphicsApi,
                properties: SkiaLayerProperties
            ): Redrawer = when (hostOs) {
                OS.MacOS -> when (renderApi) {
                    GraphicsApi.SOFTWARE -> SoftwareRedrawer(layer, properties)
                    else -> MetalRedrawer(layer, properties)
                }
                OS.Windows -> when (renderApi) {
                    GraphicsApi.SOFTWARE -> SoftwareRedrawer(layer, properties)
                    GraphicsApi.OPENGL -> WindowsOpenGLRedrawer(layer, properties)
                    else -> Direct3DRedrawer(layer, properties)
                }
                OS.Linux -> when (renderApi) {
                    GraphicsApi.SOFTWARE -> SoftwareRedrawer(layer, properties)
                    else -> LinuxOpenGLRedrawer(layer, properties)
                }
            }
        }
    }
}