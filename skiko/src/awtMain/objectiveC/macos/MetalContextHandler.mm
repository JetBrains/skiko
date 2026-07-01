#ifdef SK_METAL

#import <jawt.h>
#import <jawt_md.h>

#import <QuartzCore/CAMetalLayer.h>
#import <QuartzCore/CATransaction.h>
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
JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_context_MetalContextHandler_makeMetalContext(
    JNIEnv* env, jobject contextHandler, jlong devicePtr)
{
    @autoreleasepool {
        MetalDevice *device = (__bridge MetalDevice *) (void*) devicePtr;
        GrMtlBackendContext backendContext = {};
        backendContext.fDevice.retain((__bridge GrMTLHandle) device.adapter);
        backendContext.fQueue.retain((__bridge GrMTLHandle) device.queue);
        return (jlong) GrDirectContexts::MakeMetal(backendContext).release();
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
        GrBackendRenderTarget obj = GrBackendRenderTargets::MakeMtl(width, height, info);
        renderTarget = new GrBackendRenderTarget(obj);
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

            if (device.inLiveResize) {
                /// During live resize this runs on the AppKit main thread (driven from
                /// AWTMetalLayer.setBounds) with presentsWithTransaction = YES, inside the ambient
                /// CATransaction that is also committing the window's new size. Present synchronously so
                /// the drawable swap joins that same transaction (no nested begin/commit — that would
                /// split it back out). commit + waitUntilScheduled guarantees the drawing command buffer
                /// (submitted by Skia earlier, ahead of this one in the queue) is scheduled first.
                [commandBuffer commit];
                [commandBuffer waitUntilScheduled];

                /// Only present if the drawable still matches the layer size (it should, since setBounds
                /// set drawableSize and we rendered to it just now, but stay defensive).
                if (currentDrawable.texture.width == (NSUInteger) device.layer.drawableSize.width &&
                    currentDrawable.texture.height == (NSUInteger) device.layer.drawableSize.height) {
                    [currentDrawable present];
                }
            } else {
                [commandBuffer addScheduledHandler:^(id<MTLCommandBuffer> buffer) {
                    /// Avoid presenting a drawable on a layer that has already changed size by the moment
                    /// this was scheduled.
                    if (currentDrawable.texture.width == (NSUInteger) device.layer.drawableSize.width &&
                        currentDrawable.texture.height == (NSUInteger) device.layer.drawableSize.height) {
                        [currentDrawable present];
                    }
                }];

                [commandBuffer commit];
            }
            device.drawableHandle = nil;
        }
    }
}

} // extern C
#endif
