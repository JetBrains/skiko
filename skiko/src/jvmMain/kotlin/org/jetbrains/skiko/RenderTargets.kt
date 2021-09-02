package org.jetbrains.skiko

import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.DirectContext

internal fun makeGLContext() = DirectContext(
    makeGLContextNative()
)

internal fun makeGLRenderTarget(width: Int, height: Int, sampleCnt: Int, stencilBits: Int, fbId: Int, fbFormat: Int) = BackendRenderTarget(
    makeGLRenderTargetNative(width, height, sampleCnt, stencilBits, fbId, fbFormat)
)

internal fun makeMetalRenderTarget(width: Int, height: Int, sampleCnt: Int) = BackendRenderTarget(
    makeMetalRenderTargetNative(width, height, sampleCnt).also { if (it == 0L) TODO("not yet supported") }
)

internal fun makeMetalContext() = DirectContext(
    makeMetalContextNative().also { if (it == 0L) TODO("not yet supported") }
)

external private fun makeGLRenderTargetNative(width: Int, height: Int, sampleCnt: Int, stencilBits: Int, fbId: Int, fbFormat: Int): Long
external private fun makeGLContextNative(): Long

external private fun makeMetalRenderTargetNative(width: Int, height: Int, sampleCnt: Int): Long
external private fun makeMetalContextNative(): Long
