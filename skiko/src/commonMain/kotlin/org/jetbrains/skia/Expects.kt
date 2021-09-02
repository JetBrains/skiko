package org.jetbrains.skia

expect abstract class ByteBuffer

expect fun <R> commonSynchronized(lock: Any, block: () -> R)

expect fun String.intCodePoints(): IntArray

expect fun defaultLanguageTag(): String

expect class Pattern {
    fun split(input: CharSequence): Array<String?>?
    fun matcher(input: CharSequence): Matcher
}

expect class Matcher {
    fun group(name: String): String?
    fun matches(): Boolean
}

expect fun compilePattern(regex: String): Pattern

interface BooleanSupplier {
    /**
     * Gets a result.
     *
     * @return a result
     */
    val asBoolean: Boolean
}