package org.jetbrains.skiko

import org.jetbrains.skiko.rendercontext.Direct3DRenderContext

/**
 * The `IDXGIAdapter1` this [RenderContext] renders on as a native pointer, or `null` if this context is not
 * Direct3D-backed.
 *
 * **Contract.** The pointer identifies the adapter skiko renders with and is valid for the whole lifetime of
 * the context; you may read it from any thread while the context is open. Do not release it — skiko owns it.
 * After the context is [closed][RenderContext.close] this accessor throws [IllegalStateException] (the
 * codebase's uniform disposal convention); it never returns a dangling pointer. Because skiko renders on its
 * own thread (off the EDT), submit interop GPU work that touches this device in coordination with skiko's
 * rendering — e.g. against [direct3DQueuePointer].
 *
 * @throws IllegalStateException if this context is Direct3D-backed but has already been closed.
 */
@ExperimentalSkikoApi
val RenderContext.direct3DAdapterPointer: Long?
    get() = (this as? Direct3DRenderContext)?.direct3DAdapterPtr

/**
 * The `ID3D12Device` this [RenderContext] renders on as a native pointer, or `null` if this context is not
 * Direct3D-backed.
 *
 * Create the resources skiko will sample on this same device, then import them with
 * [org.jetbrains.skia.BackendTexture.makeDirect3D] + [org.jetbrains.skia.Image.adoptTextureFrom] against
 * [RenderContext.directContext]. Same threading/lifetime contract as [direct3DAdapterPointer].
 *
 * @throws IllegalStateException if this context is Direct3D-backed but has already been closed.
 */
@ExperimentalSkikoApi
val RenderContext.direct3DDevicePointer: Long?
    get() = (this as? Direct3DRenderContext)?.direct3DDevicePtr

/**
 * The `ID3D12CommandQueue` skiko submits its frames on as a native pointer, or `null` if this context is not
 * Direct3D-backed.
 *
 * Synchronize your own GPU work against it so skiko samples only finished frames. Same threading/lifetime
 * contract as [direct3DAdapterPointer]: valid for the context's lifetime, readable while the context is open,
 * never release it, and throws [IllegalStateException] after [close][RenderContext.close].
 *
 * @throws IllegalStateException if this context is Direct3D-backed but has already been closed.
 */
@ExperimentalSkikoApi
val RenderContext.direct3DQueuePointer: Long?
    get() = (this as? Direct3DRenderContext)?.direct3DQueuePtr
