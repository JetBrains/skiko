
// This file has been auto generated.

#include <iostream>
#include "SkColorFilter.h"
#include "SkImageFilter.h"
#include "SkMaskFilter.h"
#include "SkPaint.h"
#include "SkPathEffect.h"
#include "SkShader.h"
#include "SkFilterQuality.h"
#include "common.h"

static void deletePaint(SkPaint* paint) {
    delete paint;
}

extern "C" jlong org_jetbrains_skia_Paint__1nGetFinalizer
  (kref __Kinstance) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deletePaint));
}

extern "C" jlong org_jetbrains_skia_Paint__1nMake
  (kref __Kinstance) {
    SkPaint* obj = new SkPaint();
    obj->setAntiAlias(true);
    return reinterpret_cast<jlong>(obj);
}

extern "C" jlong org_jetbrains_skia_Paint__1nMakeClone
  (kref __Kinstance, jlong ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    SkPaint* obj = new SkPaint(*instance);
    return reinterpret_cast<jlong>(obj);
}

extern "C" jboolean org_jetbrains_skia_Paint__1nEquals
  (kref __Kinstance, jlong aPtr, jlong bPtr) {
    SkPaint* a = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(aPtr));
    SkPaint* b = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(bPtr));
    return *a == *b;
}

extern "C" void org_jetbrains_skia_Paint__1nReset
  (kref __Kinstance, jlong ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    instance->reset();
}

extern "C" jboolean org_jetbrains_skia_Paint__1nIsAntiAlias
  (kref __Kinstance, jlong ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    return instance->isAntiAlias();
}

extern "C" void org_jetbrains_skia_Paint__1nSetAntiAlias
  (kref __Kinstance, jlong ptr, jboolean value) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    instance->setAntiAlias(value);
}

extern "C" jboolean org_jetbrains_skia_Paint__1nIsDither
  (kref __Kinstance, jlong ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    return instance->isDither();
}

extern "C" void org_jetbrains_skia_Paint__1nSetDither
  (kref __Kinstance, jlong ptr, jboolean value) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    instance->setDither(value);
}

extern "C" jint org_jetbrains_skia_Paint__1nGetColor
  (kref __Kinstance, jlong ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    return instance->getColor();
}

extern "C" void org_jetbrains_skia_Paint__1nSetColor
  (kref __Kinstance, jlong ptr, jint color) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    instance->setColor(color);
}


extern "C" jobject org_jetbrains_skia_Paint__1nGetColor4f
  (kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_Paint__1nGetColor4f");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Paint__1nGetColor4f
  (kref __Kinstance, jlong ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    SkColor4f color = instance->getColor4f();
    return env->NewObject(skija::Color4f::cls, skija::Color4f::ctor, color.fR, color.fG, color.fB, color.fA);
}
#endif


extern "C" void org_jetbrains_skia_Paint__1nSetColor4f
  (kref __Kinstance, jlong ptr, jfloat r, jfloat g, jfloat b, jfloat a, jlong colorSpacePtr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr));
    instance->setColor4f({r, g, b, a}, colorSpace);
}

extern "C" jint org_jetbrains_skia_Paint__1nGetMode
  (kref __Kinstance, jlong ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    return static_cast<jlong>(instance->getStyle());
}

extern "C" void org_jetbrains_skia_Paint__1nSetMode
  (kref __Kinstance, jlong ptr, jint mode) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    instance->setStyle(static_cast<SkPaint::Style>(mode));
}

extern "C" jfloat org_jetbrains_skia_Paint__1nGetStrokeWidth
  (kref __Kinstance, jlong ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    return instance->getStrokeWidth();
}

extern "C" void org_jetbrains_skia_Paint__1nSetStrokeWidth
  (kref __Kinstance, jlong ptr, jfloat width) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    instance->setStrokeWidth(width);
}

extern "C" jfloat org_jetbrains_skia_Paint__1nGetStrokeMiter
  (kref __Kinstance, jlong ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    return instance->getStrokeMiter();
}

extern "C" void org_jetbrains_skia_Paint__1nSetStrokeMiter
  (kref __Kinstance, jlong ptr, jfloat miter) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    instance->setStrokeMiter(miter);
}

extern "C" jint org_jetbrains_skia_Paint__1nGetStrokeCap
  (kref __Kinstance, jlong ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    return static_cast<jlong>(instance->getStrokeCap());
}

extern "C" void org_jetbrains_skia_Paint__1nSetStrokeCap
  (kref __Kinstance, jlong ptr, jint cap) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    instance->setStrokeCap(static_cast<SkPaint::Cap>(cap));
}

extern "C" jint org_jetbrains_skia_Paint__1nGetStrokeJoin
  (kref __Kinstance, jlong ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    return static_cast<jlong>(instance->getStrokeJoin());
}

extern "C" void org_jetbrains_skia_Paint__1nSetStrokeJoin
  (kref __Kinstance, jlong ptr, jint join) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    instance->setStrokeJoin(static_cast<SkPaint::Join>(join));
}

extern "C" jlong org_jetbrains_skia_Paint__1nGetFillPath
  (kref __Kinstance, jlong ptr, jlong srcPtr, jfloat resScale) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    SkPath* src = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(srcPtr));
    SkPath* dst = new SkPath();
    instance->getFillPath(*src, dst, nullptr, resScale);
    return reinterpret_cast<jlong>(dst);
}

extern "C" jlong org_jetbrains_skia_Paint__1nGetFillPathCull
  (kref __Kinstance, jlong ptr, jlong srcPtr, jfloat left, jfloat top, jfloat right, jfloat bottom, jfloat resScale) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    SkPath* src = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(srcPtr));
    SkPath* dst = new SkPath();
    SkRect cull {left, top, right, bottom};
    instance->getFillPath(*src, dst, &cull, resScale);
    return reinterpret_cast<jlong>(dst);
}

extern "C" jlong org_jetbrains_skia_Paint__1nGetMaskFilter
  (kref __Kinstance, jlong ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(instance->refMaskFilter().release());
}

extern "C" void org_jetbrains_skia_Paint__1nSetMaskFilter
  (kref __Kinstance, jlong ptr, jlong filterPtr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    SkMaskFilter* filter = reinterpret_cast<SkMaskFilter*>(static_cast<uintptr_t>(filterPtr));
    instance->setMaskFilter(sk_ref_sp<SkMaskFilter>(filter));
}

extern "C" jlong org_jetbrains_skia_Paint__1nGetImageFilter
  (kref __Kinstance, jlong ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(instance->refImageFilter().release());
}

extern "C" void org_jetbrains_skia_Paint__1nSetImageFilter
  (kref __Kinstance, jlong ptr, jlong filterPtr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    SkImageFilter* filter = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(filterPtr));
    instance->setImageFilter(sk_ref_sp<SkImageFilter>(filter));
}

extern "C" jint org_jetbrains_skia_Paint__1nGetBlendMode
  (kref __Kinstance, jlong ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    return static_cast<jlong>(instance->getBlendMode_or(SkBlendMode::kSrcOver));
}

extern "C" void org_jetbrains_skia_Paint__1nSetBlendMode
  (kref __Kinstance, jlong ptr, jint mode) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    instance->setBlendMode(static_cast<SkBlendMode>(mode));
}

extern "C" jlong org_jetbrains_skia_Paint__1nGetPathEffect
  (kref __Kinstance, jlong ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(instance->refPathEffect().release());
}

extern "C" void org_jetbrains_skia_Paint__1nSetPathEffect
  (kref __Kinstance, jlong ptr, jlong pathEffectPtr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    SkPathEffect* pathEffect = reinterpret_cast<SkPathEffect*>(static_cast<uintptr_t>(pathEffectPtr));
    instance->setPathEffect(sk_ref_sp(pathEffect));
}

extern "C" jlong org_jetbrains_skia_Paint__1nGetShader
  (kref __Kinstance, jlong ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(instance->refShader().release());
}

extern "C" void org_jetbrains_skia_Paint__1nSetShader
  (kref __Kinstance, jlong ptr, jlong shaderPtr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    SkShader* shader = reinterpret_cast<SkShader*>(static_cast<uintptr_t>(shaderPtr));
    instance->setShader(sk_ref_sp<SkShader>(shader));
}

extern "C" jlong org_jetbrains_skia_Paint__1nGetColorFilter
  (kref __Kinstance, jlong ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(instance->refColorFilter().release());
}

extern "C" void org_jetbrains_skia_Paint__1nSetColorFilter
  (kref __Kinstance, jlong ptr, jlong colorFilterPtr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    SkColorFilter* colorFilter = reinterpret_cast<SkColorFilter*>(static_cast<uintptr_t>(colorFilterPtr));
    instance->setColorFilter(sk_ref_sp<SkColorFilter>(colorFilter));
}

extern "C" jboolean org_jetbrains_skia_Paint__1nHasNothingToDraw
  (kref __Kinstance, jlong ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(ptr));
    return instance->nothingToDraw();
}
