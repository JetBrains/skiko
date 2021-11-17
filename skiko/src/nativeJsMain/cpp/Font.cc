
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
  () {
    return reinterpret_cast<KNativePointer>(&deleteFont);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Font__1nMakeDefault
  () {
    SkFont* obj = new SkFont();
    return reinterpret_cast<KNativePointer>(obj);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Font__1nMakeTypeface
  (KNativePointer typefacePtr) {
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>(typefacePtr);
    SkFont* obj = new SkFont(sk_ref_sp<SkTypeface>(typeface));
    return reinterpret_cast<KNativePointer>(obj);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Font__1nMakeTypefaceSize
  (KNativePointer typefacePtr, KFloat size) {
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>(typefacePtr);
    SkFont* obj = new SkFont(sk_ref_sp<SkTypeface>(typeface), size);
    return reinterpret_cast<KNativePointer>(obj);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Font__1nMakeTypefaceSizeScaleSkew
  (KNativePointer typefacePtr, KFloat size, KFloat scaleX, KFloat skewX) {
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>(typefacePtr);
    SkFont* obj = new SkFont(sk_ref_sp<SkTypeface>(typeface), size, scaleX, skewX);
    return reinterpret_cast<KNativePointer>(obj);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Font__1nMakeClone
  (KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    SkFont* clone = new SkFont(*instance);
    return reinterpret_cast<KNativePointer>(clone);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Font__1nEquals
  (KNativePointer ptr, KNativePointer otherPtr) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    SkFont* other = reinterpret_cast<SkFont*>(otherPtr);
    return *instance == *other;
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Font__1nIsAutoHintingForced
  (KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    return instance->isForceAutoHinting();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Font__1nAreBitmapsEmbedded
  (KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    return instance->isEmbeddedBitmaps();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Font__1nIsSubpixel
  (KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    return instance->isSubpixel();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Font__1nAreMetricsLinear
  (KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    return instance->isLinearMetrics();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Font__1nIsEmboldened
  (KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    return instance->isEmbolden();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Font__1nIsBaselineSnapped
  (KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    return instance->isBaselineSnap();
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nSetAutoHintingForced
  (KNativePointer ptr, KBoolean value) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    instance->setForceAutoHinting(value);
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nSetBitmapsEmbedded
  (KNativePointer ptr, KBoolean value) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    instance->setEmbeddedBitmaps(value);
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nSetSubpixel
  (KNativePointer ptr, KBoolean value) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    instance->setSubpixel(value);
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nSetMetricsLinear
  (KNativePointer ptr, KBoolean value) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    instance->setLinearMetrics(value);
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nSetEmboldened
  (KNativePointer ptr, KBoolean value) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    instance->setEmbolden(value);
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nSetBaselineSnapped
  (KNativePointer ptr, KBoolean value) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    instance->setBaselineSnap(value);
}

SKIKO_EXPORT KInt org_jetbrains_skia_Font__1nGetEdging
 (KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    return static_cast<KInt>(instance->getEdging());
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nSetEdging
 (KNativePointer ptr, KInt value) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    instance->setEdging(static_cast<SkFont::Edging>(value));
}

SKIKO_EXPORT KInt org_jetbrains_skia_Font__1nGetHinting
 (KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    return static_cast<KInt>(instance->getHinting());
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nSetHinting
 (KNativePointer ptr, KInt value) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    instance->setHinting(static_cast<SkFontHinting>(value));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Font__1nGetTypeface
 (KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    SkTypeface* typeface = instance->refTypeface().release();
    return reinterpret_cast<KNativePointer>(typeface);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Font__1nGetTypefaceOrDefault
 (KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    SkTypeface* typeface = instance->refTypefaceOrDefault().release();
    return reinterpret_cast<KNativePointer>(typeface);
}

SKIKO_EXPORT KFloat org_jetbrains_skia_Font__1nGetSize
 (KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    return instance->getSize();
}

SKIKO_EXPORT KFloat org_jetbrains_skia_Font__1nGetScaleX
 (KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    return instance->getScaleX();
}

SKIKO_EXPORT KFloat org_jetbrains_skia_Font__1nGetSkewX
 (KNativePointer ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    return instance->getSkewX();
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nSetTypeface
 (KNativePointer ptr, KNativePointer typefacePtr) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>(typefacePtr);
    instance->setTypeface(sk_ref_sp(typeface));
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nSetSize
 (KNativePointer ptr, KFloat value) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    instance->setSize(value);
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nSetScaleX
 (KNativePointer ptr, KFloat value) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    instance->setScaleX(value);
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nSetSkewX
 (KNativePointer ptr, KFloat value) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    instance->setSkewX(value);
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nGetUTF32Glyphs
  (KNativePointer ptr, KInt* uniArr, KInt uniArrLen, KShort* resultGlyphs) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    instance->unicharsToGlyphs(
        reinterpret_cast<SkUnichar*>(uniArr), uniArrLen,
        reinterpret_cast<SkGlyphID*>(resultGlyphs)
    );
}

SKIKO_EXPORT KShort org_jetbrains_skia_Font__1nGetUTF32Glyph
  (KNativePointer ptr, KInt uni) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    return instance->unicharToGlyph(uni);
}

SKIKO_EXPORT KInt org_jetbrains_skia_Font__1nGetStringGlyphsCount
  (KNativePointer ptr, char* str, KInt len) {
  SkFont* instance = reinterpret_cast<SkFont*>(ptr);
  return instance->countText(str, len * sizeof(KChar), SkTextEncoding::kUTF16);
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nMeasureText
  (KNativePointer ptr, KInteropPointer str, KInt len, KNativePointer paintPtr, KFloat* res) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    SkPaint* paint = reinterpret_cast<SkPaint*>(paintPtr);
    SkRect bounds;
    instance->measureText(str, len * sizeof(KChar), SkTextEncoding::kUTF16, &bounds, paint);

    res[0] = bounds.left();
    res[1] = bounds.top();
    res[2] = bounds.right();
    res[3] = bounds.bottom();
}


SKIKO_EXPORT KFloat org_jetbrains_skia_Font__1nMeasureTextWidth
  (KNativePointer ptr, char* str, KInt len, KNativePointer paintPtr) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    SkPaint* paint = reinterpret_cast<SkPaint*>(paintPtr);
    return instance->measureText(str, len * sizeof(KChar), SkTextEncoding::kUTF16, nullptr, paint);
}


SKIKO_EXPORT void org_jetbrains_skia_Font__1nGetWidths
  (KNativePointer ptr, KShort* glyphs, KInt count, KFloat* widths) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    instance->getWidths(reinterpret_cast<SkGlyphID*>(glyphs), count, widths);
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nGetBounds
  (KNativePointer ptr, KShort* glyphs, KInt count, KNativePointer paintPtr, KFloat* res) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    SkPaint* paint = reinterpret_cast<SkPaint*>(paintPtr);
    std::vector<SkRect> bounds(count);
    instance->getBounds(reinterpret_cast<SkGlyphID*>(glyphs), count, bounds.data(), paint);

    for (int i = 0; i < count; ++i) {
        SkRect b = bounds[i];
        res[4*i] = b.left();
        res[4*i + 1] = b.right();
        res[4*i + 2] = b.top();
        res[4*i + 3] = b.bottom();
    }
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nGetPositions
  (KNativePointer ptr, KShort* glyphs, KInt count, KFloat dx, KFloat dy, KFloat* res) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);

    std::vector<SkPoint> positions(count);
    instance->getPos(reinterpret_cast<SkGlyphID*>(glyphs), count, positions.data(), {dx, dy});

    for (int i = 0; i < count; i++) {
        res[2*i] = positions[i].fX;
        res[2*i + 1] = positions[i].fY;
    }
}

SKIKO_EXPORT void org_jetbrains_skia_Font__1nGetXPositions
  (KNativePointer ptr, KShort* glyphs, KFloat dx, KInt count, KFloat* positions) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    instance->getXPos(reinterpret_cast<SkGlyphID*>(glyphs), count, positions, dx);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Font__1nGetPath
  (KNativePointer ptr, KShort glyph) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    SkPath* path = new SkPath();
    instance->getPath(glyph, path);
    return reinterpret_cast<KNativePointer>(path);
}


SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_Font__1nGetPaths
  (KNativePointer ptr, KShort* glyphsArr) {
    TODO("implement org_jetbrains_skia_Font__1nGetPaths");
}

#if 0
SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_Font__1nGetPaths
  (KNativePointer ptr, KShort* glyphsArr) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    int count = env->GetArrayLength(glyphsArr);
    KShort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);

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



SKIKO_EXPORT void org_jetbrains_skia_Font__1nGetMetrics
  (KNativePointer ptr, KFloat* fontMetrics) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    SkFontMetrics m;
    instance->getMetrics(&m);

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


SKIKO_EXPORT KFloat org_jetbrains_skia_Font__1nGetSpacing
  (KNativePointer ptr, KShort* glyphsArr) {
    SkFont* instance = reinterpret_cast<SkFont*>(ptr);
    return instance->getSpacing();
}
