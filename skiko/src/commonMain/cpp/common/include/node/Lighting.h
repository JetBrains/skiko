#pragma once
#include <SkPoint3.h>

// Adoption of frameworks/base/libs/hwui/Lighting.h

namespace skiko {
namespace node {

struct LightGeometry {
    SkPoint3 center;
    float radius;
};

struct LightInfo {
    float ambientShadowAlpha;
    float spotShadowAlpha;
};

} // namespace node
} // namespace skiko
