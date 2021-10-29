
// This file has been auto generated.

#include <cstring>
#include "unicode/ubrk.h"
#include "common.h"

struct BreakIteratorResult {
    void* data;
    UErrorCode code;
}

SKIKO_EXPORT void org_jetbrains_skia_BreakIterator_Result_1nDelete(KNativePointer ptr) {
    delete reinterpret_cast<BreakIteratorResult*>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_BreakIterator_Result_1nGetData(KNativePointer ptr) {
    auto res = reinterpret_cast<BreakIteratorResult*>(ptr);
    return reinterpret_cast<KNativePointer>(ptr->data);
}

SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator_Result_1nGetErrorLength(KNativePointer ptr) {
    auto res = reinterpret_cast<BreakIteratorResult*>(ptr);
    if (U_FAILURE(res->code)) {
        return static_cast<KInt>(strlen(u_errorName(res->code));
    } else {
        return 0;
    }
}

SKIKO_EXPORT void org_jetbrains_skia_BreakIterator_Result_1nGetError(KNativePointer ptr, KInteropPointer dst, KInt count) {
    auto res = reinterpret_cast<BreakIteratorResult*>(ptr);
    auto dest = reinterpret_cast<char*>(dst);
    if (U_FAILURE(res->code)) {
        strncpy(dest, u_errorName(res->code), static_cast<std::size_t>(count));
    }
}

static void deleteBreakIterator(UBreakIterator* instance) {
    ubrk_close(instance);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_BreakIterator__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>((&deleteBreakIterator));
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_BreakIterator__1nMake
  (KInt type, KInteropPointer localeStr) {
    BreakIteratorResult res = new { nullptr, U_ZERO_ERROR };
    if (localeStr == nullptr)
      res->data = ubrk_open(static_cast<UBreakIteratorType>(type), uloc_getDefault(), nullptr, 0, &res->code);
    else {
      SkString locale = skString(localeStr);
      res->data = ubrk_open(static_cast<UBreakIteratorType>(type), locale.c_str(), nullptr, 0, &res->code);
    }

    return reinterpret_cast<KNativePointer>(res);
}
     

SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nClone
  (KNativePointer ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(ptr);
    BreakIteratorResult res = new { nullptr, U_ZERO_ERROR };
    res->data = ubrk_safeClone(instance, nullptr, 0, &res->code);
    return reinterpret_cast<KNativePointer>(res);
}


SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nCurrent
  (KNativePointer ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>((ptr));
    return ubrk_current(instance);
}

SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nNext
  (KNativePointer ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>((ptr));
    return ubrk_next(instance);
}

SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nPrevious
  (KNativePointer ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>((ptr));
    return ubrk_previous(instance);
}

SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nFirst
  (KNativePointer ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>((ptr));
    return ubrk_first(instance);
}

SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nLast
  (KNativePointer ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>((ptr));
    return ubrk_last(instance);
}

SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nPreceding
  (KNativePointer ptr, KInt offset) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>((ptr));
    return ubrk_preceding(instance, offset);
}

SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nFollowing
  (KNativePointer ptr, KInt offset) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>((ptr));
    return ubrk_following(instance, offset);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_BreakIterator__1nIsBoundary
  (KNativePointer ptr, KInt offset) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>((ptr));
    return ubrk_isBoundary(instance, offset);
}

SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nGetRuleStatus
  (KNativePointer ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>((ptr));
    return ubrk_getRuleStatus(instance);
}


     
#if 0 
SKIKO_EXPORT KInt* org_jetbrains_skia_BreakIterator__1nGetRuleStatuses
  (KNativePointer ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>((ptr));
    UErrorCode status = U_ZERO_ERROR;
    int32_t len = ubrk_getRuleStatusVec(instance, nullptr, 0, &status);
    if (U_FAILURE(status))
      env->ThrowNew(java::lang::RuntimeException::cls, u_errorName(status));
    std::vector<KInt> vec(len);
    ubrk_getRuleStatusVec(instance, reinterpret_cast<int32_t*>(vec.data()), len, &status);
    if (U_FAILURE(status))
      env->ThrowNew(java::lang::RuntimeException::cls, u_errorName(status));
    return javaIntArray(env, vec);
}
#endif



SKIKO_EXPORT void org_jetbrains_skia_BreakIterator__1nSetText
  (KNativePointer ptr, KNativePointer textPtr) {
    TODO("implement org_jetbrains_skia_BreakIterator__1nSetText");
}

#if 0
SKIKO_EXPORT void org_jetbrains_skia_BreakIterator__1nSetText
  (KNativePointer ptr, KInteropPointer textPtr, KInt size) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>((ptr));
    BreakIteratorResult res = new { nullptr, U_ZERO_ERROR };
    std::u16string* text = reinterpret_cast<std::u16string*>(textPtr);
    ubrk_setText(instance, text->data() , size, &res->code);
}
#endif

