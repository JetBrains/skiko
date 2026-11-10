package org.jetbrains.skia

/**
 * ICU is always linked into the JVM binary, so the native break iterator is used.
 */
internal actual fun createBreakIteratorBackend(type: Int, locale: String?): BreakIteratorBackend? = null
