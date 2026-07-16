#ifdef SK_METAL

#import <Cocoa/Cocoa.h>
#import <QuartzCore/QuartzCore.h>
#import <Metal/Metal.h>
#import <QuartzCore/CAMetalLayer.h>

#import "MetalDevice.h"

#include "common/interop.hh"

static jmethodID getOnLiveResizeStartedMethodID(JNIEnv *env, jobject resizer) {
    static jmethodID onLiveResizeStarted = NULL;
    if (onLiveResizeStarted == NULL) {
        jclass cls = env->GetObjectClass(resizer);
        onLiveResizeStarted = env->GetMethodID(cls, "onLiveResizeStartedInAppkitThread", "()V");
    }
    return onLiveResizeStarted;
}

static jmethodID getOnLiveResizeEndedMethodID(JNIEnv *env, jobject resizer) {
    static jmethodID onLiveResizeEnded = NULL;
    if (onLiveResizeEnded == NULL) {
        jclass cls = env->GetObjectClass(resizer);
        onLiveResizeEnded = env->GetMethodID(cls, "onLiveResizeEndedInAppkitThread", "()V");
    }
    return onLiveResizeEnded;
}

static jmethodID getRenderFrameMethodID(JNIEnv *env, jobject resizer) {
    static jmethodID renderFrame = NULL;
    if (renderFrame == NULL) {
        jclass cls = env->GetObjectClass(resizer);
        renderFrame = env->GetMethodID(cls, "renderFrameInAppkitThread", "()V");
    }
    return renderFrame;
}

@interface MetalLiveResizerContext : NSObject

@property jobject javaRef;
@property (strong) id<NSObject> liveResizeStartObserver;
@property (strong) id<NSObject> liveResizeEndObserver;
@end

@implementation MetalLiveResizerContext
- (id)init {
    self = [super init];
    if (self) {
        self.javaRef = NULL;
        self.liveResizeStartObserver = nil;
        self.liveResizeEndObserver = nil;
    }
    return self;
}
@end

extern "C"
{

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_MetalLiveResizer_create(
    JNIEnv *env, jobject obj, jlong windowPtr)
{
    @autoreleasepool {
        NSWindow *window = (__bridge NSWindow *) (void *) windowPtr;

        MetalLiveResizerContext *ctx = [MetalLiveResizerContext new];
        ctx.javaRef = env->NewGlobalRef(obj);

        __weak MetalLiveResizerContext *weakCtx = ctx;

        ctx.liveResizeStartObserver =
            [[NSNotificationCenter defaultCenter]
                addObserverForName:NSWindowWillStartLiveResizeNotification
                            object:window
                             queue:nil
                        usingBlock:^(NSNotification * _Nonnull note) {
                MetalLiveResizerContext *strongCtx = weakCtx;
                if (!strongCtx) return;
                JNIEnv *jniEnv = resolveJNIEnvForCurrentThread();
                jmethodID onLiveResizeStarted = getOnLiveResizeStartedMethodID(jniEnv, strongCtx.javaRef);
                jniEnv->CallVoidMethod(strongCtx.javaRef, onLiveResizeStarted);
            }];

        ctx.liveResizeEndObserver =
            [[NSNotificationCenter defaultCenter]
                addObserverForName:NSWindowDidEndLiveResizeNotification
                            object:window
                             queue:nil
                        usingBlock:^(NSNotification * _Nonnull note) {
                MetalLiveResizerContext *strongCtx = weakCtx;
                if (!strongCtx) return;
                JNIEnv *jniEnv = resolveJNIEnvForCurrentThread();
                jmethodID onLiveResizeEnded = getOnLiveResizeEndedMethodID(jniEnv, strongCtx.javaRef);
                jniEnv->CallVoidMethod(strongCtx.javaRef, onLiveResizeEnded);
            }];

        return (jlong) (__bridge_retained void *) ctx;
    }
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalLiveResizer_scheduleRenderFrameOnAppKitThread(
    JNIEnv *env, jobject obj, jlong ctxPtr)
{
    MetalLiveResizerContext *ctx = (__bridge MetalLiveResizerContext *) (void *) ctxPtr;
    jobject javaRef = ctx.javaRef;
    dispatch_async(dispatch_get_main_queue(), ^{
        JNIEnv *jniEnv = resolveJNIEnvForCurrentThread();
        jmethodID renderFrame = getRenderFrameMethodID(jniEnv, javaRef);
        jniEnv->CallVoidMethod(javaRef, renderFrame);
    });
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalLiveResizer_dispose(
    JNIEnv *env, jobject obj, jlong ctxPtr)
{
    @autoreleasepool {
        MetalLiveResizerContext *ctx = (__bridge_transfer MetalLiveResizerContext *) (void *) ctxPtr;
        if (ctx.liveResizeStartObserver) {
            [[NSNotificationCenter defaultCenter] removeObserver:ctx.liveResizeStartObserver];
        }
        if (ctx.liveResizeEndObserver) {
            [[NSNotificationCenter defaultCenter] removeObserver:ctx.liveResizeEndObserver];
        }
        env->DeleteGlobalRef(ctx.javaRef);
    }
}

} // extern C
#endif
