
// This file has been auto generated.

#include <iostream>
#include <vector>
#include "TextStyle.h"
using namespace std;
using namespace skia::textlayout;
#include "common.h"

extern "C" jlong org_jetbrains_skia_paragraph_TextStyleKt__1nMake
  (kref __Kinstance) {
    TextStyle* instance = new TextStyle();
    return reinterpret_cast<jlong>(instance);
}

static void deleteTextStyle(TextStyle* instance) {
    delete instance;
}

extern "C" jlong org_jetbrains_skia_paragraph_TextStyleKt__1nGetFinalizer
  (kref __Kinstance) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteTextStyle));
}

extern "C" jboolean org_jetbrains_skia_paragraph_TextStyleKt__1nEquals
  (kref __Kinstance, jlong ptr, jlong otherPtr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    TextStyle* other = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(otherPtr));
    return instance->equals(*other);
}

extern "C" jboolean org_jetbrains_skia_paragraph_TextStyleKt__1nAttributeEquals
  (kref __Kinstance, jlong ptr, jint attribute, jlong otherPtr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    TextStyle* other = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(otherPtr));
    if (attribute == static_cast<jint>(StyleType::kWordSpacing) + 1) // FONT_EXACT
        return instance->equalsByFonts(*other);
    else
        return instance->matchOneAttribute(static_cast<StyleType>(attribute), *other);
}

extern "C" jint org_jetbrains_skia_paragraph_TextStyleKt__1nGetColor
  (kref __Kinstance, jlong ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    return instance->getColor();
}

extern "C" void org_jetbrains_skia_paragraph_TextStyleKt__1nSetColor
  (kref __Kinstance, jlong ptr, jint color) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    instance->setColor(color);
}

extern "C" jlong org_jetbrains_skia_paragraph_TextStyleKt__1nGetForeground
  (kref __Kinstance, jlong ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    return instance->hasForeground() ? reinterpret_cast<jlong>(new SkPaint(instance->getForeground())) : 0;
}

extern "C" void org_jetbrains_skia_paragraph_TextStyleKt__1nSetForeground
  (kref __Kinstance, jlong ptr, jlong paintPtr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    if (paintPtr == 0)
        instance->clearForegroundColor();
    else {
        SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
        instance->setForegroundColor(*paint);
    }
}

extern "C" jlong org_jetbrains_skia_paragraph_TextStyleKt__1nGetBackground
  (kref __Kinstance, jlong ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    return instance->hasBackground() ? reinterpret_cast<jlong>(new SkPaint(instance->getBackground())) : 0;
}

extern "C" void org_jetbrains_skia_paragraph_TextStyleKt__1nSetBackground
  (kref __Kinstance, jlong ptr, jlong paintPtr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    if (paintPtr == 0)
        instance->clearBackgroundColor();
    else {
        SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
        instance->setBackgroundColor(*paint);
    }
}


extern "C" jobject org_jetbrains_skia_paragraph_TextStyleKt__1nGetDecorationStyle
  (kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_paragraph_TextStyleKt__1nGetDecorationStyle");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_paragraph_TextStyleKt__1nGetDecorationStyle
  (kref __Kinstance, jlong ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    Decoration d = instance->getDecoration();
    return env->NewObject(skija::paragraph::DecorationStyle::cls, skija::paragraph::DecorationStyle::ctor,
      (d.fType & TextDecoration::kUnderline) != 0,
      (d.fType & TextDecoration::kOverline) != 0,
      (d.fType & TextDecoration::kLineThrough) != 0,
      d.fMode == TextDecorationMode::kGaps,
      d.fColor,
      static_cast<jint>(d.fStyle),
      d.fThicknessMultiplier); 
}
#endif


extern "C" void org_jetbrains_skia_paragraph_TextStyleKt__1nSetDecorationStyle
  (kref __Kinstance, jlong ptr, jboolean underline, jboolean overline, jboolean lineThrough, jboolean gaps, jint color, jint style, jfloat thicknessMultiplier) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
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


extern "C" jint org_jetbrains_skia_paragraph_TextStyleKt__1nGetFontStyle
  (kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_paragraph_TextStyleKt__1nGetFontStyle");
}
     
#if 0 
extern "C" jint org_jetbrains_skia_paragraph_TextStyleKt__1nGetFontStyle
  (kref __Kinstance, jlong ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    return skija::FontStyle::toJava(instance->getFontStyle());
}
#endif



extern "C" void org_jetbrains_skia_paragraph_TextStyleKt__1nSetFontStyle
  (kref __Kinstance, jlong ptr, jint fontStyleValue) {
    TODO("implement org_jetbrains_skia_paragraph_TextStyleKt__1nSetFontStyle");
}
     
#if 0 
extern "C" void org_jetbrains_skia_paragraph_TextStyleKt__1nSetFontStyle
  (kref __Kinstance, jlong ptr, jint fontStyleValue) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    instance->setFontStyle(skija::FontStyle::fromJava(fontStyleValue));
}
#endif



extern "C" jobjectArray org_jetbrains_skia_paragraph_TextStyleKt__1nGetShadows
  (kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_paragraph_TextStyleKt__1nGetShadows");
}
     
#if 0 
extern "C" jobjectArray org_jetbrains_skia_paragraph_TextStyleKt__1nGetShadows
  (kref __Kinstance, jlong ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    std::vector<TextShadow> shadows = instance->getShadows();
    jobjectArray shadowsArr = env->NewObjectArray((jsize) shadows.size(), skija::paragraph::Shadow::cls, nullptr);
    for (int i = 0; i < shadows.size(); ++i) {
        const TextShadow& s = shadows[i];
        skija::AutoLocal<jobject> shadowObj(env, env->NewObject(skija::paragraph::Shadow::cls, skija::paragraph::Shadow::ctor, s.fColor, s.fOffset.fX, s.fOffset.fY, s.fBlurSigma));
        env->SetObjectArrayElement(shadowsArr, i, shadowObj.get());
    }
    return shadowsArr;
}
#endif


extern "C" void org_jetbrains_skia_paragraph_TextStyleKt__1nAddShadow
  (kref __Kinstance, jlong ptr, jint color, jfloat offsetX, jfloat offsetY, jdouble blurSigma) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    instance->addShadow({static_cast<SkColor>(color), {offsetX, offsetY}, blurSigma});
}

extern "C" void org_jetbrains_skia_paragraph_TextStyleKt__1nClearShadows
  (kref __Kinstance, jlong ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    instance->resetShadows();
}


extern "C" jobjectArray org_jetbrains_skia_paragraph_TextStyleKt__1nGetFontFeatures
  (kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_paragraph_TextStyleKt__1nGetFontFeatures");
}
     
#if 0 
extern "C" jobjectArray org_jetbrains_skia_paragraph_TextStyleKt__1nGetFontFeatures
  (kref __Kinstance, jlong ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    std::vector<FontFeature> fontFeatures = instance->getFontFeatures();
    jobjectArray fontFeaturesArr = env->NewObjectArray((jsize) fontFeatures.size(), skija::FontFeature::cls, nullptr);
    for (int i = 0; i < fontFeatures.size(); ++i) {
        const FontFeature& ff = fontFeatures[i];
        auto featureObj = env->NewObject(skija::FontFeature::cls, skija::FontFeature::ctor, javaString(env, ff.fName), ff.fValue);
        env->SetObjectArrayElement(fontFeaturesArr, i, featureObj);
        env->DeleteLocalRef(featureObj);
    }
    return fontFeaturesArr;
}
#endif



extern "C" void org_jetbrains_skia_paragraph_TextStyleKt__1nAddFontFeature
  (kref __Kinstance, jlong ptr, jstring nameStr, jint value) {
    TODO("implement org_jetbrains_skia_paragraph_TextStyleKt__1nAddFontFeature");
}
     
#if 0 
extern "C" void org_jetbrains_skia_paragraph_TextStyleKt__1nAddFontFeature
  (kref __Kinstance, jlong ptr, jstring nameStr, jint value) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    instance->addFontFeature(skString(env, nameStr), value);
}
#endif


extern "C" void org_jetbrains_skia_paragraph_TextStyleKt__1nClearFontFeatures
  (kref __Kinstance, jlong ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    instance->resetFontFeatures();
}

extern "C" jfloat org_jetbrains_skia_paragraph_TextStyleKt__1nGetFontSize
  (kref __Kinstance, jlong ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    return instance->getFontSize();
}

extern "C" void org_jetbrains_skia_paragraph_TextStyleKt__1nSetFontSize
  (kref __Kinstance, jlong ptr, jfloat size) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    instance->setFontSize(size);
}


extern "C" jobjectArray org_jetbrains_skia_paragraph_TextStyleKt__1nGetFontFamilies
  (kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_paragraph_TextStyleKt__1nGetFontFamilies");
}
     
#if 0 
extern "C" jobjectArray org_jetbrains_skia_paragraph_TextStyleKt__1nGetFontFamilies
  (kref __Kinstance, jlong ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    return javaStringArray(env, instance->getFontFamilies());
}
#endif



extern "C" void org_jetbrains_skia_paragraph_TextStyleKt__1nSetFontFamilies
  (kref __Kinstance, jlong ptr, jobjectArray familiesArray) {
    TODO("implement org_jetbrains_skia_paragraph_TextStyleKt__1nSetFontFamilies");
}
     
#if 0 
extern "C" void org_jetbrains_skia_paragraph_TextStyleKt__1nSetFontFamilies
  (kref __Kinstance, jlong ptr, jobjectArray familiesArray) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    instance->setFontFamilies(skStringVector(env, familiesArray));
}
#endif



extern "C" jobject org_jetbrains_skia_paragraph_TextStyleKt__1nGetHeight
  (kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_paragraph_TextStyleKt__1nGetHeight");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_paragraph_TextStyleKt__1nGetHeight
  (kref __Kinstance, jlong ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    return instance->getHeightOverride() ? javaFloat(env, instance->getHeight()) : nullptr;
}
#endif


extern "C" void org_jetbrains_skia_paragraph_TextStyleKt__1nSetHeight
  (kref __Kinstance, jlong ptr, jboolean override, jfloat height) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    instance->setHeightOverride(override);
    instance->setHeight(height);
}

extern "C" jfloat org_jetbrains_skia_paragraph_TextStyleKt__1nGetLetterSpacing
  (kref __Kinstance, jlong ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    return instance->getLetterSpacing();
}

extern "C" void org_jetbrains_skia_paragraph_TextStyleKt__1nSetLetterSpacing
  (kref __Kinstance, jlong ptr, jfloat letterSpacing) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    instance->setLetterSpacing(letterSpacing);
}

extern "C" jfloat org_jetbrains_skia_paragraph_TextStyleKt__1nGetWordSpacing
  (kref __Kinstance, jlong ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    return instance->getWordSpacing();
}

extern "C" void org_jetbrains_skia_paragraph_TextStyleKt__1nSetWordSpacing
  (kref __Kinstance, jlong ptr, jfloat wordSpacing) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    instance->setWordSpacing(wordSpacing);
}

extern "C" jlong org_jetbrains_skia_paragraph_TextStyleKt__1nGetTypeface
  (kref __Kinstance, jlong ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(instance->refTypeface().release());
}

extern "C" void org_jetbrains_skia_paragraph_TextStyleKt__1nSetTypeface
  (kref __Kinstance, jlong ptr, jlong typefacePtr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(typefacePtr));
    instance->setTypeface(sk_ref_sp(typeface));
}


extern "C" jstring org_jetbrains_skia_paragraph_TextStyleKt__1nGetLocale
  (kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_paragraph_TextStyleKt__1nGetLocale");
}
     
#if 0 
extern "C" jstring org_jetbrains_skia_paragraph_TextStyleKt__1nGetLocale
  (kref __Kinstance, jlong ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    return javaString(env, instance->getLocale());
}
#endif



extern "C" void org_jetbrains_skia_paragraph_TextStyleKt__1nSetLocale
  (kref __Kinstance, jlong ptr, jstring locale) {
    TODO("implement org_jetbrains_skia_paragraph_TextStyleKt__1nSetLocale");
}
     
#if 0 
extern "C" void org_jetbrains_skia_paragraph_TextStyleKt__1nSetLocale
  (kref __Kinstance, jlong ptr, jstring locale) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    instance->setLocale(skString(env, locale));
}
#endif


extern "C" jint org_jetbrains_skia_paragraph_TextStyleKt__1nGetBaselineMode
  (kref __Kinstance, jlong ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    return static_cast<jint>(instance->getTextBaseline());
}

extern "C" void org_jetbrains_skia_paragraph_TextStyleKt__1nSetBaselineMode
  (kref __Kinstance, jlong ptr, jint baselineModeValue) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    instance->setTextBaseline(static_cast<TextBaseline>(baselineModeValue));
}


extern "C" jobject org_jetbrains_skia_paragraph_TextStyleKt__1nGetFontMetrics
  (kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_paragraph_TextStyleKt__1nGetFontMetrics");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_paragraph_TextStyleKt__1nGetFontMetrics
  (kref __Kinstance, jlong ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    SkFontMetrics m;
    instance->getFontMetrics(&m);
    return skija::FontMetrics::toJava(env, m);
}
#endif


extern "C" jboolean org_jetbrains_skia_paragraph_TextStyleKt__1nIsPlaceholder
  (kref __Kinstance, jlong ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    return instance->isPlaceholder();
}

extern "C" void org_jetbrains_skia_paragraph_TextStyleKt__1nSetPlaceholder
  (kref __Kinstance, jlong ptr) {
    TextStyle* instance = reinterpret_cast<TextStyle*>(static_cast<uintptr_t>(ptr));
    instance->setPlaceholder();
}
