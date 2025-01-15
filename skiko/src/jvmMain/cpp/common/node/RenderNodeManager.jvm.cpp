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

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_node_RenderNodeManagerKt_RenderNodeManager_1nCreateRenderNodeCanvas
  (JNIEnv *env, jclass jclass, jlong ptr, jlong canvasPtr) {
    auto instance = reinterpret_cast<skiko::node::RenderNodeManager *>(ptr);
    auto canvas = reinterpret_cast<SkCanvas *>(canvasPtr);
    SkCanvas *renderNodeCanvas = instance->createRenderNodeCanvas(canvas);
    return reinterpret_cast<jlong>(renderNodeCanvas);
}

//@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeManagerKt_RenderNodeManager_nSetLightingInfo")
//@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_node_RenderNodeManagerKt_RenderNodeManager_nSetLightingInfo")
//private external fun RenderNodeManager_nSetLightingInfo(ptr: NativePointer, centerX: Float, centerY: Float, centerZ: Float, radius: Float, ambientShadowAlpha: Float, spotShadowAlpha: Float)
