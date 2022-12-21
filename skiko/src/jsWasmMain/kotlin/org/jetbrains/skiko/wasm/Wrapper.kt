package org.jetbrains.skiko.wasm

import org.jetbrains.skia.impl.NativePointer
import org.w3c.dom.HTMLCanvasElement
import kotlin.js.*

/**
 * Invokes a callback [onReady] as soon as onRuntimeInitialized happens.
 * Calling onWasmReady after onRuntimeInitialized invokes [onReady] as well.
 * It's safe to call wasm functions within [onReady] callback, or after it was invoked.
 */
external fun onWasmReady(onReady: () -> Unit)

internal external val wasmSetup: Promise<Boolean>

private external interface GLInterface {
    fun createContext(context: HTMLCanvasElement, contextAttributes: ContextAttributes): NativePointer;
    fun makeContextCurrent(contextPointer: NativePointer): Boolean;
}

internal external object GL : GLInterface {
    override fun createContext(context: HTMLCanvasElement, contextAttributes: ContextAttributes): Int = definedExternally
    override fun makeContextCurrent(contextPointer: NativePointer): Boolean = definedExternally
}

internal external interface ContextAttributes {
    val alpha: Int?
    val depth: Int?
    val stencil: Int?
    val antialias: Int?
    val premultipliedAlpha: Int?
    val preserveDrawingBuffer: Int?
    val preferLowPowerToHighPerformance: Int?
    val failIfMajorPerformanceCaveat: Int?
    val enableExtensionsByDefault: Int?
    val explicitSwapControl: Int?
    val renderViaOffscreenBackBuffer: Int?
    val majorVersion: Int?
}

internal expect fun createWebGLContext(canvas: HTMLCanvasElement, attr: ContextAttributes? = null): NativePointer