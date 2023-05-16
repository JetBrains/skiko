#ifdef SK_METAL

#import <jawt.h>
#import <jawt_md.h>

#import <GrDirectContext.h>
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
        static double prevTime = CACurrentMediaTime();

        double newTime = CACurrentMediaTime();
        NSLog(@"Frame time: %f", newTime - prevTime);

        prevTime = newTime;

        MetalDevice *device = (__bridge MetalDevice *) (void *) devicePtr;
        [device recreateDisplayLinkIfNeeded];
        [device waitUntilVsync];
        [device waitForQueueSlot];

        GrBackendRenderTarget* renderTarget = NULL;

        id<CAMetalDrawable> currentDrawable = [device.layer nextDrawable];
        if (!currentDrawable) {
            [device freeQueueSlot];
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
            [commandBuffer presentDrawable:currentDrawable];
            [commandBuffer addCompletedHandler:^(id<MTLCommandBuffer> buffer) {
                [device freeQueueSlot];
            }];
            [commandBuffer commit];
            device.drawableHandle = nil;
        }
    }
}

} // extern C
#endif