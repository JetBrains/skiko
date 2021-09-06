
// This file has been auto generated.

#include "unicode/ubrk.h"
#include "common.h"

static void deleteBreakIterator(UBreakIterator* instance) {
  ubrk_close(instance);
}

extern "C" jlong org_jetbrains_skia_BreakIterator__1nGetFinalizer(kref __Kinstance) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteBreakIterator));
}


extern "C" jlong org_jetbrains_skia_BreakIterator__1nMake
  (kref __Kinstance, jint type, jstring localeStr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_BreakIterator__1nMake");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_BreakIterator__1nMake
  (kref __Kinstance, jint type, jstring localeStr) {
    UErrorCode status = U_ZERO_ERROR;
    UBreakIterator* instance;
    if (localeStr == nullptr)
      instance = ubrk_open(static_cast<UBreakIteratorType>(type), uloc_getDefault(), nullptr, 0, &status);
    else {
      SkString locale = skString(env, localeStr);
      instance = ubrk_open(static_cast<UBreakIteratorType>(type), locale.c_str(), nullptr, 0, &status);
    }
    
    if (U_FAILURE(status)) {
      env->ThrowNew(java::lang::RuntimeException::cls, u_errorName(status));
      return 0;
    } else
      return reinterpret_cast<jlong>(instance);
}
#endif



extern "C" jint org_jetbrains_skia_BreakIterator__1nClone
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_BreakIterator__1nClone");
}
     
#if 0 
extern "C" jint org_jetbrains_skia_BreakIterator__1nClone
  (kref __Kinstance, jlong ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(static_cast<uintptr_t>(ptr));
    UErrorCode status = U_ZERO_ERROR;
    UBreakIterator* clone = ubrk_safeClone(instance, nullptr, 0, &status);
    if (U_FAILURE(status)) {
      env->ThrowNew(java::lang::RuntimeException::cls, u_errorName(status));
      return 0;
    } else
      return reinterpret_cast<jlong>(clone);
}
#endif


extern "C" jint org_jetbrains_skia_BreakIterator__1nCurrent
  (kref __Kinstance, jlong ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(static_cast<uintptr_t>(ptr));
    return ubrk_current(instance);
}

extern "C" jint org_jetbrains_skia_BreakIterator__1nNext
  (kref __Kinstance, jlong ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(static_cast<uintptr_t>(ptr));
    return ubrk_next(instance);
}

extern "C" jint org_jetbrains_skia_BreakIterator__1nPrevious
  (kref __Kinstance, jlong ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(static_cast<uintptr_t>(ptr));
    return ubrk_previous(instance);
}

extern "C" jint org_jetbrains_skia_BreakIterator__1nFirst
  (kref __Kinstance, jlong ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(static_cast<uintptr_t>(ptr));
    return ubrk_first(instance);
}

extern "C" jint org_jetbrains_skia_BreakIterator__1nLast
  (kref __Kinstance, jlong ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(static_cast<uintptr_t>(ptr));
    return ubrk_last(instance);
}

extern "C" jint org_jetbrains_skia_BreakIterator__1nPreceding
  (kref __Kinstance, jlong ptr, jint offset) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(static_cast<uintptr_t>(ptr));
    return ubrk_preceding(instance, offset);
}

extern "C" jint org_jetbrains_skia_BreakIterator__1nFollowing
  (kref __Kinstance, jlong ptr, jint offset) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(static_cast<uintptr_t>(ptr));
    return ubrk_following(instance, offset);
}

extern "C" jboolean org_jetbrains_skia_BreakIterator__1nIsBoundary
  (kref __Kinstance, jlong ptr, jint offset) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(static_cast<uintptr_t>(ptr));
    return ubrk_isBoundary(instance, offset);
}

extern "C" jint org_jetbrains_skia_BreakIterator__1nGetRuleStatus
  (kref __Kinstance, jlong ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(static_cast<uintptr_t>(ptr));
    return ubrk_getRuleStatus(instance);
}


extern "C" jintArray org_jetbrains_skia_BreakIterator__1nGetRuleStatuses
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_BreakIterator__1nGetRuleStatuses");
}
     
#if 0 
extern "C" jintArray org_jetbrains_skia_BreakIterator__1nGetRuleStatuses
  (kref __Kinstance, jlong ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(static_cast<uintptr_t>(ptr));
    UErrorCode status = U_ZERO_ERROR;
    int32_t len = ubrk_getRuleStatusVec(instance, nullptr, 0, &status);
    if (U_FAILURE(status))
      env->ThrowNew(java::lang::RuntimeException::cls, u_errorName(status));
    std::vector<jint> vec(len);
    ubrk_getRuleStatusVec(instance, reinterpret_cast<int32_t*>(vec.data()), len, &status);
    if (U_FAILURE(status))
      env->ThrowNew(java::lang::RuntimeException::cls, u_errorName(status));
    return javaIntArray(env, vec);
}
#endif



extern "C" void org_jetbrains_skia_BreakIterator__1nSetText
  (kref __Kinstance, jlong ptr, jlong textPtr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_BreakIterator__1nSetText");
}
     
#if 0 
extern "C" void org_jetbrains_skia_BreakIterator__1nSetText
  (kref __Kinstance, jlong ptr, jlong textPtr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(static_cast<uintptr_t>(ptr));
    std::vector<jchar>* text = reinterpret_cast<std::vector<jchar>*>(static_cast<uintptr_t>(textPtr));
    UErrorCode status = U_ZERO_ERROR;
    ubrk_setText(instance, reinterpret_cast<UChar *>(text->data()), text->size(), &status);
    if (U_FAILURE(status))
      env->ThrowNew(java::lang::RuntimeException::cls, u_errorName(status));
}
#endif

