#ifdef SK_METAL

#import <jawt.h>
#import <jawt_md.h>

#import <QuartzCore/CAMetalLayer.h>
#import <Metal/Metal.h>
#import <GrDirectContext.h>
#import <gpu/GrBackendSurface.h>
#import <mtl/GrMtlBackendContext.h>
#import <mtl/GrMtlTypes.h>

#import "MetalDevice.h"

extern "C"
{
JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_context_MetalContextHandler_makeMetalContext(
    JNIEnv* env, jobject contextHandler, jlong devicePtr)
{
    @autoreleasepool {
        MetalDevice *device = (__bridge MetalDevice *) (void*) devicePtr;
        GrMtlBackendContext backendContext = {};
        backendContext.fDevice.retain((__bridge GrMTLHandle) device.adapter);
        backendContext.fQueue.retain((__bridge GrMTLHandle) device.queue);
        return (jlong) GrDirectContext::MakeMetal(backendContext).release();
    }
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_context_MetalContextHandler_makeMetalRenderTarget(
    JNIEnv* env, jobject contextHandler, jlong devicePtr, jint width, jint height)
{
    @autoreleasepool {
        MetalDevice *device = (__bridge MetalDevice *) (void *) devicePtr;
        GrBackendRenderTarget* renderTarget = NULL;

        /// If we have more than `maximumDrawableCount` command buffers inflight, wait until one of them finishes work.
        dispatch_semaphore_wait(device.inflightSemaphore, DISPATCH_TIME_FOREVER);

        id<CAMetalDrawable> currentDrawable = [device.layer nextDrawable];
        if (!currentDrawable) {
            /// Signal semaphore immediately, no command buffer will be commited
            dispatch_semaphore_signal(device.inflightSemaphore);

            return NULL;
        }
        device.drawableHandle = currentDrawable;
        GrMtlTextureInfo info;
        info.fTexture.retain((__bridge GrMTLHandle) currentDrawable.texture);
        renderTarget = new GrBackendRenderTarget(width, height, 0, info);
        return (jlong) renderTarget;
    }
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_context_MetalContextHandler_finishFrame(
    JNIEnv *env, jobject contextHandler, jlong devicePtr)
{
    @autoreleasepool {
        MetalDevice *device = (__bridge MetalDevice *) (void *) devicePtr;

        id<CAMetalDrawable> currentDrawable = device.drawableHandle;

        if (currentDrawable) {
            id<MTLCommandBuffer> commandBuffer = [device.queue commandBuffer];
            commandBuffer.label = @"Present";

            [commandBuffer addCompletedHandler:^(id<MTLCommandBuffer> buffer) {
                /// commands have completed, allow next waiting (if any) to start encoding new work to gpu
                dispatch_semaphore_signal(device.inflightSemaphore);
            }];

            [commandBuffer addScheduledHandler:^(id<MTLCommandBuffer> buffer) {
                int drawableWidth = currentDrawable.texture.width;
                int drawableHeight = currentDrawable.texture.height;

                int layerWidth = device.layer.drawableSize.width;
                int layerHeight = device.layer.drawableSize.height;

                /// Avoid presenting drawable on layer that has already changed size by the moment it was scheduled
                if (drawableWidth == layerWidth && drawableHeight == layerHeight) {
                    [currentDrawable present];
                }
            }];

            [commandBuffer commit];
            device.drawableHandle = nil;
        }
    }
}

} // extern C
#endif