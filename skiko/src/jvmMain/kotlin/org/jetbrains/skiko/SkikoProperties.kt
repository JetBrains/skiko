package org.jetbrains.skiko

import java.util.*

// TODO maybe we can get rid of global properties, and pass SkiaLayerProperties to Window -> ComposeWindow -> SkiaLayer
@Suppress("SameParameterValue")
/**
 * Global Skiko properties, which are read from system JDK properties or environment variables.
 */
object SkikoProperties {

    private val properties = run {
        val resourcePropertiesEnabled = System.getProperty("skiko.resource.properties.enabled")?.toBoolean() ?: false
        val resources = if (resourcePropertiesEnabled) {
            SkikoProperties::class.java.classLoader.getResourceAsStream("skiko.properties")
        } else {
            null
        }
        val systemProps = System.getProperties()
        if (resources == null) systemProps else Properties(systemProps).apply { load(resources) }
    }

    private fun getProperty(key: String): String? = properties.getProperty(key)

    private inline fun <T> lazyProperty(crossinline loader: () -> T): Lazy<T> {
        // Use NONE to avoid unnecessary synchronization overhead.
        // It is an error to change the properties after they are first accessed anyway, so synchronization
        // is not crucial.
        // In fact, because we are currently grabbing all the properties at object initialization, they can't be
        // changed.
        return lazy(LazyThreadSafetyMode.NONE) {
            loader()
        }
    }

    private inline fun <T> lazyProperty(name: String, crossinline parse: (String?) -> T): Lazy<T> {
        return lazyProperty {
            parse(getProperty(name))
        }
    }

    private inline fun lazyStringProperty(name: String, crossinline defaultValue: () -> String): Lazy<String> {
        return lazyProperty(name) {
            it ?: defaultValue()
        }
    }

    private fun lazyBooleanProperty(name: String, defaultValue: Boolean): Lazy<Boolean> {
        return lazyProperty(name) {
            it?.toBoolean() ?: defaultValue
        }
    }

    private fun lazyIntProperty(name: String, defaultValue: Int): Lazy<Int> {
        return lazyProperty(name) {
            it?.toInt() ?: defaultValue
        }
    }

    private fun lazyDoubleProperty(name: String, defaultValue: Double): Lazy<Double> {
        return lazyProperty(name) {
            it?.toDouble() ?: defaultValue
        }
    }

    /**
     * Path where the Skiko binaries (dll/so/dylib, depending on OS) are placed.
     *
     * If defined, SKiko doesn't extract binaries from `jar` files to external folder.
     *
     * If null (default), it extracts them to `libraryCachePath`
     */
    var libraryPath: String?
        get() = getProperty("skiko.library.path")
        internal set(value) {
            if (value != null) {
                System.setProperty("skiko.library.path", value)
            } else {
                System.clearProperty("skiko.library.path")
            }
        }

    /**
     * The path where to store data files.
     *
     * It is used for extracting the Skiko binaries (if `libraryPath` isn't null) and logging.
     */
    val dataPath: String
        by lazyStringProperty("skiko.data.path", defaultValue = { "${getProperty("user.home")}/.skiko/" })

    /**
     * Purge data inside the [dataPath] if it is not used/older than this 'days'
     */
    val dataCleanupDays: Int
        by lazyIntProperty("skiko.data.cleanup.days", defaultValue = 31)

    val vsyncEnabled: Boolean
        by lazyBooleanProperty("skiko.vsync.enabled", defaultValue = true)

    /**
     * Whether `SkiaSwingLayer` paces invalidation-driven repaints (`needRender`) to the display
     * refresh.
     *
     * Ticks come from Skiko's own display clocks, so this works on any JVM. Read when the layer
     * is initialized (added to a component hierarchy).
     */
    val swingFramePacingEnabled: Boolean
        by lazyBooleanProperty("skiko.swing.frame.pacing", defaultValue = false)

    /**
     * Forces Skiko's own pacing clocks to the phase-aligned timer even where a native display
     * clock (CADisplayLink, DXGI vblank, DRM vblank) is available. Debug and measurement escape
     * hatch.
     */
    val swingFramePacingForceTimer: Boolean
        by lazyBooleanProperty("skiko.swing.frame.pacing.forceTimer", defaultValue = false)

    val frameBuffering: FrameBuffering by lazyProperty("skiko.buffering") {
        when (it) {
            "DOUBLE" -> FrameBuffering.DOUBLE
            "TRIPLE" -> FrameBuffering.TRIPLE
            else -> FrameBuffering.DEFAULT
        }
    }

    val macOSWaitForPreviousFrameVsyncOnRedrawImmediately: Boolean
        by lazyBooleanProperty("skiko.rendering.macos.waitForPreviousFrameVsyncOnRedrawImmediately", defaultValue = true)

    val windowsWaitForVsyncOnRedrawImmediately: Boolean
        by lazyBooleanProperty("skiko.rendering.windows.waitForFrameVsyncOnRedrawImmediately", defaultValue = false)

    val linuxWaitForVsyncOnRedrawImmediately: Boolean
        by lazyBooleanProperty("skiko.rendering.linux.waitForFrameVsyncOnRedrawImmediately", defaultValue = false)

    /**
     * Metal on macOS: during an interactive live resize (dragging a window edge), render and present
     * synchronously on the AppKit main thread, inside the same CATransaction that commits the window's
     * new size, so content and window backing stay in sync (no white borders).
     *
     * When disabled, resize falls back to the previous behavior: geometry is updated from the EDT, and frames are
     * presented asynchronously off the resize transaction.
     */
    val metalSynchronousLiveResize: Boolean
        by lazyBooleanProperty("skiko.rendering.macos.metalSynchronousLiveResize", defaultValue = false)

    /**
     * Direct3D on Windows: during an interactive live resize (dragging a window edge), render and present
     * the content synchronously on the toolkit thread inside the same resize step that applies the window's
     * new size.
     *
     * When disabled, resize falls back to the previous behavior: geometry is updated from the EDT
     * and frames are presented asynchronously off the resize.
     */
    val direct3DSynchronousLiveResize: Boolean
        by lazyBooleanProperty("skiko.rendering.windows.direct3DSynchronousLiveResize", defaultValue = false)

    /**
     * Is experimental ANGLE renderer API enabled (https://skia.org/docs/user/special/angle/).
     *
     * If enabled, Windows uses it as a primary render API and fallbacks to the default APIs.
     *
     * Other OSes are not supported yet.
     *
     * If it is enabled, make sure that either:
     * - `org.jetbrains.skiko:skiko-awt-runtime-angle-$target:$version` added as a dependency
     * - The `skiko.library.path` property is defined and the directory has libEGL, libGLESv2 from
     *   https://github.com/JetBrains/angle-pack/releases
     */
    val renderingAngleEnabled: Boolean
        by lazyBooleanProperty("skiko.rendering.angle.enabled", defaultValue = false)

    /**
     * If vsync is enabled, but platform can't support it (Software renderer, Linux with uninstalled drivers),
     * we enable frame limit by the display refresh rate.
     */
    val vsyncFramelimitFallbackEnabled: Boolean
        by lazyBooleanProperty("skiko.vsync.framelimit.fallback.enabled", defaultValue = true)

    val fpsEnabled: Boolean
        by lazyBooleanProperty("skiko.fps.enabled", defaultValue = false)

    val fpsPeriodSeconds: Double
        by lazyDoubleProperty("skiko.fps.periodSeconds", defaultValue = 2.0)

    /**
     * Show frames that are longer than [fpsLongFramesMillis].
     * If [fpsLongFramesMillis] isn't defined will show frames longer than 1.5 * (1000 / displayRefreshRate)
     */
    val fpsLongFramesShow: Boolean
        by lazyBooleanProperty("skiko.fps.longFrames.show", defaultValue = false)

    val fpsLongFramesMillis: Double?
        by lazyProperty("skiko.fps.longFrames.millis", parse = { it?.toDouble() })

    val renderApi: GraphicsApi by lazyProperty {
        val environment = System.getenv("SKIKO_RENDER_API")
        val property = getProperty("skiko.renderApi")
        parseRenderApi(environment ?: property)
    }

    val gpuPriority: GpuPriority by lazyProperty {
        val value = getProperty("skiko.gpu.priority") ?:
            getProperty("skiko.metal.gpu.priority") ?: // for backward compatability
            getProperty("skiko.directx.gpu.priority")  // for backward compatability

        value?.let(GpuPriority::parseOrNull) ?: GpuPriority.Auto
    }

    val macOsOpenGLEnabled: Boolean
        by lazyBooleanProperty("skiko.macos.opengl.enabled", defaultValue = false)

    val gpuResourceCacheLimit: Long by lazyProperty(
        name = "skiko.gpu.resourceCacheLimit",
        parse = ::parseSize
    )

    private fun parseSize(size: String?): Long {
        if (size == null) return -1L
        val size = size.uppercase()
        val multiplier = when {
            size.endsWith("K") -> 1024L
            size.endsWith("M") -> 1024L * 1024L
            size.endsWith("G") -> 1024L * 1024L * 1024L
            else -> 1L
        }
        val numericPart = if (multiplier != 1L) size.substring(0, size.length - 1) else size
        return numericPart.toLongOrNull()?.times(multiplier)
            ?: throw IllegalArgumentException("Invalid size format: $size")
    }

    internal fun parseRenderApi(text: String?): GraphicsApi {
        when (text) {
            "SOFTWARE_COMPAT" -> return GraphicsApi.SOFTWARE_COMPAT
            "SOFTWARE_FAST", "DIRECT_SOFTWARE" -> return GraphicsApi.SOFTWARE_FAST
            "SOFTWARE" -> return if (hostOs == OS.MacOS) GraphicsApi.SOFTWARE_COMPAT else GraphicsApi.SOFTWARE_FAST
            "OPENGL" -> {
                // Skia isn't properly tested on OpenGL and Windows ARM (https://groups.google.com/g/skia-discuss/c/McoclAhLpvg?pli=1)
                return if (hostOs != OS.Windows || hostArch != Arch.Arm64) GraphicsApi.OPENGL
                else throw Exception("$hostOs-$hostArch does not support OpenGL rendering API.")
            }

            "ANGLE" -> {
                return if (hostOs == OS.Windows) GraphicsApi.ANGLE
                else throw Exception("$hostOs does not support ANGLE rendering API.")
            }

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
        return when (hostOs) {
            OS.MacOS -> GraphicsApi.METAL
            OS.Linux -> GraphicsApi.OPENGL
            OS.Windows -> if (renderingAngleEnabled) GraphicsApi.ANGLE else GraphicsApi.DIRECT3D
            OS.Android -> GraphicsApi.OPENGL
            else -> GraphicsApi.UNKNOWN
        }
    }

    internal fun fallbackRenderApiQueue(initialApi: GraphicsApi?): List<GraphicsApi> {
        var fallbackApis = when (hostOs) {
            OS.Linux -> listOf(GraphicsApi.OPENGL, GraphicsApi.SOFTWARE_FAST, GraphicsApi.SOFTWARE_COMPAT)
            OS.MacOS -> listOf(GraphicsApi.METAL, GraphicsApi.SOFTWARE_COMPAT)
            OS.Windows -> when (hostArch) {
                // Skia isn't properly tested on OpenGL and Windows ARM (https://groups.google.com/g/skia-discuss/c/McoclAhLpvg?pli=1)
                Arch.Arm64 -> listOf(
                    GraphicsApi.ANGLE,
                    GraphicsApi.DIRECT3D,
                    GraphicsApi.SOFTWARE_FAST,
                    GraphicsApi.SOFTWARE_COMPAT
                )

                else -> listOf(
                    GraphicsApi.ANGLE,
                    GraphicsApi.DIRECT3D,
                    GraphicsApi.OPENGL,
                    GraphicsApi.SOFTWARE_FAST,
                    GraphicsApi.SOFTWARE_COMPAT
                )
            }

            OS.Android -> return listOf(GraphicsApi.OPENGL)
            else -> return listOf(GraphicsApi.UNKNOWN)
        }

        return if (initialApi != null) {
            val indexOfInitialApi = fallbackApis.indexOf(initialApi)
            require(indexOfInitialApi >= 0) {
                "$hostOs does not support $initialApi rendering API."
            }
            fallbackApis = fallbackApis.drop(indexOfInitialApi + 1)

            listOf(initialApi) + fallbackApis
        } else {
            fallbackApis
        }
    }
}
