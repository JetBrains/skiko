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

            /// The transactional present is only valid on the AppKit main thread: it relies on the
            /// ambient CATransaction (from AWTMetalLayer.setBounds, committing the window's new size) to
            /// flush the present. Animation-driven frames during a live resize reach finishFrame on a
            /// background render thread with no such transaction, so they must take the async branch
            /// below — gating only on inLiveResize would deadlock them (their present would never commit).
            if (device.inLiveResize && NSThread.isMainThread) {
                /// Scope presentsWithTransaction to just this main-thread frame rather than holding it
                /// for the whole resize: enabling it layer-wide would wedge the async background presents
                /// (their [drawable present] would defer forever on a transaction that never commits).
                /// This runs inside the drawLock critical section, which serializes it against background
                /// frames, so they never observe presentsWithTransaction = YES.
                device.layer.presentsWithTransaction = YES;

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

                /// Restore before releasing drawLock so subsequent background frames present async. The
                /// present issued above stays transactional — its binding is captured at the
                /// [drawable present] call, so it still flushes with the window transaction.
                device.layer.presentsWithTransaction = NO;
            } else {
                [commandBuffer addScheduledHandler:^(id<MTLCommandBuffer> buffer) {
                    /// Normal (non-resize) frames present here, off the main thread — skiko's default, so
                    /// present work doesn't destabilize FPS on the main thread. Outside a resize
                    /// presentsWithTransaction is always NO, so this is an immediate, non-deferred present.
                    ///
                    /// During a live resize the main thread is the sole presenter (setBounds +
                    /// drawResizeFrame, transactional branch above). A frame reaching this branch then is a
                    /// stale straggler that passed the frame-loop gate just before the resize began; drop
                    /// it rather than let its deferred present race the main-thread transactional present
                    /// under the layer-wide presentsWithTransaction flag — that was the original deadlock.
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
