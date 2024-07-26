#include "FontMgrDefaultFactory.hh"
#include <jni.h>
#include "../interop.hh"
#include "interop.hh"
#include "FontRunIterator.hh"
#include "SkFontMgr.h"
#include "SkShaper.h"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_shaper_FontMgrRunIteratorKt__1nMake
  (JNIEnv* env, jclass jclass, jlong textPtr, jlong fontPtr, jobject fontMgrPtr, jint optsBooleanProps) {
    SkString* text = reinterpret_cast<SkString*>(static_cast<uintptr_t>(textPtr));
    SkFont* font = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(fontPtr));
    sk_sp<SkFontMgr> fontMgr = fontMgrPtr == nullptr
      ? SkFontMgrSkikoDefault()
      : sk_ref_sp(reinterpret_cast<SkFontMgr*>(skija::impl::Native::fromJava(env, fontMgrPtr, skija::FontMgr::cls)));
    std::shared_ptr<UBreakIterator> graphemeIter = skija::shaper::graphemeBreakIterator(*text);
    if (!graphemeIter) return 0;

    bool approximatePunctuation = (optsBooleanProps & 0x01) != 0;
    bool approximateSpaces = (optsBooleanProps & 0x02) != 0;

    auto instance = new FontRunIterator(
      text->c_str(),
      text->size(),
      *font,
      fontMgr,
      graphemeIter,
      approximateSpaces,
      approximatePunctuation
    );
    return reinterpret_cast<jlong>(instance);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_shaper_FontMgrRunIteratorKt__1nGetCurrentFont
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkShaper::FontRunIterator* instance = reinterpret_cast<SkShaper::FontRunIterator*>(static_cast<uintptr_t>(ptr));
    SkFont* font = new SkFont(instance->currentFont());
    return reinterpret_cast<jlong>(font);
}
