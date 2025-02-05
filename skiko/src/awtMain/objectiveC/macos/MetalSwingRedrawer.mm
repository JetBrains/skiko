#ifdef SK_METAL

#import <jawt.h>
#import <jawt_md.h>

#import <QuartzCore/CAMetalLayer.h>
#import <Metal/Metal.h>
#import "ganesh/GrDirectContext.h"
#import "gpu/ganesh/GrBackendSurface.h"
#import "ganesh/mtl/GrMtlBackendContext.h"
#import "ganesh/mtl/GrMtlDirectContext.h"
#import "ganesh/mtl/GrMtlBackendSurface.h"
#import "ganesh/mtl/GrMtlTypes.h"

#import "MetalDevice.h"

extern "C"
{

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_swing_MetalSwingRedrawer_makeMetalContext(
        JNIEnv *env, jobject contextHandler, jlong adapterPtr) {
    @autoreleasepool {
        id <MTLDevice> adapter = (__bridge id <MTLDevice>) (void *) adapterPtr;
        GrMtlBackendContext backendContext = {};
        backendContext.fDevice.retain((__bridge GrMTLHandle) adapter);
        id <MTLCommandQueue> fQueue = [adapter newCommandQueue];
        backendContext.fQueue.retain((__bridge GrMTLHandle) fQueue);
        return (jlong) GrDirectContexts::MakeMetal(backendContext).release();
    }
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_swing_MetalSwingRedrawer_makeMetalTexture(
        JNIEnv *env, jobject contextHandler, jlong adapterPtr, jlong oldTexturePtr, jint width, jint height
) {
    @autoreleasepool {
        id <MTLTexture> oldTexture = (__bridge_transfer id <MTLTexture>) (void *) oldTexturePtr;
        id <MTLTexture> metalTexture;
        if (oldTexture == nil || oldTexture.width != width || oldTexture.height != height) {
            id <MTLDevice> adapter = (__bridge id <MTLDevice>) (void *) adapterPtr;
            MTLTextureDescriptor *textureDescriptor = [MTLTextureDescriptor texture2DDescriptorWithPixelFormat:MTLPixelFormatBGRA8Unorm width:width height:height mipmapped:NO];
            metalTexture = [adapter newTextureWithDescriptor:textureDescriptor];
        } else {
            metalTexture = oldTexture;
        }

        return (jlong) (__bridge_retained void *) metalTexture;
    }
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_swing_MetalSwingRedrawer_disposeMetalTexture(JNIEnv *env, jobject contextHandler, jlong texturePtr) {
    @autoreleasepool {
        id <MTLTexture> oldTexture = (__bridge_transfer id <MTLTexture>) (void *) texturePtr;
    }
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_swing_MetalSwingRedrawer_makeMetalRenderTargetOffScreen(
        JNIEnv *env, jobject contextHandler, jlong texturePtr) {
    @autoreleasepool {
        id <MTLTexture> texture = (__bridge id <MTLTexture>) (void *) texturePtr;
        GrMtlTextureInfo info;
        info.fTexture.retain((__bridge GrMTLHandle) texture);
        GrBackendRenderTarget *renderTarget = NULL;
        GrBackendRenderTarget obj = GrBackendRenderTargets::MakeMtl(texture.width, texture.height, info);
        renderTarget = new GrBackendRenderTarget(obj);
        return (jlong) renderTarget;
    }
}

} // extern C
#endif
