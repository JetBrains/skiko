package org.jetbrains.skiko.wasm

import org.jetbrains.skia.impl.NativePointer
import org.w3c.dom.HTMLCanvasElement
import kotlin.js.*

@JsFun(
"""() => {
    return {
        alpha: 1,
        depth: 1,
        stencil: 8,
        antialias: 0,
        premultipliedAlpha: 1,
        preserveDrawingBuffer: 0,
        preferLowPowerToHighPerformance: 0,
        failIfMajorPerformanceCaveat: 0,
        enableExtensionsByDefault: 1,
        explicitSwapControl: 0,
        renderViaOffscreenBackBuffer: 0,
        majorVersion: 2,
    }
}
""")
private external fun createDefaultContextAttributes(): ContextAttributes

private external interface GLInterface {
    fun createContext(context: HTMLCanvasElement, contextAttributes: ContextAttributes): NativePointer;
    fun makeContextCurrent(contextPointer: NativePointer): Boolean;
}

@JsModule("GL")
internal external object GL : GLInterface {
    override fun createContext(context: HTMLCanvasElement, contextAttributes: ContextAttributes): Int = definedExternally
    override fun makeContextCurrent(contextPointer: NativePointer): Boolean = definedExternally
}

internal actual fun createWebGLContext(canvas: HTMLCanvasElement, attr: ContextAttributes?): NativePointer {
    check(attr === null) { "TODO!" }
    return GL.createContext(canvas, createDefaultContextAttributes())
}
