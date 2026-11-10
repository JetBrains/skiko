package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.paragraph.SkikoUnicodeBackend
import org.jetbrains.skia.paragraph.WebBreakIteratorProvider
import org.jetbrains.skia.paragraph.isIntlSegmenterSupported

/**
 * On web the native break iterator exists only when ICU is linked into `skiko.wasm`.
 * For an ICU-less binary (`-Pskiko.wasm.useIcu=false`) the boundaries are computed by the
 * browser with `Intl.Segmenter` ([WebBreakIteratorProvider]).
 */
internal actual fun createBreakIteratorBackend(type: Int, locale: String?): BreakIteratorBackend? {
    if (SkikoUnicodeBackend.isIcuAvailable) return null
    if (!isIntlSegmenterSupported()) {
        throw UnsupportedOperationException(
            "ICU is excluded from this Skiko binary and Intl.Segmenter is not supported by this engine, " +
                "so text boundaries cannot be computed."
        )
    }
    return WebBreakIteratorBackend(type, locale)
}

/**
 * Implements the ICU `ubrk_*` protocol over the boundaries reported by `Intl.Segmenter`.
 *
 * All boundaries for the current text are computed eagerly in [setText] (a single JS call),
 * so iteration itself never crosses the Wasm <-> JS boundary.
 */
private class WebBreakIteratorBackend(
    private val type: Int,
    locale: String?
) : BreakIteratorBackend {
    // Not a real address: `Native` refuses to wrap a null pointer, and distinct values
    // keep `equals`/`hashCode` of the owning BreakIterator meaningful.
    override val stubPointer: NativePointer = ++lastStubPointer

    private val provider = WebBreakIteratorProvider(locale)

    private var text: String = ""

    /** Sorted boundary offsets; always starts with `0` and ends with `text.length`. */
    private var boundaries: IntArray = intArrayOf(0)

    /** `ruleStatus` for each entry of [boundaries] (always `0` for non-word iterators). */
    private var statuses: IntArray = intArrayOf(BreakIterator.WORD_NONE)

    private var position: Int = 0

    override fun setText(text: String?) {
        this.text = text ?: ""
        computeBoundaries()
        position = 0
    }

    override fun current(): Int = boundaries[position]

    override fun next(): Int {
        if (position >= boundaries.size - 1) return BreakIterator.DONE
        return boundaries[++position]
    }

    override fun previous(): Int {
        if (position == 0) return BreakIterator.DONE
        return boundaries[--position]
    }

    override fun first(): Int {
        position = 0
        return boundaries[position]
    }

    override fun last(): Int {
        position = boundaries.size - 1
        return boundaries[position]
    }

    override fun preceding(offset: Int): Int {
        for (index in boundaries.indices.reversed()) {
            if (boundaries[index] < offset) {
                position = index
                return boundaries[index]
            }
        }
        return BreakIterator.DONE
    }

    override fun following(offset: Int): Int {
        for (index in boundaries.indices) {
            if (boundaries[index] > offset) {
                position = index
                return boundaries[index]
            }
        }
        return BreakIterator.DONE
    }

    override fun isBoundary(offset: Int): Boolean {
        for (index in boundaries.indices) {
            val boundary = boundaries[index]
            if (boundary == offset) {
                position = index
                return true
            }
            if (boundary > offset) {
                // ICU moves the iterator to the boundary following `offset`.
                position = index
                return false
            }
        }
        position = boundaries.size - 1
        return false
    }

    override val ruleStatus: Int
        get() = statuses[position]

    override val ruleStatuses: IntArray
        get() = intArrayOf(ruleStatus)

    private fun computeBoundaries() {
        val length = text.length
        if (length == 0) {
            boundaries = intArrayOf(0)
            statuses = intArrayOf(BreakIterator.WORD_NONE)
            return
        }
        when (type) {
            BreakIterator.TYPE_WORD -> computeWordBoundaries()
            BreakIterator.TYPE_LINE -> {
                boundaries = normalize(breakOpportunities(provider.computeLineBreaks(text)))
                statuses = IntArray(boundaries.size)
            }
            BreakIterator.TYPE_SENTENCE -> {
                boundaries = normalize(provider.computeSentenceBreaks(text))
                statuses = IntArray(boundaries.size)
            }
            else -> {
                boundaries = normalize(provider.computeGraphemeBreaks(text))
                statuses = IntArray(boundaries.size)
            }
        }
    }

    /** `[index, breakType, ...]` -> `[index, ...]`. */
    private fun breakOpportunities(flat: IntArray): IntArray {
        val result = IntArray(flat.size / 2)
        for (index in result.indices) {
            result[index] = flat[index * 2]
        }
        return result
    }

    /**
     * Word segments come as `[start, end, isWordLike]` triples covering the whole text;
     * both ends of every segment are boundaries, and the status of a boundary describes
     * the segment that *precedes* it, exactly as ICU reports it.
     */
    private fun computeWordBoundaries() {
        val segments = provider.computeWordBreaks(text)
        val offsets = ArrayList<Int>(segments.size / 3 + 2)
        val kinds = ArrayList<Int>(segments.size / 3 + 2)
        offsets.add(0)
        kinds.add(BreakIterator.WORD_NONE)
        var index = 0
        while (index + 2 < segments.size) {
            val start = segments[index]
            val end = segments[index + 1]
            val isWordLike = segments[index + 2] != 0
            if (start > offsets[offsets.size - 1]) {
                // A gap (should not happen with Intl.Segmenter, but keep boundaries monotonic).
                offsets.add(start)
                kinds.add(BreakIterator.WORD_NONE)
            }
            if (end > offsets[offsets.size - 1]) {
                offsets.add(end)
                kinds.add(if (isWordLike) wordKind(start, end) else BreakIterator.WORD_NONE)
            }
            index += 3
        }
        if (offsets[offsets.size - 1] != text.length) {
            offsets.add(text.length)
            kinds.add(BreakIterator.WORD_NONE)
        }
        boundaries = IntArray(offsets.size) { offsets[it] }
        statuses = IntArray(kinds.size) { kinds[it] }
    }

    /**
     * Maps a word-like segment onto ICU's rule status tags, so that `WORD_NONE` keeps
     * meaning "not a word" for callers that only check `ruleStatus != WORD_NONE`.
     */
    private fun wordKind(start: Int, end: Int): Int {
        var hasDigit = false
        var hasKana = false
        var hasIdeo = false
        var hasLetter = false
        var i = start
        while (i < end) {
            val code = text[i].code
            when {
                code in 0x30..0x39 -> hasDigit = true
                code in 0x3040..0x30FF || code in 0x31F0..0x31FF -> hasKana = true
                code in 0x3400..0x4DBF || code in 0x4E00..0x9FFF || code in 0xF900..0xFAFF -> hasIdeo = true
                else -> hasLetter = true
            }
            i++
        }
        return when {
            hasIdeo -> BreakIterator.WORD_IDEO
            hasKana -> BreakIterator.WORD_KANA
            hasDigit && !hasLetter -> BreakIterator.WORD_NUMBER
            else -> BreakIterator.WORD_LETTER
        }
    }

    private companion object {
        var lastStubPointer: NativePointer = 0
    }

    /** Makes sure `0` and `text.length` are present and the array is strictly increasing. */
    private fun normalize(input: IntArray): IntArray {
        val result = ArrayList<Int>(input.size + 2)
        result.add(0)
        for (value in input) {
            if (value > result[result.size - 1] && value <= text.length) {
                result.add(value)
            }
        }
        if (result[result.size - 1] != text.length) {
            result.add(text.length)
        }
        return IntArray(result.size) { result[it] }
    }
}
