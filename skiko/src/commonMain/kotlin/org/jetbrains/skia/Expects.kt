package org.jetbrains.skia

expect fun <R> commonSynchronized(lock: Any, block: () -> R)

expect fun String.intCodePoints(): IntArray

expect fun defaultLanguageTag(): String

expect abstract class OutputStream

expect class Pattern {
    fun split(input: CharSequence): Array<String>
    fun matcher(input: CharSequence): Matcher
}

expect class Matcher {
    // Named groups are not supported in k/n. That's why we can use only numeric groups
    fun group(ix: Int): String?
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

@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
expect annotation class ExternalSymbolName(val name: String)
