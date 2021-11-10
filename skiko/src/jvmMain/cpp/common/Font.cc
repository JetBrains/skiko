#include <iostream>
#include <jni.h>
#include "SkFont.h"
#include "SkPath.h"
#include "SkShaper.h"
#include "interop.hh"

static void deleteFont(SkFont* font) {
    delete font;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_FontKt_Font_1nGetFinalizer
  (JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteFont));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_FontKt__1nMakeDefault
  (JNIEnv* env, jclass jclass) {
    SkFont* obj = new SkFont();
    return reinterpret_cast<jlong>(obj);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_FontKt__1nMakeTypeface
  (JNIEnv* env, jclass jclass, jlong typefacePtr) {
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(typefacePtr));
    SkFont* obj = new SkFont(sk_ref_sp<SkTypeface>(typeface));
    return reinterpret_cast<jlong>(obj);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_FontKt__1nMakeTypefaceSize
  (JNIEnv* env, jclass jclass, jlong typefacePtr, jfloat size) {
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(typefacePtr));
    SkFont* obj = new SkFont(sk_ref_sp<SkTypeface>(typeface), size);
    return reinterpret_cast<jlong>(obj);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_FontKt__1nMakeTypefaceSizeScaleSkew
  (JNIEnv* env, jclass jclass, jlong typefacePtr, jfloat size, jfloat scaleX, jfloat skewX) {
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(typefacePtr));
    SkFont* obj = new SkFont(sk_ref_sp<SkTypeface>(typeface), size, scaleX, skewX);
    return reinterpret_cast<jlong>(obj);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_FontKt_Font_1nMakeClone
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    SkFont* clone = new SkFont(*instance);
    return reinterpret_cast<jlong>(clone);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_FontKt_Font_1nEquals
  (JNIEnv* env, jclass jclass, jlong ptr, jlong otherPtr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    SkFont* other = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(otherPtr));
    return *instance == *other;
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_FontKt__1nIsAutoHintingForced
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return instance->isForceAutoHinting();
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_FontKt__1nAreBitmapsEmbedded
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return instance->isEmbeddedBitmaps();
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_FontKt__1nIsSubpixel
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return instance->isSubpixel();
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_FontKt__1nAreMetricsLinear
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return instance->isLinearMetrics();
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_FontKt__1nIsEmboldened
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return instance->isEmbolden();
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_FontKt__1nIsBaselineSnapped
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return instance->isBaselineSnap();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_FontKt__1nSetAutoHintingForced
  (JNIEnv* env, jclass jclass, jlong ptr, jboolean value) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    instance->setForceAutoHinting(value);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_FontKt__1nSetBitmapsEmbedded
  (JNIEnv* env, jclass jclass, jlong ptr, jboolean value) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    instance->setEmbeddedBitmaps(value);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_FontKt__1nSetSubpixel
  (JNIEnv* env, jclass jclass, jlong ptr, jboolean value) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    instance->setSubpixel(value);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_FontKt__1nSetMetricsLinear
  (JNIEnv* env, jclass jclass, jlong ptr, jboolean value) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    instance->setLinearMetrics(value);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_FontKt__1nSetEmboldened
  (JNIEnv* env, jclass jclass, jlong ptr, jboolean value) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    instance->setEmbolden(value);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_FontKt__1nSetBaselineSnapped
  (JNIEnv* env, jclass jclass, jlong ptr, jboolean value) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    instance->setBaselineSnap(value);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_FontKt__1nGetEdging
 (JNIEnv* env, jclass jclass, jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return static_cast<jint>(instance->getEdging());
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_FontKt__1nSetEdging
 (JNIEnv* env, jclass jclass, jlong ptr, jint value) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    instance->setEdging(static_cast<SkFont::Edging>(value));
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_FontKt__1nGetHinting
 (JNIEnv* env, jclass jclass, jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return static_cast<jint>(instance->getHinting());
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_FontKt__1nSetHinting
 (JNIEnv* env, jclass jclass, jlong ptr, jint value) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    instance->setHinting(static_cast<SkFontHinting>(value));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_FontKt__1nGetTypeface
 (JNIEnv* env, jclass jclass, jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    SkTypeface* typeface = instance->refTypeface().release();
    return reinterpret_cast<jlong>(typeface);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_FontKt__1nGetTypefaceOrDefault
 (JNIEnv* env, jclass jclass, jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    SkTypeface* typeface = instance->refTypefaceOrDefault().release();
    return reinterpret_cast<jlong>(typeface);
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skia_FontKt_Font_1nGetSize
 (JNIEnv* env, jclass jclass, jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return instance->getSize();
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skia_FontKt__1nGetScaleX
 (JNIEnv* env, jclass jclass, jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return instance->getScaleX();
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skia_FontKt__1nGetSkewX
 (JNIEnv* env, jclass jclass, jlong ptr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return instance->getSkewX();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_FontKt__1nSetTypeface
 (JNIEnv* env, jclass jclass, jlong ptr, jlong typefacePtr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(typefacePtr));
    instance->setTypeface(sk_ref_sp(typeface));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_FontKt__1nSetSize
 (JNIEnv* env, jclass jclass, jlong ptr, jfloat value) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    instance->setSize(value);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_FontKt__1nSetScaleX
 (JNIEnv* env, jclass jclass, jlong ptr, jfloat value) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    instance->setScaleX(value);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_FontKt__1nSetSkewX
 (JNIEnv* env, jclass jclass, jlong ptr, jfloat value) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    instance->setSkewX(value);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_FontKt__1nGetUTF32Glyphs
  (JNIEnv* env, jclass jclass, jlong ptr, jintArray uniArr, jint uniArrLen, jshortArray resultGlyphs) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    jshort* shorts = env->GetShortArrayElements(resultGlyphs, nullptr);
    jint* uni = env->GetIntArrayElements(uniArr, nullptr);
    instance->unicharsToGlyphs(reinterpret_cast<SkUnichar*>(uni), uniArrLen, reinterpret_cast<SkGlyphID*>(shorts));
    env->ReleaseIntArrayElements(uniArr, uni, 0);
    env->SetShortArrayRegion(res, 0, count, glyphs.data());
}

extern "C" JNIEXPORT jshort JNICALL Java_org_jetbrains_skia_FontKt__1nGetUTF32Glyph
  (JNIEnv* env, jclass jclass, jlong ptr, jint uni) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return instance->unicharToGlyph(uni);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_FontKt__1nGetStringGlyphsCount
  (JNIEnv* env, jclass jclass, jlong ptr, jstring str, jint len) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return instance->countText(skString(env, str).c_str(), len * sizeof(jchar), SkTextEncoding::kUTF16);
}

extern "C" JNIEXPORT jobject JNICALL Java_org_jetbrains_skia_FontKt__1nMeasureText
  (JNIEnv* env, jclass jclass, jlong ptr, jstring str, jlong paintPtr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    jsize len = env->GetStringLength(str);
    const jchar* chars = env->GetStringCritical(str, nullptr);
    SkRect bounds;
    instance->measureText(chars, len * sizeof(jchar), SkTextEncoding::kUTF16, &bounds, paint);
    env->ReleaseStringCritical(str, chars);
    return skija::Rect::fromSkRect(env, bounds);
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skia_FontKt__1nMeasureTextWidth
  (JNIEnv* env, jclass jclass, jlong ptr, jstring str, jint len, jlong paintPtr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    return instance->measureText(skString(env, str).c_str(), len * sizeof(jchar), SkTextEncoding::kUTF16, nullptr, paint);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_FontKt__1nGetWidths
  (JNIEnv* env, jclass jclass, jlong ptr, jshortArray glyphsArr, jint count, jfloatArray res) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    std::vector<jfloat> widths(count);
    jshort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);
    instance->getWidths(reinterpret_cast<SkGlyphID*>(glyphs), count, widths.data());
    env->ReleaseShortArrayElements(glyphsArr, glyphs, 0);
    env->SetFloatArrayRegion(res, 0, count, widths.data());
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_FontKt__1nGetBounds
  (JNIEnv* env, jclass jclass, jlong ptr, jshortArray glyphsArr, jint count, jlong paintPtr, jfloatArray res) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    std::vector<SkRect> bounds(count);
    jshort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);
    instance->getBounds(reinterpret_cast<SkGlyphID*>(glyphs), count, bounds.data(), paint);
    env->ReleaseShortArrayElements(glyphsArr, glyphs, 0);

    for (int i = 0; i < count; ++i) {
        SkRect b = bounds[i];
        float r[4] = {b.left(), b.right(), b.top(), b.bottom()};
        env->SetFloatArrayRegion(res, 4*i, 4, r);
    }
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_FontKt__1nGetPositions
  (JNIEnv* env, jclass jclass, jlong ptr, jshortArray glyphsArr, jint count, jfloat dx, jfloat dy, jfloatArray res) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    std::vector<SkPoint> positions(count);
    jshort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);
    instance->getPos(reinterpret_cast<SkGlyphID*>(glyphs), count, positions.data(), {dx, dy});
    env->ReleaseShortArrayElements(glyphsArr, glyphs, 0);

    jfloat r[count * 2];
    for (int i = 0; i < count; i++) {
        r[2*i] = positions[i].fX;
        r[2*i + 1] = positions[i].fY;
    }

    env->SetFloatArrayRegion(res, 0, count * 2, r);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_FontKt__1nGetXPositions
  (JNIEnv* env, jclass jclass, jlong ptr, jshortArray glyphsArr, jfloat dx, jint count, jfloatArray res) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    std::vector<jfloat> positions(count);
    jshort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);
    instance->getXPos(reinterpret_cast<SkGlyphID*>(glyphs), count, positions.data(), dx);
    env->ReleaseShortArrayElements(glyphsArr, glyphs, 0);
    env->SetFloatArrayRegion(res, 0, count, positions.data());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_FontKt__1nGetPath
  (JNIEnv* env, jclass jclass, jlong ptr, jshort glyph) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    SkPath* path = new SkPath();
    instance->getPath(glyph, path);
    return reinterpret_cast<jlong>(path);
}

extern "C" JNIEXPORT jobjectArray JNICALL Java_org_jetbrains_skia_FontKt__1nGetPaths
  (JNIEnv* env, jclass jclass, jlong ptr, jshortArray glyphsArr) {
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

extern "C" JNIEXPORT jobject JNICALL Java_org_jetbrains_skia_FontKt__1nGetMetrics
  (JNIEnv* env, jclass jclass, jlong ptr, jshortArray glyphsArr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    SkFontMetrics m;
    instance->getMetrics(&m);
    return skija::FontMetrics::toJava(env, m);
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skia_FontKt__1nGetSpacing
  (JNIEnv* env, jclass jclass, jlong ptr, jshortArray glyphsArr) {
    SkFont* instance = reinterpret_cast<SkFont*>(static_cast<uintptr_t>(ptr));
    return instance->getSpacing();
}
