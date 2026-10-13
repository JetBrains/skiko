package org.jetbrains.skia.paragraph

private fun dynamicToIntArray(array: dynamic): IntArray {
    val size = array.length as Int
    val result = IntArray(size)
    for (index in 0 until size) {
        result[index] = (array[index] as Number).toInt()
    }
    return result
}

private fun segmentGraphemes(text: String, locale: String?): dynamic = js(
    """
    (function() {
        var segmenter = new Intl.Segmenter(locale || undefined, { granularity: 'grapheme' });
        var result = [];
        var iterator = segmenter.segment(text)[Symbol.iterator]();
        for (var step = iterator.next(); !step.done; step = iterator.next()) {
            result.push(step.value.index);
        }
        result.push(text.length);
        return new Int32Array(result);
    })()
    """
)

private fun segmentWords(text: String, locale: String?): dynamic = js(
    """
    (function() {
        var segmenter = new Intl.Segmenter(locale || undefined, { granularity: 'word' });
        var result = [];
        var iterator = segmenter.segment(text)[Symbol.iterator]();
        for (var step = iterator.next(); !step.done; step = iterator.next()) {
            var segment = step.value;
            result.push(segment.index);
            result.push(segment.index + segment.segment.length);
            result.push(segment.isWordLike ? 1 : 0);
        }
        return new Int32Array(result);
    })()
    """
)

private fun segmentLineBreaks(text: String, locale: String?): dynamic = js(
    """
    (function() {
        var length = text.length;
        var flags = new Int32Array(length + 1).fill(-1);
        var segmenter = new Intl.Segmenter(locale || undefined, { granularity: 'word' });
        var iterator = segmenter.segment(text)[Symbol.iterator]();
        for (var step = iterator.next(); !step.done; step = iterator.next()) {
            if (step.value.index > 0) {
                flags[step.value.index] = 0;
            }
        }
        for (var i = 0; i < length; i++) {
            var c = text.charCodeAt(i);
            if (c === 0x0A || c === 0x2028 || c === 0x2029 || c === 0x0D) {
                var next = i + 1;
                if (c === 0x0D && next < length && text.charCodeAt(next) === 0x0A) {
                    next++;
                }
                flags[next] = 1;
                i = next - 1;
            }
        }
        var result = [];
        for (var j = 0; j <= length; j++) {
            if (flags[j] >= 0) {
                result.push(j);
                result.push(flags[j]);
            }
        }
        return new Int32Array(result);
    })()
    """
)

private fun intlSegmenterSupported(): Boolean =
    js("typeof Intl !== 'undefined' && typeof Intl.Segmenter === 'function'") as Boolean

internal actual fun webComputeGraphemeBreaks(text: String, locale: String?): IntArray =
    dynamicToIntArray(segmentGraphemes(text, locale))

internal actual fun webComputeWordBreaks(text: String, locale: String?): IntArray =
    dynamicToIntArray(segmentWords(text, locale))

internal actual fun webComputeLineBreaks(text: String, locale: String?): IntArray =
    dynamicToIntArray(segmentLineBreaks(text, locale))

internal actual fun isIntlSegmenterSupported(): Boolean = intlSegmenterSupported()
