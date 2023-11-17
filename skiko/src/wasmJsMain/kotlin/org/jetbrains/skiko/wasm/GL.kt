@file:JsModule("./skiko.mjs")
package org.jetbrains.skiko.wasm

import org.jetbrains.skia.impl.NativePointer
import org.w3c.dom.HTMLCanvasElement
import kotlin.js.*

internal external object GL : GLInterface {
    override fun createContext(context: HTMLCanvasElement, contextAttributes: ContextAttributes): Int = definedExternally
    override fun makeContextCurrent(contextPointer: NativePointer): Boolean = definedExternally
}

private external interface GLInterface {
    fun createContext(context: HTMLCanvasElement, contextAttributes: ContextAttributes): NativePointer;
    fun makeContextCurrent(contextPointer: NativePointer): Boolean;
}