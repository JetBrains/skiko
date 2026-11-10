#ifndef SKIKO_USE_ICU

#include "grapheme_shim.h"
#include "src/core/SkUTF.h"
#include <algorithm>

#ifdef __EMSCRIPTEN__
#include <emscripten.h>

// Computes extended grapheme cluster boundaries with the browser `Intl.Segmenter`
// and writes them as UTF-8 byte offsets (including the leading 0 and the text length).
//
// Returns the number of written boundaries, or -1 when `Intl.Segmenter` is unavailable.
EM_JS(int, skiko_js_grapheme_boundaries, (const char* utf8, int utf8Bytes, int32_t* out, int capacity), {
    if (typeof Intl === 'undefined' || typeof Intl.Segmenter !== 'function') {
        return -1;
    }
    var text = new TextDecoder('utf-8').decode(HEAPU8.subarray(utf8, utf8 + utf8Bytes));
    // UTF-16 index -> UTF-8 byte offset of the code point starting at that index.
    var byteOf = new Int32Array(text.length + 1);
    var byteOffset = 0;
    for (var i = 0; i < text.length; ) {
        byteOf[i] = byteOffset;
        var cp = text.codePointAt(i);
        var units = cp > 0xFFFF ? 2 : 1;
        if (units === 2) {
            byteOf[i + 1] = byteOffset;
        }
        byteOffset += cp < 0x80 ? 1 : (cp < 0x800 ? 2 : (cp < 0x10000 ? 3 : 4));
        i += units;
    }
    byteOf[text.length] = byteOffset;

    var segmenter = new Intl.Segmenter(undefined, { granularity: 'grapheme' });
    var count = 0;
    var base = out >> 2;
    for (var segment of segmenter.segment(text)) {
        if (count >= capacity) return -1;
        HEAP32[base + count] = byteOf[segment.index];
        count++;
    }
    if (count >= capacity) return -1;
    HEAP32[base + count] = byteOffset;
    count++;
    return count;
});
#endif // __EMSCRIPTEN__

UBreakIterator* skiko_grapheme_break_iterator_create(const char* utf8, size_t utf8Bytes) {
    auto* bi = new SkikoGraphemeBreakIterator();
    bi->textLength = static_cast<int32_t>(utf8Bytes);

#ifdef __EMSCRIPTEN__
    // Preferred path: real extended grapheme clusters from the browser `Intl.Segmenter`.
    // There can never be more boundaries than UTF-8 bytes + 1, so a single call suffices.
    if (utf8Bytes > 0) {
        std::vector<int32_t> buffer(utf8Bytes + 1);
        int written = skiko_js_grapheme_boundaries(
            utf8, static_cast<int>(utf8Bytes), buffer.data(), static_cast<int>(buffer.size()));
        if (written > 0) {
            bi->boundaries.assign(buffer.begin(), buffer.begin() + written);
            if (bi->boundaries.front() != 0) {
                bi->boundaries.insert(bi->boundaries.begin(), 0);
            }
            bi->currentIndex = 0;
            return bi;
        }
    }
#endif // __EMSCRIPTEN__

    // Fallback (no Intl.Segmenter): each code point is treated as a grapheme cluster.
    const char* ptr = utf8;
    const char* end = utf8 + utf8Bytes;
    bi->boundaries.push_back(0);
    while (ptr < end) {
        SkUTF::NextUTF8(&ptr, end);
        bi->boundaries.push_back(static_cast<int32_t>(ptr - utf8));
    }
    bi->currentIndex = 0;
    return bi;
}

int32_t skiko_ubrk_following(UBreakIterator* bi, int32_t offset) {
    // Find the first boundary strictly after offset
    for (size_t i = 0; i < bi->boundaries.size(); ++i) {
        if (bi->boundaries[i] > offset) {
            bi->currentIndex = static_cast<int32_t>(i);
            return bi->boundaries[i];
        }
    }
    bi->currentIndex = static_cast<int32_t>(bi->boundaries.size());
    return UBRK_DONE;
}

int32_t skiko_ubrk_current(UBreakIterator* bi) {
    if (bi->currentIndex < 0 || bi->currentIndex >= (int32_t)bi->boundaries.size())
        return UBRK_DONE;
    return bi->boundaries[bi->currentIndex];
}

int32_t skiko_ubrk_next(UBreakIterator* bi) {
    if (bi->currentIndex + 1 >= (int32_t)bi->boundaries.size())
        return UBRK_DONE;
    bi->currentIndex++;
    return bi->boundaries[bi->currentIndex];
}

int32_t skiko_ubrk_previous(UBreakIterator* bi) {
    if (bi->currentIndex <= 0)
        return UBRK_DONE;
    bi->currentIndex--;
    return bi->boundaries[bi->currentIndex];
}

int32_t skiko_ubrk_first(UBreakIterator* bi) {
    bi->currentIndex = 0;
    return bi->boundaries.empty() ? UBRK_DONE : bi->boundaries[0];
}

int32_t skiko_ubrk_last(UBreakIterator* bi) {
    if (bi->boundaries.empty()) return UBRK_DONE;
    bi->currentIndex = static_cast<int32_t>(bi->boundaries.size()) - 1;
    return bi->boundaries[bi->currentIndex];
}

int32_t skiko_ubrk_preceding(UBreakIterator* bi, int32_t offset) {
    // Find the last boundary strictly before offset
    for (int i = (int)bi->boundaries.size() - 1; i >= 0; --i) {
        if (bi->boundaries[i] < offset) {
            bi->currentIndex = i;
            return bi->boundaries[i];
        }
    }
    bi->currentIndex = -1;
    return UBRK_DONE;
}

#endif // !SKIKO_USE_ICU
