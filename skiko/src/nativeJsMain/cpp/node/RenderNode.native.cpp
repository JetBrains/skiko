#include "common.h"
#include "node/RenderNode.h"
#include "node/RenderNodeContext.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nMake
  (KNativePointer contextPtr) {
    auto context = reinterpret_cast<skiko::node::RenderNodeContext *>(contextPtr);
    auto instance = sk_make_sp<skiko::node::RenderNode>(sk_ref_sp(context));
    return reinterpret_cast<KNativePointer>(instance.release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetLayerPaint
  (KNativePointer ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    std::optional<SkPaint>& layerPaint = instance->getLayerPaint();
    return reinterpret_cast<KNativePointer>(layerPaint ? &*layerPaint : nullptr);
}

SKIKO_EXPORT void org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetLayerPaint
  (KNativePointer ptr, KNativePointer paintPtr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    auto paint = reinterpret_cast<SkPaint *>(paintPtr);
    instance->setLayerPaint(paint ? std::optional<SkPaint>{*paint} : std::nullopt);
}

SKIKO_EXPORT void org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetBounds
  (KNativePointer ptr, KInteropPointer resultRect) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    SkRect bounds = instance->getBounds();

    skija::Rect::copyToInterop(bounds, resultRect);
}

SKIKO_EXPORT void org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetBounds
  (KNativePointer ptr, KFloat left, KFloat top, KFloat right, KFloat bottom) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setBounds(SkRect::MakeLTRB(left, top, right, bottom));
}

SKIKO_EXPORT void org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetPivot
  (KNativePointer ptr, KFloat* resultPoint) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    SkPoint pivot = instance->getPivot();

    skija::Point::copyToInterop(pivot, resultPoint);
}

SKIKO_EXPORT void org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetPivot
  (KNativePointer ptr, KFloat x, KFloat y) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setPivot(SkPoint::Make(x, y));
}

SKIKO_EXPORT KFloat org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetAlpha
  (KNativePointer ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getAlpha();
}

SKIKO_EXPORT void org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetAlpha
  (KNativePointer ptr, KFloat alpha) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setAlpha(alpha);
}

SKIKO_EXPORT KFloat org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetScaleX
  (KNativePointer ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getScaleX();
}

SKIKO_EXPORT void org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetScaleX
  (KNativePointer ptr, KFloat scaleX) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setScaleX(scaleX);
}

SKIKO_EXPORT KFloat org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetScaleY
  (KNativePointer ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getScaleY();
}

SKIKO_EXPORT void org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetScaleY
  (KNativePointer ptr, KFloat scaleY) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setScaleY(scaleY);
}

SKIKO_EXPORT KFloat org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetTranslationX
  (KNativePointer ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getTranslationX();
}

SKIKO_EXPORT void org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetTranslationX
  (KNativePointer ptr, KFloat translationX) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setTranslationX(translationX);
}

SKIKO_EXPORT KFloat org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetTranslationY
  (KNativePointer ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getTranslationY();
}

SKIKO_EXPORT void org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetTranslationY
  (KNativePointer ptr, KFloat translationY) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setTranslationY(translationY);
}

SKIKO_EXPORT KFloat org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetShadowElevation
  (KNativePointer ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getShadowElevation();
}

SKIKO_EXPORT void org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetShadowElevation
  (KNativePointer ptr, KFloat elevation) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setShadowElevation(elevation);
}

SKIKO_EXPORT KInt org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetAmbientShadowColor
  (KNativePointer ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getAmbientShadowColor();
}

SKIKO_EXPORT void org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetAmbientShadowColor
  (KNativePointer ptr, KInt color) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setAmbientShadowColor(static_cast<uint32_t>(color));
}

SKIKO_EXPORT KInt org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetSpotShadowColor
  (KNativePointer ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getSpotShadowColor();
}

SKIKO_EXPORT void org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetSpotShadowColor
  (KNativePointer ptr, KInt color) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setSpotShadowColor(static_cast<uint32_t>(color));
}

SKIKO_EXPORT KFloat org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetRotationX
  (KNativePointer ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getRotationX();
}

SKIKO_EXPORT void org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetRotationX
  (KNativePointer ptr, KFloat rotationX) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setRotationX(rotationX);
}

SKIKO_EXPORT KFloat org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetRotationY
  (KNativePointer ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getRotationY();
}

SKIKO_EXPORT void org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetRotationY
  (KNativePointer ptr, KFloat rotationY) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setRotationY(rotationY);
}

SKIKO_EXPORT KFloat org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetRotationZ
  (KNativePointer ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getRotationZ();
}

SKIKO_EXPORT void org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetRotationZ
  (KNativePointer ptr, KFloat rotationZ) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setRotationZ(rotationZ);
}

SKIKO_EXPORT KFloat org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetCameraDistance
  (KNativePointer ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getCameraDistance();
}

SKIKO_EXPORT void org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetCameraDistance
  (KNativePointer ptr, KFloat distance) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setCameraDistance(distance);
}

SKIKO_EXPORT void org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetClipRect
  (KNativePointer ptr, KFloat left, KFloat top, KFloat right, KFloat bottom) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setClipRect(SkRect::MakeLTRB(left, top, right, bottom));
}

SKIKO_EXPORT void org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetClipRRect
  (KNativePointer ptr, KFloat left, KFloat top, KFloat right, KFloat bottom, KFloat* radii, KInt radiiSize) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setClipRRect(skija::RRect::toSkRRect(left, top, right, bottom, radii, radiiSize));
}

SKIKO_EXPORT void org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetClipPath
  (KNativePointer ptr, KNativePointer pathPtr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    SkPath* path = reinterpret_cast<SkPath*>(pathPtr);
    instance->setClipPath(path ? std::optional<SkPath>{*path} : std::nullopt);
}

SKIKO_EXPORT KBoolean org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetClip
  (KNativePointer ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getClip();
}

SKIKO_EXPORT void org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetClip
  (KNativePointer ptr, KBoolean clip) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setClip(clip);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nBeginRecording
  (KNativePointer ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    SkCanvas *canvas = instance->beginRecording();
    return reinterpret_cast<KNativePointer>(canvas);
}

SKIKO_EXPORT void org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nEndRecording
  (KNativePointer ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->endRecording();
}

SKIKO_EXPORT void org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nDrawInto
  (KNativePointer ptr, KNativePointer canvasPtr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    auto canvas = reinterpret_cast<SkCanvas *>(canvasPtr);
    canvas->drawDrawable(instance);
}
