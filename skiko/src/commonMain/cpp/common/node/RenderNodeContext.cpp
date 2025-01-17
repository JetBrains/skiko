#include "node/RenderNodeContext.h"

namespace skiko {
namespace node {

RenderNodeContext::RenderNodeContext(bool measureDrawBounds)
    : measureDrawBounds(measureDrawBounds) {
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
