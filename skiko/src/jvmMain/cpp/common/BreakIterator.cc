#include <jni.h>
#include "interop.hh"
#include "SkikoUnicode.hh"

static void deleteBreakIterator(SkBreakIterator* instance) {
    delete instance;
}

static SkUnicode::BreakType toBreakType(jint type) {
    // Must match org.jetbrains.skia.BreakIterator.make*Instance
    switch (type) {
        case 0: return SkUnicode::BreakType::kGraphemes;
        case 1: return SkUnicode::BreakType::kWords;
        case 2: return SkUnicode::BreakType::kLines;
        case 3: return SkUnicode::BreakType::kSentences;
        default: SkUNREACHABLE;
    }
}

static std::unique_ptr<SkBreakIterator> makeBreakIterator(jint type, const char* locale) {
    sk_sp<SkUnicode> unicode = skiko::BestAvailableUnicode();
    if (!unicode) {
        return nullptr;
    }
    return unicode->makeBreakIterator(locale, toBreakType(type));
}

static SkBreakIterator* toBreakIterator(jlong ptr) {
    return reinterpret_cast<SkBreakIterator*>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BreakIteratorKt_BreakIterator_1nGetFinalizer(JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteBreakIterator));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BreakIteratorKt__1nMake
  (JNIEnv* env, jclass jclass, jint type, jstring localeStr) {
    SkString locale = localeStr == nullptr ? SkString() : skString(env, localeStr);
    std::unique_ptr<SkBreakIterator> instance =
        makeBreakIterator(type, locale.isEmpty() ? nullptr : locale.c_str());
    return reinterpret_cast<jlong>(instance.release());
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BreakIteratorKt__1nCurrent
  (JNIEnv* env, jclass jclass, jlong ptr) {
    return toBreakIterator(ptr)->current();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BreakIteratorKt__1nNext
  (JNIEnv* env, jclass jclass, jlong ptr) {
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

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BreakIteratorKt__1nPrevious
  (JNIEnv* env, jclass jclass, jlong ptr) {
    return toBreakIterator(ptr)->previous();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BreakIteratorKt__1nFirst
  (JNIEnv* env, jclass jclass, jlong ptr) {
    return toBreakIterator(ptr)->first();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BreakIteratorKt__1nLast
  (JNIEnv* env, jclass jclass, jlong ptr) {
    return toBreakIterator(ptr)->last();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BreakIteratorKt__1nPreceding
  (JNIEnv* env, jclass jclass, jlong ptr, jint offset) {
    return toBreakIterator(ptr)->preceding(offset);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BreakIteratorKt__1nFollowing
  (JNIEnv* env, jclass jclass, jlong ptr, jint offset) {
    return toBreakIterator(ptr)->following(offset);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BreakIteratorKt__1nIsBoundary
  (JNIEnv* env, jclass jclass, jlong ptr, jint offset) {
    return toBreakIterator(ptr)->isBoundary(offset);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BreakIteratorKt__1nSetText
  (JNIEnv* env, jclass jclass, jlong ptr, jshortArray textArr, jint len) {
    SkBreakIterator* instance = toBreakIterator(ptr);
    // The buffer must stay alive while the iterator is used: ICU's UText reads it lazily.
    // Kotlin keeps it alive in org.jetbrains.skia.U16String and frees it on the next setText
    // or on close.
    auto text = std::make_unique<std::vector<jshort>>(len);
    env->GetShortArrayRegion(textArr, 0, len, text->data());
    if (!instance->setText(reinterpret_cast<const char16_t*>(text->data()), len)) {
        return 0;
    }
    return reinterpret_cast<jlong>(text.release());
}
