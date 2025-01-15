#include "node/RenderNode.h"
#include "node/RenderNodeManager.h"

namespace skiko {
namespace node {


// The goal with selecting the size of the rectangle here is to avoid limiting the
// drawable area as much as possible.
// Due to https://partnerissuetracker.corp.google.com/issues/324465764 we have to
// leave room for scale between the values we specify here and Float.MAX_VALUE.
// The maximum possible scale that can be applied to the canvas will be
// Float.MAX_VALUE divided by the largest value below.
// 2^30 was chosen because it's big enough, leaves quite a lot of room between it
// and Float.MAX_VALUE, and also lets the width and height fit into int32 (just in
// case).
static const float PICTURE_MIN_VALUE = static_cast<float>(-(1L << 30));
static const float PICTURE_MAX_VALUE = static_cast<float>((1L << 30) - 1);
static SkRect PICTURE_BOUNDS {
    PICTURE_MIN_VALUE,
    PICTURE_MIN_VALUE,
    PICTURE_MAX_VALUE,
    PICTURE_MAX_VALUE
};

RenderNode::RenderNode(RenderNodeManager *manager)
   : manager(manager),
     bbhFactory(manager->shouldMeasureDrawBounds() ? new SkRTreeFactory() : nullptr),
     recorder(),
     matrix(),
     matrixIdentity(true) {
    this->placeholder = SkPicture::MakePlaceholder(PICTURE_BOUNDS);
    this->manager->registerPlaceholder(this->placeholder.get(), this);
}

RenderNode::~RenderNode() {
    this->manager->unregisterPlaceholder(this->placeholder.get());
}

SkCanvas *RenderNode::beginRecording() {
    return this->recorder.beginRecording(PICTURE_BOUNDS, nullptr);
}

void RenderNode::endRecording() {
    this->picture = this->recorder.finishRecordingAsPicture();
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

