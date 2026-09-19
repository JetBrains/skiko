#pragma once
#include <optional>
#include <unordered_set>
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
    void setClipRect(const std::optional<SkRect>& clipRect, SkClipOp op = SkClipOp::kIntersect, bool doAntiAlias = false);
    void setClipRRect(const std::optional<SkRRect>& clipRRect, SkClipOp op = SkClipOp::kIntersect, bool doAntiAlias = false);
    void setClipPath(const std::optional<SkPath>& clipPath, SkClipOp op = SkClipOp::kIntersect, bool doAntiAlias = false);
    bool getClip() const { return this->clip; }
    void setClip(bool clip);

    const SkMatrix& getMatrix();

    SkCanvas * beginRecording();
    void endRecording();

    void drawInto(SkCanvas *canvas);

    SkFlattenable::Type getFlattenableType() const override;

protected:
    // SkDrawable
    SkRect onGetBounds() override;
    size_t onApproximateBytesUsed() override;
    void onDraw(SkCanvas* canvas) override;
    sk_sp<SkPicture> onMakePictureSnapshot() override;

private:
    // What changed about a node, as far as snapshot bookkeeping is concerned.
    enum SnapshotChange : uint32_t {
        // Drawing applied around the snapshot: transform, clip, layer, alpha. This
        // node's own snapshot stays valid; the snapshots that inlined it do not.
        kAppearance = 1 << 0,
        // The recorded content, or the cull rect it is recorded with.
        kContent = 1 << 1,
        // The set of drawn nodes, or what they draw, may differ, so re-derive from
        // the current dependencies whether this content can be snapshotted.
        kDependencies = 1 << 2,
    };

    SkCanvas *beginRecordingInto(SkPictureRecorder& target);
    // Whether the content may cover more than the node's own bounds, and so has to be
    // measured while recording instead of being culled to them.
    bool mayDrawOutOfBounds() const;
    // Whether this node's own drawing only replays correctly at the transform it was
    // recorded under, which bars every node that inlines it from snapshotting too.
    bool drawsTransformDependentContent() const;
    // Whether an observer that inlines this node cannot snapshot the result: true when this
    // node draws a shadow, or when anything in its own content does. This is the single bit
    // an observer reads from each of its dependencies.
    bool preventsObserverSnapshot() const;
    void updateMatrix();
    void updateDependencies();
    void releaseDependencies();
    void updateSnapshot();
    void invalidateSnapshot(uint32_t changes);
    void drawShadow(SkCanvas *canvas, const LightGeometry& lightGeometry, const LightInfo& lightInfo);
    void setCameraLocation(float x, float y, float z);

    sk_sp<RenderNodeContext> context;

    SkBBHFactory *bbhFactory;
    SkPictureRecorder recorder;
    sk_sp<SkDrawable> contentCache;
    sk_sp<SkPicture> contentSnapshot;
    // Whether the recorded content replays correctly under any transform, and so may be
    // snapshotted at all. Cleared by drawing whose result depends on the transform it is
    // replayed under -- today only a shadow, whose spot geometry follows the occluder's
    // position relative to a light fixed in device space, so replaying the recording
    // anywhere else yields a different shadow.
    bool contentTransformInvariant;
    // Whether the observers have already been told, since this node was last drawn, that
    // their inlined copy of it is stale. Lets a node reached through several observer paths
    // propagate once instead of once per path. Cleared when the node is drawn (they inline
    // it afresh) and when preventsObserverSnapshot() flips (they must re-derive).
    bool observersNotified;

    std::unordered_set<RenderNode *> dependencies, observers;

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
    SkClipOp clipOp;
    bool clipAntiAlias;
    bool clip;

    SkMatrix transformMatrix;
    SkCamera3D transformCamera;
    bool matrixIdentity;
    bool matrixDirty;
};

} // namespace node
} // namespace skiko
