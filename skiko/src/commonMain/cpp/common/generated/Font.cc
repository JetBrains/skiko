
// This file has been auto generated.

#include <iostream>
#include "SkFont.h"
#include "SkPath.h"
#include "SkShaper.h"
#include "common.h"

static void deleteFont(SkFont* font) {
    delete font;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Font__1nGetFinalizer
  (KInteropPointer __Kinstance) {
    return reinterpret_cast<KNativePointer>((&deleteFont));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Font__1nMakeDefault
  (KInteropPointer __Kinstance) {
    SkFont* obj = new SkFont();
    return reinterpret_cast<KNativePointer>(obj);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Font__1nMakeTypeface
  (KInteropPointer __Kinstance, KNativePointer typefacePtr) {
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>((typefacePtr));
    SkFont* obj = new SkFont(sk_ref_sp<SkTypeface>(typeface));
    return reinterpret_cast<KNativePointer>(obj);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Font__1nMakeTypefaceSize
  (KInteropPointer __Kinstance, KNativePointer typefacePtr, KFloat size) {
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>((typefacePtr));
    SkFont* obj = new SkFont(sk_ref_sp<SkTypeface>(typeface), size);
    return reinterpret_cast<KNativePointer>(obj);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Font__1nMakeTypefaceSizeScaleSkew
  (KInteropPointer __Kinstance, KNativePointer typefacePtr, KFloat size, KFloat scaleX, KFloat skewX) {
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>((typefacePtr));
    SkFont* obj = new SkFont(sk_ref_sp<SkTypeface>(typeface), size, scaleX, skewX);
    return reinterpret_cast<KNativePointer>(obj);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Font__1nMakeClone
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    SkFont* clone = new SkFont(*instance);
    return reinterpret_cast<KNativePointer>(clone);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Font__1nEquals
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer otherPtr) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    SkFont* other = reinterpret_cast<SkFont*>((otherPtr));
    return *instance == *other;
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Font__1nIsAutoHintingForced
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    return instance->isForceAutoHinting();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Font__1nAreBitmapsEmbedded
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    return instance->isEmbeddedBitmaps();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Font__1nIsSubpixel
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    return instance->isSubpixel();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Font__1nAreMetricsLinear
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    return instance->isLinearMetrics();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Font__1nIsEmboldened
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    return instance->isEmbolden();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Font__1nIsBaselineSnapped
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    return instance->isBaselineSnap();
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nSetAutoHintingForced
  (KInteropPointer __Kinstance, KNativePointer ptr, KBoolean value) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    instance->setForceAutoHinting(value);
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nSetBitmapsEmbedded
  (KInteropPointer __Kinstance, KNativePointer ptr, KBoolean value) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    instance->setEmbeddedBitmaps(value);
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nSetSubpixel
  (KInteropPointer __Kinstance, KNativePointer ptr, KBoolean value) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    instance->setSubpixel(value);
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nSetMetricsLinear
  (KInteropPointer __Kinstance, KNativePointer ptr, KBoolean value) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    instance->setLinearMetrics(value);
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nSetEmboldened
  (KInteropPointer __Kinstance, KNativePointer ptr, KBoolean value) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    instance->setEmbolden(value);
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nSetBaselineSnapped
  (KInteropPointer __Kinstance, KNativePointer ptr, KBoolean value) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    instance->setBaselineSnap(value);
}

SKIKO_EXPORT KInt org_jetbrains_skia_Font__1nGetEdging
 (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    return static_cast<KInt>(instance->getEdging());
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nSetEdging
 (KInteropPointer __Kinstance, KNativePointer ptr, KInt value) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    instance->setEdging(static_cast<SkFont::Edging>(value));
}

SKIKO_EXPORT KInt org_jetbrains_skia_Font__1nGetHinting
 (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    return static_cast<KInt>(instance->getHinting());
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nSetHinting
 (KInteropPointer __Kinstance, KNativePointer ptr, KInt value) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    instance->setHinting(static_cast<SkFontHinting>(value));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Font__1nGetTypeface
 (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    SkTypeface* typeface = instance->refTypeface().release();
    return reinterpret_cast<KNativePointer>(typeface);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Font__1nGetTypefaceOrDefault
 (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    SkTypeface* typeface = instance->refTypefaceOrDefault().release();
    return reinterpret_cast<KNativePointer>(typeface);
}

SKIKO_EXPORT KFloat org_jetbrains_skia_Font__1nGetSize
 (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    return instance->getSize();
}

SKIKO_EXPORT KFloat org_jetbrains_skia_Font__1nGetScaleX
 (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    return instance->getScaleX();
}

SKIKO_EXPORT KFloat org_jetbrains_skia_Font__1nGetSkewX
 (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    return instance->getSkewX();
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nSetTypeface
 (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer typefacePtr) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>((typefacePtr));
    instance->setTypeface(sk_ref_sp(typeface));
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nSetSize
 (KInteropPointer __Kinstance, KNativePointer ptr, KFloat value) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    instance->setSize(value);
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nSetScaleX
 (KInteropPointer __Kinstance, KNativePointer ptr, KFloat value) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    instance->setScaleX(value);
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nSetSkewX
 (KInteropPointer __Kinstance, KNativePointer ptr, KFloat value) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    instance->setSkewX(value);
}


SKIKO_EXPORT KShort* org_jetbrains_skia_Font__1nGetStringGlyphs
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointer str) {
    TODO("implement org_jetbrains_skia_Font__1nGetStringGlyphs");
}
     
#if 0 
SKIKO_EXPORT KShort* org_jetbrains_skia_Font__1nGetStringGlyphs
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointer str) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    jsize len = env->GetStringLength(str);
    const jchar* chars = env->GetStringCritical(str, nullptr);
    int count = instance->textToGlyphs(chars, len * sizeof(jchar), SkTextEncoding::kUTF16, nullptr, 0);
    std::vector<short> glyphs(count);
    instance->textToGlyphs(chars, len * sizeof(jchar), SkTextEncoding::kUTF16, reinterpret_cast<SkGlyphID*>(glyphs.data()), count);
    env->ReleaseStringCritical(str, chars);
    return javaShortArray(env, glyphs);
}
#endif



SKIKO_EXPORT KShort* org_jetbrains_skia_Font__1nGetUTF32Glyphs
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt* uniArr) {
    TODO("implement org_jetbrains_skia_Font__1nGetUTF32Glyphs");
}
     
#if 0 
SKIKO_EXPORT KShort* org_jetbrains_skia_Font__1nGetUTF32Glyphs
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt* uniArr) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    int count = env->GetArrayLength(uniArr);
    std::vector<jshort> glyphs(count);
    KInt* uni = env->GetIntArrayElements(uniArr, nullptr);
    instance->unicharsToGlyphs(reinterpret_cast<SkUnichar*>(uni), count, reinterpret_cast<SkGlyphID*>(glyphs.data()));
    env->ReleaseIntArrayElements(uniArr, uni, 0);
    return javaShortArray(env, glyphs);
}
#endif


SKIKO_EXPORT jshort org_jetbrains_skia_Font__1nGetUTF32Glyph
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt uni) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    return instance->unicharToGlyph(uni);
}


SKIKO_EXPORT KInt org_jetbrains_skia_Font__1nGetStringGlyphsCount
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointer str) {
    TODO("implement org_jetbrains_skia_Font__1nGetStringGlyphsCount");
}
     
#if 0 
SKIKO_EXPORT KInt org_jetbrains_skia_Font__1nGetStringGlyphsCount
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointer str) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    jsize len = env->GetStringLength(str);
    const jchar* chars = env->GetStringCritical(str, nullptr);
    int count = instance->countText(chars, len * sizeof(jchar), SkTextEncoding::kUTF16);
    env->ReleaseStringCritical(str, chars);
    return count;
}
#endif



SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Font__1nMeasureText
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointer str, KNativePointer paintPtr) {
    TODO("implement org_jetbrains_skia_Font__1nMeasureText");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Font__1nMeasureText
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointer str, KNativePointer paintPtr) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    jsize len = env->GetStringLength(str);
    const jchar* chars = env->GetStringCritical(str, nullptr);
    SkRect bounds;
    instance->measureText(chars, len * sizeof(jchar), SkTextEncoding::kUTF16, &bounds, paint);
    env->ReleaseStringCritical(str, chars);
    return skija::Rect::fromSkRect(env, bounds);
}
#endif



SKIKO_EXPORT KFloat org_jetbrains_skia_Font__1nMeasureTextWidth
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointer str, KNativePointer paintPtr) {
    TODO("implement org_jetbrains_skia_Font__1nMeasureTextWidth");
}
     
#if 0 
SKIKO_EXPORT KFloat org_jetbrains_skia_Font__1nMeasureTextWidth
  (KInteropPointer __Kinstance, KNativePointer ptr, KInteropPointer str, KNativePointer paintPtr) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    jsize len = env->GetStringLength(str);
    const jchar* chars = env->GetStringCritical(str, nullptr);
    float width = instance->measureText(chars, len * sizeof(jchar), SkTextEncoding::kUTF16, nullptr, paint);
    env->ReleaseStringCritical(str, chars);
    return width;
}
#endif



SKIKO_EXPORT KFloat* org_jetbrains_skia_Font__1nGetWidths
  (KInteropPointer __Kinstance, KNativePointer ptr, KShort* glyphsArr) {
    TODO("implement org_jetbrains_skia_Font__1nGetWidths");
}
     
#if 0 
SKIKO_EXPORT KFloat* org_jetbrains_skia_Font__1nGetWidths
  (KInteropPointer __Kinstance, KNativePointer ptr, KShort* glyphsArr) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    int count = env->GetArrayLength(glyphsArr);
    std::vector<KFloat> widths(count);
    jshort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);
    instance->getWidths(reinterpret_cast<SkGlyphID*>(glyphs), count, widths.data());
    env->ReleaseShortArrayElements(glyphsArr, glyphs, 0);
    return javaFloatArray(env, widths);
}
#endif



SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_Font__1nGetBounds
  (KInteropPointer __Kinstance, KNativePointer ptr, KShort* glyphsArr, KNativePointer paintPtr) {
    TODO("implement org_jetbrains_skia_Font__1nGetBounds");
}
     
#if 0 
SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_Font__1nGetBounds
  (KInteropPointer __Kinstance, KNativePointer ptr, KShort* glyphsArr, KNativePointer paintPtr) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    int count = env->GetArrayLength(glyphsArr);
    std::vector<SkRect> bounds(count);
    jshort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);
    instance->getBounds(reinterpret_cast<SkGlyphID*>(glyphs), count, bounds.data(), paint);
    env->ReleaseShortArrayElements(glyphsArr, glyphs, 0);

    KInteropPointerArray res = env->NewObjectArray(count, skija::Rect::cls, nullptr);
    for (int i = 0; i < count; ++i) {
        skija::AutoLocal<KInteropPointer> boundsObj(env, skija::Rect::fromSkRect(env, bounds[i]));
        env->SetObjectArrayElement(res, i, boundsObj.get());
    }

    return res;
}
#endif



SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_Font__1nGetPositions
  (KInteropPointer __Kinstance, KNativePointer ptr, KShort* glyphsArr, KFloat dx, KFloat dy) {
    TODO("implement org_jetbrains_skia_Font__1nGetPositions");
}
     
#if 0 
SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_Font__1nGetPositions
  (KInteropPointer __Kinstance, KNativePointer ptr, KShort* glyphsArr, KFloat dx, KFloat dy) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    
    int count = env->GetArrayLength(glyphsArr);
    std::vector<SkPoint> positions(count);
    jshort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);
    instance->getPos(reinterpret_cast<SkGlyphID*>(glyphs), count, positions.data(), {dx, dy});
    env->ReleaseShortArrayElements(glyphsArr, glyphs, 0);

    return skija::Point::fromSkPoints(env, positions);
}
#endif



SKIKO_EXPORT KFloat* org_jetbrains_skia_Font__1nGetXPositions
  (KInteropPointer __Kinstance, KNativePointer ptr, KShort* glyphsArr, KFloat dx) {
    TODO("implement org_jetbrains_skia_Font__1nGetXPositions");
}
     
#if 0 
SKIKO_EXPORT KFloat* org_jetbrains_skia_Font__1nGetXPositions
  (KInteropPointer __Kinstance, KNativePointer ptr, KShort* glyphsArr, KFloat dx) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    int count = env->GetArrayLength(glyphsArr);
    std::vector<KFloat> positions(count);
    jshort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);
    instance->getXPos(reinterpret_cast<SkGlyphID*>(glyphs), count, positions.data(), dx);
    env->ReleaseShortArrayElements(glyphsArr, glyphs, 0);
    return javaFloatArray(env, positions);
}
#endif


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Font__1nGetPath
  (KInteropPointer __Kinstance, KNativePointer ptr, jshort glyph) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    SkPath* path = new SkPath();
    instance->getPath(glyph, path);
    return reinterpret_cast<KNativePointer>(path);
}


SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_Font__1nGetPaths
  (KInteropPointer __Kinstance, KNativePointer ptr, KShort* glyphsArr) {
    TODO("implement org_jetbrains_skia_Font__1nGetPaths");
}
     
#if 0 
SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_Font__1nGetPaths
  (KInteropPointer __Kinstance, KNativePointer ptr, KShort* glyphsArr) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    
    int count = env->GetArrayLength(glyphsArr);
    jshort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);

    struct Ctx {
        KInteropPointerArray paths;
        jsize        idx;
        JNIEnv*      env;
    } ctx = { env->NewObjectArray(count, skija::Path::cls, nullptr), 0, env };

    instance->getPaths(reinterpret_cast<SkGlyphID*>(glyphs), count, [](const SkPath* orig, const SkMatrix& mx, void* voidCtx) {
        Ctx* ctx = static_cast<Ctx*>(voidCtx);
        if (orig) {
            SkPath* path = new SkPath();
            orig->transform(mx, path);
            KInteropPointer pathObj = ctx->env->NewObject(skija::Path::cls, skija::Path::ctor, reinterpret_cast<KNativePointer>(path));
            ctx->env->SetObjectArrayElement(ctx->paths, ctx->idx, pathObj);
            ctx->env->DeleteLocalRef(pathObj);
            ++ctx->idx;
        }
    }, &ctx);

    env->ReleaseShortArrayElements(glyphsArr, glyphs, 0);
    return ctx.paths;
}
#endif



SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Font__1nGetMetrics
  (KInteropPointer __Kinstance, KNativePointer ptr, KShort* glyphsArr) {
    TODO("implement org_jetbrains_skia_Font__1nGetMetrics");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Font__1nGetMetrics
  (KInteropPointer __Kinstance, KNativePointer ptr, KShort* glyphsArr) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    SkFontMetrics m;
    instance->getMetrics(&m);
    return skija::FontMetrics::toJava(env, m);
}
#endif



SKIKO_EXPORT KFloat org_jetbrains_skia_Font__1nGetSpacing
  (KInteropPointer __Kinstance, KNativePointer ptr, KShort* glyphsArr) {
    TODO("implement org_jetbrains_skia_Font__1nGetSpacing");
}
     
#if 0 
SKIKO_EXPORT KFloat org_jetbrains_skia_Font__1nGetSpacing
  (KInteropPointer __Kinstance, KNativePointer ptr, KShort* glyphsArr) {
    SkFont* instance = reinterpret_cast<SkFont*>((ptr));
    return instance->getSpacing();
}
#endif

