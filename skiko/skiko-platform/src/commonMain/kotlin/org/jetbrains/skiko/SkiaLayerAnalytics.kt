package org.jetbrains.skiko

/**
 * Analytics that helps to know more about SkiaLayer behaviour.
 * Implementation usually uses third-party solution to send info to some centralized analytics gatherer.
 */
interface SkiaLayerAnalytics {
    /**
     * Create analytics for a renderer with specific API. Can be called multiple times because of API fallbacks.
     */
    @ExperimentalSkikoApi
    fun renderer(
        skikoVersion: String,
        os: OS,
        api: GraphicsApi
    ): RendererAnalytics = RendererAnalytics.Empty

    /**
     * Create analytics for a device with specific API and name. Can be called multiple times because of API fallbacks.
     */
    @ExperimentalSkikoApi
    fun device(
        skikoVersion: String,
        os: OS,
        api: GraphicsApi,
        deviceName: String?
    ): DeviceAnalytics = DeviceAnalytics.Empty

    @ExperimentalSkikoApi
    interface RendererAnalytics {
        fun init() = Unit
        fun deviceChosen() = Unit

        companion object {
            val Empty = object : RendererAnalytics {}
        }
    }

    @ExperimentalSkikoApi
    interface DeviceAnalytics {
        fun init() = Unit
        fun contextInit() = Unit
        fun beforeFirstFrameRender() = Unit
        fun afterFirstFrameRender() = Unit
        fun beforeFrameRender() = Unit
        fun afterFrameRender() = Unit

        companion object {
            val Empty = object : DeviceAnalytics {}
        }
    }

    companion object {
        val Empty = object : SkiaLayerAnalytics {}
    }
}