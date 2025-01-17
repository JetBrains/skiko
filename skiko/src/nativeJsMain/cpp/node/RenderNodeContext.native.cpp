#include "common.h"
#include "node/RenderNodeContext.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skiko_node_RenderNodeContextKt_RenderNodeContext_1nMake
  (KBoolean measureDrawBounds) {
    auto instance = sk_make_sp<skiko::node::RenderNodeContext>(measureDrawBounds);
    return reinterpret_cast<KNativePointer>(instance.release());
}

SKIKO_EXPORT void org_jetbrains_skiko_node_RenderNodeContextKt_RenderNodeContext_1nSetLightingInfo
  (KNativePointer ptr, KFloat centerX, KFloat centerY, KFloat centerZ, KFloat radius, KFloat ambientShadowAlpha, KFloat spotShadowAlpha) {
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
