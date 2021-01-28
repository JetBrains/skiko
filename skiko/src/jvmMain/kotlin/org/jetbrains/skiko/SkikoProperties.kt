package org.jetbrains.skiko

@Suppress("SameParameterValue")
internal object SkikoProperties {
    val vsyncEnabled: Boolean by property("skiko.vsync.enabled", default = true)

    val fpsEnabled: Boolean by property("skiko.fps.enabled", default = false)
    val fpsCount: Int by property("skiko.fps.count", default = 300)
    val fpsProbability: Double by property("skiko.fps.probability", default = 0.97)

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