#include <jni.h>
#include "../interop.hh"
#include "node/RenderNodeContext.h"

static void deleteRenderNodeContext(skiko::node::RenderNodeContext *RenderNodeContext) {
    delete RenderNodeContext;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_node_RenderNodeContextKt_RenderNodeContext_1nMake
  (JNIEnv *env, jclass jclass, jboolean measureDrawBounds) {
    auto instance = new skiko::node::RenderNodeContext(measureDrawBounds);
    return reinterpret_cast<jlong>(instance);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_node_RenderNodeContextKt_RenderNodeContext_1nGetFinalizer
  (JNIEnv *env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteRenderNodeContext));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skiko_node_RenderNodeContextKt_RenderNodeContext_1nSetLightingInfo
  (JNIEnv *env, jclass jclass, jlong ptr, jfloat centerX, jfloat centerY, jfloat centerZ, jfloat radius, jfloat ambientShadowAlpha, jfloat spotShadowAlpha) {
    auto instance = reinterpret_cast<skiko::node::RenderNodeContext *>(ptr);
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
