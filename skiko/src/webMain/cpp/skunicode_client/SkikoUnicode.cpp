#ifndef SKIKO_USE_ICU

#include "SkikoUnicode.h"

#include "SkikoBidi.h"
#include "SkikoSegmenter.h"

#include "include/core/SkSpan.h"
#include "include/core/SkString.h"
#include "include/private/SkAssert.h"
#include "include/private/SkTArray.h"
#include "include/private/SkTo.h"
#include "modules/skunicode/src/SkUnicode_hardcoded.h"
#include "src/core/SkBitmaskEnum.h"
#include "src/core/SkUTF.h"

#include <algorithm>
#include <memory>
#include <string>
#include <utility>
#include <vector>

using namespace skia_private;

namespace {

SkikoBidi::Direction toSkikoDirection(SkBidiIterator::Direction dir) {
    return dir == SkBidiIterator::kRTL ? SkikoBidi::Direction::kRTL : SkikoBidi::Direction::kLTR;
}

SkikoSegmenter::Granularity toGranularity(SkUnicode::BreakType type) {
    switch (type) {
        case SkUnicode::BreakType::kWords: return SkikoSegmenter::Granularity::kWord;
        case SkUnicode::BreakType::kGraphemes: return SkikoSegmenter::Granularity::kGrapheme;
        case SkUnicode::BreakType::kLines: return SkikoSegmenter::Granularity::kLine;
        case SkUnicode::BreakType::kSentences: return SkikoSegmenter::Granularity::kSentence;
    }
    return SkikoSegmenter::Granularity::kGrapheme;
}

/** An `SkBidiIterator` backed by levels resolved by `SkikoBidi`. */
class BidiIterator final : public SkBidiIterator {
public:
    explicit BidiIterator(std::vector<uint8_t> levels) : fLevels(std::move(levels)) {}

    Position getLength() override { return SkToS32(fLevels.size()); }

    Level getLevelAt(Position pos) override {
        if (pos < 0 || pos >= SkToS32(fLevels.size())) {
            return 0;
        }
        return fLevels[pos];
    }

private:
    const std::vector<uint8_t> fLevels;
};

/**
 * An `SkBreakIterator` that segments whatever text it is given with `Intl.Segmenter`
 * (as opposed to `SkBreakIterator_client`, which can only replay pre-computed boundaries).
 */
class BreakIterator final : public SkBreakIterator {
public:
    explicit BreakIterator(SkUnicode::BreakType type) : fType(type), fIndex(0) {}

    Position first() override {
        fIndex = 0;
        return current();
    }

    Position current() override {
        if (fBoundaries.empty()) {
            return 0;
        }
        return fBoundaries[std::min(fIndex, fBoundaries.size() - 1)].position;
    }

    Position next() override {
        if (fIndex + 1 >= fBoundaries.size()) {
            fIndex = fBoundaries.empty() ? 0 : fBoundaries.size() - 1;
            return current();
        }
        ++fIndex;
        return current();
    }

    Status status() override {
        if (fType != SkUnicode::BreakType::kLines || fBoundaries.empty()) {
            return 0;
        }
        return (fBoundaries[std::min(fIndex, fBoundaries.size() - 1)].flag & 2)
                       ? SkUnicode::CodeUnitFlags::kHardLineBreakBefore
                       : SkUnicode::CodeUnitFlags::kSoftLineBreakBefore;
    }

    bool isDone() override { return fBoundaries.empty() || fIndex + 1 >= fBoundaries.size(); }

    bool setText(const char utftext8[], int utf8Units) override {
        fIndex = 0;
        return SkikoSegmenter::SegmentUtf8(utftext8, utf8Units, toGranularity(fType), &fBoundaries);
    }

    bool setText(const char16_t utftext16[], int utf16Units) override {
        fIndex = 0;
        return SkikoSegmenter::SegmentUtf16(
                reinterpret_cast<const uint16_t*>(utftext16), utf16Units, toGranularity(fType), &fBoundaries);
    }

private:
    const SkUnicode::BreakType fType;
    std::vector<SkikoSegmenter::Boundary> fBoundaries;
    size_t fIndex;
};

class SkikoUnicodeImpl final : public SkUnicodeHardCodedCharProperties {
public:
    SkikoUnicodeImpl() = default;
    ~SkikoUnicodeImpl() override = default;

    SkString toUpper(const SkString& str) override { return this->toUpper(str, nullptr); }

    SkString toUpper(const SkString& str, const char* locale) override {
        // Locale sensitive casing needs the UCD; ASCII is the best we can do here.
        SkString result(str);
        for (size_t i = 0; i < result.size(); ++i) {
            char& c = result.data()[i];
            if (c >= 'a' && c <= 'z') {
                c = static_cast<char>(c - 'a' + 'A');
            }
        }
        return result;
    }

    std::unique_ptr<SkBidiIterator> makeBidiIterator(const uint16_t text[], int count,
                                                     SkBidiIterator::Direction dir) override {
        std::vector<uint8_t> levels;
        SkikoBidi::ResolveLevelsUtf16(text, count, toSkikoDirection(dir), &levels);
        return std::make_unique<BidiIterator>(std::move(levels));
    }

    std::unique_ptr<SkBidiIterator> makeBidiIterator(const char text[], int count,
                                                     SkBidiIterator::Direction dir) override {
        std::vector<uint8_t> levels;
        SkikoBidi::ResolveLevelsUtf8(text, count, toSkikoDirection(dir), &levels);
        return std::make_unique<BidiIterator>(std::move(levels));
    }

    std::unique_ptr<SkBreakIterator> makeBreakIterator(const char locale[],
                                                       BreakType breakType) override {
        return std::make_unique<BreakIterator>(breakType);
    }

    std::unique_ptr<SkBreakIterator> makeBreakIterator(BreakType breakType) override {
        return std::make_unique<BreakIterator>(breakType);
    }

    bool getBidiRegions(const char utf8[], int utf8Units, TextDirection dir,
                        std::vector<BidiRegion>* results) override {
        results->clear();
        if (utf8Units <= 0) {
            return true;
        }
        std::vector<uint8_t> levels;
        SkikoBidi::ResolveLevelsUtf8(utf8,
                                     utf8Units,
                                     dir == TextDirection::kRTL ? SkikoBidi::Direction::kRTL
                                                                : SkikoBidi::Direction::kLTR,
                                     &levels);
        size_t start = 0;
        for (size_t i = 1; i <= levels.size(); ++i) {
            if (i == levels.size() || levels[i] != levels[start]) {
                results->emplace_back(start, i, levels[start]);
                start = i;
            }
        }
        return true;
    }

    // Returns results in UTF-16, as the contract requires.
    bool getWords(const char utf8[], int utf8Units, const char* locale,
                  std::vector<Position>* results) override {
        results->clear();
        std::u16string utf16 = SkUnicode::convertUtf8ToUtf16(utf8, utf8Units);
        std::vector<SkikoSegmenter::Boundary> boundaries;
        if (!SkikoSegmenter::SegmentUtf16(reinterpret_cast<const uint16_t*>(utf16.data()),
                                          SkToS32(utf16.size()),
                                          SkikoSegmenter::Granularity::kWord,
                                          &boundaries)) {
            return false;
        }
        for (const auto& boundary : boundaries) {
            results->push_back(boundary.position);
        }
        return true;
    }

    bool getUtf8Words(const char utf8[], int utf8Units, const char* locale,
                      std::vector<Position>* results) override {
        return this->collectUtf8(utf8, utf8Units, SkikoSegmenter::Granularity::kWord, results);
    }

    bool getSentences(const char utf8[], int utf8Units, const char* locale,
                      std::vector<Position>* results) override {
        return this->collectUtf8(utf8, utf8Units, SkikoSegmenter::Granularity::kSentence, results);
    }

    bool computeCodeUnitFlags(char utf8[], int utf8Units, bool replaceTabs,
                              TArray<SkUnicode::CodeUnitFlags, true>* results) override {
        results->clear();
        results->push_back_n(utf8Units + 1, CodeUnitFlags::kNoCodeUnitFlag);

        std::vector<SkikoSegmenter::Boundary> lineBreaks;
        std::vector<SkikoSegmenter::Boundary> graphemes;
        if (!SkikoSegmenter::SegmentUtf8(utf8, utf8Units, SkikoSegmenter::Granularity::kLine, &lineBreaks) ||
            !SkikoSegmenter::SegmentUtf8(utf8, utf8Units, SkikoSegmenter::Granularity::kGrapheme, &graphemes)) {
            return false;
        }
        this->applyBreaks(lineBreaks, graphemes, utf8Units, results);

        const char* current = utf8;
        const char* end = utf8 + utf8Units;
        while (current < end) {
            auto before = current - utf8;
            SkUnichar unichar = SkUTF::NextUTF8(&current, end);
            if (unichar < 0) unichar = 0xFFFD;
            auto after = current - utf8;
            if (replaceTabs && this->isTabulation(unichar)) {
                results->at(before) |= SkUnicode::kTabulation;
                unichar = ' ';
                utf8[before] = ' ';
            }
            for (auto i = before; i < after; ++i) {
                this->applyCharFlags(unichar, &results->at(i));
            }
        }
        return true;
    }

    bool computeCodeUnitFlags(char16_t utf16[], int utf16Units, bool replaceTabs,
                              TArray<SkUnicode::CodeUnitFlags, true>* results) override {
        results->clear();
        results->push_back_n(utf16Units + 1, CodeUnitFlags::kNoCodeUnitFlag);

        const uint16_t* text = reinterpret_cast<const uint16_t*>(utf16);
        std::vector<SkikoSegmenter::Boundary> lineBreaks;
        std::vector<SkikoSegmenter::Boundary> graphemes;
        if (!SkikoSegmenter::SegmentUtf16(text, utf16Units, SkikoSegmenter::Granularity::kLine, &lineBreaks) ||
            !SkikoSegmenter::SegmentUtf16(text, utf16Units, SkikoSegmenter::Granularity::kGrapheme, &graphemes)) {
            return false;
        }
        this->applyBreaks(lineBreaks, graphemes, utf16Units, results);

        for (int i = 0; i < utf16Units; ++i) {
            SkUnichar unichar = utf16[i];
            if (replaceTabs && this->isTabulation(unichar)) {
                results->at(i) |= SkUnicode::kTabulation;
                unichar = ' ';
                utf16[i] = ' ';
            }
            this->applyCharFlags(unichar, &results->at(i));
        }
        return true;
    }

    void reorderVisual(const BidiLevel runLevels[], int levelsCount,
                       int32_t logicalFromVisual[]) override {
        if (levelsCount == 0) {
            return;
        }
        SkASSERT(runLevels != nullptr);
        SkikoBidi::ReorderVisual(runLevels, levelsCount, logicalFromVisual);
    }

private:
    bool collectUtf8(const char* utf8, int utf8Units, SkikoSegmenter::Granularity granularity,
                     std::vector<Position>* results) {
        results->clear();
        std::vector<SkikoSegmenter::Boundary> boundaries;
        if (!SkikoSegmenter::SegmentUtf8(utf8, utf8Units, granularity, &boundaries)) {
            return false;
        }
        for (const auto& boundary : boundaries) {
            results->push_back(boundary.position);
        }
        return true;
    }

    void applyBreaks(const std::vector<SkikoSegmenter::Boundary>& lineBreaks,
                     const std::vector<SkikoSegmenter::Boundary>& graphemes,
                     int units,
                     TArray<SkUnicode::CodeUnitFlags, true>* results) {
        for (const auto& lineBreak : lineBreaks) {
            if (lineBreak.position > units) {
                continue;
            }
            if (lineBreak.flag & 1) {
                (*results)[lineBreak.position] |= CodeUnitFlags::kSoftLineBreakBefore;
            }
            if (lineBreak.flag & 2) {
                (*results)[lineBreak.position] |= CodeUnitFlags::kHardLineBreakBefore;
            }
        }
        for (const auto& grapheme : graphemes) {
            if (grapheme.position <= units) {
                (*results)[grapheme.position] |= CodeUnitFlags::kGraphemeStart;
            }
        }
    }

    void applyCharFlags(SkUnichar unichar, SkUnicode::CodeUnitFlags* flags) {
        if (this->isSpace(unichar)) {
            *flags |= SkUnicode::kPartOfIntraWordBreak;
        }
        if (this->isWhitespace(unichar)) {
            *flags |= SkUnicode::kPartOfWhiteSpaceBreak;
        }
        if (this->isControl(unichar)) {
            *flags |= SkUnicode::kControl;
        }
        if (this->isIdeographic(unichar)) {
            *flags |= SkUnicode::kIdeographic;
        }
    }
};

}  // namespace

namespace SkikoUnicode {

sk_sp<SkUnicode> Make() {
    if (!SkikoSegmenter::IsAvailable()) {
        return nullptr;
    }
    // One shared instance: the implementation is stateless.
    static sk_sp<SkUnicode> instance = sk_make_sp<SkikoUnicodeImpl>();
    return instance;
}

}  // namespace SkikoUnicode

#endif  // !SKIKO_USE_ICU
