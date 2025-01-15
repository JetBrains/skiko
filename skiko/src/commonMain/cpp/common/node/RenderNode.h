#pragma once
#include <SkM44.h>
#include <SkPicture.h>
#include <SkPictureRecorder.h>

namespace skiko {
namespace node {

class RenderNodeManager;

class RenderNode {
public:
    RenderNode(RenderNodeManager *manager);
    ~RenderNode();

    SkCanvas * beginRecording();
    void endRecording();

    void drawPlaceholder(SkCanvas *canvas);
    void drawContent(SkCanvas *canvas);
private:
    RenderNodeManager *manager;
    SkPictureRecorder *recorder;
    sk_sp<SkPicture> placeholder;
    sk_sp<SkPicture> picture;
    SkM44 matrix;
    bool matrixIdentity;
};

} // namespace node
} // namespace skiko
