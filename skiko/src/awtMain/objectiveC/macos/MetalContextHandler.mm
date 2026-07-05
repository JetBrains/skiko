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

#import <stdatomic.h>

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

            /// Present transactionally only on the AppKit main thread, where the ambient CATransaction —
            /// from AWTMetalLayer.setBounds, committing the window's new size — flushes the present.
            /// During a resize every real frame arrives here on the main thread (setBounds and
            /// drawFrameWhileLiveResizing both draw + finishFrame there). A frame that reaches finishFrame off the
            /// main thread during a resize is a background straggler from just before it began; it falls
            /// to the async branch below and is dropped.
            if (device.inLiveResize && NSThread.isMainThread) {
                /// presentsWithTransaction is already YES for the whole resize session — the live-resize
                /// start observer (MetalRedrawer.mm) sets it, the end observer clears it. Holding it
                /// layer-wide is safe because during a resize the main thread is the sole presenter; the
                /// only frames that could reach the async branch below are stragglers, and they're dropped
                /// there rather than presenting under YES.
                ///
                /// Present synchronously so the drawable swap joins the ambient window resize transaction
                /// (no nested begin/commit — that would split it back out). commit + waitUntilScheduled
                /// guarantees the drawing command buffer (submitted by Skia earlier, ahead of this one in
                /// the queue) is scheduled first.
                [commandBuffer commit];
                [commandBuffer waitUntilScheduled];

                /// Only present if the drawable still matches the layer size (it should, since setBounds
                /// set drawableSize and we rendered to it just now, but stay defensive).
                BOOL sizeMatches = (currentDrawable.texture.width == (NSUInteger) device.layer.drawableSize.width &&
                                    currentDrawable.texture.height == (NSUInteger) device.layer.drawableSize.height);
                if (sizeMatches) {
                    [currentDrawable present];
                }
            } else {
                [commandBuffer addScheduledHandler:^(id<MTLCommandBuffer> buffer) {
                    /// Normal (non-resize) frames present here, off the main thread — skiko's default, so
                    /// present work doesn't destabilize FPS on the main thread. Outside a resize
                    /// presentsWithTransaction is always NO, so this is an immediate, non-deferred present.
                    ///
                    /// During a live resize the main thread is the sole presenter (setBounds +
                    /// drawFrameWhileLiveResizing, transactional branch above). A frame reaching this branch then is a
                    /// stale straggler that passed the frame-loop gate just before the resize began; drop
                    /// it rather than let its deferred present race the main-thread transactional present
                    /// under the layer-wide presentsWithTransaction flag.
                    /// inLiveResize is a plain atomic ivar (not a CoreAnimation property), so reading it on
                    /// the scheduler thread takes no CA lock and can't block.
                    if (device.inLiveResize) {
                        return;
                    }
                    [currentDrawable present];
                }];

                [commandBuffer commit];
            }
            device.drawableHandle = nil;
        }
    }
}

} // extern C
#endif
