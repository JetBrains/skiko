#include "node/RenderNodeContext.h"

namespace skiko {
namespace node {

RenderNodeContext::RenderNodeContext(bool measureDrawBounds, bool snapshotCache)
    : measureDrawBounds(measureDrawBounds), snapshotCache(snapshotCache) {
}

void RenderNodeContext::setLightingInfo(
    const LightGeometry& lightGeometry,
    const LightInfo& lightInfo
) {
    this->lightGeometry = lightGeometry;
    this->lightInfo = lightInfo;
}

} // namespace node
} // namespace skiko
