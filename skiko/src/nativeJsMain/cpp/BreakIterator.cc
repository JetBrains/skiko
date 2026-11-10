#include "common.h"
#include <iostream>

#ifdef SKIKO_USE_ICU

#include "unicode/ubrk.h"

static void deleteBreakIterator(UBreakIterator* instance) {
  ubrk_close(instance);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_BreakIterator__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>((&deleteBreakIterator));
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_BreakIterator__1nMake
  (KInt type, KInteropPointer localeStr, KInt* errorCode) {
    UErrorCode status = U_ZERO_ERROR;
    UBreakIterator* instance;
    if (localeStr == nullptr)
      instance = ubrk_open(static_cast<UBreakIteratorType>(type), uloc_getDefault(), nullptr, 0, &status);
    else {
      instance = ubrk_open(static_cast<UBreakIteratorType>(type), skString(localeStr).c_str(), nullptr, 0, &status);
    }

    errorCode[0] = status;
    if (U_FAILURE(status))
      return 0;
    else
      return reinterpret_cast<KNativePointer>(instance);
}


SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nCurrent
  (KNativePointer ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(ptr);
    return ubrk_current(instance);
}

SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nNext
  (KNativePointer ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(ptr);
    return ubrk_next(instance);
}

SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nPrevious
  (KNativePointer ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(ptr);
    return ubrk_previous(instance);
}

SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nFirst
  (KNativePointer ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(ptr);
    return ubrk_first(instance);
}

SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nLast
  (KNativePointer ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(ptr);
    return ubrk_last(instance);
}

SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nPreceding
  (KNativePointer ptr, KInt offset) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(ptr);
    return ubrk_preceding(instance, offset);
}

SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nFollowing
  (KNativePointer ptr, KInt offset) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(ptr);
    return ubrk_following(instance, offset);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_BreakIterator__1nIsBoundary
  (KNativePointer ptr, KInt offset) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(ptr);
    return ubrk_isBoundary(instance, offset);
}

SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nGetRuleStatus
  (KNativePointer ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(ptr);
    return ubrk_getRuleStatus(instance);
}


SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nGetRuleStatusesLen
  (KNativePointer ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(ptr);
    UErrorCode status = U_ZERO_ERROR;
    int32_t len = ubrk_getRuleStatusVec(instance, nullptr, 0, &status);
    SKIKO_ASSERT(status == U_BUFFER_OVERFLOW_ERROR || !U_FAILURE(status), "Failed to get rule statuses");
    return len;
}

SKIKO_EXPORT void org_jetbrains_skia_BreakIterator__1nGetRuleStatuses
  (KNativePointer ptr, KInteropPointer result, KInt len) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(ptr);
    UErrorCode status = U_ZERO_ERROR;
    ubrk_getRuleStatusVec(instance, reinterpret_cast<int32_t*>(result), len, &status);
    SKIKO_ASSERT(!U_FAILURE(status), "Failed to get rule statuses");
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_BreakIterator__1nSetText
  (KNativePointer ptr, KChar* textArr, KInt len, KInt* errorCode) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(ptr);
    std::vector<UChar>* text = new std::vector<UChar>(textArr, textArr + len);

    UErrorCode status = U_ZERO_ERROR;
    ubrk_setText(instance, text->data(), len, &status);
    errorCode[0] = status;
    return reinterpret_cast<KNativePointer>(text);
}
#else

// ICU is excluded from this binary, so the ICU break iterator subsystem is not
// available. The exported symbols are kept (the JS glue code references them),
// but they report a failure so that the Kotlin side can fall back to
// `WebBreakIteratorProvider`, which is based on the browser `Intl.Segmenter`.
//
// U_UNSUPPORTED_ERROR of ICU's UErrorCode enum.
static const KInt kUnsupportedError = 16;

SKIKO_EXPORT KNativePointer org_jetbrains_skia_BreakIterator__1nGetFinalizer() {
    return nullptr;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_BreakIterator__1nMake
  (KInt type, KInteropPointer localeStr, KInt* errorCode) {
    errorCode[0] = kUnsupportedError;
    return nullptr;
}

SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nCurrent(KNativePointer ptr) { return -1; }
SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nNext(KNativePointer ptr) { return -1; }
SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nPrevious(KNativePointer ptr) { return -1; }
SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nFirst(KNativePointer ptr) { return -1; }
SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nLast(KNativePointer ptr) { return -1; }
SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nPreceding(KNativePointer ptr, KInt offset) { return -1; }
SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nFollowing(KNativePointer ptr, KInt offset) { return -1; }
SKIKO_EXPORT KBoolean org_jetbrains_skia_BreakIterator__1nIsBoundary(KNativePointer ptr, KInt offset) { return false; }
SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nGetRuleStatus(KNativePointer ptr) { return 0; }
SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nGetRuleStatusesLen(KNativePointer ptr) { return 0; }

SKIKO_EXPORT void org_jetbrains_skia_BreakIterator__1nGetRuleStatuses
  (KNativePointer ptr, KInteropPointer result, KInt len) {
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_BreakIterator__1nSetText
  (KNativePointer ptr, KChar* textArr, KInt len, KInt* errorCode) {
    errorCode[0] = kUnsupportedError;
    return nullptr;
}

#endif  // SKIKO_USE_ICU
