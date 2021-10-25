package org.jetbrains.skia

import java.util.*

actual fun <R> commonSynchronized(lock: Any, block: () -> R) {
    synchronized(lock, block)
}

actual fun String.intCodePoints(): IntArray = this.codePoints().toArray()

actual typealias Pattern = java.util.regex.Pattern

actual typealias Matcher = java.util.regex.Matcher

actual fun defaultLanguageTag(): String = Locale.getDefault().toLanguageTag()

actual fun compilePattern(regex: String): Pattern = Pattern.compile(regex)

actual typealias OutputStream = java.io.OutputStream
