package org.jetbrains.skia

import java.util.*

internal actual fun <R> commonSynchronized(lock: Any, block: () -> R) {
    synchronized(lock, block)
}

actual typealias Pattern = java.util.regex.Pattern

actual typealias Matcher = java.util.regex.Matcher

actual fun defaultLanguageTag(): String = Locale.getDefault().toLanguageTag()

internal actual fun compilePattern(regex: String): Pattern = Pattern.compile(regex)
