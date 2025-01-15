#include "RenderNode.h"
#include "RenderNodeManager.h"

namespace skiko {
namespace node {

RenderNode::RenderNode(RenderNodeManager *manager) : manager(manager) {

}

RenderNode::~RenderNode() {

}

SkCanvas *RenderNode::beginRecording() {
    // TODO size
    return this->recorder->beginRecording(0, 0);
}

void RenderNode::endRecording() {
    this->picture = this->recorder->finishRecordingAsPicture();
}

void RenderNode::drawPlaceholder(SkCanvas *canvas) {
    canvas->drawPicture(this->placeholder.get());
}

void RenderNode::drawContent(SkCanvas *canvas) {
    // TODO configureOutline()
    // TODO updateMatrix()

    int restoreCount = canvas->save();
    if (!this->matrixIdentity) {
        canvas->concat(this->matrix);
    }

//    if (shadowElevation > 0) {
//        drawShadow(canvas)
//    }
//
//    if (clip) {
//        canvas.save()
//        canvas.clipOutline(internalOutline, bounds)
//    }
//
//    val useLayer = requiresLayer()
//    if (useLayer) {
//        canvas.saveLayer(
//            bounds,
//            SkPaint().apply {
//                setAlphaf(this@GraphicsLayer.alpha)
//                imageFilter = this@GraphicsLayer.renderEffect?.asSkiaImageFilter()
//                colorFilter = this@GraphicsLayer.colorFilter?.asSkiaColorFilter()
//                blendMode = this@GraphicsLayer.blendMode.toSkia()
//            }
//        )
//    } else {
//        canvas.save()
//    }

    canvas->drawPicture(this->picture.get());
    canvas->restoreToCount(restoreCount);
}

} // namespace node
} // namespace skiko

