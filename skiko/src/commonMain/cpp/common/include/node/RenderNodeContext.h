#pragma once
#include <SkRefCnt.h>
#include "Lighting.h"

namespace skiko {
namespace node {

class RenderNode;

class RenderNodeContext : public SkRefCnt {
public:
    RenderNodeContext(bool measureDrawBounds, bool snapshotCache);

    bool shouldMeasureDrawBounds() const { return this->measureDrawBounds; }
    // Whether the nodes created with this context keep an SkPicture snapshot of their
    // recorded content. A snapshot inlines the drawing of the nodes below it, so this
    // holds for the whole tree rather than for a single node.
    bool shouldUseSnapshotCache() const { return this->snapshotCache; }

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
    bool snapshotCache;
};

} // namespace node
} // namespace skiko
