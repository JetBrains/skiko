#include "SkShaper.h"
#include "common.h"

#ifndef SKIKO_USE_ICU

#include <vector>

#include "skunicode_client/SkikoBidi.h"

namespace {

// A `SkShaper::BiDiRunIterator` backed by Skiko's embedded UAX #9 implementation,
// used when ICU (and therefore `SkShaper::MakeIcuBiDiRunIterator`) is excluded
// from the binary.
class SkikoBiDiRunIterator final : public SkShaper::BiDiRunIterator {
public:
    SkikoBiDiRunIterator(size_t utf8Bytes, std::vector<uint8_t> levels)
        : fLevels(std::move(levels)), fSize(utf8Bytes), fEnd(0), fLevel(0) {}

    void consume() override {
        if (fEnd >= fSize) {
            return;
        }
        fLevel = fLevels[fEnd];
        size_t end = fEnd;
        while (end < fSize && fLevels[end] == fLevel) {
            ++end;
        }
        fEnd = end;
    }

    size_t endOfCurrentRun() const override { return fEnd; }

    bool atEnd() const override { return fEnd == fSize; }

    uint8_t currentLevel() const override { return fLevel; }

private:
    const std::vector<uint8_t> fLevels;
    const size_t fSize;
    size_t fEnd;
    uint8_t fLevel;
};

SkikoBidi::Direction directionOf(int bidiLevel) {
    const int level = bidiLevel & 0xFF;
    if (level == UBIDI_DEFAULT_LTR) return SkikoBidi::Direction::kAuto;
    if (level == UBIDI_DEFAULT_RTL) return SkikoBidi::Direction::kAuto;
    return (level & 1) ? SkikoBidi::Direction::kRTL : SkikoBidi::Direction::kLTR;
}

}  // namespace

#endif  // !SKIKO_USE_ICU

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_IcuBidiRunIterator__1nMake
  (KNativePointer textPtr, KInt bidiLevel) {
    SkString* text = reinterpret_cast<SkString*>((textPtr));
#ifdef SKIKO_USE_ICU
    std::unique_ptr<SkShaper::BiDiRunIterator> instance(SkShaper::MakeIcuBiDiRunIterator(text->c_str(), text->size(), bidiLevel & 0xFF));
    return reinterpret_cast<KNativePointer>(instance.release());
#else
    // ICU is excluded from this binary: resolve the levels with the embedded UAX #9.
    std::vector<uint8_t> levels;
    SkikoBidi::ResolveLevelsUtf8(text->c_str(), static_cast<int>(text->size()), directionOf(bidiLevel), &levels);
    std::unique_ptr<SkShaper::BiDiRunIterator> instance(
        new SkikoBiDiRunIterator(text->size(), std::move(levels)));
    return reinterpret_cast<KNativePointer>(instance.release());
#endif
}

SKIKO_EXPORT KInt org_jetbrains_skia_shaper_IcuBidiRunIterator__1nGetCurrentLevel
  (KNativePointer ptr) {
    SkShaper::BiDiRunIterator* instance = reinterpret_cast<SkShaper::BiDiRunIterator*>((ptr));
    return instance->currentLevel();
}
