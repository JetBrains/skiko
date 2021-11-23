
// This file has been auto generated.

#include <iostream>
#include "SkShaper.h"
#include "src/utils/SkUTF.h"
#include "unicode/ubidi.h"
#include "common.h"
#include "FontRunIterator.hh"
#include "src/utils/SkUTF.h"
#include "TextLineRunHandler.hh"

static void deleteShaper(SkShaper* instance) {
    // std::cout << "Deleting [SkShaper " << instance << "]" << std::endl;
    delete instance;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>((&deleteShaper));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nMakePrimitive
  () {
    return reinterpret_cast<KNativePointer>(SkShaper::MakePrimitive().release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nMakeShaperDrivenWrapper
  (KNativePointer fontMgrPtr) {
    SkFontMgr* fontMgr = reinterpret_cast<SkFontMgr*>((fontMgrPtr));
    return reinterpret_cast<KNativePointer>(SkShaper::MakeShaperDrivenWrapper(sk_ref_sp(fontMgr)).release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nMakeShapeThenWrap
  (KNativePointer fontMgrPtr) {
    SkFontMgr* fontMgr = reinterpret_cast<SkFontMgr*>((fontMgrPtr));
    return reinterpret_cast<KNativePointer>(SkShaper::MakeShapeThenWrap(sk_ref_sp(fontMgr)).release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nMakeShapeDontWrapOrReorder
  (KNativePointer fontMgrPtr) {
    SkFontMgr* fontMgr = reinterpret_cast<SkFontMgr*>((fontMgrPtr));
    return reinterpret_cast<KNativePointer>(SkShaper::MakeShapeDontWrapOrReorder(sk_ref_sp(fontMgr)).release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nMakeCoreText() {
    #ifdef SK_SHAPER_CORETEXT_AVAILABLE
        return reinterpret_cast<KNativePointer>(SkShaper::MakeCoreText().release());
    #else
        return 0;
    #endif
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nMake
  (KNativePointer fontMgrPtr) {
    SkFontMgr* fontMgr = reinterpret_cast<SkFontMgr*>((fontMgrPtr));
    return reinterpret_cast<KNativePointer>(SkShaper::Make(sk_ref_sp(fontMgr)).release());
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nShapeBlob
  (KNativePointer ptr, KNativePointer textPtr, KNativePointer fontPtr, KInt optsFeaturesLen, KInt* optsFeatures, KInt optsBooleanProps, KFloat width, KFloat offsetX, KFloat offsetY) {
    SkShaper* instance = reinterpret_cast<SkShaper*>(ptr);
    SkString& text = *(reinterpret_cast<SkString*>(textPtr));

    std::shared_ptr<UBreakIterator> graphemeIter = skija::shaper::graphemeBreakIterator(text);
    if (!graphemeIter) return 0;
    SkFont* font = reinterpret_cast<SkFont*>(fontPtr);

    std::vector<SkShaper::Feature> features = skija::shaper::ShapingOptions::getFeaturesFromIntsArray(optsFeatures, optsFeaturesLen);

    bool aproximatePunctuation = (optsBooleanProps & 0x01) != 0;
    bool aproximateSpaces = (optsBooleanProps & 0x02) != 0;
    bool isLeftToRight = (optsBooleanProps & 0x04) != 0;

    uint8_t defaultBiDiLevel = isLeftToRight ? UBIDI_DEFAULT_LTR : UBIDI_DEFAULT_RTL;
    std::unique_ptr<SkShaper::BiDiRunIterator> bidiRunIter(SkShaper::MakeBiDiRunIterator(text.c_str(), text.size(), defaultBiDiLevel));
    if (!bidiRunIter) return 0;

    std::unique_ptr<SkShaper::ScriptRunIterator> scriptRunIter(SkShaper::MakeHbIcuScriptRunIterator(text.c_str(), text.size()));
    if (!scriptRunIter) return 0;

    std::unique_ptr<SkShaper::LanguageRunIterator> languageRunIter(SkShaper::MakeStdLanguageRunIterator(text.c_str(), text.size()));
    if (!languageRunIter) return 0;

     FontRunIterator fontRunIter(
        text.c_str(),
        text.size(),
        *font,
        SkFontMgr::RefDefault(),
        graphemeIter,
        aproximateSpaces,
        aproximatePunctuation
    );

    SkTextBlobBuilderRunHandler rh(text.c_str(), {offsetX, offsetY});
    instance->shape(text.c_str(), text.size(), fontRunIter, *bidiRunIter, *scriptRunIter, *languageRunIter, features.data(), features.size(), width, &rh);
    SkTextBlob* blob = rh.makeBlob().release();

    return reinterpret_cast<KNativePointer>(blob);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper__1nShapeLine
  (KNativePointer ptr, KNativePointer textManagedStringPtr, KNativePointer fontPtr, KInt optsFeaturesLen, KInt* optsFeatures, KInt optsBooleanProps) {
    SkShaper* instance = reinterpret_cast<SkShaper*>(ptr);

    SkString& text = *(reinterpret_cast<SkString*>(textManagedStringPtr));
    SkFont* font = reinterpret_cast<SkFont*>(fontPtr);

    if (text.size() == 0) {
        return reinterpret_cast<KNativePointer>(new TextLine(*font));
    }

    std::shared_ptr<UBreakIterator> graphemeIter = skija::shaper::graphemeBreakIterator(text);
    if (!graphemeIter) return 0;

    std::vector<SkShaper::Feature> features = skija::shaper::ShapingOptions::getFeaturesFromIntsArray(optsFeatures, optsFeaturesLen);

    bool aproximatePunctuation = (optsBooleanProps & 0x01) != 0;
    bool aproximateSpaces = (optsBooleanProps & 0x02) != 0;
    bool isLeftToRight = (optsBooleanProps & 0x04) != 0;

    uint8_t defaultBiDiLevel = isLeftToRight ? UBIDI_DEFAULT_LTR : UBIDI_DEFAULT_RTL;
    std::unique_ptr<SkShaper::BiDiRunIterator> bidiRunIter(SkShaper::MakeBiDiRunIterator(text.c_str(), text.size(), defaultBiDiLevel));
    if (!bidiRunIter) return 0;

    std::unique_ptr<SkShaper::ScriptRunIterator> scriptRunIter(SkShaper::MakeHbIcuScriptRunIterator(text.c_str(), text.size()));
    if (!scriptRunIter) return 0;

    std::unique_ptr<SkShaper::LanguageRunIterator> languageRunIter(SkShaper::MakeStdLanguageRunIterator(text.c_str(), text.size()));
    if (!languageRunIter) return 0;

    FontRunIterator fontRunIter(
        text.c_str(),
        text.size(),
        *font,
        SkFontMgr::RefDefault(),
        graphemeIter,
        aproximateSpaces,
        aproximatePunctuation);

    TextLineRunHandler rh(text, graphemeIter);
    instance->shape(text.c_str(), text.size(), fontRunIter, *bidiRunIter, *scriptRunIter, *languageRunIter, features.data(), features.size(), std::numeric_limits<float>::infinity(), &rh);
    return reinterpret_cast<KNativePointer>(rh.makeLine().release());
}

SKIKO_EXPORT void org_jetbrains_skia_shaper_Shaper__1nShape
  (KNativePointer ptr, KNativePointer textPtr, KInteropPointer fontRunIterObj, KInteropPointer bidiRunIterObj, KInteropPointer scriptRunIterObj, KInteropPointer languageRunIterObj, KInt optsFeaturesLen, KInt* optsFeatures, KInt optsBooleanProps, KFloat width, KInteropPointer runHandlerObj)
{
    auto* instance = reinterpret_cast<SkShaper*>(ptr);

    SkString& text = *(reinterpret_cast<SkString*>(textPtr));
    if (text.size() == 0) {
        return;
    }

    std::vector<SkShaper::Feature> features = skija::shaper::ShapingOptions::getFeaturesFromIntsArray(optsFeatures, optsFeaturesLen);

    auto* fontRunIter = reinterpret_cast<SkShaper::FontRunIterator*>(fontRunIterObj);
    auto* languageRunIter = reinterpret_cast<SkShaper::LanguageRunIterator*>(languageRunIterObj);
    auto* scriptRunIter = reinterpret_cast<SkShaper::ScriptRunIterator*>(scriptRunIterObj);
    auto* bidiRunIter = reinterpret_cast<SkShaper::BiDiRunIterator*>(bidiRunIterObj);
    auto* runHandler = reinterpret_cast<SkShaper::RunHandler*>(runHandlerObj);

    instance->shape(text.c_str(), text.size(), *fontRunIter, *bidiRunIter, *scriptRunIter, *languageRunIter, features.data(), features.size(), std::numeric_limits<float>::infinity(), runHandler);
}

class RunIteratorImplBase {
public:
    virtual ~RunIteratorImplBase() {}
    virtual void init(KInteropPointer onConsume, KInteropPointer onEndOfCurrentRun, KInteropPointer onAtEnd, KInteropPointer onCurrent) = 0;
};

template<typename T, typename CurrentCallback>
class RunIteratorImpl: public T, public virtual RunIteratorImplBase {
    static_assert(std::is_base_of<SkShaper::RunIterator, T>::value, "");
public:
    RunIteratorImpl() : _onCurrent(0), _onAtEnd(0), _onEndOfCurrentRun(0), _onConsume(0) {}

    void consume() override {
        _onConsume();
    }
    
    size_t endOfCurrentRun() const override {
        return static_cast<size_t>(_onEndOfCurrentRun());
    }
    
    bool atEnd() const override {
        return static_cast<bool>(_onAtEnd());  
    }
    
    virtual void init(KInteropPointer onConsume, KInteropPointer onEndOfCurrentRun, KInteropPointer onAtEnd, KInteropPointer onCurrent) {
        _onConsume = KVoidCallback(onConsume);
        _onEndOfCurrentRun = KIntCallback(onEndOfCurrentRun);
        _onAtEnd = KBooleanCallback(onAtEnd);
        _onCurrent = CurrentCallback(onCurrent);
    }
protected:
    CurrentCallback _onCurrent;
private:
    KVoidCallback _onConsume;
    KIntCallback  _onEndOfCurrentRun;
    KBooleanCallback _onAtEnd;
};

class FontRunIteratorImpl : public RunIteratorImpl<SkShaper::FontRunIterator, KNativePointerCallback> {
    const SkFont& currentFont() const override {
        return *reinterpret_cast<SkFont*>(_onCurrent());
    }
};
class BiDiRunIteratorImpl : public RunIteratorImpl<SkShaper::BiDiRunIterator, KIntCallback> {
    uint8_t currentLevel() const override {
        return static_cast<uint8_t>(_onCurrent());
    }
};
class ScriptRunIteratorImpl : public RunIteratorImpl<SkShaper::ScriptRunIterator, KIntCallback>  {
    SkFourByteTag currentScript() const override {
        return static_cast<SkFourByteTag>(_onCurrent());
    }
};
class LanguageRunIteratorImpl : public RunIteratorImpl<SkShaper::LanguageRunIterator, KInteropPointerCallback>  {
    const char* currentLanguage() const override {
        return reinterpret_cast<char *>(_onCurrent());
    }
};

static void deleteRunIterator(SkShaper::RunIterator* runIterator) {
    delete runIterator;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper_RunIterator_1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>(deleteRunIterator);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_Shaper_RunIterator_1nCreateRunIterator(KInt type) {
    switch (type) {
        case 1: // FontRunIterator
            return reinterpret_cast<KNativePointer>(new FontRunIteratorImpl());
        case 2: // BiDiRunIterator
            return reinterpret_cast<KNativePointer>(new BiDiRunIteratorImpl());
        case 3: // ScriptRunIterator
            return reinterpret_cast<KNativePointer>(new ScriptRunIteratorImpl());
        case 4: // LanguageRunIterator
            return reinterpret_cast<KNativePointer>(new LanguageRunIteratorImpl());
        default:
            TODO("unsupported run iterator type");
    }
}

SKIKO_EXPORT void org_jetbrains_skia_shaper_Shaper_RunIterator_1nInitRunIterator
  (KNativePointer ptr, KInteropPointer onConsume, KInteropPointer onEndOfCurrentRun, KInteropPointer onAtEnd, KInteropPointer onCurrent) {
    auto* iter = reinterpret_cast<RunIteratorImplBase*>(ptr);
    iter->init(onConsume, onEndOfCurrentRun, onAtEnd, onCurrent);
}
