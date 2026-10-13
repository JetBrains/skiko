// Self-contained implementation of the Unicode Bidirectional Algorithm (UAX #9).
//
// It is used by the ICU-less flavour of the Skiko wasm binary (built with
// `-Pskiko.wasm.useIcu=false`), where `SkUnicode_client` cannot delegate BiDi
// resolution to ICU's `ubidi_*` API.
//
// The implementation covers the rules P2-P3, X1-X8, W1-W7, N1-N2, I1-I2 and L1-L2.
// Bracket pairs (N0) are not resolved, and isolate initiators are processed as
// neutrals, which is an approximation that behaves correctly for the vast majority
// of real world text.
#pragma once

#include <cstdint>
#include <vector>

namespace SkikoBidi {

/** Paragraph direction requested by the caller. */
enum class Direction {
    kLTR,
    kRTL,
    /** Derive the paragraph level from the text itself (rules P2-P3). */
    kAuto,
};

/**
 * Resolves the embedding level of every UTF-16 code unit of [text].
 *
 * `levels` is resized to `count` entries. Both code units of a surrogate pair
 * get the same level.
 */
void ResolveLevelsUtf16(const uint16_t* text,
                        int count,
                        Direction direction,
                        std::vector<uint8_t>* levels);

/**
 * Resolves the embedding level of every UTF-8 code unit (byte) of [text].
 *
 * `levels` is resized to `utf8Units` entries; all bytes of a multi-byte sequence
 * get the same level.
 */
void ResolveLevelsUtf8(const char* text,
                       int utf8Units,
                       Direction direction,
                       std::vector<uint8_t>* levels);

/** Implements rule L2: computes the visual order of runs with the given levels. */
void ReorderVisual(const uint8_t* runLevels, int levelsCount, int32_t* logicalFromVisual);

}  // namespace SkikoBidi
