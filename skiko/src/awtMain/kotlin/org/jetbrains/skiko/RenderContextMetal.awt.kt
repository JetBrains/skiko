package org.jetbrains.skiko

import org.jetbrains.skiko.rendercontext.MetalRenderContext

/**
 * The `id<MTLDevice>` this [RenderContext] renders on as a native pointer (a `__bridge`-castable address),
 * or `null` if this context is not Metal-backed.
 *
 * Pass it across your own JNI/`cinterop` boundary to allocate the textures skiko will sample on the same
 * device, then import them with [org.jetbrains.skia.BackendTexture.makeMetal] +
 * [org.jetbrains.skia.Image.adoptTextureFrom] against [RenderContext.directContext].
 *
 * **Contract.** The pointer identifies the device skiko renders with and is valid for the whole lifetime of
 * the context; you may read it from any thread while the context is open. Do not release it — skiko owns it.
 * After the context is [closed][RenderContext.close] this accessor throws [IllegalStateException] (the
 * codebase's uniform disposal convention); it never returns a dangling pointer. Because skiko renders on its
 * own thread (off the EDT for the AWT backend), submit interop GPU work that touches this device in
 * coordination with skiko's rendering — e.g. against [metalCommandQueuePointer].
 *
 * @throws IllegalStateException if this context is Metal-backed but has already been closed.
 */
@ExperimentalSkikoApi
val RenderContext.metalDevicePointer: Long?
    get() = (this as? MetalRenderContext)?.metalDeviceObjcPtr

/**
 * The `id<MTLCommandQueue>` skiko submits its frames on as a native pointer, or `null` if this context is not
 * Metal-backed.
 *
 * Submit your interop work against it (or synchronize your own queue against it) so skiko samples only
 * finished frames. Same threading/lifetime contract as [metalDevicePointer]: valid for the context's
 * lifetime, readable while the context is open, never release it, and throws [IllegalStateException] after
 * [close][RenderContext.close].
 *
 * @throws IllegalStateException if this context is Metal-backed but has already been closed.
 */
@ExperimentalSkikoApi
val RenderContext.metalCommandQueuePointer: Long?
    get() = (this as? MetalRenderContext)?.metalCommandQueueObjcPtr
