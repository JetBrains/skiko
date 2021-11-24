
// This file has been auto generated.

#include <iostream>
#include <limits>
#include <vector>
#include "TextStyle.h"
using namespace std;
using namespace skia::textlayout;
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_TextStyle__1nMake
  () {
    TextStyle* instance = new TextStyle();
    return reinterpret_cast<KNativePointer>(instance);
}

static void deleteTextStyle(TextStyle* instance) {
    delete instance;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_TextStyle__1nGetFinalizer
  () {
    return reinterpret_cast<KNativePointer>((&deleteTextStyle));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_paragraph_TextStyle__1nEquals
  (KNativePointer ptr, KNativePointer otherPtr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    TextStyle* other = reinterpret_cast<TextStyle*>((otherPtr));
    return instance->equals(*other);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_paragraph_TextStyle__1nAttributeEquals
  (KNativePointer ptr, KInt attribute, KNativePointer otherPtr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    TextStyle* other = reinterpret_cast<TextStyle*>((otherPtr));
    if (attribute == static_cast<KInt>(StyleType::kWordSpacing) + 1) // FONT_EXACT
        return instance->equalsByFonts(*other);
    else
        return instance->matchOneAttribute(static_cast<StyleType>(attribute), *other);
}

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_TextStyle__1nGetColor
  (KNativePointer ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    return instance->getColor();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nSetColor
  (KNativePointer ptr, KInt color) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    instance->setColor(color);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_TextStyle__1nGetForeground
  (KNativePointer ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    return instance->hasForeground() ? reinterpret_cast<KNativePointer>(new SkPaint(instance->getForeground())) : 0;
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nSetForeground
  (KNativePointer ptr, KNativePointer paintPtr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    if (paintPtr == 0)
        instance->clearForegroundColor();
    else {
        SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
        instance->setForegroundColor(*paint);
    }
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_TextStyle__1nGetBackground
  (KNativePointer ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    return instance->hasBackground() ? reinterpret_cast<KNativePointer>(new SkPaint(instance->getBackground())) : 0;
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nSetBackground
  (KNativePointer ptr, KNativePointer paintPtr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    if (paintPtr == 0)
        instance->clearBackgroundColor();
    else {
        SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
        instance->setBackgroundColor(*paint);
    }
}


SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nGetDecorationStyle
  (KNativePointer ptr, KInt* res) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    Decoration d = instance->getDecoration();

    res[0] = 0;
    if ((d.fType & TextDecoration::kUnderline) != 0) {
        res[0] = res[0] | (1 << 0);
    }

    if ((d.fType & TextDecoration::kOverline) != 0) {
        res[0] = res[0] | (1 << 1);
    }

    if ((d.fType & TextDecoration::kLineThrough) != 0) {
        res[0] = res[0] | (1 << 2);
    }

    if (d.fMode == TextDecorationMode::kGaps) {
        res[0] = res[0] | (1 << 3);
    }

    res[1] = static_cast<KInt>(d.fColor);
    res[2] = static_cast<KInt>(d.fStyle);
    res[3] = rawBits(d.fThicknessMultiplier);
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nSetDecorationStyle
  (KNativePointer ptr, KBoolean underline, KBoolean overline, KBoolean lineThrough, KBoolean gaps, KInt color, KInt style, KFloat thicknessMultiplier) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    int typeMask = 0;
    if (underline)   typeMask |= TextDecoration::kUnderline;
    if (overline)    typeMask |= TextDecoration::kOverline;
    if (lineThrough) typeMask |= TextDecoration::kLineThrough;
    instance->setDecoration(static_cast<TextDecoration>(typeMask));
    instance->setDecorationMode(gaps ? TextDecorationMode::kGaps : TextDecorationMode::kThrough);
    instance->setDecorationColor(color);
    instance->setDecorationStyle(static_cast<TextDecorationStyle>(style));
    instance->setDecorationThicknessMultiplier(thicknessMultiplier);
}

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_TextStyle__1nGetFontStyle
  (KNativePointer ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    return skija::FontStyle::toKotlin(instance->getFontStyle());
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nSetFontStyle
  (KNativePointer ptr, KInt fontStyleValue) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    instance->setFontStyle(skija::FontStyle::fromKotlin(fontStyleValue));
}

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_TextStyle__1nGetShadowsCount
  (KNativePointer ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    return static_cast<KInt>(instance->getShadows().size());
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nGetShadows
  (KNativePointer ptr, KInt* res) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    std::vector<TextShadow> shadows = instance->getShadows();

    for (int i = 0; i < shadows.size(); ++i) {
        const TextShadow& s = shadows[i];
        KLong blurSigma = rawBits(s.fBlurSigma);
        res[5*i] = s.fColor;
        res[5*i + 1] = rawBits(s.fOffset.fX);
        res[5*i + 2] = rawBits(s.fOffset.fY);
        res[5*i + 3] = (KInt)(blurSigma >> 32);
        res[5*i + 4] = (KInt)blurSigma;
    }
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nAddShadow
  (KNativePointer ptr, KInt color, KFloat offsetX, KFloat offsetY, KDouble blurSigma) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    instance->addShadow({static_cast<SkColor>(color), {offsetX, offsetY}, blurSigma});
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nClearShadows
  (KNativePointer ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    instance->resetShadows();
}

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_TextStyle__1nGetFontFeaturesSize
  (KNativePointer ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    std::vector<FontFeature> fontFeatures = instance->getFontFeatures();
    return static_cast<KInt>(fontFeatures.size());
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nGetFontFeatures
  (KNativePointer ptr, KInt* resultArr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    std::vector<FontFeature> fontFeatures = instance->getFontFeatures();
    skija::FontFeature::writeToIntArray(fontFeatures, resultArr);
}


SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nAddFontFeature
  (KNativePointer ptr, KInteropPointer nameStr, KInt value) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    instance->addFontFeature(skString(nameStr), value);
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nClearFontFeatures
  (KNativePointer ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    instance->resetFontFeatures();
}

SKIKO_EXPORT KFloat org_jetbrains_skia_paragraph_TextStyle__1nGetFontSize
  (KNativePointer ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    return instance->getFontSize();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nSetFontSize
  (KNativePointer ptr, KFloat size) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    instance->setFontSize(size);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_TextStyle__1nGetFontFamilies
  (KNativePointer ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    std::vector<KNativePointer>* res = new std::vector<KNativePointer>();
    for (auto& f : instance->getFontFamilies()) {
        res->push_back(reinterpret_cast<KNativePointer>(new SkString(f)));
    }
    return reinterpret_cast<KNativePointer>(res);
}


SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nSetFontFamilies
  (KNativePointer ptr, KInteropPointerArray familiesArray, KInt familiesArraySize) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    instance->setFontFamilies(skStringVector(familiesArray, familiesArraySize));
}


SKIKO_EXPORT KFloat org_jetbrains_skia_paragraph_TextStyle__1nGetHeight
  (KNativePointer ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    return instance->getHeightOverride() ? instance->getHeight() : std::numeric_limits<KFloat>::quiet_NaN();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nSetHeight
  (KNativePointer ptr, KBoolean override, KFloat height) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    instance->setHeightOverride(override);
    instance->setHeight(height);
}

SKIKO_EXPORT KFloat org_jetbrains_skia_paragraph_TextStyle__1nGetBaselineShift
  (KNativePointer ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    return instance->getBaselineShift();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nSetBaselineShift
  (KNativePointer ptr, KFloat baselineShift) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    instance->setBaselineShift(baselineShift);
}

SKIKO_EXPORT KFloat org_jetbrains_skia_paragraph_TextStyle__1nGetLetterSpacing
  (KNativePointer ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    return instance->getLetterSpacing();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nSetLetterSpacing
  (KNativePointer ptr, KFloat letterSpacing) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    instance->setLetterSpacing(letterSpacing);
}

SKIKO_EXPORT KFloat org_jetbrains_skia_paragraph_TextStyle__1nGetWordSpacing
  (KNativePointer ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    return instance->getWordSpacing();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nSetWordSpacing
  (KNativePointer ptr, KFloat wordSpacing) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    instance->setWordSpacing(wordSpacing);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_TextStyle__1nGetTypeface
  (KNativePointer ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    return reinterpret_cast<KNativePointer>(instance->refTypeface().release());
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nSetTypeface
  (KNativePointer ptr, KNativePointer typefacePtr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>((typefacePtr));
    instance->setTypeface(sk_ref_sp(typeface));
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_TextStyle__1nGetLocale
  (KNativePointer ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    return reinterpret_cast<KNativePointer>(new SkString(instance->getLocale()));
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nSetLocale
  (KNativePointer ptr, KInteropPointer locale) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    instance->setLocale(skString(locale));
}

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_TextStyle__1nGetBaselineMode
  (KNativePointer ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    return static_cast<KInt>(instance->getTextBaseline());
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nSetBaselineMode
  (KNativePointer ptr, KInt baselineModeValue) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    instance->setTextBaseline(static_cast<TextBaseline>(baselineModeValue));
}


SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nGetFontMetrics
  (KNativePointer ptr, KFloat* fontMetrics) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    SkFontMetrics m;
    instance->getFontMetrics(&m);
    fontMetrics[0] = m.fTop;
    fontMetrics[1] = m.fAscent;
    fontMetrics[2] = m.fDescent;
    fontMetrics[3] = m.fBottom;
    fontMetrics[4] = m.fLeading;
    fontMetrics[5] = m.fAvgCharWidth;
    fontMetrics[6] = m.fMaxCharWidth;
    fontMetrics[7] = m.fXMin;
    fontMetrics[8] = m.fXMax;
    fontMetrics[9] = m.fXHeight;
    fontMetrics[10] = m.fCapHeight;
    fontMetrics[11] = std::numeric_limits<float>::quiet_NaN();
    fontMetrics[12] = std::numeric_limits<float>::quiet_NaN();
    fontMetrics[13] = std::numeric_limits<float>::quiet_NaN();
    fontMetrics[14] = std::numeric_limits<float>::quiet_NaN();

    SkScalar thickness;
    SkScalar position;
    if (m.hasUnderlineThickness(&thickness)) {
        fontMetrics[11] = thickness;
    }
    if (m.hasUnderlinePosition(&position)) {
        fontMetrics[12] = position;
    }
    if (m.hasStrikeoutThickness(&thickness)) {
        fontMetrics[13] = thickness;
    }
    if (m.hasStrikeoutPosition(&position)) {
        fontMetrics[14] = position;
    }
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_paragraph_TextStyle__1nIsPlaceholder
  (KNativePointer ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    return instance->isPlaceholder();
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nSetPlaceholder
  (KNativePointer ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    instance->setPlaceholder();
}
