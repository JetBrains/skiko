// Bridge between browser-native text segmentation (Intl.Segmenter) and Skia's
// SkUnicode_client implementation.
//
// The Kotlin side computes grapheme/word/line boundaries in a single JS pass and
// hands them over as flat Int32 buffers with UTF-16 code unit indices. Here they are
// translated into the UTF-8 byte offsets expected by SkUnicodes::Client::Make().
//
// ICU stays linked in the binary: SkUnicode_client delegates BiDi resolution (UAX #9)
// to the embedded ICU bidi implementation and uses hardcoded character properties,
// so mixed-direction text keeps working.
#include <string>
#include <vector>

#include "modules/skunicode/include/SkUnicode.h"
#include "modules/skunicode/include/SkUnicode_client.h"
#include "src/core/SkUTF.h"

#include "SkikoUnicodeHolder.hh"
#include "common.h"

static void deleteSkUnicodeHolder(SkikoUnicodeHolder* instance) {
    delete instance;
}

// Converts UTF-16 text to UTF-8 and fills utf16ToUtf8 with the UTF-8 byte offset of
// every UTF-16 code unit. The map has textLength + 1 entries, the last one being the
// total UTF-8 length.
static std::string utf16ToUtf8WithMap(
    const uint16_t* utf16Text,
    int textLength,
    std::vector<int>* utf16ToUtf8
) {
    std::string utf8;
    utf8.reserve(static_cast<size_t>(textLength) * 3 / 2 + 1);
    utf16ToUtf8->assign(static_cast<size_t>(textLength) + 1, 0);

    const uint16_t* current = utf16Text;
    const uint16_t* begin = current;
    const uint16_t* end = current + textLength;
    char buffer[SkUTF::kMaxBytesInUTF8Sequence];
    while (current < end) {
        int32_t unitIndex = static_cast<int32_t>(current - begin);
        (*utf16ToUtf8)[unitIndex] = static_cast<int>(utf8.size());
        SkUnichar unichar = SkUTF::NextUTF16(&current, end);
        if (unichar < 0) unichar = 0xFFFD;
        size_t written = SkUTF::ToUTF8(unichar, buffer);
        // Surrogate pairs occupy two UTF-16 code units: the trailing one maps to the
        // very same UTF-8 offset as the leading one.
        for (int32_t i = unitIndex + 1; i < static_cast<int32_t>(current - begin); ++i) {
            (*utf16ToUtf8)[i] = static_cast<int>(utf8.size());
        }
        utf8.append(buffer, written);
    }
    (*utf16ToUtf8)[textLength] = static_cast<int>(utf8.size());
    return utf8;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_SkUnicodeClient__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>(&deleteSkUnicodeHolder);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_SkUnicodeClient__1nCreate(
    KInteropPointer utf16TextPtr,
    KInt textLength,
    KInteropPointer graphemesPtr,
    KInt graphemeCount,
    KInteropPointer wordsPtr,
    KInt wordCount,
    KInteropPointer lineBreaksPtr,
    KInt lineBreakCount
) {
    const uint16_t* utf16Text = reinterpret_cast<const uint16_t*>(utf16TextPtr);
    const int* graphemes = reinterpret_cast<const int*>(graphemesPtr);
    const int* words = reinterpret_cast<const int*>(wordsPtr);
    const int* lineBreaks = reinterpret_cast<const int*>(lineBreaksPtr);

    if (textLength < 0) textLength = 0;

    std::vector<int> utf16ToUtf8;
    SkikoUnicodeHolder* holder = new SkikoUnicodeHolder();
    holder->text8 = utf16ToUtf8WithMap(utf16Text, textLength, &utf16ToUtf8);
    const int utf8Length = static_cast<int>(holder->text8.size());

    auto toUtf8 = [&](int utf16Index) -> int {
        if (utf16Index <= 0) return 0;
        if (utf16Index >= textLength) return utf8Length;
        return utf16ToUtf8[static_cast<size_t>(utf16Index)];
    };

    std::vector<SkUnicode::Position> graphemeBreaks;
    graphemeBreaks.reserve(static_cast<size_t>(graphemeCount));
    for (int i = 0; i < graphemeCount; ++i) {
        graphemeBreaks.push_back(static_cast<SkUnicode::Position>(toUtf8(graphemes[i])));
    }

    // Incoming layout: [start, end, isWordLike, ...]. The client implementation expects
    // a flat list of word boundary positions.
    std::vector<SkUnicode::Position> wordPositions;
    wordPositions.reserve(static_cast<size_t>(wordCount) / 3 * 2 + 1);
    for (int i = 0; i + 2 < wordCount; i += 3) {
        const int start = toUtf8(words[i]);
        const int end = toUtf8(words[i + 1]);
        if (wordPositions.empty() || wordPositions.back() != static_cast<SkUnicode::Position>(start)) {
            wordPositions.push_back(static_cast<SkUnicode::Position>(start));
        }
        wordPositions.push_back(static_cast<SkUnicode::Position>(end));
    }

    // Incoming layout: [index, breakType, ...], breakType: 0 = soft, 1 = hard.
    std::vector<SkUnicode::LineBreakBefore> lineBreakBefores;
    lineBreakBefores.reserve(static_cast<size_t>(lineBreakCount));
    bool hasBreakAtEnd = false;
    for (int i = 0; i + 1 < lineBreakCount; i += 2) {
        const int position = toUtf8(lineBreaks[i]);
        const auto breakType = lineBreaks[i + 1] == 0
            ? SkUnicode::LineBreakType::kSoftLineBreak
            : SkUnicode::LineBreakType::kHardLineBreak;
        if (position == utf8Length) hasBreakAtEnd = true;
        lineBreakBefores.emplace_back(static_cast<SkUnicode::Position>(position), breakType);
    }
    // ICU always reports a break opportunity right past the last code unit and
    // SkParagraph relies on it.
    if (!hasBreakAtEnd) {
        lineBreakBefores.emplace_back(
            static_cast<SkUnicode::Position>(utf8Length),
            SkUnicode::LineBreakType::kHardLineBreak
        );
    }

    holder->unicode = SkUnicodes::Client::Make(
        SkSpan<char>(holder->text8.data(), holder->text8.size()),
        std::move(wordPositions),
        std::move(graphemeBreaks),
        std::move(lineBreakBefores)
    );

    if (holder->unicode == nullptr) {
        delete holder;
        return nullptr;
    }
    return reinterpret_cast<KNativePointer>(holder);
}
