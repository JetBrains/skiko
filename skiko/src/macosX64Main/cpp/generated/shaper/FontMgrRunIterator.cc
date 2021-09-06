
// This file has been auto generated.

#include "SkFontMgr.h"
#include "SkShaper.h"
#include "common.h"


extern "C" jlong org_jetbrains_skia_shaper_FontMgrRunIterator__1nMake
  (kref __Kinstance, jlong textPtr, jlong fontPtr, jobject opts) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_shaper_FontMgrRunIterator__1nMake");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_shaper_FontMgrRunIterator__1nMake
  (kref __Kinstance, jlong textPtr, jlong fontPtr, jobject opts) {
    SkString* text = reinterpret_cast<SkString*>(static_cast<uintptr_t>(textPtr));
    SkFont* font = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(fontPtr));
    jobject fontMgrObj = env->GetObjectField(opts, skija::shaper::ShapingOptions::_fontMgr);
    sk_sp<SkFontMgr> fontMgr = fontMgrObj == nullptr
      ? SkFontMgr::RefDefault()
      : sk_ref_sp(reinterpret_cast<SkFontMgr*>(skija::impl::Native::fromJava(env, fontMgrObj, skija::FontMgr::cls)));
    std::shared_ptr<UBreakIterator> graphemeIter = skija::shaper::graphemeBreakIterator(*text);
    if (!graphemeIter) return 0;

    auto instance = new FontRunIterator(
      text->c_str(),
      text->size(),
      *font,
      fontMgr,
      graphemeIter,
      env->GetBooleanField(opts, skija::shaper::ShapingOptions::_approximateSpaces),
      env->GetBooleanField(opts, skija::shaper::ShapingOptions::_approximatePunctuation)
    );
    return reinterpret_cast<jlong>(instance);
}
#endif



extern "C" jlong org_jetbrains_skia_shaper_FontMgrRunIterator__1nGetCurrentFont
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_shaper_FontMgrRunIterator__1nGetCurrentFont");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_shaper_FontMgrRunIterator__1nGetCurrentFont
  (kref __Kinstance, jlong ptr) {
    SkShaper::FontRunIterator* instance = reinterpret_cast<SkShaper::FontRunIterator*>(static_cast<uintptr_t>(ptr));
    SkFont* font = new SkFont(instance->currentFont());
    return reinterpret_cast<jlong>(font);
}
#endif

