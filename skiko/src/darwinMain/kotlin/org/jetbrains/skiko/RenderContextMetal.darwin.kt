package org.jetbrains.skiko

import platform.Metal.MTLCommandQueueProtocol
import platform.Metal.MTLDeviceProtocol

/**
 * The `MTLDevice` this [RenderContext] renders on, or `null` if this context is not Metal-backed.
 *
 * Allocate the textures you want skiko to sample on this same device, then import them with
 * [org.jetbrains.skia.BackendTexture.makeMetal] + [org.jetbrains.skia.Image.adoptTextureFrom] against
 * [RenderContext.directContext].
 *
 * **Contract.** The device is valid for the whole lifetime of the context; you may read it from any thread
 * while the context is open. Do not retain it past [close][RenderContext.close] — after the context is closed
 * this accessor throws [IllegalStateException] (the codebase's uniform disposal convention). Submit interop
 * GPU work that touches this device in coordination with skiko's rendering — e.g. against [metalCommandQueue].
 *
 * @throws IllegalStateException if this context is Metal-backed but has already been closed.
 */
@ExperimentalSkikoApi
val RenderContext.metalDevice: MTLDeviceProtocol?
    get() = (this as? MetalRenderContext)?.device

/**
 * The `MTLCommandQueue` skiko submits its frames on, or `null` if this context is not Metal-backed.
 *
 * Encode your interop work against it (or synchronize your own queue against it) so skiko samples only
 * finished frames. Same threading/lifetime contract as [metalDevice].
 *
 * @throws IllegalStateException if this context is Metal-backed but has already been closed.
 */
@ExperimentalSkikoApi
val RenderContext.metalCommandQueue: MTLCommandQueueProtocol?
    get() = (this as? MetalRenderContext)?.queue
