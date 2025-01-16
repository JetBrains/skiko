#include <jni.h>
#include "../interop.hh"
#include "node/RenderNode.h"

static void deleteRenderNode(skiko::node::RenderNode *renderNode) {
    delete renderNode;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nMake
  (JNIEnv *env, jclass jclass, skiko::node::RenderNodeManager *manager) {
    auto instance = new skiko::node::RenderNode(manager);
    return reinterpret_cast<jlong>(instance);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetFinalizer
  (JNIEnv *env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteRenderNode));
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

//extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetBlendMode
//  (JNIEnv *env, jclass jclass, jlong ptr) {
//    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
//    return static_cast<jint>(instance->getBlendMode());
//}
//
//extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetBlendMode
//  (JNIEnv *env, jclass jclass, jlong ptr, jint mode) {
//    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
//    instance->setBlendMode(static_cast<SkBlendMode>(mode));
//}
//
//extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetColorFilter
//  (JNIEnv *env, jclass jclass, jlong ptr) {
//    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
//    return reinterpret_cast<jlong>(instance->getColorFilter().release());
//}
//
//extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetColorFilter
//  (JNIEnv *env, jclass jclass, jlong ptr, jlong colorFilterPtr) {
//    auto instance = reinterpret_cast<skiko::node::RenderNode *>(ptr);
//    SkColorFilter* colorFilter = reinterpret_cast<SkColorFilter*>(static_cast<uintptr_t>(colorFilterPtr));
//    instance->setColorFilter(sk_ref_sp<SkColorFilter>(colorFilter));
//}

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
    instance->drawPlaceholder(canvas);
}
