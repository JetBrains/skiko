package org.jetbrains.skia

actual abstract class ByteBuffer

actual fun <R> commonSynchronized(lock: Any, block: () -> R) {
    block()
}

actual fun String.intCodePoints(): IntArray = TODO()

actual class Pattern {
    actual fun split(input: CharSequence): Array<String?>? = TODO()
    actual fun matcher(input: CharSequence): Matcher = TODO()
}

actual class Matcher {
    actual fun group(name: String): String? = TODO()
    actual fun matches(): Boolean = TODO()
}

actual fun defaultLanguageTag(): String = TODO()

actual fun compilePattern(regex: String): Pattern = TODO()

actual typealias ExternalSymbolName = kotlin.js.JsName
