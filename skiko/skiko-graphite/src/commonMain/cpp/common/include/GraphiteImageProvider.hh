#pragma once

#include "include/core/SkImage.h"
#include "include/gpu/graphite/ImageProvider.h"

#include <memory>

// Adapted from Skia's GraphiteToolUtils.cpp:
// https://github.com/google/skia/blob/c16d0e9f30b1a1613401c0db3c93a3c2aa37c8ba/tools/graphite/GraphiteToolUtils.cpp#L26
class SkikoGraphiteImageProvider final : public skgpu::graphite::ImageProvider {
public:
    static sk_sp<SkikoGraphiteImageProvider> Make();

    ~SkikoGraphiteImageProvider() override;

    sk_sp<SkImage> findOrCreate(
            skgpu::graphite::Recorder* recorder,
            const SkImage* image,
            SkImage::RequiredProperties requiredProperties) override;

private:
    SkikoGraphiteImageProvider();

    struct Impl;
    std::unique_ptr<Impl> fImpl;
};
