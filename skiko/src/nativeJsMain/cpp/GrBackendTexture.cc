#include <iostream>
#include "SkData.h"
#include "GrBackendSurface.h"
#include "SkBitmap.h"
#include "common.h"

#ifdef SK_METAL
SKIKO_EXPORT KNativePointer org_jetbrains_skia_GrBackendTexture__1nCreateFromMetalTexture
  (long mtlTexturePtr, int width, int height) {//todo get width and height from mtlTexturePtr
    GrMtlTextureInfo mtlInfo;
//    MTLTexture* mtlTexture = reinterpret_cast<MTLTexture*>(mtlTexturePtr);
    GrMTLHandle mtlTexture = reinterpret_cast<GrMTLHandle>(mtlTexturePtr);
    mtlInfo.fTexture.retain(mtlTexture);
    GrBackendTexture* backendTexture = new GrBackendTexture(
            width,
            height,
            GrMipmapped::kNo,
            mtlInfo,
            "Metal to Skia texture"
        );
    return reinterpret_cast<KNativePointer>(backendTexture);
}
#endif // SK_METAL
