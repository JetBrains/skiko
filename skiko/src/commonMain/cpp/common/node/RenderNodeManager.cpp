#include <SkNWayCanvas.h>
#include "node/RenderNode.h"
#include "node/RenderNodeManager.h"

namespace skiko {
namespace node {

class SkikoPictureFilterCanvas : public SkNWayCanvas {
public:
    SkikoPictureFilterCanvas(RenderNodeManager * manager, SkCanvas* canvas)
      : SkNWayCanvas(canvas->imageInfo().width(), canvas->imageInfo().height()),
        manager(manager) {
        this->addCanvas(canvas);
    }

protected:
    void onDrawPicture(const SkPicture* picture, const SkMatrix* matrix, const SkPaint* paint) override {
        bool handled = manager->drawPlaceholder(this, picture);
        if (!handled) {
            SkCanvas::onDrawPicture(picture, matrix, paint);
        }
    }

private:
    RenderNodeManager *manager;
};

RenderNodeManager::RenderNodeManager(bool measureDrawBounds)
    : measureDrawBounds(measureDrawBounds) {
}

void RenderNodeManager::setLightingInfo(
    const LightGeometry& lightGeometry,
    const LightInfo& lightInfo
) {
    this->lightGeometry = lightGeometry;
    this->lightInfo = lightInfo;
}

SkCanvas * RenderNodeManager::createRenderNodeCanvas(SkCanvas *canvas) {
    return new SkikoPictureFilterCanvas(this, canvas);
}

void RenderNodeManager::registerPlaceholder(SkPicture *picture, RenderNode *renderNode) {
    this->placeholders[picture->uniqueID()] = renderNode;
}

void RenderNodeManager::unregisterPlaceholder(SkPicture *picture) {
    this->placeholders.erase(picture->uniqueID());
}

bool RenderNodeManager::drawPlaceholder(SkCanvas *canvas, const SkPicture *picture) {
    auto it = this->placeholders.find(picture->uniqueID());
    if (it != this->placeholders.end()) {
        it->second->drawContent(canvas);
        return true;
    } else {
        return false;
    }
}

} // namespace node
} // namespace skiko
