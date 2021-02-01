package org.jetbrains.skiko

@Suppress("SameParameterValue")
internal object SkikoProperties {
    val vsyncEnabled: Boolean by property("skiko.vsync.enabled", default = true)

    val fpsEnabled: Boolean by property("skiko.fps.enabled", default = false)
    val fpsCount: Int by property("skiko.fps.count", default = 300)
    val fpsProbability: Double by property("skiko.fps.probability", default = 0.97)

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
            else -> return GraphicsApi.OPENGL
        }
    }

    private fun property(name: String, default: Boolean) = lazy {
        System.getProperty(name)?.toBoolean() ?: default
    }

    private fun property(name: String, default: Int) = lazy {
        System.getProperty(name)?.toInt() ?: default
    }

    private fun property(name: String, default: Double) = lazy {
        System.getProperty(name)?.toDouble() ?: default
    }
}