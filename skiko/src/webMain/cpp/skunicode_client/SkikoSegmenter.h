// Text segmentation backed by the browser `Intl.Segmenter`.
//
// It is the "JS ICU" counterpart used by the ICU-less flavour of the Skiko wasm binary
// (built with `-Pskiko.wasm.useIcu=false`), where ICU's `ubrk_*` API is not available.
#pragma once

#ifndef SKIKO_USE_ICU

#include <cstdint>
#include <vector>

namespace SkikoSegmenter {

enum class Granularity {
    kGrapheme,
    kWord,
    kSentence,
    /** Line break opportunities (UAX #14 approximation, see the note below). */
    kLine,
};

/**
 * A boundary with an extra bit:
 * - for [Granularity::kWord] `flag` is `1` when the segment *starting* at this
 *   boundary is word-like,
 * - for [Granularity::kLine] `flag` is a bitmask: `1` = soft break opportunity,
 *   `2` = mandatory (hard) break, mirroring `SkUnicode_icu::extractPositions`,
 * - otherwise `flag` is always `0`.
 */
struct Boundary {
    int32_t position;
    int32_t flag;
};

/** `true` when the engine provides `Intl.Segmenter`. */
bool IsAvailable();

/**
 * Segments UTF-8 text; the returned positions are UTF-8 byte offsets and always
 * include `0` and `utf8Units`. Returns `false` when `Intl.Segmenter` is unavailable.
 *
 * Note: `Intl.Segmenter` has no `line` granularity, so [Granularity::kLine] is
 * approximated with word boundaries plus the mandatory break characters of UAX #14.
 */
bool SegmentUtf8(const char* utf8, int utf8Units, Granularity granularity,
                 std::vector<Boundary>* results);

/**
 * Same as [SegmentUtf8], but both the input and the returned positions are
 * UTF-16 code units.
 */
bool SegmentUtf16(const uint16_t* utf16, int utf16Units, Granularity granularity,
                  std::vector<Boundary>* results);

}  // namespace SkikoSegmenter

#endif  // !SKIKO_USE_ICU
