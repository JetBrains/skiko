package org.jetbrains.skiko.w3c

/**
 * The purpose of this file is to facilitate shared W3C type declarations
 * for use in both kotlin/js and kotlin/wasm code.
 * Given that the Kotlin standard library does not provide common (shared between kotlin/js and kotlin/wasm) W3C API,
 * this file serves to fill that void with custom partial declarations.
 *
 * These declarations do not cover all attributes and methods of the respective W3C types,
 * but only those needed by our implementations.
 */

@Suppress("ClassName")
internal external object window {
    val devicePixelRatio: Double
    val navigator: Navigator
    val performance: Performance

    fun requestAnimationFrame(block: (Double) -> Unit)
    fun open(url: String?, target: String?)
}

internal external interface Performance {
    fun now(): Double
}

internal external interface Navigator {
    val language: String
    val clipboard: Clipboard
    val userAgent: String
}

internal external interface Clipboard {
    fun writeText(text: String)
}

internal abstract external class HTMLCanvasElement {
    internal var width: Int
    internal var height: Int
    internal val style: CSSStyleDeclaration
}

internal abstract external class CSSStyleDeclaration {
    var width: String
    var height: String
}