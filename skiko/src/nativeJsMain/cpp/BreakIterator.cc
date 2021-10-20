
// This file has been auto generated.

#include "unicode/ubrk.h"
#include "common.h"

static void deleteBreakIterator(UBreakIterator* instance) {
  ubrk_close(instance);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_BreakIterator__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>((&deleteBreakIterator));
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_BreakIterator__1nMake
  (KInt type, KInteropPointer localeStr) {
    TODO("implement org_jetbrains_skia_BreakIterator__1nMake");
}
     

SKIKO_EXPORT KInt org_jetbrains_skia_BreakIterator__1nClone
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_BreakIterator__1nClone");
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
  (KNativePointer ptr, KNativePointer textPtr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>((ptr));
    std::vector<KChar>* text = reinterpret_cast<std::vector<KChar>*>((textPtr));
    UErrorCode status = U_ZERO_ERROR;
    ubrk_setText(instance, reinterpret_cast<UChar *>(text->data()), text->size(), &status);
    if (U_FAILURE(status))
      env->ThrowNew(java::lang::RuntimeException::cls, u_errorName(status));
}
#endif

