package org.jetbrains.skiko

import org.jetbrains.skia.impl.NativePointer

/**
 * The WebGL context handle skiko renders through (the emscripten GL context id), or `null` if this context is
 * not WebGL-backed.
 *
 * Unlike Metal/Direct3D there is no separate device or queue in the GL family: the rendering context is
 * **thread-current**. To sample a GL texture you produced, make this context current, wrap your texture id
 * with [org.jetbrains.skia.BackendTexture.makeGL], and import it with
 * [org.jetbrains.skia.Image.adoptTextureFrom] against [RenderContext.directContext] — all on skiko's render
 * thread.
 *
 * **Contract.** The handle is valid for the whole lifetime of the context; you may read it while the context
 * is open. After the context is [closed][RenderContext.close] this accessor throws [IllegalStateException]
 * (the codebase's uniform disposal convention). Because a WebGL context is thread-current, do the actual
 * texture wrapping/import on skiko's render thread (the thread that owns this context).
 *
 * @throws IllegalStateException if this context is WebGL-backed but has already been closed.
 */
@ExperimentalSkikoApi
val RenderContext.openGLContextHandle: NativePointer?
    get() = (this as? WebGLRenderContext)?.glContextHandle
