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
