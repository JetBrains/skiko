package org.jetbrains.skiko

import java.lang.System.getProperty

// TODO maybe we can get rid of global properties, and pass SkiaLayerProperties to Window -> ComposeWindow -> SkiaLayer
@Suppress("SameParameterValue")
/**
 * Global Skiko properties, which are read from system JDK variables orr from environment variables
 */
object SkikoProperties {
    val vsyncEnabled: Boolean get() = getProperty("skiko.vsync.enabled")?.toBoolean() ?: true

    /**
     * If vsync is enabled, but platform can't support it (Software renderer, Linux with uninstalled drivers),
     * we enable frame limit by the display refresh rate.
     */
    val vsyncFramelimitFallbackEnabled: Boolean get() = getProperty(
        "skiko.vsync.framelimit.fallback.enabled"
    )?.toBoolean() ?: true

    val fpsEnabled: Boolean get() = getProperty("skiko.fps.enabled")?.toBoolean() ?: false
    val fpsPeriodSeconds: Double get() = getProperty("skiko.fps.periodSeconds")?.toDouble() ?: 2.0

    /**
     * Show long frames which is longer than [fpsLongFramesMillis].
     * If [fpsLongFramesMillis] isn't defined will show frames longer than 1.5 * (1000 / displayRefreshRate)
     */
    val fpsLongFramesShow: Boolean get() = getProperty("skiko.fps.longFrames.show")?.toBoolean() ?: false

    val fpsLongFramesMillis: Double? get() = getProperty("skiko.fps.longFrames.millis")?.toDouble()

    val renderApi: GraphicsApi get() {
        val environment = System.getenv("SKIKO_RENDER_API")
        val property = getProperty("skiko.renderApi")
        return parseRenderApi(environment ?: property)
    }

    val gpuPriority: GpuPriority get() {
        val value = getProperty("skiko.gpu.priority") ?:
            getProperty("skiko.metal.gpu.priority") ?: // for backward compatability
            getProperty("skiko.directx.gpu.priority")  // for backward compatability

        return value?.let(GpuPriority::parseOrNull) ?: GpuPriority.Auto
    }

    internal fun parseRenderApi(text: String?): GraphicsApi {
        when(text) {
            "SOFTWARE_COMPAT" -> return GraphicsApi.SOFTWARE_COMPAT
            "SOFTWARE_FAST", "DIRECT_SOFTWARE" -> return GraphicsApi.SOFTWARE_FAST
            "SOFTWARE" -> return if (hostOs == OS.MacOS) GraphicsApi.SOFTWARE_COMPAT else GraphicsApi.SOFTWARE_FAST
            "OPENGL" ->
                // Skia isn't properly tested on OpenGL and Windows ARM (https://groups.google.com/g/skia-discuss/c/McoclAhLpvg?pli=1)
                return if (hostOs != OS.Windows || hostArch != Arch.Arm64) GraphicsApi.OPENGL
                    else throw Exception("$hostOs-$hostArch does not support OpenGL rendering API.")
            "DIRECT3D" -> {
                return if (hostOs == OS.Windows) GraphicsApi.DIRECT3D
                    else throw Exception("$hostOs does not support DirectX rendering API.")
            }
            "METAL" -> {
                return if (hostOs == OS.MacOS) GraphicsApi.METAL
                    else throw Exception("$hostOs does not support Metal rendering API.")
            }
            else -> return bestRenderApiForCurrentOS()
        }
    }

    private fun bestRenderApiForCurrentOS(): GraphicsApi {
        when(hostOs) {
            OS.MacOS -> return GraphicsApi.METAL
            OS.Linux -> return GraphicsApi.OPENGL
            OS.Windows -> return GraphicsApi.DIRECT3D
            OS.Android -> return GraphicsApi.OPENGL
            OS.JS, OS.Ios, OS.Tvos -> TODO("commonize me")
        }
    }

    internal fun fallbackRenderApiQueue(initialApi: GraphicsApi) : List<GraphicsApi> {
        var fallbackApis = when (hostOs) {
            OS.Linux -> listOf(GraphicsApi.OPENGL, GraphicsApi.SOFTWARE_FAST, GraphicsApi.SOFTWARE_COMPAT)
            OS.MacOS -> listOf(GraphicsApi.METAL, GraphicsApi.SOFTWARE_COMPAT)
            OS.Windows -> when (hostArch) {
                // Skia isn't properly tested on OpenGL and Windows ARM (https://groups.google.com/g/skia-discuss/c/McoclAhLpvg?pli=1)
                Arch.Arm64 -> listOf(GraphicsApi.DIRECT3D, GraphicsApi.SOFTWARE_FAST, GraphicsApi.SOFTWARE_COMPAT)
                else -> listOf(GraphicsApi.DIRECT3D, GraphicsApi.OPENGL, GraphicsApi.SOFTWARE_FAST, GraphicsApi.SOFTWARE_COMPAT)
            }
            OS.Android -> return listOf(GraphicsApi.OPENGL)
            OS.JS, OS.Ios, OS.Tvos -> TODO("commonize me")
        }

        val indexOfInitialApi = fallbackApis.indexOf(initialApi)
        require(indexOfInitialApi >= 0) {
            "$hostOs does not support $initialApi rendering API."
        }
        fallbackApis = fallbackApis.drop(indexOfInitialApi + 1)

        return listOf(initialApi) + fallbackApis
    }
}
