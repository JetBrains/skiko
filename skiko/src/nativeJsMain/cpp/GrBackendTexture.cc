#include <iostream>
#include "SkData.h"
#include "GrBackendSurface.h"
#include "SkBitmap.h"
#include "common.h"

#ifdef SK_METAL
SKIKO_EXPORT KNativePointer org_jetbrains_skia_GrBackendTexture__1nCreateFromMetalTexture
  (long mtlTexturePtr) {
    GrMtlTextureInfo mtlInfo;
    GrMTLHandle mtlTexture = reinterpret_cast<GrMTLHandle>((mtlTexturePtr));
    mtlInfo.fTexture.retain(mtlTexture);
    GrBackendTexture* backendTexture = new GrBackendTexture(100, 100, GrMipmapped::kNo, mtlInfo, "Metal to Skia texture");
    return reinterpret_cast<KNativePointer>(backendTexture);

//    GrMtlBackendContext backendContext = {};
//    GrMTLHandle device = reinterpret_cast<GrMTLHandle>((devicePtr));
//    GrMTLHandle queue = reinterpret_cast<GrMTLHandle>((queuePtr));
//    backendContext.fDevice.retain(device);
//    backendContext.fQueue.retain(queue);
//    sk_sp<GrDirectContext> instance = GrDirectContext::MakeMetal(backendContext);
//    return reinterpret_cast<KNativePointer>(instance.release());
}
#endif // SK_METAL
