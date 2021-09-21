
// This file has been auto generated.

#include <iostream>
#include "SkColorFilter.h"
#include "SkImageFilter.h"
#include "SkMaskFilter.h"
#include "SkPaint.h"
#include "SkPathEffect.h"
#include "SkShader.h"
#include "common.h"

void deletePaint(SkPaint* paint) {
    delete paint;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Paint__1nGetFinalizer(){
    return reinterpret_cast<KNativePointer>(&deletePaint);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Paint__1nMake(){
    SkPaint* obj = new SkPaint();
    obj->setAntiAlias(true);
    return reinterpret_cast<KNativePointer>(obj);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Paint__1nMakeClone
  (KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    SkPaint* obj = new SkPaint(*instance);
    return reinterpret_cast<KNativePointer>(obj);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Paint__1nEquals
  (KNativePointer aPtr, KNativePointer bPtr) {
    SkPaint* a = reinterpret_cast<SkPaint*>((aPtr));
    SkPaint* b = reinterpret_cast<SkPaint*>((bPtr));
    return *a == *b;
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nReset
  (KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    instance->reset();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Paint__1nIsAntiAlias
  (KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return instance->isAntiAlias();
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetAntiAlias
  (KNativePointer ptr, KBoolean value) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    instance->setAntiAlias(value);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Paint__1nIsDither
  (KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return instance->isDither();
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetDither
  (KNativePointer ptr, KBoolean value) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    instance->setDither(value);
}

SKIKO_EXPORT KInt org_jetbrains_skia_Paint__1nGetColor
  (KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return instance->getColor();
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetColor
  (KNativePointer ptr, KInt color) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    instance->setColor(color);
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Paint__1nGetColor4f
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Paint__1nGetColor4f");
}

#if 0
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Paint__1nGetColor4f
  (KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    SkColor4f color = instance->getColor4f();
    return env->NewObject(skija::Color4f::cls, skija::Color4f::ctor, color.fR, color.fG, color.fB, color.fA);
}
#endif


SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetColor4f
  (KNativePointer ptr, KFloat r, KFloat g, KFloat b, KFloat a, KNativePointer colorSpacePtr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>((colorSpacePtr));
    instance->setColor4f({r, g, b, a}, colorSpace);
}

SKIKO_EXPORT KInt org_jetbrains_skia_Paint__1nGetMode
  (KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return instance->getStyle();
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetMode
  (KNativePointer ptr, KInt mode) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    instance->setStyle(static_cast<SkPaint::Style>(mode));
}

SKIKO_EXPORT KFloat org_jetbrains_skia_Paint__1nGetStrokeWidth
  (KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return instance->getStrokeWidth();
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetStrokeWidth
  (KNativePointer ptr, KFloat width) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    instance->setStrokeWidth(width);
}

SKIKO_EXPORT KFloat org_jetbrains_skia_Paint__1nGetStrokeMiter
  (KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return instance->getStrokeMiter();
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetStrokeMiter
  (KNativePointer ptr, KFloat miter) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    instance->setStrokeMiter(miter);
}

SKIKO_EXPORT KInt org_jetbrains_skia_Paint__1nGetStrokeCap
  (KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return instance->getStrokeCap();
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetStrokeCap
  (KNativePointer ptr, KInt cap) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    instance->setStrokeCap(static_cast<SkPaint::Cap>(cap));
}

SKIKO_EXPORT KInt org_jetbrains_skia_Paint__1nGetStrokeJoin
  (KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return instance->getStrokeJoin();
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetStrokeJoin
  (KNativePointer ptr, KInt join) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    instance->setStrokeJoin(static_cast<SkPaint::Join>(join));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Paint__1nGetFillPath
  (KNativePointer ptr, KNativePointer srcPtr, KFloat resScale) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    SkPath* src = reinterpret_cast<SkPath*>((srcPtr));
    SkPath* dst = new SkPath();
    instance->getFillPath(*src, dst, nullptr, resScale);
    return reinterpret_cast<KNativePointer>(dst);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Paint__1nGetFillPathCull
  (KNativePointer ptr, KNativePointer srcPtr, KFloat left, KFloat top, KFloat right, KFloat bottom, KFloat resScale) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    SkPath* src = reinterpret_cast<SkPath*>((srcPtr));
    SkPath* dst = new SkPath();
    SkRect cull {left, top, right, bottom};
    instance->getFillPath(*src, dst, &cull, resScale);
    return reinterpret_cast<KNativePointer>(dst);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Paint__1nGetMaskFilter
  (KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return reinterpret_cast<KNativePointer>(instance->refMaskFilter().release());
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetMaskFilter
  (KNativePointer ptr, KNativePointer filterPtr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    SkMaskFilter* filter = reinterpret_cast<SkMaskFilter*>((filterPtr));
    instance->setMaskFilter(sk_ref_sp<SkMaskFilter>(filter));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Paint__1nGetImageFilter
  (KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return reinterpret_cast<KNativePointer>(instance->refImageFilter().release());
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetImageFilter
  (KNativePointer ptr, KNativePointer filterPtr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    SkImageFilter* filter = reinterpret_cast<SkImageFilter*>((filterPtr));
    instance->setImageFilter(sk_ref_sp<SkImageFilter>(filter));
}

SKIKO_EXPORT KInt org_jetbrains_skia_Paint__1nGetBlendMode
  (KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return static_cast<KInt>(instance->getBlendMode_or(SkBlendMode::kSrcOver));
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetBlendMode
  (KNativePointer ptr, KInt mode) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    instance->setBlendMode(static_cast<SkBlendMode>(mode));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Paint__1nGetPathEffect
  (KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return reinterpret_cast<KNativePointer>(instance->refPathEffect().release());
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetPathEffect
  (KNativePointer ptr, KNativePointer pathEffectPtr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    SkPathEffect* pathEffect = reinterpret_cast<SkPathEffect*>((pathEffectPtr));
    instance->setPathEffect(sk_ref_sp(pathEffect));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Paint__1nGetShader
  (KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return reinterpret_cast<KNativePointer>(instance->refShader().release());
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetShader
  (KNativePointer ptr, KNativePointer shaderPtr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    SkShader* shader = reinterpret_cast<SkShader*>((shaderPtr));
    instance->setShader(sk_ref_sp<SkShader>(shader));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Paint__1nGetColorFilter
  (KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return reinterpret_cast<KNativePointer>(instance->refColorFilter().release());
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetColorFilter
  (KNativePointer ptr, KNativePointer colorFilterPtr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    SkColorFilter* colorFilter = reinterpret_cast<SkColorFilter*>((colorFilterPtr));
    instance->setColorFilter(sk_ref_sp<SkColorFilter>(colorFilter));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Paint__1nHasNothingToDraw
  (KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return instance->nothingToDraw();
}
