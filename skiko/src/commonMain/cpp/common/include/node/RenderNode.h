#pragma once
#include <optional>
#include <set>
#include <SkBBHFactory.h>
#include <SkCamera.h>
#include <SkCanvas.h>
#include <SkColor.h>
#include <SkDrawable.h>
#include <SkMatrix.h>
#include <SkPaint.h>
#include <SkPath.h>
#include <SkPictureRecorder.h>
#include <SkPoint.h>
#include <SkRRect.h>
#include <SkRect.h>
#include <SkRefCnt.h>
#include "Lighting.h"

namespace skiko {
namespace node {

class RenderNodeContext;

class RenderNode : public SkDrawable {
public:
    RenderNode(const sk_sp<RenderNodeContext>& context);
    ~RenderNode();

    std::optional<SkPaint>& getLayerPaint() { return this->layerPaint; }
    void setLayerPaint(const std::optional<SkPaint>& layerPaint);
    const SkRect& getBounds() const { return this->bounds; }
    void setBounds(const SkRect& bounds);
    const SkPoint& getPivot() const { return this->pivot; }
    void setPivot(const SkPoint& pivot);
    float getAlpha() const { return this->alpha; }
    void setAlpha(float alpha);
    float getScaleX() const { return this->scaleX; }
    void setScaleX(float scaleX);
    float getScaleY() const { return this->scaleY; }
    void setScaleY(float scaleY);
    float getTranslationX() const { return this->translationX; }
    void setTranslationX(float translationX);
    float getTranslationY() const { return this->translationY; }
    void setTranslationY(float translationY);
    float getShadowElevation() const { return this->shadowElevation; }
    void setShadowElevation(float shadowElevation);
    SkColor getAmbientShadowColor() const { return this->ambientShadowColor; }
    void setAmbientShadowColor(SkColor ambientShadowColor);
    SkColor getSpotShadowColor() const { return this->spotShadowColor; }
    void setSpotShadowColor(SkColor spotShadowColor);
    float getRotationX() const { return this->rotationX; }
    void setRotationX(float rotationX);
    float getRotationY() const { return this->rotationY; }
    void setRotationY(float rotationY);
    float getRotationZ() const { return this->rotationZ; }
    void setRotationZ(float rotationZ);
    float getCameraDistance() const;
    void setCameraDistance(float cameraDistance);
    void setClipRect(const std::optional<SkRect>& clipRect);
    void setClipRRect(const std::optional<SkRRect>& clipRRect);
    void setClipPath(const std::optional<SkPath>& clipPath);
    bool getClip() const { return this->clip; }
    void setClip(bool clip);

    const SkMatrix& getMatrix();
    int64_t getSnapshotId();

    SkCanvas * beginRecording();
    void endRecording();

    void drawInto(SkCanvas *canvas);

protected:
    // SkDrawable
    SkRect onGetBounds() override;
    size_t onApproximateBytesUsed() override;

    void onDraw(SkCanvas* canvas) override;
    sk_sp<SkPicture> onMakePictureSnapshot() override;

private:
    void updateMatrix();
    void updateSnapshot();
    void drawShadow(SkCanvas *canvas, const LightGeometry& lightGeometry, const LightInfo& lightInfo);
    int64_t getContentSnapshotId();
    bool isContentContainsShadow() const;
    void setCameraLocation(float x, float y, float z);

    sk_sp<RenderNodeContext> context;

    SkBBHFactory *bbhFactory;
    SkPictureRecorder recorder;
    sk_sp<SkDrawable> contentCache;
    std::set<RenderNode *> contentDependencies;
    sk_sp<SkPicture> contentSnapshot;
    int64_t contentSnapshotId;

    std::optional<SkPaint> layerPaint;
    SkRect bounds;
    SkPoint pivot;
    float alpha;
    float scaleX, scaleY;
    float translationX, translationY;
    float shadowElevation;
    SkColor ambientShadowColor;
    SkColor spotShadowColor;
    float rotationX, rotationY, rotationZ;
    std::optional<SkRect> clipRect;
    std::optional<SkRRect> clipRRect;
    std::optional<SkPath> clipPath;
    bool clip;

    SkMatrix transformMatrix;
    SkCamera3D transformCamera;
    bool matrixIdentity;
    bool matrixDirty;
};

} // namespace node
} // namespace skiko
