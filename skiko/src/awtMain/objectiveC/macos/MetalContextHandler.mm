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

/// Shared scaffolding for the two present paths. Retrieves the drawable acquired in
/// makeMetalRenderTarget, builds its "Present" command buffer wired to release one inflight slot on
/// completion — every acquired drawable (which waited on inflightSemaphore) must be balanced by exactly
/// one such committed command buffer, or the semaphore wedges and the drawable pool starves — then hands
/// the drawable and command buffer to `present`, which commits and presents per its own policy.
static void finishFrame(jlong devicePtr, void (^present)(MetalDevice *device, id<CAMetalDrawable> drawable, id<MTLCommandBuffer> commandBuffer)) {
    @autoreleasepool {
        MetalDevice *device = (__bridge MetalDevice *) (void *) devicePtr;

        id<CAMetalDrawable> currentDrawable = device.drawableHandle;
        if (!currentDrawable) {
            return;
        }

        id<MTLCommandBuffer> commandBuffer = [device.queue commandBuffer];
        commandBuffer.label = @"Present";
        [commandBuffer addCompletedHandler:^(id<MTLCommandBuffer> buffer) {
            /// commands have completed, allow next waiting (if any) to start encoding new work to gpu
            dispatch_semaphore_signal(device.inflightSemaphore);
        }];

        present(device, currentDrawable, commandBuffer);
        device.drawableHandle = nil;
    }
}

/// Presents the current drawable asynchronously — skiko's default, used for every frame outside a live
/// resize. Called off the AppKit main thread (from the background frame loop) so present work doesn't
/// destabilize FPS on the main thread.
JNIEXPORT void JNICALL Java_org_jetbrains_skiko_context_MetalContextHandler_finishFrame(
    JNIEnv *env, jobject contextHandler, jlong devicePtr)
{
    finishFrame(devicePtr, ^(MetalDevice *device, id<CAMetalDrawable> currentDrawable, id<MTLCommandBuffer> commandBuffer) {
        [commandBuffer addScheduledHandler:^(id<MTLCommandBuffer> buffer) {
            /// Present off the main thread. Outside a resize presentsWithTransaction is always NO, so
            /// this is an immediate, non-deferred present.
            ///
            /// During a live resize the main thread is the sole presenter (setBounds +
            /// drawFrameWhileLiveResizing, via finishFrameInLiveResize below). A frame reaching this
            /// async path while a resize is in progress is a stale straggler that passed the frame-loop
            /// gate just before the resize began; drop it rather than let its deferred present race the
            /// main-thread transactional present under the layer-wide presentsWithTransaction flag.
            /// inLiveResize is a plain atomic ivar (not a CoreAnimation property), so reading it on the
            /// scheduler thread takes no CA lock and can't block.
            if (device.inLiveResize) {
                return;
            }

            /// The layer can be resized (drawableSize changed) between this command buffer being
            /// committed and this deferred handler firing — e.g. via the legacy async resize path used
            /// when synchronous live resize is off or for embedded Swing layers. Skip presenting a
            /// drawable that no longer matches the layer size rather than flashing a wrong-sized frame.
            BOOL sizeMatches = (currentDrawable.texture.width == (NSUInteger) device.layer.drawableSize.width &&
                                currentDrawable.texture.height == (NSUInteger) device.layer.drawableSize.height);
            if (sizeMatches) {
                [currentDrawable present];
            }
        }];

        [commandBuffer commit];
    });
}

/// Presents the current drawable synchronously, joining the ambient window-resize CATransaction.
/// Must be called on the AppKit main thread during a live resize (from renderImmediatelyInAppKitThread),
/// where the ambient CATransaction — committing the window's new size — flushes the present.
/// This is the sole presenter for the duration of the resize.
JNIEXPORT void JNICALL Java_org_jetbrains_skiko_context_MetalContextHandler_finishFrameInLiveResize(
    JNIEnv *env, jobject contextHandler, jlong devicePtr)
{
    finishFrame(devicePtr, ^(MetalDevice *device, id<CAMetalDrawable> currentDrawable, id<MTLCommandBuffer> commandBuffer) {
        /// presentsWithTransaction is YES for the whole resize session — the live-resize start
        /// observer (MetalRedrawer.mm) sets it, the end observer clears it. Holding it layer-wide is
        /// safe because during a resize the main thread is the sole presenter; the only frames that
        /// could reach the async finishFrame path are stragglers, and they're dropped there rather
        /// than presenting under YES.
        ///
        /// Present synchronously so the drawable swap joins the ambient window resize transaction
        /// (no nested begin/commit — that would split it back out). commit + waitUntilScheduled
        /// guarantees the drawing command buffer (submitted by Skia earlier, ahead of this one in
        /// the queue) is scheduled first.
        ///
        /// No drawable-vs-layer size guard is needed here (unlike the async finishFrame path): drawableSize
        /// was set at the start of renderImmediatelyInAppKitThread, then we acquired the drawable, rendered,
        /// committed and present it all synchronously on this thread inside one transaction, so the layer
        /// size cannot change underneath.
        [commandBuffer commit];
        [commandBuffer waitUntilScheduled];
        [currentDrawable present];
    });
}

} // extern C
#endif
