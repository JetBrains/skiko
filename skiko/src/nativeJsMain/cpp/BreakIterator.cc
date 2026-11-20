#include "common.h"
#include "SkikoUnicode.hh"

static void deleteBreakIterator(SkBreakIterator* instance) {
    delete instance;
}

static SkUnicode::BreakType toBreakType(KInt type) {
    // Must match org.jetbrains.skia.BreakIterator.make*Instance
    switch (type) {
        case 0: return SkUnicode::BreakType::kGraphemes;
        case 1: return SkUnicode::BreakType::kWords;
        case 2: return SkUnicode::BreakType::kLines;
        case 3: return SkUnicode::BreakType::kSentences;
        default: TODO("Unsupported BreakIterator type");
    }
}

static std::unique_ptr<SkBreakIterator> makeBreakIterator(KInt type, const char* locale) {
    sk_sp<SkUnicode> unicode = skiko::BestAvailableUnicode();
    if (!unicode) {
        return nullptr;
    }
    return unicode->makeBreakIterator(locale, toBreakType(type));
}

static inline SkBreakIterator* toBreakIterator(KNativePointer ptr) {
    return reinterpret_cast<SkBreakIterator*>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_BreakIterator__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>((&deleteBreakIterator));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_BreakIterator__1nMake
  (KInt type, KInteropPointer localeStr) {
    SkString locale = localeStr == nullptr ? SkString() : skString(localeStr);
    std::unique_ptr<SkBreakIterator> instance =
        makeBreakIterator(type, locale.isEmpty() ? nullptr : locale.c_str());
    return reinterpret_cast<KNativePointer>(instance.release());
}

SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nCurrent
  (KNativePointer ptr) {
    return toBreakIterator(ptr)->current();
}

SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nNext
  (KNativePointer ptr) {
    SkBreakIterator* instance = toBreakIterator(ptr);
    SkBreakIterator::Position result = instance->next();
    if (result == SkBreakIterator::kDone) {
        // The Apple backend leaves its index past the last break when iteration is done. Move it
        // back to the last boundary, like ICU, where the position stays valid when DONE is
        // returned.
        instance->last();
    }
    return result;
}

SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nPrevious
  (KNativePointer ptr) {
    return toBreakIterator(ptr)->previous();
}

SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nFirst
  (KNativePointer ptr) {
    return toBreakIterator(ptr)->first();
}

SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nLast
  (KNativePointer ptr) {
    return toBreakIterator(ptr)->last();
}

SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nPreceding
  (KNativePointer ptr, KInt offset) {
    return toBreakIterator(ptr)->preceding(offset);
}

SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nFollowing
  (KNativePointer ptr, KInt offset) {
    return toBreakIterator(ptr)->following(offset);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_BreakIterator__1nIsBoundary
  (KNativePointer ptr, KInt offset) {
    return toBreakIterator(ptr)->isBoundary(offset);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_BreakIterator__1nSetText
  (KNativePointer ptr, KChar* textArr, KInt len) {
    SkBreakIterator* instance = toBreakIterator(ptr);
    // The buffer must stay alive while the iterator is used: ICU's UText reads it lazily.
    // Kotlin keeps it alive in org.jetbrains.skia.U16String and frees it on the next setText
    // or on close.
    auto text = std::make_unique<std::vector<KChar>>(textArr, textArr + len);
    if (!instance->setText(reinterpret_cast<const char16_t*>(text->data()), len)) {
        return 0;
    }
    return reinterpret_cast<KNativePointer>(text.release());
}
