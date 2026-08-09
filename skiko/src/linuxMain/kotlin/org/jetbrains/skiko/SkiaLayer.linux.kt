@file:OptIn(
    kotlinx.cinterop.ExperimentalForeignApi::class,
    org.jetbrains.skiko.ExperimentalSkikoApi::class,
)

package org.jetbrains.skiko

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.PixelGeometry

actual open class SkiaLayer(
    val properties: SkiaLayerProperties = SkiaLayerProperties(),
    private val analytics: SkiaLayerAnalytics = SkiaLayerAnalytics.Empty,
) {
    private var linuxComponent: LinuxSkiaLayerComponent? = null
    private var renderer: LinuxLayerRenderer? = null
    private var rendererAnalytics: SkiaLayerAnalytics.RendererAnalytics? = null
    private var deviceAnalytics: SkiaLayerAnalytics.DeviceAnalytics? = null
    private var firstFrameRendered = false
    private var renderPending = false
    private var pendingRenderWaitsForVsync = true
    private var rendering = false
    private var fallbackCount = 0
    private var contextRecoveryCount = 0
    private var renderedFrameCount = 0L
    private var lastRenderFailure: String? = null

    private val softwareFrameLimiter = LinuxFrameLimiter()
    private val fpsCounter =
        if (SkikoProperties.fpsEnabled) {
            FPSCounter(
                periodSeconds = SkikoProperties.fpsPeriodSeconds,
                showLongFrames = SkikoProperties.fpsLongFramesShow,
                getLongFrameMillis = {
                    SkikoProperties.fpsLongFramesMillis
                        ?: (1.5 * 1_000.0 / (linuxComponent?.displayRefreshRate ?: 60f))
                },
                logOnTick = true,
            )
        } else {
            null
        }

    private var activeRenderApi: GraphicsApi = properties.renderApi

    actual var renderApi: GraphicsApi
        get() = activeRenderApi
        set(value) {
            require(value in SupportedLinuxRenderApis) {
                "$value is not implemented by Kotlin/Native Linux"
            }
            if (activeRenderApi == value) return
            activeRenderApi = value
            if (linuxComponent != null) {
                replaceRenderer(value, countAsFallback = false)
                needRender(throttledToVsync = false)
            }
        }

    actual val contentScale: Float
        get() = linuxComponent?.contentScale ?: 1f

    actual var fullscreen: Boolean
        get() = linuxComponent?.fullscreen ?: false
        set(value) {
            val component =
                linuxComponent
                    ?: throw IllegalStateException("SkiaLayer must be attached before changing fullscreen")
            component.fullscreen = value
        }

    actual val component: Any?
        get() = linuxComponent?.windowHandle

    actual fun needRender(throttledToVsync: Boolean) {
        if (renderDelegate == null || linuxComponent == null) return
        pendingRenderWaitsForVsync =
            if (renderPending) pendingRenderWaitsForVsync && throttledToVsync else throttledToVsync
        renderPending = true
        linuxComponent?.requestRender()
    }

    @Deprecated(
        message = "Use needRender() instead",
        replaceWith = ReplaceWith("needRender()"),
    )
    actual fun needRedraw() = needRender()

    actual fun attachTo(container: Any) {
        check(linuxComponent == null) { "SkiaLayer is already attached" }
        val component =
            container as? LinuxSkiaLayerComponent
                ?: error("container must implement LinuxSkiaLayerComponent")
        linuxComponent = component
        try {
            replaceRenderer(renderApi, countAsFallback = false)
        } catch (failure: Throwable) {
            linuxComponent = null
            throw failure
        }
        if (renderDelegate != null) needRender(throttledToVsync = false)
    }

    actual fun detach() {
        closeRenderer(contextLost = renderer?.isContextLost() == true)
        linuxComponent = null
        renderPending = false
        rendering = false
    }

    internal actual fun draw(canvas: Canvas) {
        val component = linuxComponent ?: return
        val width = component.drawableWidth.coerceAtLeast(0)
        val height = component.drawableHeight.coerceAtLeast(0)
        if (width > 0 && height > 0) {
            renderDelegate?.onRender(canvas, width, height, currentNanoTime())
        }
    }

    actual var renderDelegate: SkikoRenderDelegate? = null
        set(value) {
            field = value
            if (value != null && linuxComponent != null) needRender(throttledToVsync = false)
        }

    actual val pixelGeometry: PixelGeometry
        get() = linuxComponent?.pixelGeometry ?: PixelGeometry.UNKNOWN

    /** True when [needRender] has requested a frame that has not been presented yet. */
    @InternalSkikoApi
    val hasPendingRender: Boolean
        get() = renderPending

    /** Current renderer and recovery counters for diagnostics and support tooling. */
    @InternalSkikoApi
    val diagnostics: LinuxSkiaLayerDiagnostics
        get() =
            LinuxSkiaLayerDiagnostics(
                renderApi = renderApi,
                rendererDescription = renderer?.description,
                deviceName = renderer?.deviceName,
                renderedFrameCount = renderedFrameCount,
                fallbackCount = fallbackCount,
                contextRecoveryCount = contextRecoveryCount,
                lastFailure = lastRenderFailure,
                isVsyncEnabled = properties.isVsyncEnabled,
                frameBuffering = properties.frameBuffering,
                effectiveFrameBufferCount = linuxComponent?.effectiveFrameBufferCount,
                transparencyRequested = linuxComponent?.transparency == true,
                hasTransparentWindowBuffer = linuxComponent?.transparencySupported == true,
                adapterPriority = properties.adapterPriority,
                gpuResourceCacheLimit = properties.gpuResourceCacheLimit,
                pixelGeometry = pixelGeometry,
                fpsAverage = fpsCounter?.average,
                fpsMinimum = fpsCounter?.min,
                fpsMaximum = fpsCounter?.max,
            )

    /**
     * Renders a pending frame. The embedding toolkit calls this from its native window thread.
     *
     * @return true when a frame was presented.
     */
    @InternalSkikoApi
    fun render(force: Boolean = false): Boolean {
        if (rendering || (!force && !renderPending)) return false
        val component = linuxComponent ?: return false
        val delegate = renderDelegate ?: return false
        val width = component.drawableWidth
        val height = component.drawableHeight
        if (width <= 0 || height <= 0) return false

        val waitForVsync = if (renderPending) pendingRenderWaitsForVsync else true
        rendering = true
        renderPending = false
        pendingRenderWaitsForVsync = true
        return try {
            renderWithRecovery(width, height, waitForVsync) { canvas ->
                try {
                    delegate.onRender(canvas, width, height, currentNanoTime())
                } catch (failure: Throwable) {
                    throw RenderDelegateFailure(failure)
                }
            }
            true
        } catch (failure: RenderDelegateFailure) {
            throw failure.cause ?: failure
        } finally {
            rendering = false
            if (renderPending) component.requestRender()
        }
    }

    @InternalSkikoApi
    fun snapshot(width: Int, height: Int): Bitmap =
        rendererOperationWithRecovery { it.snapshot(width, height) }

    @InternalSkikoApi
    fun <T> withOpenGlContext(block: () -> T): T =
        rendererOperationWithRecovery { it.withExternalOpenGl(block) }

    @InternalSkikoApi
    fun drawOpenGlTexture(textureId: Int, width: Int, height: Int, canvas: Canvas) {
        rendererOperationWithRecovery { it.drawTexture(textureId, width, height, canvas) }
    }

    @InternalSkikoApi
    val rendererDescription: String
        get() = checkNotNull(renderer) { "SkiaLayer is not attached" }.description

    private fun renderWithRecovery(
        width: Int,
        height: Int,
        waitForVsync: Boolean,
        block: (Canvas) -> Unit,
    ) {
        var activeRenderer = healthyRenderer()
        if (
            waitForVsync &&
                activeRenderer.renderApi != GraphicsApi.OPENGL &&
                properties.isVsyncEnabled &&
                properties.isVsyncFramelimitFallbackEnabled
        ) {
            softwareFrameLimiter.awaitNextFrame(
                refreshRate = linuxComponent?.displayRefreshRate ?: 60f,
            )
        }

        var recoveryAttempt = 0
        while (true) {
            try {
                renderFrame(activeRenderer, width, height, waitForVsync, block)
                return
            } catch (failure: RenderDelegateFailure) {
                throw failure
            } catch (failure: Throwable) {
                if (recoveryAttempt >= 2) throw failure
                recoverRenderer(
                    failedRenderer = activeRenderer,
                    failure = failure,
                    attemptSameApiRecreation = recoveryAttempt == 0,
                )
                recoveryAttempt += 1
                activeRenderer = checkNotNull(renderer)
            }
        }
    }

    private fun renderFrame(
        renderer: LinuxLayerRenderer,
        width: Int,
        height: Int,
        waitForVsync: Boolean,
        block: (Canvas) -> Unit,
    ) {
        val isFirstFrame = !firstFrameRendered
        if (isFirstFrame) deviceAnalytics?.beforeFirstFrameRender()
        deviceAnalytics?.beforeFrameRender()
        renderer.render(width, height, waitForVsync, block)
        fpsCounter?.tick()
        renderedFrameCount += 1
        firstFrameRendered = true
        if (isFirstFrame) deviceAnalytics?.afterFirstFrameRender()
        deviceAnalytics?.afterFrameRender()
    }

    private inline fun <T> rendererOperationWithRecovery(
        operation: (LinuxLayerRenderer) -> T,
    ): T {
        val activeRenderer = healthyRenderer()
        return try {
            operation(activeRenderer)
        } catch (failure: Throwable) {
            recoverRenderer(activeRenderer, failure, attemptSameApiRecreation = true)
            operation(checkNotNull(renderer))
        }
    }

    private fun healthyRenderer(): LinuxLayerRenderer {
        val activeRenderer = checkNotNull(renderer) { "SkiaLayer is not attached" }
        if (activeRenderer.isContextLost()) {
            recoverRenderer(
                activeRenderer,
                RenderException("OpenGL context was lost"),
                attemptSameApiRecreation = true,
                knownContextLost = true,
            )
        }
        return checkNotNull(renderer)
    }

    private fun recoverRenderer(
        failedRenderer: LinuxLayerRenderer,
        failure: Throwable,
        attemptSameApiRecreation: Boolean,
        knownContextLost: Boolean? = null,
    ) {
        lastRenderFailure = failure.message ?: failure::class.simpleName
        val failedApi = failedRenderer.renderApi
        val contextLost = knownContextLost ?: failedRenderer.isContextLost()
        closeRenderer(contextLost = contextLost, ignoreFailure = true)

        if (failedApi == GraphicsApi.OPENGL && attemptSameApiRecreation) {
            try {
                installRenderer(GraphicsApi.OPENGL)
                contextRecoveryCount += 1
                return
            } catch (recreationFailure: Throwable) {
                lastRenderFailure = recreationFailure.message ?: recreationFailure::class.simpleName
            }
        }

        val fallbackQueue = SkikoProperties.fallbackRenderApiQueue(failedApi).drop(1)
        installFirstWorkingRenderer(fallbackQueue, countAsFallback = true)
    }

    private fun replaceRenderer(api: GraphicsApi, countAsFallback: Boolean) {
        closeRenderer(contextLost = renderer?.isContextLost() == true)
        installFirstWorkingRenderer(
            SkikoProperties.fallbackRenderApiQueue(api),
            countAsFallback = countAsFallback,
        )
    }

    private fun installFirstWorkingRenderer(
        candidates: List<GraphicsApi>,
        countAsFallback: Boolean,
    ) {
        var lastFailure: Throwable? = null
        candidates.forEachIndexed { index, api ->
            try {
                installRenderer(api)
                if (countAsFallback || index > 0) fallbackCount += 1
                return
            } catch (failure: Throwable) {
                lastFailure = failure
                lastRenderFailure = failure.message ?: failure::class.simpleName
            }
        }
        throw RenderException("Cannot initialize any Kotlin/Native Linux renderer", lastFailure)
    }

    private fun installRenderer(api: GraphicsApi) {
        val component = checkNotNull(linuxComponent)
        val nextRenderer =
            linuxLayerRendererFactoryOverride?.invoke(api, component, properties)
                ?: when (api) {
                    GraphicsApi.OPENGL -> LinuxOpenGLRenderer(component, properties)
                    GraphicsApi.SOFTWARE_FAST,
                    GraphicsApi.SOFTWARE_COMPAT -> LinuxSoftwareRenderer(component, api)
                    else -> error("Kotlin/Native Linux does not support $api rendering")
                }
        val nextRendererAnalytics = analytics.renderer(LinuxNativeSkikoVersion, hostOs, api)
        nextRendererAnalytics.init()
        nextRendererAnalytics.deviceChosen()
        val nextDeviceAnalytics =
            analytics.device(LinuxNativeSkikoVersion, hostOs, api, nextRenderer.deviceName)
        nextDeviceAnalytics.init()
        nextDeviceAnalytics.contextInit()

        renderer = nextRenderer
        rendererAnalytics = nextRendererAnalytics
        deviceAnalytics = nextDeviceAnalytics
        firstFrameRendered = false
        activeRenderApi = api
    }

    private fun closeRenderer(contextLost: Boolean, ignoreFailure: Boolean = false) {
        val oldRenderer = renderer ?: return
        renderer = null
        rendererAnalytics = null
        deviceAnalytics = null
        try {
            oldRenderer.close(contextLost)
        } catch (failure: Throwable) {
            if (!ignoreFailure) throw failure
            lastRenderFailure = failure.message ?: failure::class.simpleName
        }
    }
}

@InternalSkikoApi
data class LinuxSkiaLayerDiagnostics(
    val renderApi: GraphicsApi,
    val rendererDescription: String?,
    val deviceName: String?,
    val renderedFrameCount: Long,
    val fallbackCount: Int,
    val contextRecoveryCount: Int,
    val lastFailure: String?,
    val isVsyncEnabled: Boolean,
    val frameBuffering: FrameBuffering,
    val effectiveFrameBufferCount: Int?,
    val transparencyRequested: Boolean,
    val hasTransparentWindowBuffer: Boolean,
    val adapterPriority: GpuPriority,
    val gpuResourceCacheLimit: Long,
    val pixelGeometry: PixelGeometry,
    val fpsAverage: Int?,
    val fpsMinimum: Int?,
    val fpsMaximum: Int?,
)

private class RenderDelegateFailure(cause: Throwable) : RuntimeException(cause)

private var linuxCurrentSystemTheme: SystemTheme = SystemTheme.UNKNOWN

private const val LinuxNativeSkikoVersion = "Kotlin/Native"

/** Updates Skiko's synchronous theme value from the platform host's existing theme observer. */
@InternalSkikoApi
fun updateLinuxSystemTheme(theme: SystemTheme) {
    linuxCurrentSystemTheme = theme
}

actual val currentSystemTheme: SystemTheme
    get() = linuxCurrentSystemTheme
