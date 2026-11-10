#ifndef SKIKO_USE_ICU

#include "SkikoSegmenter.h"

#include <algorithm>

#ifdef __EMSCRIPTEN__
#include <emscripten.h>

// Segments the text at `ptr` (UTF-8 bytes, or UTF-16 code units when `isUtf16` is set)
// with the browser `Intl.Segmenter` and writes `[position, flag]` pairs into `out`.
//
// Returns the number of written pairs, or -1 when `Intl.Segmenter` is unavailable or
// `capacityPairs` is too small.
EM_JS(int, skiko_js_segment,
      (const void* ptr, int units, int isUtf16, int granularity, int32_t* out, int capacityPairs), {
    if (typeof Intl === 'undefined' || typeof Intl.Segmenter !== 'function') {
        return -1;
    }

    var text;
    var positionOf; // UTF-16 index -> reported position
    if (isUtf16) {
        var start = ptr >> 1;
        var chunks = [];
        for (var offset = 0; offset < units; offset += 8192) {
            chunks.push(String.fromCharCode.apply(
                null, HEAPU16.subarray(start + offset, start + Math.min(offset + 8192, units))));
        }
        text = chunks.join('');
        positionOf = null; // identity
    } else {
        text = new TextDecoder('utf-8').decode(HEAPU8.subarray(ptr, ptr + units));
        positionOf = new Int32Array(text.length + 1);
        var byteOffset = 0;
        for (var i = 0; i < text.length; ) {
            positionOf[i] = byteOffset;
            var cp = text.codePointAt(i);
            var codeUnits = cp > 0xFFFF ? 2 : 1;
            if (codeUnits === 2) {
                positionOf[i + 1] = byteOffset;
            }
            byteOffset += cp < 0x80 ? 1 : (cp < 0x800 ? 2 : (cp < 0x10000 ? 3 : 4));
            i += codeUnits;
        }
        positionOf[text.length] = byteOffset;
    }
    var mapPosition = function(index) { return positionOf === null ? index : positionOf[index]; };
    var textEnd = mapPosition(text.length);

    var pairs = [];
    if (granularity === 3) {
        // Intl.Segmenter has no 'line' granularity: approximate UAX #14 with word
        // boundaries (soft) plus the mandatory break characters (hard), exactly the way
        // SkUnicode_icu::extractPositions does it (every boundary is soft, and hard is
        // added after a line terminator).
        var flags = new Int32Array(text.length + 1);
        var wordSegmenter = new Intl.Segmenter(undefined, { granularity: 'word' });
        // UAX #14 LB13/LB16: no break before closing punctuation, exclamation,
        // separators or closing brackets.
        var noBreakBefore = '!?,.;:\u2019\u201D)]}\u00BB\u203A\u060C\u061B\u061F\u3001\u3002\uFF01\uFF0C\uFF0E\uFF1A\uFF1B\uFF1F\u2026\u2010\u2013\u2014/';
        for (var segment of wordSegmenter.segment(text)) {
            if (segment.index > 0 && noBreakBefore.indexOf(text[segment.index]) >= 0) {
                continue;
            }
            flags[segment.index] |= 1;
        }
        flags[0] |= 1;
        flags[text.length] |= 1;
        for (var j = 0; j < text.length; j++) {
            var c = text.charCodeAt(j);
            if (c === 0x0A || c === 0x0B || c === 0x0C || c === 0x0D ||
                c === 0x85 || c === 0x2028 || c === 0x2029) {
                var next = j + 1;
                if (c === 0x0D && next < text.length && text.charCodeAt(next) === 0x0A) {
                    next++;
                }
                flags[next] |= 2;
                j = next - 1;
            }
        }
        for (var k = 0; k <= text.length; k++) {
            if (flags[k] !== 0) {
                pairs.push(mapPosition(k), flags[k]);
            }
        }
    } else {
        var names = ['grapheme', 'word', 'sentence'];
        var segmenter = new Intl.Segmenter(undefined, { granularity: names[granularity] });
        for (var s of segmenter.segment(text)) {
            pairs.push(mapPosition(s.index), (granularity === 1 && s.isWordLike) ? 1 : 0);
        }
        pairs.push(textEnd, 0);
    }

    var count = pairs.length >> 1;
    if (count > capacityPairs) {
        return -1;
    }
    var base = out >> 2;
    for (var n = 0; n < pairs.length; n++) {
        HEAP32[base + n] = pairs[n];
    }
    return count;
});

EM_JS(int, skiko_js_segmenter_available, (), {
    return (typeof Intl !== 'undefined' && typeof Intl.Segmenter === 'function') ? 1 : 0;
});

#endif  // __EMSCRIPTEN__

namespace SkikoSegmenter {

namespace {

bool segment(const void* ptr, int units, bool isUtf16, Granularity granularity,
             std::vector<Boundary>* results) {
    results->clear();
    if (units <= 0) {
        results->push_back({0, 0});
        return true;
    }
#ifdef __EMSCRIPTEN__
    // There can never be more boundaries than code units + 1.
    std::vector<int32_t> buffer(static_cast<size_t>(units + 1) * 2);
    const int pairs = skiko_js_segment(ptr,
                                       units,
                                       isUtf16 ? 1 : 0,
                                       static_cast<int>(granularity),
                                       buffer.data(),
                                       units + 1);
    if (pairs <= 0) {
        return false;
    }
    results->reserve(pairs);
    for (int i = 0; i < pairs; ++i) {
        results->push_back({buffer[i * 2], buffer[i * 2 + 1]});
    }
    // Keep the boundaries strictly increasing and starting at 0.
    if (results->front().position != 0) {
        results->insert(results->begin(), {0, 0});
    }
    return true;
#else
    (void)ptr;
    (void)isUtf16;
    (void)granularity;
    return false;
#endif
}

}  // namespace

bool IsAvailable() {
#ifdef __EMSCRIPTEN__
    return skiko_js_segmenter_available() != 0;
#else
    return false;
#endif
}

bool SegmentUtf8(const char* utf8, int utf8Units, Granularity granularity,
                 std::vector<Boundary>* results) {
    return segment(utf8, utf8Units, /*isUtf16=*/false, granularity, results);
}

bool SegmentUtf16(const uint16_t* utf16, int utf16Units, Granularity granularity,
                  std::vector<Boundary>* results) {
    return segment(utf16, utf16Units, /*isUtf16=*/true, granularity, results);
}

}  // namespace SkikoSegmenter

#endif  // !SKIKO_USE_ICU
