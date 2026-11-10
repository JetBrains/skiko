package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer

/**
 * A pure-Kotlin implementation of the [BreakIterator] protocol.
 *
 * It is used when the platform binary has no ICU break iterator (an ICU-less
 * `skiko.wasm` built with `-Pskiko.wasm.useIcu=false`), in which case the boundaries
 * are computed by the browser (`Intl.Segmenter`) instead.
 *
 * Index semantics are identical to ICU's `ubrk_*`: all offsets are UTF-16 code units
 * and [BreakIterator.DONE] is returned once iteration runs out of boundaries.
 */
internal interface BreakIteratorBackend {
    /**
     * A non-null placeholder for [org.jetbrains.skia.impl.Native] (which refuses to wrap
     * a null pointer). It is never dereferenced: a backed [BreakIterator] owns no native memory.
     */
    val stubPointer: NativePointer

    fun setText(text: String?)
    fun current(): Int
    fun next(): Int
    fun previous(): Int
    fun first(): Int
    fun last(): Int
    fun preceding(offset: Int): Int
    fun following(offset: Int): Int
    fun isBoundary(offset: Int): Boolean
    val ruleStatus: Int
    val ruleStatuses: IntArray
}

/**
 * Returns a non-native [BreakIteratorBackend] for the given ICU `UBreakIteratorType`
 * ([BreakIterator.TYPE_CHARACTER], [BreakIterator.TYPE_WORD], [BreakIterator.TYPE_LINE],
 * [BreakIterator.TYPE_SENTENCE]), or `null` when the platform provides ICU natively
 * and the native implementation has to be used.
 */
internal expect fun createBreakIteratorBackend(type: Int, locale: String?): BreakIteratorBackend?
