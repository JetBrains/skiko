#include <jni.h>
#include "../interop.hh"
#include "node/RenderNodeContext.h"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_node_RenderNodeContextKt_RenderNodeContext_1nMake
  (JNIEnv *env, jclass jclass, jboolean measureDrawBounds) {
    auto instance = sk_make_sp<skiko::node::RenderNodeContext>(measureDrawBounds);
    return reinterpret_cast<jlong>(instance.release());
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
