package org.jetbrains.skiko.wasm

import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.w3c.HTMLCanvasElement

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