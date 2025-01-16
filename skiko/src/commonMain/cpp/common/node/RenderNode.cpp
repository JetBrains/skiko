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

static const float DEFAULT_CAMERA_DISTANCE = 8.0f;
static const float NON_ZERO_EPSILON = 0.001f;

/**
 * Check for floats that are close enough to zero.
 */
inline static bool isZero(float value) {
    // Using fabsf is more performant as ARM computes
    // fabsf in a single instruction.
    return fabsf(value) <= NON_ZERO_EPSILON;
}

RenderNode::RenderNode(RenderNodeManager *manager)
    : manager(manager),
      bbhFactory(manager->shouldMeasureDrawBounds() ? new SkRTreeFactory() : nullptr),
      recorder(),
      picture(),
      placeholder(),
      bounds { 0.0f, 0.0f, 0.0f, 0.0f },
      pivot { SK_FloatNaN, SK_FloatNaN },
      alpha(1.0f),
      scaleX(1.0f),
      scaleY(1.0f),
      translationX(0.0f),
      translationY(0.0f),
      shadowElevation(0.0f),
      ambientShadowColor(SK_ColorBLACK),
      spotShadowColor(SK_ColorBLACK),
      rotationX(0.0f),
      rotationY(0.0f),
      rotationZ(0.0f),
      clip(false),
      transformMatrix(),
      transformCamera(),
      matrixIdentity(true),
      matrixDirty(false) {
    this->placeholder = SkPicture::MakePlaceholder(PICTURE_BOUNDS);
    this->manager->registerPlaceholder(this->placeholder.get(), this);
    this->setCameraLocation(0.0f, 0.0f, DEFAULT_CAMERA_DISTANCE);
}

RenderNode::~RenderNode() {
    this->manager->unregisterPlaceholder(this->placeholder.get());
    if (this->bbhFactory) {
          delete this->bbhFactory;
    }
}

void RenderNode::setBounds(const SkRect& bounds) {
    this->bounds = bounds;
    //    this->outlineDirty = true;
    this->matrixDirty = true;
}

void RenderNode::setPivot(const SkPoint& pivot) {
    this->pivot = pivot;
    this->matrixDirty = true;
}

void RenderNode::setAlpha(float alpha) {
    this->alpha = alpha;
    // TODO recheck if we need to invalidate something
}

void RenderNode::setScaleX(float scaleX) {
    this->scaleX = scaleX;
    this->matrixDirty = true;
}

void RenderNode::setScaleY(float scaleY) {
    this->scaleY = scaleY;
    this->matrixDirty = true;
}

void RenderNode::setTranslationX(float translationX) {
    this->translationX = translationX;
    this->matrixDirty = true;
}

void RenderNode::setTranslationY(float translationY) {
    this->translationY = translationY;
    this->matrixDirty = true;
}

void RenderNode::setShadowElevation(float shadowElevation) {
    this->shadowElevation = shadowElevation;
//    this->outlineDirty = true;
}

void RenderNode::setAmbientShadowColor(SkColor ambientShadowColor) {
    this->ambientShadowColor = ambientShadowColor;
}

void RenderNode::setSpotShadowColor(SkColor spotShadowColor) {
    this->spotShadowColor = spotShadowColor;
}

void RenderNode::setRotationX(float rotationX) {
    this->rotationX = rotationX;
    this->matrixDirty = true;
}

void RenderNode::setRotationY(float rotationY) {
    this->rotationY = rotationY;
    this->matrixDirty = true;
}

void RenderNode::setRotationZ(float rotationZ) {
    this->rotationZ = rotationZ;
    this->matrixDirty = true;
}

float RenderNode::getCameraDistance() const {
    return this->transformCamera.fLocation.z / 72.0f;
}

void RenderNode::setCameraDistance(float cameraDistance) {
    this->setCameraLocation(0.0f, 0.0f, cameraDistance);
    this->matrixDirty = true;
}

void RenderNode::setClip(bool clip) {
  this->clip = clip;
}

const SkMatrix& RenderNode::getMatrix() {
    this->updateMatrix();
    return this->transformMatrix;
}

SkCanvas *RenderNode::beginRecording() {
    bool measureDrawBounds = !clip || shadowElevation > 0.0f;
    const SkRect& bounds = measureDrawBounds ? PICTURE_BOUNDS : this->bounds;
    SkBBHFactory* bbhFactory = measureDrawBounds ? this->bbhFactory : nullptr;
    return this->recorder.beginRecording(bounds, bbhFactory);
}

void RenderNode::endRecording() {
    this->picture = this->recorder.finishRecordingAsPicture();
}

void RenderNode::drawPlaceholder(SkCanvas *canvas) {
    canvas->drawPicture(this->placeholder.get());
}

void RenderNode::drawContent(SkCanvas *canvas) {
    // TODO configureOutline()
    this->updateMatrix();

    int restoreCount = canvas->save();
    canvas->translate(this->bounds.left(), this->bounds.top());
    if (!this->matrixIdentity) {
        canvas->concat(this->transformMatrix);
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

// Adoption from frameworks/base/libs/hwui/RenderProperties.cpp
void RenderNode::updateMatrix() {
    if (!this->matrixDirty) {
        return;
    }
    float pivotX, pivotY;
    if (this->pivot.isFinite()) {
        pivotX = this->pivot.fX;
        pivotY = this->pivot.fY;
    } else {
        pivotX = this->bounds.width() / 2.0f;
        pivotY = this->bounds.height() / 2.0f;
    }
    this->transformMatrix.reset();
    if (isZero(this->rotationX) && isZero(this->rotationY)) {
        this->transformMatrix.setTranslate(this->translationX, this->translationY);
        this->transformMatrix.preRotate(this->rotationZ, pivotX, pivotY);
        this->transformMatrix.preScale(this->scaleX, this->scaleY, pivotX, pivotY);
    } else {
        this->transformMatrix.preScale(this->scaleX, this->scaleY, pivotX, pivotY);

        SkM44 transform3D;
        transform3D.preConcat(SkM44::Rotate({1, 0, 0}, -this->rotationX * SK_ScalarPI / 180));
        transform3D.preConcat(SkM44::Rotate({0,-1, 0}, -this->rotationY * SK_ScalarPI / 180));
        transform3D.preConcat(SkM44::Rotate({0, 0, 1}, -this->rotationZ * SK_ScalarPI / 180));
        SkPatch3D patch3D;
        patch3D.transform(transform3D);

        SkMatrix transform;
        this->transformCamera.patchToMatrix(patch3D, &transform);
        transform.preTranslate(-pivotX, -pivotY);
        transform.postTranslate(pivotX + this->translationX, pivotY + this->translationY);
        this->transformMatrix.postConcat(transform);
    }
    this->matrixDirty = false;
    this->matrixIdentity = this->transformMatrix.isIdentity();
}

// Adoption from frameworks/base/libs/hwui/RenderProperties.cpp
void RenderNode::setCameraLocation(float x, float y, float z) {
    // the camera location is passed in inches, set in pt
    SkScalar lz = z * 72.0f;
    this->transformCamera.fLocation = {x * 72.0f, y * 72.0f, lz};
    this->transformCamera.fObserver = {0, 0, lz};
    this->transformCamera.update();
}

} // namespace node
} // namespace skiko

