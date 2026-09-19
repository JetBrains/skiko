#include "GraphiteImageProvider.hh"

#include "include/core/SkTiledImageUtils.h"
#include "include/gpu/graphite/Image.h"
#include "src/core/SkChecksum.h"
#include "src/core/SkLRUCache.h"

namespace {
constexpr int kDefaultNumCachedImages = 256;

class ImageKey {
public:
    ImageKey(const SkImage* image, bool mipmapped) {
        fValues[0] = 0;
        SkTiledImageUtils::GetImageKeyValues(image, &fValues[1]);
        fValues[kNumValues - 1] = mipmapped ? 1 : 0;
        fValues[0] = SkChecksum::Hash32(&fValues[1], (kNumValues - 1) * sizeof(uint32_t));
    }

    uint32_t hash() const { return fValues[0]; }

    bool operator==(const ImageKey& other) const {
        for (int i = 0; i < kNumValues; ++i) {
            if (fValues[i] != other.fValues[i]) return false;
        }
        return true;
    }

private:
    static constexpr int kNumValues = SkTiledImageUtils::kNumImageKeyValues + 2;
    uint32_t fValues[kNumValues];
};

struct ImageHash {
    size_t operator()(const ImageKey& key) const { return key.hash(); }
};
}  // namespace

struct SkikoGraphiteImageProvider::Impl {
    SkLRUCache<ImageKey, sk_sp<SkImage>, ImageHash> cache{kDefaultNumCachedImages};
};

SkikoGraphiteImageProvider::SkikoGraphiteImageProvider() : fImpl(std::make_unique<Impl>()) {}

SkikoGraphiteImageProvider::~SkikoGraphiteImageProvider() = default;

sk_sp<SkikoGraphiteImageProvider> SkikoGraphiteImageProvider::Make() {
    return sk_sp<SkikoGraphiteImageProvider>(new SkikoGraphiteImageProvider());
}

sk_sp<SkImage> SkikoGraphiteImageProvider::findOrCreate(
        skgpu::graphite::Recorder* recorder,
        const SkImage* image,
        SkImage::RequiredProperties requiredProperties) {
    if (!requiredProperties.fMipmapped) {
        if (auto cached = fImpl->cache.find(ImageKey(image, true))) return *cached;
    }

    ImageKey key(image, requiredProperties.fMipmapped);
    if (auto cached = fImpl->cache.find(key)) return *cached;

    sk_sp<SkImage> textureImage = SkImages::TextureFromImage(recorder, image, requiredProperties);
    if (!textureImage) return nullptr;

    return *fImpl->cache.insert(key, std::move(textureImage));
}
