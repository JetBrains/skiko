package org.jetbrains.skiko

// TODO maybe we can get rid of global properties, and pass SkiaLayerProperties to Window -> ComposeWindow -> SkiaLayer
@Suppress("SameParameterValue")
internal object SkikoProperties {
    val vsyncEnabled: Boolean = property("skiko.vsync.enabled", default = true)

    /**
     * If vsync is enabled, but platform can't support it (Software renderer, Linux with uninstalled drivers),
     * we enable frame limit by the display refresh rate.
     */
    val vsyncFramelimitFallbackEnabled: Boolean = property(
        "skiko.vsync.framelimit.fallback.enabled", default = true
    )

    val fpsEnabled: Boolean = property("skiko.fps.enabled", default = false)
    val fpsPeriodSeconds: Double = property("skiko.fps.periodSeconds", default = 2.0)

    /**
     * Show long frames which is longer than [fpsLongFramesMillis].
     * If [fpsLongFramesMillis] isn't defined will show frames longer than 1.5 * (1000 / displayRefreshRate)
     */
    val fpsLongFramesShow: Boolean = property("skiko.fps.longFrames.show", default = false)

    val fpsLongFramesMillis: Double? = property("skiko.fps.longFrames.millis", default = null)

    val renderApi: GraphicsApi get() {
        val environment = System.getenv("SKIKO_RENDER_API")
        val property = System.getProperty("skiko.renderApi")
        return if (environment != null) {
            parseRenderApi(environment)
        } else {
            parseRenderApi(property)
        }
    }

    internal fun parseRenderApi(text: String?): GraphicsApi {
        when(text) {
            "SOFTWARE_COMPAT" -> return GraphicsApi.SOFTWARE_COMPAT
            "SOFTWARE_FAST", "DIRECT_SOFTWARE", "SOFTWARE" -> return GraphicsApi.SOFTWARE_FAST
            "OPENGL" -> return GraphicsApi.OPENGL
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
            OS.JS, OS.Ios -> TODO("commonize me")
        }
    }

    fun fallbackRenderApiQueue(initialApi: GraphicsApi) : List<GraphicsApi> {
        var fallbackApis = when (hostOs) {
            OS.Linux -> listOf(GraphicsApi.OPENGL, GraphicsApi.SOFTWARE_FAST, GraphicsApi.SOFTWARE_COMPAT)
            OS.MacOS -> listOf(GraphicsApi.METAL, GraphicsApi.SOFTWARE_COMPAT)
            OS.Windows -> listOf(GraphicsApi.DIRECT3D, GraphicsApi.OPENGL, GraphicsApi.SOFTWARE_FAST, GraphicsApi.SOFTWARE_COMPAT)
            OS.Android -> return listOf(GraphicsApi.OPENGL)
            OS.JS, OS.Ios -> TODO("commonize me")
        }

        val indexOfInitialApi = fallbackApis.indexOf(initialApi)
        if (indexOfInitialApi >= 0) {
            fallbackApis = fallbackApis.drop(indexOfInitialApi + 1)
        }

        return listOf(initialApi) + fallbackApis
    }

    private fun property(name: String, default: Boolean) =
        System.getProperty(name)?.toBoolean() ?: default

    private fun property(name: String, default: Double) =
        System.getProperty(name)?.toDouble() ?: default

    private fun property(name: String, default: Double?) =
        System.getProperty(name)?.toDouble() ?: default
}