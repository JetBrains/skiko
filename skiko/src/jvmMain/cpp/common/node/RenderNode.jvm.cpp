#include <jni.h>
#include "../interop.hh"
#include "node/RenderNode.h"
#include "node/RenderNodeContext.h"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nMake
  (JNIEnv *env, jclass jclass, skiko::node::RenderNodeContext *context) {
    auto instance = sk_make_sp<skiko::node::RenderNode>(sk_ref_sp(context));
    return reinterpret_cast<jlong>(instance.release());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetLayerPaint
  (JNIEnv *env, jclass jclass, jlong ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    std::optional<SkPaint>& layerPaint = instance->getLayerPaint();
    return reinterpret_cast<jlong>(layerPaint ? &*layerPaint : nullptr);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetLayerPaint
  (JNIEnv *env, jclass jclass, jlong ptr, jlong paintPtr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    auto paint = reinterpret_cast<SkPaint *>(paintPtr);
    instance->setLayerPaint(paint ? std::optional<SkPaint>{*paint} : std::nullopt);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetBounds
  (JNIEnv *env, jclass jclass, jlong ptr, jfloatArray resultRect) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    SkRect bounds = instance->getBounds();

    jfloat* floats = env->GetFloatArrayElements(resultRect, 0);
    skikoMpp::skrect::serializeAs4Floats(bounds, floats);
    env->ReleaseFloatArrayElements(resultRect, floats, 0);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetBounds
  (JNIEnv *env, jclass jclass, jlong ptr, jfloat left, jfloat top, jfloat right, jfloat bottom) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setBounds(SkRect::MakeLTRB(left, top, right, bottom));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetPivot
  (JNIEnv *env, jclass jclass, jlong ptr, jfloatArray resultPoint) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    SkPoint pivot = instance->getPivot();
    skija::Point::copyToInterop(env, pivot, resultPoint);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetPivot
  (JNIEnv *env, jclass jclass, jlong ptr, jfloat x, jfloat y) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setPivot(SkPoint::Make(x, y));
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetAlpha
  (JNIEnv *env, jclass jclass, jlong ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getAlpha();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetAlpha
  (JNIEnv *env, jclass jclass, jlong ptr, jfloat alpha) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setAlpha(alpha);
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetScaleX
  (JNIEnv *env, jclass jclass, jlong ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getScaleX();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetScaleX
  (JNIEnv *env, jclass jclass, jlong ptr, jfloat scaleX) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setScaleX(scaleX);
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetScaleY
  (JNIEnv *env, jclass jclass, jlong ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getScaleY();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetScaleY
  (JNIEnv *env, jclass jclass, jlong ptr, jfloat scaleY) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setScaleY(scaleY);
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetTranslationX
  (JNIEnv *env, jclass jclass, jlong ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getTranslationX();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetTranslationX
  (JNIEnv *env, jclass jclass, jlong ptr, jfloat translationX) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setTranslationX(translationX);
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetTranslationY
  (JNIEnv *env, jclass jclass, jlong ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getTranslationY();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetTranslationY
  (JNIEnv *env, jclass jclass, jlong ptr, jfloat translationY) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setTranslationY(translationY);
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetShadowElevation
  (JNIEnv *env, jclass jclass, jlong ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getShadowElevation();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetShadowElevation
  (JNIEnv *env, jclass jclass, jlong ptr, jfloat elevation) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setShadowElevation(elevation);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetAmbientShadowColor
  (JNIEnv *env, jclass jclass, jlong ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getAmbientShadowColor();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetAmbientShadowColor
  (JNIEnv *env, jclass jclass, jlong ptr, jint color) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setAmbientShadowColor(static_cast<uint32_t>(color));
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetSpotShadowColor
  (JNIEnv *env, jclass jclass, jlong ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getSpotShadowColor();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetSpotShadowColor
  (JNIEnv *env, jclass jclass, jlong ptr, jint color) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setSpotShadowColor(static_cast<uint32_t>(color));
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetRotationX
  (JNIEnv *env, jclass jclass, jlong ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getRotationX();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetRotationX
  (JNIEnv *env, jclass jclass, jlong ptr, jfloat rotationX) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setRotationX(rotationX);
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetRotationY
  (JNIEnv *env, jclass jclass, jlong ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getRotationY();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetRotationY
  (JNIEnv *env, jclass jclass, jlong ptr, jfloat rotationY) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setRotationY(rotationY);
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetRotationZ
  (JNIEnv *env, jclass jclass, jlong ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getRotationZ();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetRotationZ
  (JNIEnv *env, jclass jclass, jlong ptr, jfloat rotationZ) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setRotationZ(rotationZ);
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetCameraDistance
  (JNIEnv *env, jclass jclass, jlong ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getCameraDistance();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetCameraDistance
  (JNIEnv *env, jclass jclass, jlong ptr, jfloat distance) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setCameraDistance(distance);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetClipRect
  (JNIEnv *env, jclass jclass, jlong ptr, jfloat left, jfloat top, jfloat right, jfloat bottom, jint mode, jboolean antiAlias) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setClipRect(SkRect::MakeLTRB(left, top, right, bottom), static_cast<SkClipOp>(mode), antiAlias);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetClipRRect
  (JNIEnv *env, jclass jclass, jlong ptr, jfloat left, jfloat top, jfloat right, jfloat bottom, jfloatArray radii, jint radiiSize, jint mode, jboolean antiAlias) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setClipRRect(skija::RRect::toSkRRect(env, left, top, right, bottom, radii), static_cast<SkClipOp>(mode), antiAlias);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetClipPath
  (JNIEnv *env, jclass jclass, jlong ptr, jlong pathPtr, jint mode, jboolean antiAlias) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    SkPath* path = reinterpret_cast<SkPath*>(static_cast<uintptr_t>(pathPtr));
    instance->setClipPath(path ? std::optional<SkPath>{*path} : std::nullopt, static_cast<SkClipOp>(mode), antiAlias);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetClip
  (JNIEnv *env, jclass jclass, jlong ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    return instance->getClip();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetClip
  (JNIEnv *env, jclass jclass, jlong ptr, jboolean clip) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->setClip(clip);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nBeginRecording
  (JNIEnv *env, jclass jclass, jlong ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    SkCanvas *canvas = instance->beginRecording();
    return reinterpret_cast<jlong>(canvas);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nEndRecording
  (JNIEnv *env, jclass jclass, jlong ptr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    instance->endRecording();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nDrawInto
  (JNIEnv *env, jclass jclass, jlong ptr, jlong canvasPtr) {
    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
    auto canvas = reinterpret_cast<SkCanvas *>(canvasPtr);
    instance->drawInto(canvas);
}
