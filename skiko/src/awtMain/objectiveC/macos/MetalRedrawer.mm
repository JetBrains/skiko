#ifdef SK_METAL

#import <jawt.h>
#import <jawt_md.h>

#import <Cocoa/Cocoa.h>
#import <QuartzCore/QuartzCore.h>
#import <Metal/Metal.h>
#import <QuartzCore/CAMetalLayer.h>

#import "ganesh/GrBackendSurface.h"
#import "ganesh/GrDirectContext.h"
#import "ganesh/mtl/GrMtlBackendContext.h"
#import "ganesh/mtl/GrMtlDirectContext.h"
#import "ganesh/mtl/GrMtlTypes.h"

#import "MetalDevice.h"

#include <assert.h>

#include "common/interop.hh"

// Defined later in this file; forward-declared so AWTMetalLayer (below) can use them.
static JNIEnv *resolveJNIEnvForCurrentThread();
static jmethodID getDrawInLiveResizeMethodID(JNIEnv *env, jobject redrawer);

@implementation AWTMetalLayer

- (id)init
{
    self = [super init];

    assert(self != NULL);

    [self removeAllAnimations];
    [self setAutoresizingMask: (kCALayerWidthSizable|kCALayerHeightSizable)];
    [self setNeedsDisplayOnBoundsChange: YES];

    return self;
}

/// During a live resize this fires (on the AppKit main thread) when the layer autoresizes to track the
/// window. We update drawableSize and synchronously draw + present, all within the ambient
/// CATransaction that is also committing the window's new size, so content and backing stay in sync.
- (void)setBounds:(CGRect)bounds
{
    [super setBounds:bounds];
    if (!self.liveResizing || self.javaRef == NULL) {
        return;
    }

    CGFloat scale = self.contentsScale;
    int pixelWidth = (int)(bounds.size.width * scale);
    int pixelHeight = (int)(bounds.size.height * scale);
    if (pixelWidth <= 0 || pixelHeight <= 0) {
        return;
    }

    self.drawableSize = CGSizeMake(pixelWidth, pixelHeight);

    JNIEnv *env = resolveJNIEnvForCurrentThread();
    jmethodID drawInLiveResize = getDrawInLiveResizeMethodID(env, self.javaRef);
    if (drawInLiveResize) {
        env->CallVoidMethod(self.javaRef, drawInLiveResize, (jint)pixelWidth, (jint)pixelHeight);
    }
}

@end

@implementation MetalDevice

- (id) init
{
    self = [super init];

    if (self)
    {
        self.layer = nil;
        self.adapter = nil;
        self.queue = nil;
        self.drawableHandle = nil;
    }

    return self;
}

@end

@interface MTLCommandQueueCache : NSObject
@property (strong, nonatomic) NSMapTable<id<MTLDevice>, id<MTLCommandQueue>> *commandQueueMap;
@end

@implementation MTLCommandQueueCache

- (instancetype)init {
    if (self = [super init]) {
        _commandQueueMap = [NSMapTable strongToStrongObjectsMapTable];  // Retains both keys and values
    }
    return self;
}

- (id<MTLCommandQueue>)commandQueueForDevice:(id<MTLDevice>)device {
    id<MTLCommandQueue> commandQueue = [_commandQueueMap objectForKey:device];
    if (!commandQueue) {
        commandQueue = [device newCommandQueue];
        [_commandQueueMap setObject:commandQueue forKey:device];
    }
    return commandQueue;
}

+ (instancetype)sharedCache {
    static MTLCommandQueueCache *cache = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        cache = [MTLCommandQueueCache new];
    });
    return cache;
}

@end

/// Linked from skiko/src/jvmMain/cpp/common/impl/Library.cc
/// clang treats extern symbol declarations as C in Objective-C++(.mm) and doesn't mangle them
extern JavaVM *jvm;

static JNIEnv *resolveJNIEnvForCurrentThread() {
    JNIEnv *env;
    int envStat = jvm->GetEnv((void **)&env, SKIKO_JNI_VERSION);

    if (envStat == JNI_EDETACHED) {
        jvm->AttachCurrentThread((void **) &env, NULL);
    }

    assert(env);

    return env;
}

static jmethodID getOnOcclusionStateChangedMethodID(JNIEnv *env, jobject redrawer) {
    static jmethodID onOcclusionStateChanged = NULL;
    if (onOcclusionStateChanged == NULL) {
        jclass redrawerClass = env->GetObjectClass(redrawer);
        onOcclusionStateChanged = env->GetMethodID(redrawerClass, "onOcclusionStateChanged", "(Z)V");
    }
    return onOcclusionStateChanged;
}

static jmethodID getDrawInLiveResizeMethodID(JNIEnv *env, jobject redrawer) {
    static jmethodID drawInLiveResize = NULL;
    if (drawInLiveResize == NULL) {
        jclass redrawerClass = env->GetObjectClass(redrawer);
        drawInLiveResize = env->GetMethodID(redrawerClass, "drawInLiveResize", "(II)V");
    }
    return drawInLiveResize;
}

static jmethodID getOnLiveResizeChangedMethodID(JNIEnv *env, jobject redrawer) {
    static jmethodID onLiveResizeChanged = NULL;
    if (onLiveResizeChanged == NULL) {
        jclass redrawerClass = env->GetObjectClass(redrawer);
        onLiveResizeChanged = env->GetMethodID(redrawerClass, "onLiveResizeChanged", "(Z)V");
    }
    return onLiveResizeChanged;
}

static jmethodID getDrawResizeFrameMethodID(JNIEnv *env, jobject redrawer) {
    static jmethodID drawResizeFrame = NULL;
    if (drawResizeFrame == NULL) {
        jclass redrawerClass = env->GetObjectClass(redrawer);
        drawResizeFrame = env->GetMethodID(redrawerClass, "drawResizeFrame", "(II)V");
    }
    return drawResizeFrame;
}

extern "C"
{

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_createMetalDevice(
    JNIEnv *env, jobject redrawer, jlong windowPtr, jboolean transparency, jint frameBuffering, jlong adapterPtr, jlong platformInfoPtr, jboolean liveResizeEnabled)
{
    @autoreleasepool {
        id<MTLDevice> adapter = (__bridge id<MTLDevice>) (void *) adapterPtr;

        MetalDevice *device = [MetalDevice new];

        NSObject<JAWT_SurfaceLayers>* dsi_mac = (__bridge NSObject<JAWT_SurfaceLayers> *) (void*) platformInfoPtr;

        CALayer *container = [dsi_mac windowLayer];
        [container removeAllAnimations];
        [container setAutoresizingMask: (kCALayerWidthSizable|kCALayerHeightSizable)];
        [container setNeedsDisplayOnBoundsChange: YES];

        AWTMetalLayer *layer = [AWTMetalLayer new];
        if (frameBuffering == 2 || frameBuffering == 3) {
            layer.maximumDrawableCount = frameBuffering;
        }

        [container addSublayer: layer];
        layer.javaRef = env->NewGlobalRef(redrawer);

        id<MTLCommandQueue> fQueue = [MTLCommandQueueCache.sharedCache commandQueueForDevice:adapter];

        device.container = container;
        device.layer = layer;
        device.adapter = adapter;
        device.queue = fQueue;

        device.layer.device = device.adapter;
        device.layer.pixelFormat = MTLPixelFormatBGRA8Unorm;
        device.layer.contentsGravity = kCAGravityTopLeft;

        CGFloat transparent[] = { 0.0f, 0.0f, 0.0f, 0.0f };
        device.layer.backgroundColor = CGColorCreate(CGColorSpaceCreateDeviceRGB(), transparent);
        device.layer.opaque = NO;
        device.layer.framebufferOnly = NO;

        /// max inflight command buffers count matches swapchain size to avoid overcommitment
        device.inflightSemaphore = dispatch_semaphore_create(device.layer.maximumDrawableCount);

        NSWindow* window = (__bridge NSWindow*) (void *) windowPtr;
        jmethodID onOcclusionStateChanged = getOnOcclusionStateChangedMethodID(env, redrawer);
        device.occlusionObserver =
            [[NSNotificationCenter defaultCenter] addObserverForName:NSWindowDidChangeOcclusionStateNotification
                                                              object:window
                                                               queue:[NSOperationQueue mainQueue]
                                                          usingBlock:^(NSNotification * _Nonnull note) {
                BOOL isOccluded = ([window occlusionState] & NSWindowOcclusionStateVisible) == 0;
                JNIEnv *jniEnv = resolveJNIEnvForCurrentThread();
                jniEnv->CallObjectMethod(layer.javaRef, onOcclusionStateChanged, isOccluded);
            }];

        /// Track interactive live-resize state. These fire on the AppKit main thread. weakDevice avoids
        /// a device -> observer -> block -> device retain cycle. Gated by liveResizeEnabled: when
        /// disabled the observers aren't installed, so inLiveResize/liveResizing stay NO and every path
        /// (setBounds, finishFrame, syncBounds, the frame loop) uses the legacy behavior.
        ///
        /// Note: presentsWithTransaction is deliberately NOT toggled here. It's scoped per-frame to the
        /// main-thread present in MetalContextHandler.finishFrame instead, so animation frames that run
        /// the async present path on a background thread during the resize never observe it as YES —
        /// otherwise their [drawable present] would defer forever on a transaction that never commits,
        /// wedging the command queue.
        if (liveResizeEnabled) {
            __weak MetalDevice *weakDevice = device;
            device.liveResizeStartObserver =
                [[NSNotificationCenter defaultCenter] addObserverForName:NSWindowWillStartLiveResizeNotification
                                                                  object:window
                                                                   queue:nil
                                                              usingBlock:^(NSNotification * _Nonnull note) {
                    MetalDevice *strongDevice = weakDevice;
                    if (!strongDevice) return;
                    strongDevice.inLiveResize = YES;
                    strongDevice.layer.liveResizing = YES;
                    JNIEnv *jniEnv = resolveJNIEnvForCurrentThread();
                    jniEnv->CallVoidMethod(strongDevice.layer.javaRef, getOnLiveResizeChangedMethodID(jniEnv, strongDevice.layer.javaRef), (jboolean)YES);
                }];
            device.liveResizeEndObserver =
                [[NSNotificationCenter defaultCenter] addObserverForName:NSWindowDidEndLiveResizeNotification
                                                                  object:window
                                                                   queue:nil
                                                              usingBlock:^(NSNotification * _Nonnull note) {
                    MetalDevice *strongDevice = weakDevice;
                    if (!strongDevice) return;
                    /// Stop the main-thread live-resize draw (liveResizing) before clearing inLiveResize,
                    /// so setBounds stops driving frames as the resize winds down.
                    strongDevice.layer.liveResizing = NO;
                    strongDevice.inLiveResize = NO;
                    JNIEnv *jniEnv = resolveJNIEnvForCurrentThread();
                    jniEnv->CallVoidMethod(strongDevice.layer.javaRef, getOnLiveResizeChangedMethodID(jniEnv, strongDevice.layer.javaRef), (jboolean)NO);
                }];
        }

        return (jlong) (__bridge_retained void *) device;
    }
}

/// Schedules a frame on the AppKit main thread. Called from Kotlin (MetalRedrawer.needRender) during a live resize,
// when the background frame loop is gated off. The main queue serializes this with setBounds-driven frames, so there
// is only ever one presenter and one drawable in flight — no async present, no drawable-pool contention. The current
// pixel size is read on the main thread right before the callback, so the frame is rendered at the layer's live size.
JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_scheduleFrameOnAppKitThread(
    JNIEnv *env, jobject redrawer, jlong devicePtr)
{
    MetalDevice *device = (__bridge MetalDevice *) (void *) devicePtr;
    /// javaRef is a global ref (created in createMetalDevice), safe to use from the deferred block.
    jobject javaRef = device.layer.javaRef;
    /// Coalesce: only dispatch if no resize frame is already pending. atomic_exchange returns the prior
    /// value, so a `true` return means one is in flight and we skip. Scheduling may come from the EDT or
    /// the main thread, hence the atomic.
    if (atomic_exchange(&device->frameOnAppKitThreadScheduled, true)) {
        return;
    }
    dispatch_async(dispatch_get_main_queue(), ^{
        /// Clear FIRST, unconditionally — before any guard below can bail. This keeps the flag honest
        /// even when the frame is dropped (e.g. the resize already ended), so a later resize can schedule
        /// again; and it lets an onRender -> needRender inside drawResizeFrame re-arm the next frame,
        /// sustaining the animation while the pointer is held still.
        atomic_store(&device->frameOnAppKitThreadScheduled, false);
        if (!device.inLiveResize) {
            return;
        }
        CGSize size = device.layer.drawableSize;
        int pixelWidth = (int) size.width;
        int pixelHeight = (int) size.height;
        if (pixelWidth <= 0 || pixelHeight <= 0) {
            return;
        }
        JNIEnv *jniEnv = resolveJNIEnvForCurrentThread();
        jmethodID drawResizeFrame = getDrawResizeFrameMethodID(jniEnv, javaRef);
        if (drawResizeFrame) {
            jniEnv->CallVoidMethod(javaRef, drawResizeFrame, (jint)pixelWidth, (jint)pixelHeight);
        }
    });
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_resizeLayers(
    JNIEnv *env, jobject redrawer, jlong devicePtr, jint x, jint y, jint width, jint height)
{
    @autoreleasepool {
        MetalDevice *device = (__bridge MetalDevice *) (void *) devicePtr;
        float scale = device.layer.contentsScale;
        CGRect frame = CGRectMake(x, y, width, height);
        CGSize drawableSize = CGSizeMake(width * scale, height * scale);
        [CATransaction begin];
        [CATransaction setValue:(id)kCFBooleanTrue forKey:kCATransactionDisableActions];
        device.layer.frame = frame;
        // to avoid warning in console:
        // CAMetalLayer ignoring invalid setDrawableSize width=0.000000 height=0.000000
        if (width > 0 && height > 0) {
            device.layer.drawableSize = drawableSize;
        }
        [CATransaction commit];
        [CATransaction flush];
    }
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_setLayerVisible(
    JNIEnv *env, jobject redrawer, jlong devicePtr, jboolean isVisible)
{
    @autoreleasepool {
        MetalDevice *device = (__bridge MetalDevice *) (void *) devicePtr;
        BOOL hidden = !isVisible;
        if (!device || !device.layer || device.layer.hidden == hidden) {
            return;
        }
        [CATransaction begin];
        [CATransaction setValue:(id)kCFBooleanTrue forKey:kCATransactionDisableActions];
        device.layer.hidden = hidden;
        [CATransaction commit];
        [CATransaction flush];
    }
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_setContentScale(JNIEnv *env, jobject obj, jlong devicePtr, jfloat contentScale)
{
    @autoreleasepool {
        MetalDevice *device = (__bridge MetalDevice *) (void *) devicePtr;
        if (!device || !device.layer || device.layer.contentsScale == contentScale) {
            return;
        }
        [CATransaction begin];
        [CATransaction setValue:(id)kCFBooleanTrue forKey:kCATransactionDisableActions];
        assert(contentScale != 0);
        device.container.contentsScale = contentScale;
        device.layer.contentsScale = contentScale;
        [CATransaction commit];
        [CATransaction flush];
    }
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_setDisplaySyncEnabled(JNIEnv *env, jobject obj, jlong devicePtr, jboolean enabled)
{
    @autoreleasepool {
        MetalDevice *device = (__bridge MetalDevice *) (void *) devicePtr;
        device.layer.displaySyncEnabled = enabled;
    }
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_disposeDevice(
    JNIEnv *env, jobject redrawer, jlong devicePtr)
{
    @autoreleasepool {
        MetalDevice *device = (__bridge_transfer MetalDevice *) (void *) devicePtr;
        env->DeleteGlobalRef(device.layer.javaRef);
        [[NSNotificationCenter defaultCenter] removeObserver:device.occlusionObserver];
        /// These two are only installed when live resize is enabled (see createMetalDevice).
        if (device.liveResizeStartObserver) {
            [[NSNotificationCenter defaultCenter] removeObserver:device.liveResizeStartObserver];
        }
        if (device.liveResizeEndObserver) {
            [[NSNotificationCenter defaultCenter] removeObserver:device.liveResizeEndObserver];
        }
        device.layer.displaySyncEnabled = false;  // Prevents window background flashing when the window is disposed
        [device.layer removeFromSuperlayer];
    }
}

} // extern C
#endif
