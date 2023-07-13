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

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_swing_MetalSwingRedrawer_makeMetalContext(
        JNIEnv *env, jobject contextHandler, jlong adapterPtr, jlong commandQueuePtr) {
    @autoreleasepool {
        id <MTLDevice> adapter = (__bridge id <MTLDevice>) (void *) adapterPtr;
        GrMtlBackendContext backendContext = {};
        backendContext.fDevice.retain((__bridge GrMTLHandle) adapter);
        id <MTLCommandQueue> commandQueue = (__bridge id <MTLCommandQueue>) (void *) commandQueuePtr;
        backendContext.fQueue.retain((__bridge GrMTLHandle) commandQueue);
        return (jlong) GrDirectContext::MakeMetal(backendContext).release();
    }
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_swing_MetalSwingRedrawer_createCommandQueue(
        JNIEnv *env, jobject contextHandler, jlong adapterPtr) {
    @autoreleasepool {
        id <MTLDevice> adapter = (__bridge id <MTLDevice>) (void *) adapterPtr;
        id <MTLCommandQueue> fQueue = [adapter newCommandQueue];
        return (jlong) (__bridge_retained void *) fQueue;
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
        renderTarget = new GrBackendRenderTarget(texture.width, texture.height, 0, info);
        return (jlong) renderTarget;
    }
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_swing_MetalSwingRedrawer_readPixelsFromTexture(
        JNIEnv *env, jobject contextHandler, jlong texturePtr, jbyteArray readBytes, jlong commandQueuePtr) {
    @autoreleasepool {
        id <MTLTexture> texture = (__bridge id <MTLTexture>) (void *) texturePtr;
        id <MTLCommandQueue> commandQueue = (__bridge id <MTLCommandQueue>) (void *) commandQueuePtr;
        id <MTLCommandBuffer> commandBuffer = [commandQueue commandBuffer];
        id <MTLBlitCommandEncoder> commandEncoder = [commandBuffer blitCommandEncoder];
        [commandEncoder synchronizeTexture:texture slice:0 level:0];
        [commandEncoder endEncoding];
        [commandBuffer commit];
        [commandBuffer waitUntilCompleted];


        jbyte *result_bytes = env->GetByteArrayElements(readBytes, NULL);
        [texture getBytes:result_bytes bytesPerRow:(texture.width * 4) fromRegion:MTLRegionMake2D(0, 0, texture.width, texture.height) mipmapLevel:0];
        env->ReleaseByteArrayElements(readBytes, result_bytes, 0);
    }
}

} // extern C
#endif