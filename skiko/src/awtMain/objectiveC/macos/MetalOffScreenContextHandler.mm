#ifdef SK_METAL

#import <jawt.h>
#import <jawt_md.h>

#import <QuartzCore/CAMetalLayer.h>
#import <Metal/Metal.h>
#import <GrDirectContext.h>
#import <mtl/GrMtlBackendContext.h>
#import <mtl/GrMtlTypes.h>

#import "MetalDevice.h"

extern "C"
{

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_context_MetalOffScreenContextHandler_makeMetalContext(
        JNIEnv *env, jobject contextHandler, jlong adapterPtr) {
    @autoreleasepool {
        id <MTLDevice> adapter = (__bridge id <MTLDevice>) (void *) adapterPtr;
        GrMtlBackendContext backendContext = {};
        backendContext.fDevice.retain((__bridge GrMTLHandle) adapter);
        id <MTLCommandQueue> fQueue = [adapter newCommandQueue];
        backendContext.fQueue.retain((__bridge GrMTLHandle) fQueue);
        return (jlong) GrDirectContext::MakeMetal(backendContext).release();
    }
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_context_MetalOffScreenContextHandler_makeMetalRenderTargetOffScreen(
        JNIEnv *env, jobject contextHandler, jlong adapterPtr, jint width, jint height) {
    @autoreleasepool {
        id <MTLDevice> adapter = (__bridge id <MTLDevice>) (void *) adapterPtr;
        MTLTextureDescriptor *textureDescriptor = [MTLTextureDescriptor texture2DDescriptorWithPixelFormat:MTLPixelFormatBGRA8Unorm width:width height:height mipmapped:NO];
        // TODO: use double buffer
        id <MTLTexture> metalTexture = [adapter newTextureWithDescriptor:textureDescriptor];
        GrMtlTextureInfo info;
        info.fTexture.retain((__bridge GrMTLHandle) metalTexture);
        GrBackendRenderTarget *renderTarget = NULL;
        renderTarget = new GrBackendRenderTarget(width, height, 0, info);
        return (jlong) renderTarget;
    }
}

} // extern C
#endif