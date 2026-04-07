#include <jni.h>
#include "../../interop.hh"
#include "include/core/SkImage.h"
#include "include/core/SkTiledImageUtils.h"
#include "src/core/SkLRUCache.h"

#include "include/gpu/graphite/Context.h"
#include "include/gpu/graphite/GraphiteTypes.h"
#include "include/gpu/graphite/ContextOptions.h"
#include "include/gpu/graphite/Recorder.h"
#include "include/gpu/graphite/ImageProvider.h"
#include "include/gpu/graphite/Image.h"

#ifdef SK_METAL
#include "gpu/graphite/mtl/MtlGraphiteTypes_cpp.h"
#include "include/gpu/graphite/mtl/MtlBackendContext.h"
#endif

// https://github.com/google/skia/blob/c16d0e9f30b1a1613401c0db3c93a3c2aa37c8ba/tools/graphite/GraphiteToolUtils.cpp#L26
// or
// https://github.com/Shopify/react-native-skia/blob/dbabb7dfd840ae871cf24282d9617d69abf1e17c/packages/skia/cpp/rnskia/RNImageProvider.h#L7
class ImageProvider : public skgpu::graphite::ImageProvider {
public:
    ImageProvider() : fCache(kDefaultNumCachedImages) {}
    ~ImageProvider() override {}

    static sk_sp<ImageProvider> Make() { return sk_ref_sp(new ImageProvider); }

    sk_sp<SkImage>
    findOrCreate(skgpu::graphite::Recorder *recorder, const SkImage *image,
            SkImage::RequiredProperties requiredProps) override {
        if (!requiredProps.fMipmapped) {
            // If no mipmaps are required, check to see if we have a mipmapped version
            // anyway - since it can be used in that case.
            // TODO: we could get fancy and, if ever a mipmapped key eclipsed a
            // non-mipmapped key, we could remove the hidden non-mipmapped key/image
            // from the cache.
            ImageKey mipMappedKey(image, /* mipmapped= */ true);
            auto result = fCache.find(mipMappedKey);
            if (result) {
                return *result;
            }
        }

        ImageKey key(image, requiredProps.fMipmapped);

        auto result = fCache.find(key);
        if (result) {
            return *result;
        }

        sk_sp<SkImage> newImage =
                SkImages::TextureFromImage(recorder, image, requiredProps);
        if (!newImage) {
            return nullptr;
        }

        result = fCache.insert(key, std::move(newImage));
        SkASSERT(result);

        return *result;
    }

private:
    static constexpr int kDefaultNumCachedImages = 256;

    class ImageKey {
    public:
        ImageKey(const SkImage *image, bool mipmapped) {
            uint32_t flags = mipmapped ? 0x1 : 0x0;
            SkTiledImageUtils::GetImageKeyValues(image, &fValues[1]);
            fValues[kNumValues - 1] = flags;
            fValues[0] =
                    SkChecksum::Hash32(&fValues[1], (kNumValues - 1) * sizeof(uint32_t));
        }

        uint32_t hash() const { return fValues[0]; }

        bool operator==(const ImageKey &other) const {
            for (int i = 0; i < kNumValues; ++i) {
                if (fValues[i] != other.fValues[i]) {
                    return false;
                }
            }

            return true;
        }
        bool operator!=(const ImageKey &other) const { return !(*this == other); }

    private:
        static const int kNumValues = SkTiledImageUtils::kNumImageKeyValues + 2;

        uint32_t fValues[kNumValues];
    };

    struct ImageHash {
        size_t operator()(const ImageKey &key) const { return key.hash(); }
    };

    SkLRUCache<ImageKey, sk_sp<SkImage>, ImageHash> fCache;
};

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_gpu_graphite_GraphiteContextKt__1nMakeRecorder
        (JNIEnv* env, jclass jclass, jlong contextPtr) {
    skgpu::graphite::Context *context = reinterpret_cast<skgpu::graphite::Context*>(contextPtr);
    skgpu::graphite::RecorderOptions options = {};
    options.fImageProvider = ImageProvider::Make();

    std::unique_ptr<skgpu::graphite::Recorder> graphiteRecorder = context->makeRecorder(options);
    return reinterpret_cast<jlong>(graphiteRecorder.release());
}


#ifdef SK_METAL
extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_gpu_graphite_GraphiteContextKt__1nGraphiteMakeMetal
        (JNIEnv* env, jclass jclass, jlong devicePtr, jlong queuePtr) {
    skgpu::graphite::MtlBackendContext backendContext = {};
    backendContext.fDevice.retain(reinterpret_cast<CFTypeRef>(devicePtr));
    backendContext.fQueue.retain(reinterpret_cast<CFTypeRef>(queuePtr));

    skgpu::graphite::ContextOptions options;
    options.fRequireOrderedRecordings = true;

    return reinterpret_cast<jlong>(skgpu::graphite::ContextFactory::MakeMetal(backendContext, options).release());
}
#endif

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_gpu_graphite_GraphiteContextKt__1nInsertRecording
        (JNIEnv* env, jclass jclass, jlong contextPtr, jlong recordingPtr) {
    skgpu::graphite::Context *context = reinterpret_cast<skgpu::graphite::Context*>(contextPtr);
    skgpu::graphite::Recording *recording = reinterpret_cast<skgpu::graphite::Recording*>(recordingPtr);
    skgpu::graphite::InsertRecordingInfo info;
    info.fRecording = recording;

    context->insertRecording(info);
}

skgpu::graphite::SyncToCpu syncToCpuFromBool(bool syncCpu) {
    if (syncCpu) return skgpu::graphite::SyncToCpu::kYes;
    return skgpu::graphite::SyncToCpu::kNo;
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_gpu_graphite_GraphiteContextKt__1nSubmit
        (JNIEnv* env, jclass jclass, jlong contextPtr, jboolean syncCpu) {
    skgpu::graphite::Context *context = reinterpret_cast<skgpu::graphite::Context*>(contextPtr);

    context->submit(syncToCpuFromBool(syncCpu));
}