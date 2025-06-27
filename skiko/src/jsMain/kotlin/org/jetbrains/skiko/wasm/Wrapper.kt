package org.jetbrains.skiko.wasm

import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.w3c.HTMLCanvasElement
import kotlin.js.*

/**
 * Invokes a callback [onReady] as soon as onRuntimeInitialized happens.
 * Calling onWasmReady after onRuntimeInitialized invokes [onReady] as well.
 * It's safe to call wasm functions within [onReady] callback, or after it was invoked.
 */
 @JsModule("./skiko.mjs")
 @JsNonModule
external fun onWasmReady(onReady: () -> Unit)

internal external val wasmSetup: Promise<Boolean>

internal fun ContextAttributes.asJsObject(): dynamic {
    val jsObject = js("{}")
    alpha?.let { jsObject.alpha = alpha }
    depth?.let { jsObject.depth = depth }
    stencil?.let { jsObject.stencil = stencil }
    antialias?.let { jsObject.antialias = antialias }
    premultipliedAlpha?.let { jsObject.premultipliedAlpha = premultipliedAlpha }
    preserveDrawingBuffer?.let { jsObject.preserveDrawingBuffer = preserveDrawingBuffer }
    preferLowPowerToHighPerformance?.let { jsObject.preferLowPowerToHighPerformance = preferLowPowerToHighPerformance }
    failIfMajorPerformanceCaveat?.let { jsObject.failIfMajorPerformanceCaveat = failIfMajorPerformanceCaveat }
    enableExtensionsByDefault?.let { jsObject.enableExtensionsByDefault = enableExtensionsByDefault }
    explicitSwapControl?.let { jsObject.explicitSwapControl = explicitSwapControl }
    renderViaOffscreenBackBuffer?.let { jsObject.renderViaOffscreenBackBuffer = renderViaOffscreenBackBuffer }
    majorVersion?.let { jsObject.majorVersion = majorVersion }
    return jsObject
}

private external interface GLInterface {
    fun createContext(context: HTMLCanvasElement, contextAttributes: ContextAttributes): NativePointer;
    fun makeContextCurrent(contextPointer: NativePointer): Boolean;
}

internal external object GL : GLInterface {
    override fun createContext(context: HTMLCanvasElement, contextAttributes: ContextAttributes): Int = definedExternally
    override fun makeContextCurrent(contextPointer: NativePointer): Boolean = definedExternally
}

internal actual fun createWebGLContext(canvas: HTMLCanvasElement, attr: ContextAttributes?): NativePointer {
    val contextAttributes = object : ContextAttributes {
        override val alpha = attr?.alpha ?: 1
        override val depth = attr?.depth ?: 1
        override val stencil = attr?.stencil ?: 8
        override val antialias = attr?.antialias ?: 0
        override val premultipliedAlpha = attr?.premultipliedAlpha ?: 1
        override val preserveDrawingBuffer = attr?.preserveDrawingBuffer ?: 0
        override val preferLowPowerToHighPerformance = attr?.preferLowPowerToHighPerformance ?: 0
        override val failIfMajorPerformanceCaveat = attr?.failIfMajorPerformanceCaveat ?: 0
        override val enableExtensionsByDefault = attr?.enableExtensionsByDefault ?: 1
        override val explicitSwapControl = attr?.explicitSwapControl ?: 0
        override val renderViaOffscreenBackBuffer = attr?.renderViaOffscreenBackBuffer ?: 0
        override val majorVersion = attr?.majorVersion ?: 2
    }

    return GL.createContext(canvas, contextAttributes.asJsObject())
}
