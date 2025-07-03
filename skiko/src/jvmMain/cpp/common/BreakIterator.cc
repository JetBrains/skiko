#include <jni.h>
#include "interop.hh"
#include "unicode/ubrk.h"

static void deleteBreakIterator(UBreakIterator* instance) {
  ubrk_close(instance);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BreakIteratorExternalKt_BreakIterator_1nGetFinalizer(JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteBreakIterator));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BreakIteratorExternalKt_BreakIterator_1nMake
  (JNIEnv* env, jclass jclass, jint type, jstring localeStr, jintArray errorCode) {
    UErrorCode errorCodes[1] = { U_ZERO_ERROR };
    UBreakIterator* instance;
    if (localeStr == nullptr)
      instance = ubrk_open(static_cast<UBreakIteratorType>(type), uloc_getDefault(), nullptr, 0, errorCodes);
    else {
      SkString locale = skString(env, localeStr);
      instance = ubrk_open(static_cast<UBreakIteratorType>(type), locale.c_str(), nullptr, 0, errorCodes);
    }

    env->SetIntArrayRegion(errorCode, 0, 1, reinterpret_cast<jint*>(errorCodes));

    if (U_FAILURE(errorCodes[0])) {
      return 0;
    } else
      return reinterpret_cast<jlong>(instance);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BreakIteratorExternalKt_BreakIterator_1nCurrent
  (JNIEnv* env, jclass jclass, jlong ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(static_cast<uintptr_t>(ptr));
    return ubrk_current(instance);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BreakIteratorExternalKt_BreakIterator_1nNext
  (JNIEnv* env, jclass jclass, jlong ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(static_cast<uintptr_t>(ptr));
    return ubrk_next(instance);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BreakIteratorExternalKt_BreakIterator_1nPrevious
  (JNIEnv* env, jclass jclass, jlong ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(static_cast<uintptr_t>(ptr));
    return ubrk_previous(instance);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BreakIteratorExternalKt_BreakIterator_1nFirst
  (JNIEnv* env, jclass jclass, jlong ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(static_cast<uintptr_t>(ptr));
    return ubrk_first(instance);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BreakIteratorExternalKt_BreakIterator_1nLast
  (JNIEnv* env, jclass jclass, jlong ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(static_cast<uintptr_t>(ptr));
    return ubrk_last(instance);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BreakIteratorExternalKt_BreakIterator_1nPreceding
  (JNIEnv* env, jclass jclass, jlong ptr, jint offset) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(static_cast<uintptr_t>(ptr));
    return ubrk_preceding(instance, offset);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BreakIteratorExternalKt_BreakIterator_1nFollowing
  (JNIEnv* env, jclass jclass, jlong ptr, jint offset) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(static_cast<uintptr_t>(ptr));
    return ubrk_following(instance, offset);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BreakIteratorExternalKt_BreakIterator_1nIsBoundary
  (JNIEnv* env, jclass jclass, jlong ptr, jint offset) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(static_cast<uintptr_t>(ptr));
    return ubrk_isBoundary(instance, offset);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BreakIteratorExternalKt_BreakIterator_1nGetRuleStatus
  (JNIEnv* env, jclass jclass, jlong ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(static_cast<uintptr_t>(ptr));
    return ubrk_getRuleStatus(instance);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BreakIteratorExternalKt_BreakIterator_1nGetRuleStatusesLen
  (JNIEnv* env, jclass jclass, jlong ptr) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(static_cast<uintptr_t>(ptr));
    UErrorCode status = U_ZERO_ERROR;
    int32_t len = ubrk_getRuleStatusVec(instance, nullptr, 0, &status);
    if (status != U_BUFFER_OVERFLOW_ERROR && U_FAILURE(status))
      env->ThrowNew(java::lang::RuntimeException::cls, u_errorName(status));
    return len;
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_BreakIteratorExternalKt_BreakIterator_1nGetRuleStatuses
  (JNIEnv* env, jclass jclass, jlong ptr, jintArray result, jint len) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(static_cast<uintptr_t>(ptr));
    UErrorCode status = U_ZERO_ERROR;
    std::vector<jint> statuses(len);
    ubrk_getRuleStatusVec(instance, reinterpret_cast<int32_t*>(statuses.data()), len, &status);
    if (U_FAILURE(status))
      env->ThrowNew(java::lang::RuntimeException::cls, u_errorName(status));
    env->SetIntArrayRegion(result, 0, len, statuses.data());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BreakIteratorExternalKt_BreakIterator_1nSetText
  (JNIEnv* env, jclass jclass, jlong ptr, jcharArray textArr, jint len, jintArray errorCode) {
    UBreakIterator* instance = reinterpret_cast<UBreakIterator*>(static_cast<uintptr_t>(ptr));

    std::vector<jchar>* text = new std::vector<jchar>(len);
    env->GetCharArrayRegion(textArr, 0, len, text->data());

    UErrorCode errorCodes[1] = { U_ZERO_ERROR };
    ubrk_setText(instance, reinterpret_cast<UChar *>(text->data()), len, errorCodes);

    env->SetIntArrayRegion(errorCode, 0, 1, reinterpret_cast<jint*>(errorCodes));

    return reinterpret_cast<jlong>(text);
}