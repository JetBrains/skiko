package org.jetbrains.skia.paragraph

import kotlin.js.JsArray
import kotlin.js.JsNumber
import kotlin.js.JsString
import kotlin.js.get

private fun JsArray<JsNumber>.toIntArray(): IntArray {
    val size = length
    val result = IntArray(size)
    for (index in 0 until size) {
        result[index] = this[index]?.toInt() ?: 0
    }
    return result
}

@JsFun(
    """(text, locale) => {
        const segmenter = new Intl.Segmenter(locale || undefined, { granularity: 'grapheme' });
        const result = [];
        for (const segment of segmenter.segment(text)) {
            result.push(segment.index);
        }
        result.push(text.length);
        return result;
    }"""
)
private external fun computeGraphemeBreaksJs(text: JsString, locale: JsString?): JsArray<JsNumber>

@JsFun(
    """(text, locale) => {
        const segmenter = new Intl.Segmenter(locale || undefined, { granularity: 'word' });
        const result = [];
        for (const segment of segmenter.segment(text)) {
            result.push(segment.index);
            result.push(segment.index + segment.segment.length);
            result.push(segment.isWordLike ? 1 : 0);
        }
        return result;
    }"""
)
private external fun computeWordBreaksJs(text: JsString, locale: JsString?): JsArray<JsNumber>

@JsFun(
    """(text, locale) => {
        const length = text.length;
        const flags = new Int32Array(length + 1).fill(-1);
        const segmenter = new Intl.Segmenter(locale || undefined, { granularity: 'word' });
        for (const segment of segmenter.segment(text)) {
            if (segment.index > 0) {
                flags[segment.index] = 0;
            }
        }
        for (let i = 0; i < length; i++) {
            const c = text.charCodeAt(i);
            if (c === 0x0A || c === 0x2028 || c === 0x2029 || c === 0x0D) {
                let next = i + 1;
                if (c === 0x0D && next < length && text.charCodeAt(next) === 0x0A) {
                    next++;
                }
                flags[next] = 1;
                i = next - 1;
            }
        }
        const result = [];
        for (let i = 0; i <= length; i++) {
            if (flags[i] >= 0) {
                result.push(i);
                result.push(flags[i]);
            }
        }
        return result;
    }"""
)
private external fun computeLineBreaksJs(text: JsString, locale: JsString?): JsArray<JsNumber>

@JsFun("() => (typeof Intl !== 'undefined' && typeof Intl.Segmenter === 'function')")
private external fun isIntlSegmenterSupportedJs(): Boolean

internal actual fun webComputeGraphemeBreaks(text: String, locale: String?): IntArray =
    computeGraphemeBreaksJs(text.toJsString(), locale?.toJsString()).toIntArray()

internal actual fun webComputeWordBreaks(text: String, locale: String?): IntArray =
    computeWordBreaksJs(text.toJsString(), locale?.toJsString()).toIntArray()

internal actual fun webComputeLineBreaks(text: String, locale: String?): IntArray =
    computeLineBreaksJs(text.toJsString(), locale?.toJsString()).toIntArray()

internal actual fun isIntlSegmenterSupported(): Boolean = isIntlSegmenterSupportedJs()
