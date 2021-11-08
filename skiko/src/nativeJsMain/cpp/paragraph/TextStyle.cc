
// This file has been auto generated.

#include <iostream>
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


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_paragraph_TextStyle__1nGetDecorationStyle
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_paragraph_TextStyle__1nGetDecorationStyle");
}

#if 0
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_paragraph_TextStyle__1nGetDecorationStyle
  (KNativePointer ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    Decoration d = instance->getDecoration();
    return env->NewObject(skija::paragraph::DecorationStyle::cls, skija::paragraph::DecorationStyle::ctor,
      (d.fType & TextDecoration::kUnderline) != 0,
      (d.fType & TextDecoration::kOverline) != 0,
      (d.fType & TextDecoration::kLineThrough) != 0,
      d.fMode == TextDecorationMode::kGaps,
      d.fColor,
      static_cast<KInt>(d.fStyle),
      d.fThicknessMultiplier);
}
#endif


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

SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_paragraph_TextStyle__1nGetShadows
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_paragraph_TextStyle__1nGetShadows");
}

#if 0
SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_paragraph_TextStyle__1nGetShadows
  (KNativePointer ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    std::vector<TextShadow> shadows = instance->getShadows();
    KInteropPointerArray shadowsArr = env->NewObjectArray((jsize) shadows.size(), skija::paragraph::Shadow::cls, nullptr);
    for (int i = 0; i < shadows.size(); ++i) {
        const TextShadow& s = shadows[i];
        skija::AutoLocal<KInteropPointer> shadowObj(env, env->NewObject(skija::paragraph::Shadow::cls, skija::paragraph::Shadow::ctor, s.fColor, s.fOffset.fX, s.fOffset.fY, s.fBlurSigma));
        env->SetObjectArrayElement(shadowsArr, i, shadowObj.get());
    }
    return shadowsArr;
}
#endif


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
    TODO("implement org_jetbrains_skia_paragraph_TextStyle__1nAddFontFeature");
}

#if 0
SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nAddFontFeature
  (KNativePointer ptr, KInteropPointer nameStr, KInt value) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    instance->addFontFeature(skString(env, nameStr), value);
}
#endif


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


SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_paragraph_TextStyle__1nGetFontFamilies
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_paragraph_TextStyle__1nGetFontFamilies");
}

#if 0
SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_paragraph_TextStyle__1nGetFontFamilies
  (KNativePointer ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    return javaStringArray(env, instance->getFontFamilies());
}
#endif



SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nSetFontFamilies
  (KNativePointer ptr, KInteropPointerArray familiesArray, KInt familiesArraySize) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    instance->setFontFamilies(skStringVector(familiesArray, familiesArraySize));
}



SKIKO_EXPORT KInteropPointer org_jetbrains_skia_paragraph_TextStyle__1nGetHeight
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_paragraph_TextStyle__1nGetHeight");
}

#if 0
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_paragraph_TextStyle__1nGetHeight
  (KNativePointer ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    return instance->getHeightOverride() ? javaFloat(env, instance->getHeight()) : nullptr;
}
#endif


SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nSetHeight
  (KNativePointer ptr, KBoolean override, KFloat height) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    instance->setHeightOverride(override);
    instance->setHeight(height);
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


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_paragraph_TextStyle__1nGetLocale
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_paragraph_TextStyle__1nGetLocale");
}

#if 0
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_paragraph_TextStyle__1nGetLocale
  (KNativePointer ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    return javaString(env, instance->getLocale());
}
#endif



SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nSetLocale
  (KNativePointer ptr, KInteropPointer locale) {
    TODO("implement org_jetbrains_skia_paragraph_TextStyle__1nSetLocale");
}

#if 0
SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextStyle__1nSetLocale
  (KNativePointer ptr, KInteropPointer locale) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(ptr);
    instance->setLocale(skString(env, locale));
}
#endif


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
    fontMetrics[11] = m.fUnderlineThickness;
    fontMetrics[12] = m.fUnderlinePosition;
    fontMetrics[13] = m.fStrikeoutThickness;
    fontMetrics[14] = m.fStrikeoutPosition;
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
