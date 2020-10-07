package org.jetbrains.skiko

import org.jetbrains.skija.BackendRenderTarget
import org.jetbrains.skija.Context

internal fun makeGLContext() = Context(
    makeGLContextNative()
)

internal fun makeGLRenderTarget(width: Int, height: Int, sampleCnt: Int, stencilBits: Int, fbId: Int, fbFormat: Int) = BackendRenderTarget(
    makeGLRenderTargetNative(width, height, sampleCnt, stencilBits, fbId, fbFormat)
)

@Suppress("UNUSED_PARAMETER")
internal fun makeMetalRenderTarget(width: Int, height: Int, sampleCnt: Int) = BackendRenderTarget(
    makeMetalRenderTargetNative(width, height, sampleCnt).also { if (it == 0L) TODO("not yet supported") }
)

internal fun makeMetalContext() = Context(
    makeMetalContextNative().also { if (it == 0L) TODO("not yet supported") }
)

external private fun makeGLRenderTargetNative(width: Int, height: Int, sampleCnt: Int, stencilBits: Int, fbId: Int, fbFormat: Int): Long
external private fun makeGLContextNative(): Long

external private fun makeMetalRenderTargetNative(width: Int, height: Int, sampleCnt: Int): Long
external private fun makeMetalContextNative(): Long
