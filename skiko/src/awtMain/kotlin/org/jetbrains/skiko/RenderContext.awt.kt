package org.jetbrains.skiko

import org.jetbrains.skiko.swing.Direct3DSwingRenderContext
import org.jetbrains.skiko.swing.LinuxOpenGLSwingRenderContext
import org.jetbrains.skiko.swing.MetalSwingRenderContext

/**
 * Create a genuinely view-less, offscreen [RenderContext] of exactly [api], sized [width] x [height] device
 * pixels, or throw [RenderException] if that API cannot render offscreen standalone on this host.
 *
 * GPU backends render into an offscreen GPU texture built from an adapter/offscreen device, with no AWT peer,
 * native window, or JAWT drawing surface: [GraphicsApi.METAL] on macOS, [GraphicsApi.DIRECT3D] on Windows,
 * [GraphicsApi.OPENGL] (a GLX pbuffer) on Linux. The software APIs ([GraphicsApi.SOFTWARE_FAST],
 * [GraphicsApi.SOFTWARE_COMPAT]) rasterise on the CPU and are always available. A GPU api on the wrong OS — or
 * one whose device cannot initialise on this host — throws [RenderException]; the [apiPreference] overload
 * falls through to the next api in the chain.
 *
 * The returned context is fully standalone: `acquireSurface` → the caller draws → `present`, then read the
 * pixels back off the surface. There is no skiko frame loop and no view involved.
 *
 * AWT is the one multi-API host, so this factory names a graphics API (or a preference chain) instead of
 * taking a native drawable the way `RenderContext.createFromCanvas` and `RenderContext.createFromMetalLayer`
 * do on the single-drawable platforms.
 */
fun RenderContext.Companion.createOffscreen(width: Int, height: Int, api: GraphicsApi): RenderContext = when (api) {
    GraphicsApi.SOFTWARE_FAST, GraphicsApi.SOFTWARE_COMPAT ->
        AwtOffscreenSoftwareRenderContext(api, width, height)
    GraphicsApi.METAL ->
        if (hostOs == OS.MacOS) MetalSwingRenderContext()
        else throw RenderException("$api offscreen rendering is only available on macOS (host is $hostOs)")
    GraphicsApi.DIRECT3D ->
        if (hostOs == OS.Windows) Direct3DSwingRenderContext()
        else throw RenderException("$api offscreen rendering is only available on Windows (host is $hostOs)")
    GraphicsApi.OPENGL ->
        if (hostOs == OS.Linux) LinuxOpenGLSwingRenderContext()
        else throw RenderException("$api offscreen rendering is only available on Linux (host is $hostOs)")
    else -> throw RenderException("$api cannot render offscreen standalone on AWT")
}

/**
 * Create a genuinely view-less, offscreen [RenderContext] sized [width] x [height] device pixels, trying each
 * API in [apiPreference] in order and returning the first that constructs, or throwing [RenderException] if
 * none do.
 *
 * An api that cannot initialise offscreen on this host (see the single-API overload) is skipped and the chain
 * falls through to the next — so the default preference resolves to the host's GPU offscreen backend where
 * available, and to a software context otherwise.
 */
fun RenderContext.Companion.createOffscreen(
    width: Int,
    height: Int,
    apiPreference: List<GraphicsApi> = SkikoProperties.fallbackRenderApiQueue(SkikoProperties.renderApi),
): RenderContext {
    for (api in apiPreference) {
        try {
            return createOffscreen(width, height, api)
        } catch (e: RenderException) {
            Logger.warn(e) { "Failed to create $api offscreen render context, trying next API" }
        }
    }
    throw RenderException("Cannot create an offscreen render context for any of $apiPreference")
}
