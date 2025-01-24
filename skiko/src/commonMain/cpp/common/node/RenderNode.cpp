#include <SkNWayCanvas.h>
#include <SkPicture.h>
#include <SkShadowUtils.h>
#include "node/RenderNode.h"
#include "node/RenderNodeContext.h"

namespace skiko {
namespace node {

// The goal with selecting the size of the rectangle here is to avoid limiting the
// drawable area as much as possible.
// Due to https://issuetracker.google.com/issues/324465764 we have to
// leave room for scale between the values we specify here and Float.MAX_VALUE.
// The maximum possible scale that can be applied to the canvas will be
// Float.MAX_VALUE divided by the largest value below.
// 2^30 was chosen because it's big enough, leaves quite a lot of room between it
// and Float.MAX_VALUE, and also lets the width and height fit into int32 (just in
// case).
static const float UNKNOWN_BOUNDS_MIN_VALUE = static_cast<float>(-(1L << 30));
static const float UNKNOWN_BOUNDS_MAX_VALUE = static_cast<float>((1L << 30) - 1);
static SkRect UNKNOWN_BOUNDS {
    UNKNOWN_BOUNDS_MIN_VALUE,
    UNKNOWN_BOUNDS_MIN_VALUE,
    UNKNOWN_BOUNDS_MAX_VALUE,
    UNKNOWN_BOUNDS_MAX_VALUE
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

static SkColor multiplyAlpha(SkColor color, float alpha) {
    return SkColorSetA(color, alpha * SkColorGetA(color));
}

class UnrollDrawableCanvas : public SkNWayCanvas {
public:
    UnrollDrawableCanvas(SkCanvas* canvas)
        : SkNWayCanvas(canvas->imageInfo().width(), canvas->imageInfo().height()) {
        this->addCanvas(canvas);
    }

protected:
    void onDrawDrawable(SkDrawable* drawable, const SkMatrix* matrix) override {
        drawable->draw(this, matrix);
    }
};

class DependencyTrackerCanvas : public SkNoDrawCanvas {
public:
    DependencyTrackerCanvas(std::set<RenderNode *> *dependencies)
        : SkNoDrawCanvas(INT_MAX, INT_MAX), dependencies(dependencies) {
        dependencies->clear();
    }

protected:
    void onDrawDrawable(SkDrawable* drawable, const SkMatrix* matrix) override {
        if (auto renderNode = dynamic_cast<RenderNode *>(drawable)) {
            dependencies->insert(renderNode);
        } else {
            drawable->draw(this, matrix);
        }
    }

private:
    std::set<RenderNode *> *dependencies;
};

RenderNode::RenderNode(const sk_sp<RenderNodeContext>& context)
    : context(context),
      bbhFactory(context->shouldMeasureDrawBounds() ? new SkRTreeFactory() : nullptr),
      recorder(),
      contentCache(),
      contentSnapshot(),
      contentSnapshotId(0),
      contentDependencies(),
      layerPaint(),
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
      clipRect(),
      clipRRect(),
      clipPath(),
      clip(false),
      transformMatrix(),
      transformCamera(),
      matrixIdentity(true),
      matrixDirty(false) {
    this->setCameraLocation(0.0f, 0.0f, DEFAULT_CAMERA_DISTANCE);
}

RenderNode::~RenderNode() {
    if (this->bbhFactory) {
        delete this->bbhFactory;
        this->bbhFactory = nullptr;
    }
}

void RenderNode::setLayerPaint(const std::optional<SkPaint>& layerPaint) {
    this->layerPaint = layerPaint;
    this->notifyDrawingChanged();
}

void RenderNode::setBounds(const SkRect& bounds) {
    this->bounds = bounds;
    this->matrixDirty = true;
    this->notifyDrawingChanged();
}

void RenderNode::setPivot(const SkPoint& pivot) {
    this->pivot = pivot;
    this->matrixDirty = true;
    this->notifyDrawingChanged();
}

void RenderNode::setAlpha(float alpha) {
    this->alpha = alpha;
    this->notifyDrawingChanged();
}

void RenderNode::setScaleX(float scaleX) {
    this->scaleX = scaleX;
    this->matrixDirty = true;
    this->notifyDrawingChanged();
}

void RenderNode::setScaleY(float scaleY) {
    this->scaleY = scaleY;
    this->matrixDirty = true;
    this->notifyDrawingChanged();
}

void RenderNode::setTranslationX(float translationX) {
    this->translationX = translationX;
    this->matrixDirty = true;
    this->notifyDrawingChanged();
}

void RenderNode::setTranslationY(float translationY) {
    this->translationY = translationY;
    this->matrixDirty = true;
    this->notifyDrawingChanged();
}

void RenderNode::setShadowElevation(float shadowElevation) {
    this->shadowElevation = shadowElevation;
    this->notifyDrawingChanged();
}

void RenderNode::setAmbientShadowColor(SkColor ambientShadowColor) {
    this->ambientShadowColor = ambientShadowColor;
    this->notifyDrawingChanged();
}

void RenderNode::setSpotShadowColor(SkColor spotShadowColor) {
    this->spotShadowColor = spotShadowColor;
    this->notifyDrawingChanged();
}

void RenderNode::setRotationX(float rotationX) {
    this->rotationX = rotationX;
    this->matrixDirty = true;
    this->notifyDrawingChanged();
}

void RenderNode::setRotationY(float rotationY) {
    this->rotationY = rotationY;
    this->matrixDirty = true;
    this->notifyDrawingChanged();
}

void RenderNode::setRotationZ(float rotationZ) {
    this->rotationZ = rotationZ;
    this->matrixDirty = true;
    this->notifyDrawingChanged();
}

float RenderNode::getCameraDistance() const {
    return this->transformCamera.fLocation.z / 72.0f;
}

void RenderNode::setCameraDistance(float cameraDistance) {
    this->setCameraLocation(0.0f, 0.0f, cameraDistance);
    this->matrixDirty = true;
    this->notifyDrawingChanged();
}

void RenderNode::setClipRect(const std::optional<SkRect>& clipRect) {
    this->clipRect = clipRect;
    this->clipRRect.reset();
    this->clipPath.reset();
    this->notifyDrawingChanged();
}

void RenderNode::setClipRRect(const std::optional<SkRRect>& clipRRect) {
    this->clipRect.reset();
    this->clipRRect = clipRRect;
    this->clipPath.reset();
    this->notifyDrawingChanged();
}

void RenderNode::setClipPath(const std::optional<SkPath>& clipPath) {
    this->clipRect.reset();
    this->clipRRect.reset();
    this->clipPath = clipPath;
    this->notifyDrawingChanged();
}

void RenderNode::setClip(bool clip) {
    this->clip = clip;
    this->notifyDrawingChanged();
}

const SkMatrix& RenderNode::getMatrix() {
    this->updateMatrix();
    return this->transformMatrix;
}

int64_t RenderNode::getSnapshotId() {
    int64_t snapshotId = this->getGenerationID();
    for (auto renderNode : this->contentDependencies) {
        snapshotId += renderNode->getSnapshotId();
    }
    return snapshotId;
}

SkCanvas *RenderNode::beginRecording() {
    bool canDrawOutOfBounds = !clip || shadowElevation > 0.0f;
    const SkRect& bounds = canDrawOutOfBounds ? UNKNOWN_BOUNDS : SkRect::MakeWH(this->bounds.width(), this->bounds.height());
    SkBBHFactory* bbhFactory = canDrawOutOfBounds ? this->bbhFactory : nullptr;
    return this->recorder.beginRecording(bounds, bbhFactory);
}

void RenderNode::endRecording() {
    this->contentCache = this->recorder.finishRecordingAsDrawable();
    this->contentSnapshot.reset();
    this->contentSnapshotId = 0;

    // Update dependencies
    DependencyTrackerCanvas dependencyTracker(&this->contentDependencies);
    this->contentCache->draw(&dependencyTracker);

    this->notifyDrawingChanged();
}

void RenderNode::drawInto(SkCanvas* canvas) {
    canvas->drawDrawable(this);
}

SkRect RenderNode::onGetBounds() {
    this->updateMatrix();
    SkRect result;
    if (this->contentCache) {
        result = this->contentCache->getBounds().makeOffset(this->bounds.left(), this->bounds.top());
    } else {
        bool canDrawOutOfBounds = !clip || shadowElevation > 0.0f;
        result = canDrawOutOfBounds ? UNKNOWN_BOUNDS : this->bounds;
    }
    if (!this->matrixIdentity) {
        this->transformMatrix.mapRect(&result);
    }
    return result;
}

size_t RenderNode::onApproximateBytesUsed() {
    size_t contentSize = 0;
    if (this->contentCache) {
        contentSize += this->contentCache->approximateBytesUsed();
    }
    if (this->clipPath) {
        contentSize += this->clipPath->approximateBytesUsed();
    }
    return sizeof(*this) + contentSize;
}

void RenderNode::onDraw(SkCanvas* canvas) {
    this->updateMatrix();

    canvas->translate(this->bounds.left(), this->bounds.top());
    if (!this->matrixIdentity) {
        canvas->concat(this->transformMatrix);
    }

    if (this->shadowElevation > 0) {
        auto lightGeometry = this->context->getLightGeometry();
        auto lightInfo = this->context->getLightInfo();
        this->drawShadow(canvas, lightGeometry, lightInfo);
    }

    if (this->clip) {
        canvas->save();
        if (this->clipRect) {
            canvas->clipRect(*this->clipRect);
        } else if (this->clipRRect) {
            canvas->clipRRect(*this->clipRRect);
        } else if (this->clipPath) {
            canvas->clipPath(*this->clipPath);
        } else {
            canvas->clipRect(SkRect::MakeWH(this->bounds.width(), this->bounds.height()));
        }
    }

    if (this->layerPaint) {
        canvas->saveLayer(SkRect::MakeWH(this->bounds.width(), this->bounds.height()), &*this->layerPaint);
    } else {
        canvas->save();
    }

    this->updateSnapshot();
    if (this->contentSnapshot) {
        canvas->drawPicture(this->contentSnapshot.get());
    } else {
        canvas->drawDrawable(this->contentCache.get());
    }
}

// Note: the assupmtion is that the context's light geometry uses the same coordinate space.
sk_sp<SkPicture> RenderNode::onMakePictureSnapshot() {
    SkPictureRecorder recorder;

    const SkRect bounds = this->getBounds();
    SkCanvas* canvas = recorder.beginRecording(bounds);
    // Force unrolling all drawables to avoid nested snapshoting.
    UnrollDrawableCanvas unrollCanvas(canvas);
    this->draw(&unrollCanvas);
    return recorder.finishRecordingAsPicture();
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

void RenderNode::updateSnapshot() {
    if (!this->contentCache) {
        return;
    }
    int64_t currentContentSnapshotId = this->getContentSnapshotId();
    if (this->contentSnapshot && this->contentSnapshotId == currentContentSnapshotId) {
        return;
    }
    if (this->isContentContainsShadow()) {
        this->contentSnapshot.reset();
        this->contentSnapshotId = 0;
        return;
    }

    SkPictureRecorder recorder;
    auto contentBounds = this->contentCache->getBounds();
    SkCanvas* recordingCanvas = recorder.beginRecording(contentBounds);
    // Force unrolling all drawables to avoid nested snapshoting.
    UnrollDrawableCanvas unrollCanvas(recordingCanvas);
    this->contentCache->draw(&unrollCanvas);
    this->contentSnapshot = recorder.finishRecordingAsPicture();
    this->contentSnapshotId = currentContentSnapshotId;
}

void RenderNode::drawShadow(SkCanvas *canvas, const LightGeometry& lightGeometry, const LightInfo& lightInfo) {
    SkPath tmpPath, *path = &tmpPath;
    if (this->clipRect) {
        tmpPath.addRect(*this->clipRect);
    } else if (this->clipRRect) {
        tmpPath.addRRect(*this->clipRRect);
    } else if (this->clipPath) {
        path = &*this->clipPath;
    } else {
        return;
    }

    SkPoint3 zParams{0.0f, 0.0f, this->shadowElevation};
    float ambientAlpha = lightInfo.ambientShadowAlpha * this->alpha;
    float spotAlpha = lightInfo.spotShadowAlpha * this->alpha;
    SkColor ambientColor = multiplyAlpha(this->ambientShadowColor, ambientAlpha);
    SkColor spotColor = multiplyAlpha(this->spotShadowColor, spotAlpha);

    SkShadowUtils::DrawShadow(
        canvas,
        *path,
        zParams,
        lightGeometry.center,
        lightGeometry.radius,
        ambientColor,
        spotColor,
        this->alpha < 1.0f ? SkShadowFlags::kTransparentOccluder_ShadowFlag : SkShadowFlags::kNone_ShadowFlag
    );
}

int64_t RenderNode::getContentSnapshotId() {
    int64_t snapshotId = 0L;
    for (auto renderNode : this->contentDependencies) {
        snapshotId += renderNode->getSnapshotId();
    }
    return snapshotId;
}


bool RenderNode::isContentContainsShadow() const {
    if (this->shadowElevation > 0) {
        return true;
    }
    for (auto renderNode : this->contentDependencies) {
        if (renderNode->isContentContainsShadow()) {
            return true;
        }
    }
    return false;
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
