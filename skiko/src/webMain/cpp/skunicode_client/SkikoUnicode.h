// A process-wide, text-agnostic `SkUnicode` for the ICU-less Skiko wasm binary.
//
// Unlike `SkUnicode_client`, which has to be constructed from one concrete text with
// pre-computed boundaries, this implementation answers queries for arbitrary text:
// segmentation comes from the browser `Intl.Segmenter` (see `SkikoSegmenter`) and the
// bidirectional algorithm from `SkikoBidi`.
//
// This is what makes the text-agnostic Skia APIs (`SkShapers::HB::ShapeDontWrapOrReorder`,
// and therefore `Shaper`/`TextLine`) work without ICU.
#pragma once

#ifndef SKIKO_USE_ICU

#include "include/core/SkRefCnt.h"
#include "modules/skunicode/include/SkUnicode.h"

namespace SkikoUnicode {

/**
 * Returns a shared `SkUnicode` backed by `Intl.Segmenter` + `SkikoBidi`,
 * or `nullptr` when the engine provides no `Intl.Segmenter`.
 */
sk_sp<SkUnicode> Make();

}  // namespace SkikoUnicode

#endif  // !SKIKO_USE_ICU
