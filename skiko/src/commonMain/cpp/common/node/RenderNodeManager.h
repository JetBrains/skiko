#pragma once
#include <unordered_map>
#include <SkCanvas.h>
#include <SkPicture.h>
#include "Lighting.h"

namespace skiko {
namespace node {

class RenderNode;

class RenderNodeManager {
public:
    void setLightingInfo(
        const LightGeometry& lightGeometry,
        const LightInfo& lightInfo
    );

    SkCanvas * createRenderNodeCanvas(SkCanvas *canvas);

    void registerPlaceholder(SkPicture *picture, RenderNode *renderNode);
    void unregisterPlaceholder(SkPicture *picture);

    void drawRenderNode(SkCanvas *canvas, RenderNode *renderNode);
    bool drawPlaceholder(SkCanvas *canvas, const SkPicture* picture);

private:
    // Picture.uniqueId -> RenderNode
    std::unordered_map<uint32_t, RenderNode *> placeholders;

    LightGeometry lightGeometry;
    LightInfo lightInfo;
};

} // namespace node
} // namespace skiko
