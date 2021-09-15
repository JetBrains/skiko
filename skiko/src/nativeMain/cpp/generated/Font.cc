
// This file has been auto generated.

#include <iostream>
#include "SkFont.h"
#include "SkPath.h"
#include "SkShaper.h"
#include "common.h"

static void deleteFont(SkFont* font) {
    delete font;
}

extern "C" jlong org_jetbrains_skia_Font__1nGetFinalizer
  () {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteFont));
}

extern "C" jlong org_jetbrains_skia_Font__1nMakeDefault
  () {
    SkFont* obj = new SkFont();
    return reinterpret_cast<jlong>(obj);
}

extern "C" jlong org_jetbrains_skia_Font__1nMakeTypeface
  (jlong typefacePtr) {
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(typefacePtr));
    SkFont* obj = new SkFont(sk_ref_sp<SkTypeface>(typeface));
    return reinterpret_cast<jlong>(obj);
}

extern "C" jlong org_jetbrains_skia_Font__1nMakeTypefaceSize
  (jlong typefacePtr, jfloat size) {
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(typefacePtr));
    SkFont* obj = new SkFont(sk_ref_sp<SkTypeface>(typeface), size);
    return reinterpret_cast<jlong>(obj);
}

extern "C" jlong org_jetbrains_skia_Font__1nMakeTypefaceSizeScaleSkew
  (jlong typefacePtr, jfloat size, jfloat scaleX, jfloat skewX) {
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(typefacePtr));
    SkFont* obj = new SkFont(sk_ref_sp<SkTypeface>(typeface), size, scaleX, skewX);
    return reinterpret_cast<jlong>(obj);
}

extern "C" jlong org_jetbrains_skia_Font__1nMakeClone
  (jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    SkFont* clone = new SkFont(*instance);
    return reinterpret_cast<jlong>(clone);
}

extern "C" jboolean org_jetbrains_skia_Font__1nEquals
  (jlong ptr, jlong otherPtr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    SkFont* other = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(otherPtr));
    return *instance == *other;
}

extern "C" jboolean org_jetbrains_skia_Font__1nIsAutoHintingForced
  (jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return instance->isForceAutoHinting();
}

extern "C" jboolean org_jetbrains_skia_Font__1nAreBitmapsEmbedded
  (jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return instance->isEmbeddedBitmaps();
}

extern "C" jboolean org_jetbrains_skia_Font__1nIsSubpixel
  (jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return instance->isSubpixel();
}

extern "C" jboolean org_jetbrains_skia_Font__1nAreMetricsLinear
  (jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return instance->isLinearMetrics();
}

extern "C" jboolean org_jetbrains_skia_Font__1nIsEmboldened
  (jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return instance->isEmbolden();
}

extern "C" jboolean org_jetbrains_skia_Font__1nIsBaselineSnapped
  (jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return instance->isBaselineSnap();
}

extern "C" void org_jetbrains_skia_Font__1nSetAutoHintingForced
  (jlong ptr, jboolean value) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    instance->setForceAutoHinting(value);
}

extern "C" void org_jetbrains_skia_Font__1nSetBitmapsEmbedded
  (jlong ptr, jboolean value) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    instance->setEmbeddedBitmaps(value);
}

extern "C" void org_jetbrains_skia_Font__1nSetSubpixel
  (jlong ptr, jboolean value) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    instance->setSubpixel(value);
}

extern "C" void org_jetbrains_skia_Font__1nSetMetricsLinear
  (jlong ptr, jboolean value) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    instance->setLinearMetrics(value);
}

extern "C" void org_jetbrains_skia_Font__1nSetEmboldened
  (jlong ptr, jboolean value) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    instance->setEmbolden(value);
}

extern "C" void org_jetbrains_skia_Font__1nSetBaselineSnapped
  (jlong ptr, jboolean value) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    instance->setBaselineSnap(value);
}

extern "C" jint org_jetbrains_skia_Font__1nGetEdging
 (jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return static_cast<jint>(instance->getEdging());
}

extern "C" void org_jetbrains_skia_Font__1nSetEdging
 (jlong ptr, jint value) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    instance->setEdging(static_cast<SkFont::Edging>(value));
}

extern "C" jint org_jetbrains_skia_Font__1nGetHinting
 (jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return static_cast<jint>(instance->getHinting());
}

extern "C" void org_jetbrains_skia_Font__1nSetHinting
 (jlong ptr, jint value) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    instance->setHinting(static_cast<SkFontHinting>(value));
}

extern "C" jlong org_jetbrains_skia_Font__1nGetTypeface
 (jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    SkTypeface* typeface = instance->refTypeface().release();
    return reinterpret_cast<jlong>(typeface);
}

extern "C" jlong org_jetbrains_skia_Font__1nGetTypefaceOrDefault
 (jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    SkTypeface* typeface = instance->refTypefaceOrDefault().release();
    return reinterpret_cast<jlong>(typeface);
}

extern "C" jfloat org_jetbrains_skia_Font__1nGetSize
 (jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return instance->getSize();
}

extern "C" jfloat org_jetbrains_skia_Font__1nGetScaleX
 (jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return instance->getScaleX();
}

extern "C" jfloat org_jetbrains_skia_Font__1nGetSkewX
 (jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return instance->getSkewX();
}

extern "C" void org_jetbrains_skia_Font__1nSetTypeface
 (jlong ptr, jlong typefacePtr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(typefacePtr));
    instance->setTypeface(sk_ref_sp(typeface));
}

extern "C" void org_jetbrains_skia_Font__1nSetSize
 (jlong ptr, jfloat value) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    instance->setSize(value);
}

extern "C" void org_jetbrains_skia_Font__1nSetScaleX
 (jlong ptr, jfloat value) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    instance->setScaleX(value);
}

extern "C" void org_jetbrains_skia_Font__1nSetSkewX
 (jlong ptr, jfloat value) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    instance->setSkewX(value);
}


extern "C" jshortArray org_jetbrains_skia_Font__1nGetStringGlyphs
  (jlong ptr, jstring str) {
    TODO("implement org_jetbrains_skia_Font__1nGetStringGlyphs");
}
     
#if 0 
extern "C" jshortArray org_jetbrains_skia_Font__1nGetStringGlyphs
  (jlong ptr, jstring str) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    jsize len = env->GetStringLength(str);
    const jchar* chars = env->GetStringCritical(str, nullptr);
    int count = instance->textToGlyphs(chars, len * sizeof(jchar), SkTextEncoding::kUTF16, nullptr, 0);
    std::vector<short> glyphs(count);
    instance->textToGlyphs(chars, len * sizeof(jchar), SkTextEncoding::kUTF16, reinterpret_cast<SkGlyphID*>(glyphs.data()), count);
    env->ReleaseStringCritical(str, chars);
    return javaShortArray(env, glyphs);
}
#endif



extern "C" jshortArray org_jetbrains_skia_Font__1nGetUTF32Glyphs
  (jlong ptr, jintArray uniArr) {
    TODO("implement org_jetbrains_skia_Font__1nGetUTF32Glyphs");
}
     
#if 0 
extern "C" jshortArray org_jetbrains_skia_Font__1nGetUTF32Glyphs
  (jlong ptr, jintArray uniArr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    int count = env->GetArrayLength(uniArr);
    std::vector<jshort> glyphs(count);
    jint* uni = env->GetIntArrayElements(uniArr, nullptr);
    instance->unicharsToGlyphs(reinterpret_cast<SkUnichar*>(uni), count, reinterpret_cast<SkGlyphID*>(glyphs.data()));
    env->ReleaseIntArrayElements(uniArr, uni, 0);
    return javaShortArray(env, glyphs);
}
#endif


extern "C" jshort org_jetbrains_skia_Font__1nGetUTF32Glyph
  (jlong ptr, jint uni) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return instance->unicharToGlyph(uni);
}


extern "C" jint org_jetbrains_skia_Font__1nGetStringGlyphsCount
  (jlong ptr, jstring str) {
    TODO("implement org_jetbrains_skia_Font__1nGetStringGlyphsCount");
}
     
#if 0 
extern "C" jint org_jetbrains_skia_Font__1nGetStringGlyphsCount
  (jlong ptr, jstring str) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    jsize len = env->GetStringLength(str);
    const jchar* chars = env->GetStringCritical(str, nullptr);
    int count = instance->countText(chars, len * sizeof(jchar), SkTextEncoding::kUTF16);
    env->ReleaseStringCritical(str, chars);
    return count;
}
#endif



extern "C" jobject org_jetbrains_skia_Font__1nMeasureText
  (jlong ptr, jstring str, jlong paintPtr) {
    TODO("implement org_jetbrains_skia_Font__1nMeasureText");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Font__1nMeasureText
  (jlong ptr, jstring str, jlong paintPtr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    jsize len = env->GetStringLength(str);
    const jchar* chars = env->GetStringCritical(str, nullptr);
    SkRect bounds;
    instance->measureText(chars, len * sizeof(jchar), SkTextEncoding::kUTF16, &bounds, paint);
    env->ReleaseStringCritical(str, chars);
    return skija::Rect::fromSkRect(env, bounds);
}
#endif



extern "C" jfloat org_jetbrains_skia_Font__1nMeasureTextWidth
  (jlong ptr, jstring str, jlong paintPtr) {
    TODO("implement org_jetbrains_skia_Font__1nMeasureTextWidth");
}
     
#if 0 
extern "C" jfloat org_jetbrains_skia_Font__1nMeasureTextWidth
  (jlong ptr, jstring str, jlong paintPtr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    jsize len = env->GetStringLength(str);
    const jchar* chars = env->GetStringCritical(str, nullptr);
    float width = instance->measureText(chars, len * sizeof(jchar), SkTextEncoding::kUTF16, nullptr, paint);
    env->ReleaseStringCritical(str, chars);
    return width;
}
#endif



extern "C" jfloatArray org_jetbrains_skia_Font__1nGetWidths
  (jlong ptr, jshortArray glyphsArr) {
    TODO("implement org_jetbrains_skia_Font__1nGetWidths");
}
     
#if 0 
extern "C" jfloatArray org_jetbrains_skia_Font__1nGetWidths
  (jlong ptr, jshortArray glyphsArr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    int count = env->GetArrayLength(glyphsArr);
    std::vector<jfloat> widths(count);
    jshort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);
    instance->getWidths(reinterpret_cast<SkGlyphID*>(glyphs), count, widths.data());
    env->ReleaseShortArrayElements(glyphsArr, glyphs, 0);
    return javaFloatArray(env, widths);
}
#endif



extern "C" jobjectArray org_jetbrains_skia_Font__1nGetBounds
  (jlong ptr, jshortArray glyphsArr, jlong paintPtr) {
    TODO("implement org_jetbrains_skia_Font__1nGetBounds");
}
     
#if 0 
extern "C" jobjectArray org_jetbrains_skia_Font__1nGetBounds
  (jlong ptr, jshortArray glyphsArr, jlong paintPtr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    int count = env->GetArrayLength(glyphsArr);
    std::vector<SkRect> bounds(count);
    jshort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);
    instance->getBounds(reinterpret_cast<SkGlyphID*>(glyphs), count, bounds.data(), paint);
    env->ReleaseShortArrayElements(glyphsArr, glyphs, 0);

    jobjectArray res = env->NewObjectArray(count, skija::Rect::cls, nullptr);
    for (int i = 0; i < count; ++i) {
        skija::AutoLocal<jobject> boundsObj(env, skija::Rect::fromSkRect(env, bounds[i]));
        env->SetObjectArrayElement(res, i, boundsObj.get());
    }

    return res;
}
#endif



extern "C" jobjectArray org_jetbrains_skia_Font__1nGetPositions
  (jlong ptr, jshortArray glyphsArr, jfloat dx, jfloat dy) {
    TODO("implement org_jetbrains_skia_Font__1nGetPositions");
}
     
#if 0 
extern "C" jobjectArray org_jetbrains_skia_Font__1nGetPositions
  (jlong ptr, jshortArray glyphsArr, jfloat dx, jfloat dy) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    
    int count = env->GetArrayLength(glyphsArr);
    std::vector<SkPoint> positions(count);
    jshort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);
    instance->getPos(reinterpret_cast<SkGlyphID*>(glyphs), count, positions.data(), {dx, dy});
    env->ReleaseShortArrayElements(glyphsArr, glyphs, 0);

    return skija::Point::fromSkPoints(env, positions);
}
#endif



extern "C" jfloatArray org_jetbrains_skia_Font__1nGetXPositions
  (jlong ptr, jshortArray glyphsArr, jfloat dx) {
    TODO("implement org_jetbrains_skia_Font__1nGetXPositions");
}
     
#if 0 
extern "C" jfloatArray org_jetbrains_skia_Font__1nGetXPositions
  (jlong ptr, jshortArray glyphsArr, jfloat dx) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    int count = env->GetArrayLength(glyphsArr);
    std::vector<jfloat> positions(count);
    jshort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);
    instance->getXPos(reinterpret_cast<SkGlyphID*>(glyphs), count, positions.data(), dx);
    env->ReleaseShortArrayElements(glyphsArr, glyphs, 0);
    return javaFloatArray(env, positions);
}
#endif


extern "C" jlong org_jetbrains_skia_Font__1nGetPath
  (jlong ptr, jshort glyph) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    SkPath* path = new SkPath();
    instance->getPath(glyph, path);
    return reinterpret_cast<jlong>(path);
}


extern "C" jobjectArray org_jetbrains_skia_Font__1nGetPaths
  (jlong ptr, jshortArray glyphsArr) {
    TODO("implement org_jetbrains_skia_Font__1nGetPaths");
}
     
#if 0 
extern "C" jobjectArray org_jetbrains_skia_Font__1nGetPaths
  (jlong ptr, jshortArray glyphsArr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    
    int count = env->GetArrayLength(glyphsArr);
    jshort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);

    struct Ctx {
        jobjectArray paths;
        jsize        idx;
        JNIEnv*      env;
    } ctx = { env->NewObjectArray(count, skija::Path::cls, nullptr), 0, env };

    instance->getPaths(reinterpret_cast<SkGlyphID*>(glyphs), count, [](const SkPath* orig, const SkMatrix& mx, void* voidCtx) {
        Ctx* ctx = static_cast<Ctx*>(voidCtx);
        if (orig) {
            SkPath* path = new SkPath();
            orig->transform(mx, path);
            jobject pathObj = ctx->env->NewObject(skija::Path::cls, skija::Path::ctor, reinterpret_cast<jlong>(path));
            ctx->env->SetObjectArrayElement(ctx->paths, ctx->idx, pathObj);
            ctx->env->DeleteLocalRef(pathObj);
            ++ctx->idx;
        }
    }, &ctx);

    env->ReleaseShortArrayElements(glyphsArr, glyphs, 0);
    return ctx.paths;
}
#endif



extern "C" jobject org_jetbrains_skia_Font__1nGetMetrics
  (jlong ptr, jshortArray glyphsArr) {
    TODO("implement org_jetbrains_skia_Font__1nGetMetrics");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Font__1nGetMetrics
  (jlong ptr, jshortArray glyphsArr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    SkFontMetrics m;
    instance->getMetrics(&m);
    return skija::FontMetrics::toJava(env, m);
}
#endif



extern "C" jfloat org_jetbrains_skia_Font__1nGetSpacing
  (jlong ptr, jshortArray glyphsArr) {
    TODO("implement org_jetbrains_skia_Font__1nGetSpacing");
}
     
#if 0 
extern "C" jfloat org_jetbrains_skia_Font__1nGetSpacing
  (jlong ptr, jshortArray glyphsArr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return instance->getSpacing();
}
#endif

