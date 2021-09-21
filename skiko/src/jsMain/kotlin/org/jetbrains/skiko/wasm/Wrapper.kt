package org.jetbrains.skiko.wasm

import kotlinx.dom.createElement
import kotlinx.browser.document
import org.jetbrains.skia.impl.NativePointer
import org.khronos.webgl.WebGLRenderingContext
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.RenderingContext
import kotlin.js.Promise

external val wasmSetup: Promise<Boolean>
external fun onWasmReady(onReady: () -> Unit)

external interface GLInterface {
    fun createContext(context: HTMLCanvasElement, contextAttributes: dynamic): NativePointer;
    fun makeContextCurrent(contextPointer: NativePointer): Boolean;
}

external object GL: GLInterface {
    override fun createContext(context: HTMLCanvasElement, contextAttributes: dynamic): Int = definedExternally
    override fun makeContextCurrent(contextPointer: NativePointer): Boolean = definedExternally
}

external interface ContextAttributes {
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

fun ContextAttributes.asJsObject(): dynamic {
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


fun GetWebGLContext(canvas: HTMLCanvasElement, attr: ContextAttributes? = null): Boolean {
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
        override val majorVersion = attr?.majorVersion ?: 1
    }

    val contextPointer = GL.createContext(canvas, contextAttributes.asJsObject())
    return GL.makeContextCurrent(contextPointer)
}