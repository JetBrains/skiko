package org.jetbrains.skiko

import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.wasm.EmscriptenWebGLContextAttributes
import org.khronos.webgl.WebGLRenderingContextBase
import org.w3c.dom.HTMLCanvasElement
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.js

/**
 * Minimal bridge to the browser's WebGL entry points used by [WebGLRenderContext].
 *
 * The WebGL context binding lives next to, but independently of, the internal render context. The
 * `actual val GL` is provided by the js and wasmJs source sets.
 */
internal external interface GLInterface {
    fun createContext(context: HTMLCanvasElement, contextAttributes: EmscriptenWebGLContextAttributes): NativePointer
    fun makeContextCurrent(contextPointer: NativePointer): Boolean
}

internal expect val GL: GLInterface

@OptIn(ExperimentalWasmJsInterop::class)
internal fun currentGLContext(gl: GLInterface): WebGLRenderingContextBase? =
    js("gl.currentContext ? gl.currentContext.GLctx : null")
