package org.jetbrains.skiko.wasm

import org.jetbrains.skia.impl.NativePointer
import org.w3c.dom.HTMLCanvasElement

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

internal actual fun createWebGLContext(canvas: HTMLCanvasElement, attr: ContextAttributes?): NativePointer {
    check(attr === null) { "TODO!" }
    return GL.createContext(canvas, createDefaultContextAttributes())
}
