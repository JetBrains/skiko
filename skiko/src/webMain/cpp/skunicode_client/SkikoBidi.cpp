#ifndef SKIKO_USE_ICU

#include "SkikoBidi.h"

#include <algorithm>

#include "src/core/SkUTF.h"

namespace {

enum Class : uint8_t {
    kL, kR, kAL,
    kEN, kES, kET, kAN, kCS, kNSM, kBN,
    kB, kS, kWS, kON,
    kLRE, kRLE, kLRO, kRLO, kPDF,
    kLRI, kRLI, kFSI, kPDI,
};

struct Range {
    int32_t first;
    int32_t last;
    Class cls;
};

// Ranges are checked in order; the first match wins.
const Range kRanges[] = {
    // Explicit formatting characters.
    {0x202A, 0x202A, kLRE}, {0x202B, 0x202B, kRLE}, {0x202C, 0x202C, kPDF},
    {0x202D, 0x202D, kLRO}, {0x202E, 0x202E, kRLO},
    {0x2066, 0x2066, kLRI}, {0x2067, 0x2067, kRLI}, {0x2068, 0x2068, kFSI},
    {0x2069, 0x2069, kPDI},

    // Paragraph and segment separators, whitespace, boundary neutrals.
    {0x0000, 0x0008, kBN}, {0x0009, 0x0009, kS}, {0x000A, 0x000A, kB},
    {0x000B, 0x000B, kS}, {0x000C, 0x000C, kWS}, {0x000D, 0x000D, kB},
    {0x000E, 0x001B, kBN}, {0x001C, 0x001E, kB}, {0x001F, 0x001F, kS},
    {0x0020, 0x0020, kWS},
    {0x007F, 0x0084, kBN}, {0x0085, 0x0085, kB}, {0x0086, 0x009F, kBN},
    {0x00AD, 0x00AD, kBN},
    {0x200B, 0x200D, kBN}, {0x2028, 0x2028, kWS}, {0x2029, 0x2029, kB},
    {0x2060, 0x2064, kBN}, {0x206A, 0x206F, kBN},
    {0x1680, 0x1680, kWS}, {0x2000, 0x200A, kWS}, {0x205F, 0x205F, kWS},
    {0x3000, 0x3000, kWS}, {0xFEFF, 0xFEFF, kBN},

    // European numbers and number related classes.
    {0x0023, 0x0025, kET}, {0x002B, 0x002B, kES}, {0x002C, 0x002C, kCS},
    {0x002D, 0x002D, kES}, {0x002E, 0x002F, kCS}, {0x0030, 0x0039, kEN},
    {0x003A, 0x003A, kCS},
    {0x00A0, 0x00A0, kCS}, {0x00A2, 0x00A5, kET}, {0x00B0, 0x00B1, kET},
    {0x00B2, 0x00B3, kEN}, {0x00B9, 0x00B9, kEN},
    {0x2070, 0x2070, kEN}, {0x2074, 0x2079, kEN}, {0x2080, 0x2089, kEN},
    {0x207A, 0x207B, kES}, {0x208A, 0x208B, kES}, {0x2212, 0x2212, kES},
    {0x2030, 0x2034, kET}, {0x20A0, 0x20CF, kET}, {0x2044, 0x2044, kCS},
    {0xFE62, 0xFE63, kES}, {0xFF0B, 0xFF0B, kES}, {0xFF0D, 0xFF0D, kES},
    {0xFF10, 0xFF19, kEN}, {0xFF03, 0xFF05, kET},
    {0xFF0C, 0xFF0C, kCS}, {0xFF0E, 0xFF0F, kCS}, {0xFF1A, 0xFF1A, kCS},

    // Arabic numbers.
    {0x0600, 0x0605, kAN}, {0x0660, 0x0669, kAN}, {0x066B, 0x066C, kAN},
    {0x066A, 0x066A, kET}, {0x06DD, 0x06DD, kAN}, {0x0890, 0x0891, kAN},
    {0x08E2, 0x08E2, kAN}, {0x10E60, 0x10E7E, kAN},
    {0x060C, 0x060C, kCS},
    {0x06F0, 0x06F9, kEN},

    // Non spacing marks (the most common blocks).
    {0x0300, 0x036F, kNSM}, {0x0483, 0x0489, kNSM},
    {0x0591, 0x05BD, kNSM}, {0x05BF, 0x05BF, kNSM}, {0x05C1, 0x05C2, kNSM},
    {0x05C4, 0x05C5, kNSM}, {0x05C7, 0x05C7, kNSM},
    {0x0610, 0x061A, kNSM}, {0x064B, 0x065F, kNSM}, {0x0670, 0x0670, kNSM},
    {0x06D6, 0x06DC, kNSM}, {0x06DF, 0x06E4, kNSM}, {0x06E7, 0x06E8, kNSM},
    {0x06EA, 0x06ED, kNSM}, {0x0711, 0x0711, kNSM}, {0x0730, 0x074A, kNSM},
    {0x07A6, 0x07B0, kNSM}, {0x07EB, 0x07F3, kNSM}, {0x0816, 0x082D, kNSM},
    {0x0859, 0x085B, kNSM}, {0x08D3, 0x0902, kNSM},
    {0x1AB0, 0x1AFF, kNSM}, {0x1DC0, 0x1DFF, kNSM}, {0x20D0, 0x20F0, kNSM},
    {0xFE00, 0xFE0F, kNSM}, {0xFE20, 0xFE2F, kNSM},

    // Right to left scripts.
    {0x0590, 0x05FF, kR}, {0x07C0, 0x085F, kR}, {0xFB1D, 0xFB4F, kR},
    {0x10800, 0x10CFF, kR}, {0x10D40, 0x10EBF, kR}, {0x10F00, 0x10FFF, kR},
    {0x1E800, 0x1EC6F, kR},
    {0x0608, 0x0608, kAL}, {0x060B, 0x060B, kAL}, {0x0606, 0x065F, kAL},
    {0x0671, 0x08FF, kAL}, {0x08A0, 0x08FF, kAL},
    {0xFB50, 0xFDFF, kAL}, {0xFE70, 0xFEFE, kAL},
    {0x10D00, 0x10D3F, kAL}, {0x10EC0, 0x10EFF, kAL},
    {0x1EC70, 0x1ECBF, kAL}, {0x1ED00, 0x1ED4F, kAL}, {0x1EE00, 0x1EEFF, kAL},

    // Other neutrals (punctuation and symbols).
    {0x0021, 0x0022, kON}, {0x0026, 0x002A, kON}, {0x003B, 0x0040, kON},
    {0x005B, 0x0060, kON}, {0x007B, 0x007E, kON},
    {0x00A1, 0x00A1, kON}, {0x00A6, 0x00A9, kON}, {0x00AB, 0x00AC, kON},
    {0x00AE, 0x00AF, kON}, {0x00B4, 0x00B4, kON}, {0x00B6, 0x00B8, kON},
    {0x00BB, 0x00BF, kON}, {0x00D7, 0x00D7, kON}, {0x00F7, 0x00F7, kON},
    {0x2010, 0x2027, kON}, {0x2035, 0x2043, kON}, {0x2045, 0x205E, kON},
    {0x2065, 0x2065, kON}, {0x2075, 0x2079, kON},
    {0x2100, 0x2BFF, kON}, {0x2E00, 0x2E7F, kON},
    {0x3001, 0x3020, kON}, {0x3030, 0x303F, kON},
    {0xFE30, 0xFE52, kON}, {0xFE54, 0xFE61, kON}, {0xFE64, 0xFE6B, kON},
    {0xFF01, 0xFF02, kON}, {0xFF06, 0xFF0A, kON}, {0xFF1B, 0xFF20, kON},
    {0xFF3B, 0xFF40, kON}, {0xFF5B, 0xFF65, kON},
};

Class classify(int32_t codepoint) {
    for (const Range& range : kRanges) {
        if (codepoint >= range.first && codepoint <= range.last) {
            return range.cls;
        }
    }
    return kL;
}

bool isIsolateInitiator(Class cls) {
    return cls == kLRI || cls == kRLI || cls == kFSI;
}

bool isRemovedByX9(Class cls) {
    return cls == kLRE || cls == kRLE || cls == kLRO || cls == kRLO ||
           cls == kPDF || cls == kBN;
}

bool isStrong(Class cls) {
    return cls == kL || cls == kR || cls == kAL;
}

// Rules P2-P3: the paragraph level is defined by the first strong character,
// skipping the characters between an isolate initiator and its matching PDI.
uint8_t paragraphLevel(const std::vector<Class>& classes) {
    int isolates = 0;
    for (Class cls : classes) {
        if (isIsolateInitiator(cls)) {
            ++isolates;
        } else if (cls == kPDI) {
            if (isolates > 0) --isolates;
        } else if (isolates == 0) {
            if (cls == kL) return 0;
            if (cls == kR || cls == kAL) return 1;
        }
    }
    return 0;
}

constexpr int kMaxDepth = 125;

// Rules X1-X8. Isolate initiators and PDI are kept as neutrals on the current
// level, which is a simplification of the full isolate handling.
void resolveExplicitLevels(const std::vector<Class>& classes,
                           uint8_t paraLevel,
                           std::vector<uint8_t>* levels,
                           std::vector<Class>* resolved) {
    struct Entry {
        uint8_t level;
        Class overrideType;  // kON means "neutral", i.e. no override
    };
    std::vector<Entry> stack;
    stack.push_back({paraLevel, kON});

    const size_t count = classes.size();
    levels->assign(count, paraLevel);
    *resolved = classes;

    int overflow = 0;
    for (size_t i = 0; i < count; ++i) {
        const Class cls = classes[i];
        switch (cls) {
            case kRLE:
            case kRLO:
            case kLRE:
            case kLRO: {
                const bool rtl = (cls == kRLE || cls == kRLO);
                const uint8_t current = stack.back().level;
                const uint8_t next = rtl ? (uint8_t)((current + 1) | 1)
                                         : (uint8_t)((current + 2) & ~1);
                (*levels)[i] = current;
                if (next <= kMaxDepth && overflow == 0 && stack.size() < kMaxDepth) {
                    Class overrideType = kON;
                    if (cls == kRLO) overrideType = kR;
                    if (cls == kLRO) overrideType = kL;
                    stack.push_back({next, overrideType});
                } else {
                    ++overflow;
                }
                break;
            }
            case kPDF: {
                (*levels)[i] = stack.back().level;
                if (overflow > 0) {
                    --overflow;
                } else if (stack.size() > 1) {
                    stack.pop_back();
                }
                break;
            }
            case kB: {
                stack.clear();
                stack.push_back({paraLevel, kON});
                overflow = 0;
                (*levels)[i] = paraLevel;
                break;
            }
            default: {
                const Entry& top = stack.back();
                (*levels)[i] = top.level;
                if (top.overrideType != kON) {
                    (*resolved)[i] = top.overrideType;
                }
                break;
            }
        }
    }
}

// Rules W1-W7, N1-N2 and I1-I2 applied to a single level run.
void resolveWeakAndNeutralTypes(std::vector<Class>* types,
                                std::vector<uint8_t>* levels,
                                const std::vector<size_t>& indices,
                                uint8_t runLevel,
                                Class sos,
                                Class eos) {
    const size_t length = indices.size();
    if (length == 0) {
        return;
    }
    std::vector<Class> run(length);
    for (size_t i = 0; i < length; ++i) {
        run[i] = (*types)[indices[i]];
    }

    // W1: NSM takes the type of the previous character.
    Class previous = sos;
    for (size_t i = 0; i < length; ++i) {
        if (run[i] == kNSM) {
            run[i] = isIsolateInitiator(previous) || previous == kPDI ? kON : previous;
        }
        previous = run[i];
    }

    // W2: EN becomes AN when the last strong type is AL.
    Class lastStrong = sos;
    for (size_t i = 0; i < length; ++i) {
        if (isStrong(run[i])) {
            lastStrong = run[i];
        } else if (run[i] == kEN && lastStrong == kAL) {
            run[i] = kAN;
        }
    }

    // W3: AL becomes R.
    for (size_t i = 0; i < length; ++i) {
        if (run[i] == kAL) run[i] = kR;
    }

    // W4: a single ES between two EN, or a single CS between two numbers of the
    // same type, becomes that number type.
    for (size_t i = 1; i + 1 < length; ++i) {
        if (run[i] == kES && run[i - 1] == kEN && run[i + 1] == kEN) {
            run[i] = kEN;
        } else if (run[i] == kCS && run[i - 1] == run[i + 1] &&
                   (run[i - 1] == kEN || run[i - 1] == kAN)) {
            run[i] = run[i - 1];
        }
    }

    // W5: a sequence of ET adjacent to EN becomes EN.
    for (size_t i = 0; i < length; ++i) {
        if (run[i] != kET) continue;
        size_t end = i;
        while (end < length && run[end] == kET) ++end;
        const bool before = i > 0 && run[i - 1] == kEN;
        const bool after = end < length && run[end] == kEN;
        if (before || after) {
            for (size_t j = i; j < end; ++j) run[j] = kEN;
        }
        i = end - 1;
    }

    // W6: remaining separators and terminators become ON.
    for (size_t i = 0; i < length; ++i) {
        if (run[i] == kES || run[i] == kET || run[i] == kCS) run[i] = kON;
    }

    // W7: EN becomes L when the last strong type is L.
    lastStrong = sos;
    for (size_t i = 0; i < length; ++i) {
        if (run[i] == kL || run[i] == kR) {
            lastStrong = run[i];
        } else if (run[i] == kEN && lastStrong == kL) {
            run[i] = kL;
        }
    }

    // N1-N2: resolve the remaining neutrals.
    const Class embedding = (runLevel & 1) ? kR : kL;
    auto strongDirection = [](Class cls) {
        if (cls == kL) return kL;
        if (cls == kR || cls == kEN || cls == kAN) return kR;
        return kON;
    };
    for (size_t i = 0; i < length; ++i) {
        const Class cls = run[i];
        const bool neutral = cls == kON || cls == kWS || cls == kS || cls == kB ||
                             isIsolateInitiator(cls) || cls == kPDI;
        if (!neutral) continue;
        size_t end = i;
        while (end < length) {
            const Class c = run[end];
            const bool n = c == kON || c == kWS || c == kS || c == kB ||
                           isIsolateInitiator(c) || c == kPDI;
            if (!n) break;
            ++end;
        }
        const Class before = i > 0 ? strongDirection(run[i - 1]) : strongDirection(sos);
        const Class after = end < length ? strongDirection(run[end]) : strongDirection(eos);
        const Class resolvedType =
            (before == after && before != kON) ? before : embedding;
        for (size_t j = i; j < end; ++j) run[j] = resolvedType;
        i = end - 1;
    }

    // I1-I2: compute the implicit levels.
    for (size_t i = 0; i < length; ++i) {
        uint8_t level = runLevel;
        if ((runLevel & 1) == 0) {
            if (run[i] == kR) level = runLevel + 1;
            else if (run[i] == kAN || run[i] == kEN) level = runLevel + 2;
        } else {
            if (run[i] == kL || run[i] == kAN || run[i] == kEN) level = runLevel + 1;
        }
        (*levels)[indices[i]] = level;
        (*types)[indices[i]] = run[i];
    }
}

}  // namespace

namespace SkikoBidi {

static void resolveLevels(const std::vector<Class>& rawClasses,
                          Direction direction,
                          std::vector<uint8_t>* outLevels) {
    const size_t count = rawClasses.size();
    if (count == 0) {
        outLevels->clear();
        return;
    }

    uint8_t paraLevel;
    switch (direction) {
        case Direction::kLTR: paraLevel = 0; break;
        case Direction::kRTL: paraLevel = 1; break;
        default: paraLevel = paragraphLevel(rawClasses); break;
    }

    std::vector<uint8_t> levels;
    std::vector<Class> types;
    resolveExplicitLevels(rawClasses, paraLevel, &levels, &types);

    // X9: characters removed by the explicit rules take the level of the
    // preceding character, so that they never split a level run.
    for (size_t i = 0; i < count; ++i) {
        if (isRemovedByX9(types[i])) {
            levels[i] = i > 0 ? levels[i - 1] : paraLevel;
        }
    }

    // Split into level runs and resolve each of them independently.
    size_t start = 0;
    while (start < count) {
        while (start < count && isRemovedByX9(types[start])) ++start;
        if (start >= count) break;
        const uint8_t runLevel = levels[start];
        std::vector<size_t> indices;
        size_t i = start;
        while (i < count && (isRemovedByX9(types[i]) || levels[i] == runLevel)) {
            if (!isRemovedByX9(types[i])) {
                indices.push_back(i);
            }
            ++i;
        }
        const size_t previous = indices.front();
        const size_t next = indices.back();
        const uint8_t previousLevel = previous > 0 ? levels[previous - 1] : paraLevel;
        const uint8_t nextLevel = next + 1 < count ? levels[next + 1] : paraLevel;
        const Class sos = (std::max(runLevel, previousLevel) & 1) ? kR : kL;
        const Class eos = (std::max(runLevel, nextLevel) & 1) ? kR : kL;
        resolveWeakAndNeutralTypes(&types, &levels, indices, runLevel, sos, eos);
        start = i;
    }

    // L1: segment separators, paragraph separators and any contiguous whitespace
    // preceding them (or ending the text) get the paragraph level.
    bool resetTrailing = true;
    for (size_t i = count; i-- > 0;) {
        const Class cls = rawClasses[i];
        if (cls == kB || cls == kS) {
            levels[i] = paraLevel;
            resetTrailing = true;
        } else if (resetTrailing &&
                   (cls == kWS || isRemovedByX9(cls) || isIsolateInitiator(cls) ||
                    cls == kPDI)) {
            levels[i] = paraLevel;
        } else {
            resetTrailing = false;
        }
    }

    *outLevels = std::move(levels);
}

void ResolveLevelsUtf16(const uint16_t* text,
                        int count,
                        Direction direction,
                        std::vector<uint8_t>* levels) {
    std::vector<Class> classes;
    std::vector<int> codeUnitsPerCodePoint;
    classes.reserve(count);
    codeUnitsPerCodePoint.reserve(count);

    const uint16_t* current = text;
    const uint16_t* end = text + count;
    while (current < end) {
        const uint16_t* before = current;
        SkUnichar unichar = SkUTF::NextUTF16(&current, end);
        if (unichar < 0) {
            unichar = 0xFFFD;
            current = before + 1;
        }
        classes.push_back(classify(unichar));
        codeUnitsPerCodePoint.push_back(static_cast<int>(current - before));
    }

    std::vector<uint8_t> codePointLevels;
    resolveLevels(classes, direction, &codePointLevels);

    levels->assign(static_cast<size_t>(count), 0);
    size_t position = 0;
    for (size_t i = 0; i < codePointLevels.size(); ++i) {
        for (int j = 0; j < codeUnitsPerCodePoint[i] && position < (size_t)count; ++j) {
            (*levels)[position++] = codePointLevels[i];
        }
    }
}

void ResolveLevelsUtf8(const char* text,
                       int utf8Units,
                       Direction direction,
                       std::vector<uint8_t>* levels) {
    std::vector<Class> classes;
    std::vector<int> bytesPerCodePoint;

    const char* current = text;
    const char* end = text + utf8Units;
    while (current < end) {
        const char* before = current;
        SkUnichar unichar = SkUTF::NextUTF8(&current, end);
        if (unichar < 0) {
            unichar = 0xFFFD;
            current = before + 1;
        }
        classes.push_back(classify(unichar));
        bytesPerCodePoint.push_back(static_cast<int>(current - before));
    }

    std::vector<uint8_t> codePointLevels;
    resolveLevels(classes, direction, &codePointLevels);

    levels->assign(static_cast<size_t>(utf8Units), 0);
    size_t position = 0;
    for (size_t i = 0; i < codePointLevels.size(); ++i) {
        for (int j = 0; j < bytesPerCodePoint[i] && position < (size_t)utf8Units; ++j) {
            (*levels)[position++] = codePointLevels[i];
        }
    }
}

void ReorderVisual(const uint8_t* runLevels, int levelsCount, int32_t* logicalFromVisual) {
    if (levelsCount <= 0) {
        return;
    }
    for (int i = 0; i < levelsCount; ++i) {
        logicalFromVisual[i] = i;
    }

    uint8_t highest = 0;
    uint8_t lowestOdd = 0xFF;
    for (int i = 0; i < levelsCount; ++i) {
        highest = std::max(highest, runLevels[i]);
        if (runLevels[i] & 1) {
            lowestOdd = std::min(lowestOdd, runLevels[i]);
        }
    }
    if (lowestOdd == 0xFF) {
        return;
    }

    for (int level = highest; level >= lowestOdd; --level) {
        int i = 0;
        while (i < levelsCount) {
            if (runLevels[logicalFromVisual[i]] < level) {
                ++i;
                continue;
            }
            int start = i;
            while (i < levelsCount && runLevels[logicalFromVisual[i]] >= level) {
                ++i;
            }
            std::reverse(logicalFromVisual + start, logicalFromVisual + i);
        }
    }
}

}  // namespace SkikoBidi

#endif  // !SKIKO_USE_ICU
