#pragma once
#include <SkRefCnt.h>
#include "Lighting.h"

namespace skiko {
namespace node {

class RenderNode;

class RenderNodeContext : public SkRefCnt {
public:
    RenderNodeContext(bool measureDrawBounds);

    bool shouldMeasureDrawBounds() const { return this->measureDrawBounds; }

    const LightGeometry& getLightGeometry() const { return this->lightGeometry; }
    const LightInfo& getLightInfo() const { return this->lightInfo; }
    void setLightingInfo(
        const LightGeometry& lightGeometry,
        const LightInfo& lightInfo
    );

private:
    LightGeometry lightGeometry;
    LightInfo lightInfo;
    bool measureDrawBounds;
};

} // namespace node
} // namespace skiko
