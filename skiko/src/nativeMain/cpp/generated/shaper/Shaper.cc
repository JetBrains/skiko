
// This file has been auto generated.

#include <iostream>
#include "SkShaper.h"
#include "src/utils/SkUTF.h"
#include "unicode/ubidi.h"
#include "common.h"

static void deleteShaper(SkShaper* instance) {
    // std::cout << "Deleting [SkShaper " << instance << "]" << std::endl;
    delete instance;
}

extern "C" jlong org_jetbrains_skia_shaper_Shaper__1nGetFinalizer() {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteShaper));
}

extern "C" jlong org_jetbrains_skia_shaper_Shaper__1nMakePrimitive
  () {
    return reinterpret_cast<jlong>(SkShaper::MakePrimitive().release());
}

extern "C" jlong org_jetbrains_skia_shaper_Shaper__1nMakeShaperDrivenWrapper
  (jlong fontMgrPtr) {
    SkFontMgr* fontMgr = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(fontMgrPtr));
    return reinterpret_cast<jlong>(SkShaper::MakeShaperDrivenWrapper(sk_ref_sp(fontMgr)).release());
}

extern "C" jlong org_jetbrains_skia_shaper_Shaper__1nMakeShapeThenWrap
  (jlong fontMgrPtr) {
    SkFontMgr* fontMgr = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(fontMgrPtr));
    return reinterpret_cast<jlong>(SkShaper::MakeShapeThenWrap(sk_ref_sp(fontMgr)).release());
}

extern "C" jlong org_jetbrains_skia_shaper_Shaper__1nMakeShapeDontWrapOrReorder
  (jlong fontMgrPtr) {
    SkFontMgr* fontMgr = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(fontMgrPtr));
    return reinterpret_cast<jlong>(SkShaper::MakeShapeDontWrapOrReorder(sk_ref_sp(fontMgr)).release());
}

extern "C" jlong org_jetbrains_skia_shaper_Shaper__1nMakeCoreText
  () {
    #ifdef SK_SHAPER_CORETEXT_AVAILABLE
        return reinterpret_cast<jlong>(SkShaper::MakeCoreText().release());
    #else
        return 0;
    #endif
}

extern "C" jlong org_jetbrains_skia_shaper_Shaper__1nMake
  (jlong fontMgrPtr) {
    SkFontMgr* fontMgr = reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(fontMgrPtr));
    return reinterpret_cast<jlong>(SkShaper::Make(sk_ref_sp(fontMgr)).release());
}


extern "C" jlong org_jetbrains_skia_shaper_Shaper__1nShapeBlob
  (jlong ptr, jstring textObj, jlong fontPtr, jobject opts, jfloat width, jfloat offsetX, jfloat offsetY) {
    TODO("implement org_jetbrains_skia_shaper_Shaper__1nShapeBlob");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_shaper_Shaper__1nShapeBlob
  (jlong ptr, jstring textObj, jlong fontPtr, jobject opts, jfloat width, jfloat offsetX, jfloat offsetY) {
    SkShaper* instance = reinterpret_cast<SkShaper*>(static_cast<uintptr_t>(ptr));
    SkString text = skString(env, textObj);
    std::shared_ptr<UBreakIterator> graphemeIter = skija::shaper::graphemeBreakIterator(text);
    if (!graphemeIter) return 0;
    SkFont* font = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(fontPtr));
    std::vector<SkShaper::Feature> features = skija::shaper::ShapingOptions::getFeatures(env, opts);

    std::unique_ptr<SkShaper::FontRunIterator> fontRunIter(new FontRunIterator(
        text.c_str(),
        text.size(),
        *font,
        SkFontMgr::RefDefault(),
        graphemeIter,
        env->GetBooleanField(opts, skija::shaper::ShapingOptions::_approximateSpaces),
        env->GetBooleanField(opts, skija::shaper::ShapingOptions::_approximatePunctuation)
    ));
    if (!fontRunIter) return 0;

    uint8_t defaultBiDiLevel = env->GetBooleanField(opts, skija::shaper::ShapingOptions::_leftToRight) ? UBIDI_DEFAULT_LTR : UBIDI_DEFAULT_RTL;
    std::unique_ptr<SkShaper::BiDiRunIterator> bidiRunIter(SkShaper::MakeBiDiRunIterator(text.c_str(), text.size(), defaultBiDiLevel));
    if (!bidiRunIter) return 0;

    std::unique_ptr<SkShaper::ScriptRunIterator> scriptRunIter(SkShaper::MakeHbIcuScriptRunIterator(text.c_str(), text.size()));
    if (!scriptRunIter) return 0;

    std::unique_ptr<SkShaper::LanguageRunIterator> languageRunIter(SkShaper::MakeStdLanguageRunIterator(text.c_str(), text.size()));
    if (!languageRunIter) return 0;

    SkTextBlobBuilderRunHandler rh(text.c_str(), {offsetX, offsetY});
    instance->shape(text.c_str(), text.size(), *fontRunIter, *bidiRunIter, *scriptRunIter, *languageRunIter, features.data(), features.size(), width, &rh);
    SkTextBlob* blob = rh.makeBlob().release();
    
    return reinterpret_cast<jlong>(blob);
}
#endif



extern "C" jlong org_jetbrains_skia_shaper_Shaper__1nShapeLine
  (jlong ptr, jstring textObj, jlong fontPtr, jobject opts) {
    TODO("implement org_jetbrains_skia_shaper_Shaper__1nShapeLine");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_shaper_Shaper__1nShapeLine
  (jlong ptr, jstring textObj, jlong fontPtr, jobject opts) {
    SkShaper* instance = reinterpret_cast<SkShaper*>(static_cast<uintptr_t>(ptr));

    SkString text = skString(env, textObj);
    std::shared_ptr<UBreakIterator> graphemeIter = skija::shaper::graphemeBreakIterator(text);
    if (!graphemeIter) return 0;

    SkFont* font = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(fontPtr));
    std::vector<SkShaper::Feature> features = skija::shaper::ShapingOptions::getFeatures(env, opts);

    std::unique_ptr<SkShaper::FontRunIterator> fontRunIter(new FontRunIterator(
        text.c_str(),
        text.size(),
        *font,
        SkFontMgr::RefDefault(),
        graphemeIter, 
        env->GetBooleanField(opts, skija::shaper::ShapingOptions::_approximateSpaces),
        env->GetBooleanField(opts, skija::shaper::ShapingOptions::_approximatePunctuation)));
    if (!fontRunIter) return 0;

    uint8_t defaultBiDiLevel = env->GetBooleanField(opts, skija::shaper::ShapingOptions::_leftToRight) ? UBIDI_DEFAULT_LTR : UBIDI_DEFAULT_RTL;
    std::unique_ptr<SkShaper::BiDiRunIterator> bidiRunIter(SkShaper::MakeBiDiRunIterator(text.c_str(), text.size(), defaultBiDiLevel));
    if (!bidiRunIter) return 0;

    std::unique_ptr<SkShaper::ScriptRunIterator> scriptRunIter(SkShaper::MakeHbIcuScriptRunIterator(text.c_str(), text.size()));
    if (!scriptRunIter) return 0;

    std::unique_ptr<SkShaper::LanguageRunIterator> languageRunIter(SkShaper::MakeStdLanguageRunIterator(text.c_str(), text.size()));
    if (!languageRunIter) return 0;

    TextLine* line;
    if (text.size() == 0)
        line = new TextLine(*font);
    else {
        TextLineRunHandler rh(text, graphemeIter);
        instance->shape(text.c_str(), text.size(), *fontRunIter, *bidiRunIter, *scriptRunIter, *languageRunIter, features.data(), features.size(), std::numeric_limits<float>::infinity(), &rh);
        line = rh.makeLine().release();
    }
    return reinterpret_cast<jlong>(line);
}
#endif



extern "C" void org_jetbrains_skia_shaper_Shaper__1nShape
  (jlong ptr, jlong textPtr, jobject fontRunIterObj, jobject bidiRunIterObj, jobject scriptRunIterObj, jobject languageRunIterObj, jobject opts, jfloat width, jobject runHandlerObj)
{
    TODO("implement org_jetbrains_skia_shaper_Shaper__1nShape");
}
     
#if 0 
extern "C" void org_jetbrains_skia_shaper_Shaper__1nShape
  (jlong ptr, jlong textPtr, jobject fontRunIterObj, jobject bidiRunIterObj, jobject scriptRunIterObj, jobject languageRunIterObj, jobject opts, jfloat width, jobject runHandlerObj)
{
    SkShaper* instance = reinterpret_cast<SkShaper*>(static_cast<uintptr_t>(ptr));
    SkString* text = reinterpret_cast<SkString*>(static_cast<uintptr_t>(textPtr));

    auto nativeFontRunIter = (SkShaper::FontRunIterator*) skija::impl::Native::fromJava(env, fontRunIterObj, skija::shaper::FontMgrRunIterator::cls);
    std::unique_ptr<SkijaFontRunIterator> localFontRunIter;
    if (nativeFontRunIter == nullptr)
        localFontRunIter.reset(new SkijaFontRunIterator(env, fontRunIterObj, *text));

    auto nativeBidiRunIter = (SkShaper::BiDiRunIterator*) skija::impl::Native::fromJava(env, bidiRunIterObj, skija::shaper::IcuBidiRunIterator::cls);
    std::unique_ptr<SkijaBidiRunIterator> localBidiRunIter;
    if (nativeBidiRunIter == nullptr)
        localBidiRunIter.reset(new SkijaBidiRunIterator(env, bidiRunIterObj, *text));

    auto nativeScriptRunIter = (SkShaper::ScriptRunIterator*) skija::impl::Native::fromJava(env, scriptRunIterObj, skija::shaper::HbIcuScriptRunIterator::cls);
    std::unique_ptr<SkijaScriptRunIterator> localScriptRunIter;
    if (nativeScriptRunIter == nullptr)
        localScriptRunIter.reset(new SkijaScriptRunIterator(env, scriptRunIterObj, *text));
    
    auto languageRunIter = SkijaLanguageRunIterator(env, languageRunIterObj, *text);

    std::vector<SkShaper::Feature> features = skija::shaper::ShapingOptions::getFeatures(env, opts);
    
    auto nativeRunHandler = (SkShaper::RunHandler*) skija::impl::Native::fromJava(env, runHandlerObj, skija::shaper::TextBlobBuilderRunHandler::cls);
    std::unique_ptr<SkijaRunHandler> localRunHandler;
    if (nativeRunHandler == nullptr)
        localRunHandler.reset(new SkijaRunHandler(env, runHandlerObj, *text));

    instance->shape(text->c_str(), text->size(),
        nativeFontRunIter != nullptr ? *nativeFontRunIter : *localFontRunIter,
        nativeBidiRunIter != nullptr ? *nativeBidiRunIter : *localBidiRunIter,
        nativeScriptRunIter != nullptr ? *nativeScriptRunIter : *localScriptRunIter,
        languageRunIter,
        features.data(),
        features.size(),
        width,
        nativeRunHandler != nullptr ? nativeRunHandler : localRunHandler.get());
}
#endif

