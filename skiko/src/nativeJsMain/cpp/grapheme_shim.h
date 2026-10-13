// Lightweight grapheme break iterator shim for the ICU-less wasm build.
// Provides a UBreakIterator-compatible interface backed by pre-computed
// grapheme boundaries (from JS Intl.Segmenter), avoiding the full ICU
// break iterator subsystem.
#pragma once

#ifndef SKIKO_USE_ICU

#include <cstdint>
#include <cstddef>
#include <vector>
#include <string>
#include "src/core/SkUTF.h"

// Minimal UBreakIterator-compatible types and constants
// so that existing code using ubrk_following / ubrk_close compiles.
#ifndef UBRK_DONE
#define UBRK_DONE ((int32_t) -1)
#endif

// Minimal UErrorCode compatible type, so that the shared shaper sources keep compiling.
typedef int32_t UErrorCode;
#ifndef U_ZERO_ERROR
#define U_ZERO_ERROR ((UErrorCode) 0)
#endif
#ifndef U_UNSUPPORTED_ERROR
#define U_UNSUPPORTED_ERROR ((UErrorCode) 16)
#endif
#ifndef U_FAILURE
#define U_FAILURE(code) ((code) > 0)
#endif

// Values of ICU's UBiDiLevel "default" paragraph levels (see unicode/ubidi.h).
#ifndef UBIDI_DEFAULT_LTR
#define UBIDI_DEFAULT_LTR 0xfe
#endif
#ifndef UBIDI_DEFAULT_RTL
#define UBIDI_DEFAULT_RTL 0xff
#endif

// Forward-declare the opaque type that existing code uses.
struct SkikoGraphemeBreakIterator {
    std::vector<int32_t> boundaries; // byte offsets of grapheme boundaries in UTF-8
    int32_t currentIndex;            // index into boundaries (-1 = before first)
    int32_t textLength;              // total byte length of text

    SkikoGraphemeBreakIterator() : currentIndex(-1), textLength(0) {}
};

// When ICU is excluded from the binary, UBreakIterator is our shim type.
typedef SkikoGraphemeBreakIterator UBreakIterator;

// Compute grapheme boundaries for UTF-8 text.
// This is a simple implementation that treats each Unicode code point as a
// grapheme cluster. For proper grapheme cluster detection on wasm, the JS
// side should provide boundaries via Intl.Segmenter.
//
// The returned iterator must be freed with skiko_ubrk_close.
UBreakIterator* skiko_grapheme_break_iterator_create(const char* utf8, size_t utf8Bytes);

// UBreakIterator-compatible API
inline void skiko_ubrk_close(UBreakIterator* bi) { delete bi; }

int32_t skiko_ubrk_following(UBreakIterator* bi, int32_t offset);
int32_t skiko_ubrk_current(UBreakIterator* bi);
int32_t skiko_ubrk_next(UBreakIterator* bi);
int32_t skiko_ubrk_previous(UBreakIterator* bi);
int32_t skiko_ubrk_first(UBreakIterator* bi);
int32_t skiko_ubrk_last(UBreakIterator* bi);
int32_t skiko_ubrk_preceding(UBreakIterator* bi, int32_t offset);

// Redirect ubrk_* calls to our shim
#define ubrk_close skiko_ubrk_close
#define ubrk_following skiko_ubrk_following
#define ubrk_current skiko_ubrk_current
#define ubrk_next skiko_ubrk_next
#define ubrk_previous skiko_ubrk_previous
#define ubrk_first skiko_ubrk_first
#define ubrk_last skiko_ubrk_last
#define ubrk_preceding skiko_ubrk_preceding

// Minimal u_* character property shims (used by FontRunIterator.cc)
inline bool u_iscntrl_shim(int32_t c) {
    return (c >= 0 && c <= 0x1F) || (c >= 0x7F && c <= 0x9F);
}
inline bool u_isWhitespace_shim(int32_t c) {
    return c == 0x20 || c == 0x09 || c == 0x0A || c == 0x0D ||
           c == 0x0C || c == 0x85 || c == 0xA0 ||
           (c >= 0x2000 && c <= 0x200A) || c == 0x2028 || c == 0x2029 ||
           c == 0x202F || c == 0x205F || c == 0x3000;
}
inline bool u_ispunct_shim(int32_t c) {
    // Basic ASCII punctuation + common Unicode punctuation ranges
    return (c >= 0x21 && c <= 0x2F) || (c >= 0x3A && c <= 0x40) ||
           (c >= 0x5B && c <= 0x60) || (c >= 0x7B && c <= 0x7E) ||
           (c >= 0x2000 && c <= 0x206F) || (c >= 0x3000 && c <= 0x303F);
}

#define u_iscntrl u_iscntrl_shim
#define u_isWhitespace u_isWhitespace_shim
#define u_ispunct u_ispunct_shim

#endif // !SKIKO_USE_ICU
