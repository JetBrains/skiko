#include "GraphiteImageProvider.hh"

#include "include/core/SkTiledImageUtils.h"
#include "include/gpu/graphite/Image.h"
#include "src/core/SkChecksum.h"

#include <list>
#include <unordered_map>

namespace {
constexpr size_t kMaxCachedImages = 256;
// Textures uploaded by SkImages::TextureFromImage are not budgeted by the Recorder while the cache
// holds them; once evicted, they are recycled by the Recorder's own resource cache. So this limit
// is separate from, and smaller than, the Recorder's GPU budget.
constexpr size_t kMaxCachedImageBytes = 64 * 1024 * 1024;

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

// LRU cache of uploaded images, bounded by count and by size.
struct SkikoGraphiteImageProvider::Impl {
    struct Entry {
        ImageKey key;
        sk_sp<SkImage> image;
        size_t bytes;
    };

    // Most recently used first.
    std::list<Entry> entries;
    std::unordered_map<ImageKey, std::list<Entry>::iterator, ImageHash> index;
    size_t totalBytes = 0;

    Impl() { index.reserve(kMaxCachedImages); }

    sk_sp<SkImage> find(const ImageKey& key) {
        auto found = index.find(key);
        if (found == index.end()) return nullptr;
        entries.splice(entries.begin(), entries, found->second);
        return found->second->image;
    }

    void insert(const ImageKey& key, sk_sp<SkImage> image) {
        size_t bytes = image->textureSize();
        entries.push_front({key, std::move(image), bytes});
        index[key] = entries.begin();
        totalBytes += bytes;
        // Always keep the newest entry, even if it exceeds the limit on its own.
        while (entries.size() > 1 &&
               (entries.size() > kMaxCachedImages || totalBytes > kMaxCachedImageBytes)) {
            totalBytes -= entries.back().bytes;
            index.erase(entries.back().key);
            entries.pop_back();
        }
    }
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
        if (auto cached = fImpl->find(ImageKey(image, true))) return cached;
    }

    ImageKey key(image, requiredProperties.fMipmapped);
    if (auto cached = fImpl->find(key)) return cached;

    sk_sp<SkImage> textureImage = SkImages::TextureFromImage(recorder, image, requiredProperties);
    if (!textureImage) return nullptr;

    fImpl->insert(key, textureImage);
    return textureImage;
}
