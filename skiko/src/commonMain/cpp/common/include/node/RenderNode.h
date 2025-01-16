#pragma once
#include <SkBBHFactory.h>
#include <SkCamera.h>
#include <SkCanvas.h>
#include <SkColor.h>
#include <SkMatrix.h>
#include <SkPicture.h>
#include <SkPictureRecorder.h>
#include <SkPoint.h>
#include <SkRect.h>

namespace skiko {
namespace node {

class RenderNodeManager;

class RenderNode {
public:
    RenderNode(RenderNodeManager *manager);
    ~RenderNode();

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
    // compositingStrategy
    // blendMode
    // colorFilter
    // outline
    float getRotationX() const { return this->rotationX; }
    void setRotationX(float rotationX);
    float getRotationY() const { return this->rotationY; }
    void setRotationY(float rotationY);
    float getRotationZ() const { return this->rotationZ; }
    void setRotationZ(float rotationZ);
    float getCameraDistance() const;
    void setCameraDistance(float cameraDistance);
    bool getClip() const { return this->clip; }
    void setClip(bool clip);
    // renderEffect

    const SkMatrix& getMatrix();

    SkCanvas * beginRecording();
    void endRecording();

    void drawPlaceholder(SkCanvas *canvas);
    void drawContent(SkCanvas *canvas);

private:
    void updateMatrix();
    void setCameraLocation(float x, float y, float z);

    RenderNodeManager *manager;

    SkBBHFactory *bbhFactory;
    SkPictureRecorder recorder;
    sk_sp<SkPicture> placeholder;
    sk_sp<SkPicture> picture;

    // compositingStrategy
    SkRect bounds;
    SkPoint pivot;
    float alpha;
    float scaleX, scaleY;
    float translationX, translationY;
    float shadowElevation;
    SkColor ambientShadowColor;
    SkColor spotShadowColor;
    // blendMode
    // colorFilter
    // outline
    float rotationX, rotationY, rotationZ;
    bool clip;
    // renderEffect

    SkMatrix transformMatrix;
    SkCamera3D transformCamera;
    bool matrixIdentity;
    bool matrixDirty;
};

} // namespace node
} // namespace skiko
