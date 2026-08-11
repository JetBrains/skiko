#ifdef SK_METAL

#import <jawt.h>
#import <jawt_md.h>

#import <QuartzCore/CAMetalLayer.h>
#import <Metal/Metal.h>

extern "C"
{

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_swing_MetalSwingRenderer_getCommandQueue(
        JNIEnv *env, jobject contextHandler, jlong devicePtr) {
    @autoreleasepool {
        id <MTLDevice> device = (__bridge id <MTLDevice>) (void *) devicePtr;
        id <MTLCommandQueue> queue = [device newCommandQueue];
        return (jlong) (__bridge_retained void *) queue;
    }
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_swing_MetalSwingRenderer_disposeCommandQueue(
        JNIEnv *env, jobject contextHandler, jlong queuePtr) {
    @autoreleasepool {
        id <MTLCommandQueue> queue = (__bridge_transfer id <MTLCommandQueue>) (void *) queuePtr;
    }
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_swing_MetalSwingRenderer_makeMetalTexture(
        JNIEnv *env, jobject contextHandler, jlong adapterPtr, jlong oldTexturePtr, jint width, jint height
) {
    @autoreleasepool {
        id <MTLTexture> oldTexture = (__bridge_transfer id <MTLTexture>) (void *) oldTexturePtr;
        id <MTLTexture> metalTexture;
        if (oldTexture == nil || oldTexture.width != width || oldTexture.height != height) {
            id <MTLDevice> adapter = (__bridge id <MTLDevice>) (void *) adapterPtr;
            MTLTextureDescriptor *textureDescriptor = [MTLTextureDescriptor texture2DDescriptorWithPixelFormat:MTLPixelFormatBGRA8Unorm width:width height:height mipmapped:NO];
            // Skia renders into this texture as a backend render target, then it
            // is sampled when blitted back to Graphics2D via the shared texture.
            // The default usage of a texture2DDescriptor is MTLTextureUsageShaderRead
            // only, which trips GrMtlGpu::onWrapBackendRenderTarget's render-target
            // usage assert on a debug Skia.
            textureDescriptor.usage = MTLTextureUsageRenderTarget | MTLTextureUsageShaderRead;
            metalTexture = [adapter newTextureWithDescriptor:textureDescriptor];
        } else {
            metalTexture = oldTexture;
        }

        return (jlong) (__bridge_retained void *) metalTexture;
    }
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_swing_MetalSwingRenderer_disposeMetalTexture(JNIEnv *env, jobject contextHandler, jlong texturePtr) {
    @autoreleasepool {
        id <MTLTexture> oldTexture = (__bridge_transfer id <MTLTexture>) (void *) texturePtr;
    }
}

} // extern C
#endif
