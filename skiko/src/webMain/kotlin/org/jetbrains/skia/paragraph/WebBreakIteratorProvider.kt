package org.jetbrains.skia.paragraph

/**
 * Computes text segmentation boundaries with the browser-native `Intl.Segmenter`.
 *
 * All boundaries are computed in a single pass on the JS side and transferred as flat
 * integer buffers, so the Wasm <-> JS boundary is never crossed per character.
 *
 * All returned indices are UTF-16 code unit offsets, exactly as reported by
 * `Intl.Segmenter` and by `String.length`.
 */
internal class WebBreakIteratorProvider(val locale: String? = null) {
    /**
     * Returns grapheme cluster boundaries as `[index0, index1, ...]`.
     * The first boundary is `0`, the last one is the text length.
     */
    fun computeGraphemeBreaks(text: String): IntArray =
        if (text.isEmpty()) intArrayOf(0) else webComputeGraphemeBreaks(text, locale)

    /**
     * Returns word segments as `[start0, end0, isWordLike0, start1, end1, isWordLike1, ...]`,
     * where `isWordLike` is `1` for word-like segments and `0` otherwise.
     */
    fun computeWordBreaks(text: String): IntArray =
        if (text.isEmpty()) IntArray(0) else webComputeWordBreaks(text, locale)

    /**
     * Returns line break opportunities as `[index0, breakType0, index1, breakType1, ...]`,
     * where `breakType` is `0` for a soft break opportunity and `1` for a hard (mandatory) one.
     */
    fun computeLineBreaks(text: String): IntArray =
        if (text.isEmpty()) IntArray(0) else webComputeLineBreaks(text, locale)
}

internal expect fun webComputeGraphemeBreaks(text: String, locale: String?): IntArray

internal expect fun webComputeWordBreaks(text: String, locale: String?): IntArray

internal expect fun webComputeLineBreaks(text: String, locale: String?): IntArray

/**
 * `true` when the current engine provides `Intl.Segmenter`.
 */
internal expect fun isIntlSegmenterSupported(): Boolean
