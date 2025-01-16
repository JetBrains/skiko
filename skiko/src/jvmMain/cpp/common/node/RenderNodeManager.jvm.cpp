#include <jni.h>
#include "../interop.hh"
#include "node/RenderNodeManager.h"

static void deleteRenderNodeManager(skiko::node::RenderNodeManager *renderNodeManager) {
    delete renderNodeManager;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_node_RenderNodeManagerKt_RenderNodeManager_1nMake
  (JNIEnv *env, jclass jclass, jboolean measureDrawBounds) {
    auto instance = new skiko::node::RenderNodeManager(measureDrawBounds);
    return reinterpret_cast<jlong>(instance);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_node_RenderNodeManagerKt_RenderNodeManager_1nGetFinalizer
  (JNIEnv *env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteRenderNodeManager));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeManagerKt_RenderNodeManager_1nSetLightingInfo
  (JNIEnv *env, jclass jclass, jlong ptr, jfloat centerX, jfloat centerY, jfloat centerZ, jfloat radius, jfloat ambientShadowAlpha, jfloat spotShadowAlpha) {
    auto instance = reinterpret_cast<skiko::node::RenderNodeManager *>(ptr);
    skiko::node::LightGeometry lightGeometry {
        SkPoint3{centerX, centerY, centerZ},
        radius
    };
    skiko::node::LightInfo lightInfo {
        ambientShadowAlpha,
        spotShadowAlpha
    };
    instance->setLightingInfo(lightGeometry, lightInfo);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_node_RenderNodeManagerKt_RenderNodeManager_1nCreateRenderNodeCanvas
  (JNIEnv *env, jclass jclass, jlong ptr, jlong canvasPtr) {
    auto instance = reinterpret_cast<skiko::node::RenderNodeManager *>(ptr);
    auto canvas = reinterpret_cast<SkCanvas *>(canvasPtr);
    SkCanvas *renderNodeCanvas = instance->createRenderNodeCanvas(canvas);
    return reinterpret_cast<jlong>(renderNodeCanvas);
}
