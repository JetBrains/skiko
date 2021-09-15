
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

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Paint__1nGetFinalizer
  (KInteropPointer __Kinstance) {
    return reinterpret_cast<KNativePointer>((&deletePaint));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Paint__1nMake
  (KInteropPointer __Kinstance) {
    SkPaint* obj = new SkPaint();
    obj->setAntiAlias(true);
    return reinterpret_cast<KNativePointer>(obj);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Paint__1nMakeClone
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    SkPaint* obj = new SkPaint(*instance);
    return reinterpret_cast<KNativePointer>(obj);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Paint__1nEquals
  (KInteropPointer __Kinstance, KNativePointer aPtr, KNativePointer bPtr) {
    SkPaint* a = reinterpret_cast<SkPaint*>((aPtr));
    SkPaint* b = reinterpret_cast<SkPaint*>((bPtr));
    return *a == *b;
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nReset
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    instance->reset();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Paint__1nIsAntiAlias
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return instance->isAntiAlias();
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetAntiAlias
  (KInteropPointer __Kinstance, KNativePointer ptr, KBoolean value) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    instance->setAntiAlias(value);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Paint__1nIsDither
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return instance->isDither();
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetDither
  (KInteropPointer __Kinstance, KNativePointer ptr, KBoolean value) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    instance->setDither(value);
}

SKIKO_EXPORT KInt org_jetbrains_skia_Paint__1nGetColor
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return instance->getColor();
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetColor
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt color) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    instance->setColor(color);
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Paint__1nGetColor4f
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Paint__1nGetColor4f");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Paint__1nGetColor4f
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    SkColor4f color = instance->getColor4f();
    return env->NewObject(skija::Color4f::cls, skija::Color4f::ctor, color.fR, color.fG, color.fB, color.fA);
}
#endif


SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetColor4f
  (KInteropPointer __Kinstance, KNativePointer ptr, KFloat r, KFloat g, KFloat b, KFloat a, KNativePointer colorSpacePtr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>((colorSpacePtr));
    instance->setColor4f({r, g, b, a}, colorSpace);
}

SKIKO_EXPORT KInt org_jetbrains_skia_Paint__1nGetMode
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return static_cast<KNativePointer>(instance->getStyle());
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetMode
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt mode) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    instance->setStyle(static_cast<SkPaint::Style>(mode));
}

SKIKO_EXPORT KFloat org_jetbrains_skia_Paint__1nGetStrokeWidth
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return instance->getStrokeWidth();
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetStrokeWidth
  (KInteropPointer __Kinstance, KNativePointer ptr, KFloat width) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    instance->setStrokeWidth(width);
}

SKIKO_EXPORT KFloat org_jetbrains_skia_Paint__1nGetStrokeMiter
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return instance->getStrokeMiter();
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetStrokeMiter
  (KInteropPointer __Kinstance, KNativePointer ptr, KFloat miter) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    instance->setStrokeMiter(miter);
}

SKIKO_EXPORT KInt org_jetbrains_skia_Paint__1nGetStrokeCap
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return static_cast<KNativePointer>(instance->getStrokeCap());
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetStrokeCap
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt cap) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    instance->setStrokeCap(static_cast<SkPaint::Cap>(cap));
}

SKIKO_EXPORT KInt org_jetbrains_skia_Paint__1nGetStrokeJoin
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return static_cast<KNativePointer>(instance->getStrokeJoin());
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetStrokeJoin
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt join) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    instance->setStrokeJoin(static_cast<SkPaint::Join>(join));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Paint__1nGetFillPath
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer srcPtr, KFloat resScale) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    SkPath* src = reinterpret_cast<SkPath*>((srcPtr));
    SkPath* dst = new SkPath();
    instance->getFillPath(*src, dst, nullptr, resScale);
    return reinterpret_cast<KNativePointer>(dst);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Paint__1nGetFillPathCull
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer srcPtr, KFloat left, KFloat top, KFloat right, KFloat bottom, KFloat resScale) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    SkPath* src = reinterpret_cast<SkPath*>((srcPtr));
    SkPath* dst = new SkPath();
    SkRect cull {left, top, right, bottom};
    instance->getFillPath(*src, dst, &cull, resScale);
    return reinterpret_cast<KNativePointer>(dst);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Paint__1nGetMaskFilter
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return reinterpret_cast<KNativePointer>(instance->refMaskFilter().release());
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetMaskFilter
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer filterPtr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    SkMaskFilter* filter = reinterpret_cast<SkMaskFilter*>((filterPtr));
    instance->setMaskFilter(sk_ref_sp<SkMaskFilter>(filter));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Paint__1nGetImageFilter
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return reinterpret_cast<KNativePointer>(instance->refImageFilter().release());
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetImageFilter
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer filterPtr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    SkImageFilter* filter = reinterpret_cast<SkImageFilter*>((filterPtr));
    instance->setImageFilter(sk_ref_sp<SkImageFilter>(filter));
}

SKIKO_EXPORT KInt org_jetbrains_skia_Paint__1nGetBlendMode
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return static_cast<KNativePointer>(instance->getBlendMode_or(SkBlendMode::kSrcOver));
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetBlendMode
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt mode) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    instance->setBlendMode(static_cast<SkBlendMode>(mode));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Paint__1nGetPathEffect
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return reinterpret_cast<KNativePointer>(instance->refPathEffect().release());
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetPathEffect
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer pathEffectPtr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    SkPathEffect* pathEffect = reinterpret_cast<SkPathEffect*>((pathEffectPtr));
    instance->setPathEffect(sk_ref_sp(pathEffect));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Paint__1nGetShader
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return reinterpret_cast<KNativePointer>(instance->refShader().release());
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetShader
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer shaderPtr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    SkShader* shader = reinterpret_cast<SkShader*>((shaderPtr));
    instance->setShader(sk_ref_sp<SkShader>(shader));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Paint__1nGetColorFilter
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return reinterpret_cast<KNativePointer>(instance->refColorFilter().release());
}

SKIKO_EXPORT void org_jetbrains_skia_Paint__1nSetColorFilter
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer colorFilterPtr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    SkColorFilter* colorFilter = reinterpret_cast<SkColorFilter*>((colorFilterPtr));
    instance->setColorFilter(sk_ref_sp<SkColorFilter>(colorFilter));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Paint__1nHasNothingToDraw
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPaint* instance = reinterpret_cast<SkPaint*>((ptr));
    return instance->nothingToDraw();
}
