package org.jetbrains.skiko

@Suppress("SameParameterValue")
internal object SkikoProperties {
    val vsyncEnabled: Boolean by property("skiko.vsync.enabled", default = true)

    val fpsEnabled: Boolean by property("skiko.fps.enabled", default = false)
    val fpsPeriodSeconds: Double by property("skiko.fps.periodSeconds", default = 2.0)

    /**
     * Show long frames which is longer than [fpsLongFramesMillis].
     * If [fpsLongFramesMillis] isn't defined will show frames longer than 1.5 * (1000 / displayRefreshRate)
     */
    val fpsLongFramesShow: Boolean by property("skiko.fps.longFrames.show", default = false)

    val fpsLongFramesMillis: Double? by property("skiko.fps.longFrames.millis", default = null)

    val renderApi: GraphicsApi by lazy {
        val environment = System.getenv("SKIKO_RENDER_API")
        val property = System.getProperty("skiko.renderApi")
        if (environment != null) {
            parseRenderApi(environment)
        } else {
            parseRenderApi(property)
        }
    }

    private fun parseRenderApi(text: String?): GraphicsApi {
        when(text) {
            "SOFTWARE" -> return GraphicsApi.SOFTWARE
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
            OS.MacOS -> return GraphicsApi.OPENGL
            OS.Linux -> return GraphicsApi.OPENGL
            OS.Windows -> return GraphicsApi.DIRECT3D
        }
    }

    private fun property(name: String, default: Boolean) = lazy {
        System.getProperty(name)?.toBoolean() ?: default
    }

    private fun property(name: String, default: Double) = lazy {
        System.getProperty(name)?.toDouble() ?: default
    }

    private fun property(name: String, default: Double?) = lazy {
        System.getProperty(name)?.toDouble() ?: default
    }
}