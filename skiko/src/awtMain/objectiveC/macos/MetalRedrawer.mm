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
static jmethodID getDrawFrameWhileLiveResizingMethodID(JNIEnv *env, jobject liveResize);

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

- (CGSize)pixelSize
{
    CGFloat scale = self.contentsScale;
    int pixelWidth = (int)(self.bounds.size.width * scale);
    int pixelHeight = (int)(self.bounds.size.height * scale);
    return CGSizeMake(pixelWidth, pixelHeight);
}

/// During a live resize this fires (on the AppKit main thread) when the layer autoresizes to track the
/// window. We update drawableSize and synchronously draw + present, all within the ambient
/// CATransaction that is also committing the window's new size, so content and backing stay in sync.
- (void)setBounds:(CGRect)bounds
{
    [super setBounds:bounds];
    if (!self.liveResizing || self.liveResizeRef == NULL) {
        return;
    }

    CGSize pixelSize = self.pixelSize;
    if (pixelSize.width <= 0 || pixelSize.height <= 0) {
        return;
    }

    self.drawableSize = pixelSize;

    JNIEnv *env = resolveJNIEnvForCurrentThread();
    jmethodID drawFrameWhileLiveResizing = getDrawFrameWhileLiveResizingMethodID(env, self.liveResizeRef);
    env->CallVoidMethod(self.liveResizeRef, drawFrameWhileLiveResizing, (jint)pixelSize.width, (jint)pixelSize.height);
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

static jmethodID getOnLiveResizeStartedMethodID(JNIEnv *env, jobject liveResize) {
    static jmethodID onLiveResizeStarted = NULL;
    if (onLiveResizeStarted == NULL) {
        jclass liveResizeClass = env->GetObjectClass(liveResize);
        onLiveResizeStarted = env->GetMethodID(liveResizeClass, "onLiveResizeStarted", "()V");
    }
    return onLiveResizeStarted;
}

static jmethodID getOnLiveResizeEndedMethodID(JNIEnv *env, jobject liveResize) {
    static jmethodID onLiveResizeEnded = NULL;
    if (onLiveResizeEnded == NULL) {
        jclass liveResizeClass = env->GetObjectClass(liveResize);
        onLiveResizeEnded = env->GetMethodID(liveResizeClass, "onLiveResizeEnded", "()V");
    }
    return onLiveResizeEnded;
}

static jmethodID getDrawFrameWhileLiveResizingMethodID(JNIEnv *env, jobject liveResize) {
    static jmethodID drawFrameWhileLiveResizing = NULL;
    if (drawFrameWhileLiveResizing == NULL) {
        jclass liveResizeClass = env->GetObjectClass(liveResize);
        drawFrameWhileLiveResizing = env->GetMethodID(liveResizeClass, "drawFrameWhileLiveResizing", "(II)V");
    }
    return drawFrameWhileLiveResizing;
}

extern "C"
{

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_createMetalDevice(
    JNIEnv *env, jobject redrawer, jlong windowPtr, jboolean transparency, jint frameBuffering, jlong adapterPtr, jlong platformInfoPtr, jboolean liveResizeEnabled, jobject liveResize)
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
        layer.liveResizeRef = env->NewGlobalRef(liveResize);

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
        /// presentsWithTransaction is scoped to the whole resize session here (not per-frame): the start
        /// observer sets it YES, the end observer clears it. This is safe because during a resize the
        /// main thread is the sole presenter (setBounds + drawFrameWhileLiveResizing) and any frame that
        /// reaches the async present path in finishFrame is dropped there. The ordering below keeps
        /// presentsWithTransaction = YES a strict subset of inLiveResize = YES (set inLiveResize first at
        /// start, clear the flag first at end) so that whenever the flag is YES a background straggler is
        /// already being dropped — it can never present async under YES and defer forever on a
        /// transaction that never commits.
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
                    strongDevice.layer.presentsWithTransaction = YES;
                    strongDevice.layer.liveResizing = YES;
                    JNIEnv *jniEnv = resolveJNIEnvForCurrentThread();
                    jmethodID onLiveResizeStarted = getOnLiveResizeStartedMethodID(jniEnv, strongDevice.layer.liveResizeRef);
                    jniEnv->CallVoidMethod(strongDevice.layer.liveResizeRef, onLiveResizeStarted);
                }];
            device.liveResizeEndObserver =
                [[NSNotificationCenter defaultCenter] addObserverForName:NSWindowDidEndLiveResizeNotification
                                                                  object:window
                                                                   queue:nil
                                                              usingBlock:^(NSNotification * _Nonnull note) {
                    MetalDevice *strongDevice = weakDevice;
                    if (!strongDevice) return;
                    /// Stop the main-thread live-resize draw (liveResizing) before clearing inLiveResize,
                    /// so setBounds stops driving frames as the resize winds down. Clear
                    /// presentsWithTransaction before inLiveResize so background frames, which resume
                    /// presenting async once inLiveResize goes NO, never observe the flag as YES.
                    strongDevice.layer.liveResizing = NO;
                    strongDevice.layer.presentsWithTransaction = NO;
                    strongDevice.inLiveResize = NO;
                    JNIEnv *jniEnv = resolveJNIEnvForCurrentThread();
                    jmethodID onLiveResizeEnded = getOnLiveResizeEndedMethodID(jniEnv, strongDevice.layer.liveResizeRef);
                    jniEnv->CallVoidMethod(strongDevice.layer.liveResizeRef, onLiveResizeEnded);
                }];
        }

        return (jlong) (__bridge_retained void *) device;
    }
}

/// Schedules a frame on the AppKit main thread. Called from Kotlin (MetalLiveResize.scheduleFrame) during a live
// resize, when the background frame loop is gated off. The main queue serializes this with setBounds-driven frames, so
// there is only ever one presenter and one drawable in flight — no async present, no drawable-pool contention. The
// current pixel size is read on the main thread right before the callback, so the frame is rendered at the layer's live
// size.
JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalLiveResize_scheduleFrameOnAppKitThread(
    JNIEnv *env, jobject liveResize, jlong devicePtr)
{
    MetalDevice *device = (__bridge MetalDevice *) (void *) devicePtr;
    /// liveResizeRef is a global ref (created in createMetalDevice), safe to use from the deferred block.
    jobject liveResizeRef = device.layer.liveResizeRef;
    /// Coalesce: only dispatch if no resize frame is already pending. atomic_exchange returns the prior
    /// value, so a `true` return means one is in flight and we skip. Scheduling may come from the EDT or
    /// the main thread, hence the atomic.
    if (atomic_exchange(&device->frameOnAppKitThreadScheduled, true)) {
        return;
    }
    dispatch_async(dispatch_get_main_queue(), ^{
        /// Clear FIRST, unconditionally — before any guard below can bail. This keeps the flag honest
        /// even when the frame is dropped (e.g. the resize already ended), so a later resize can schedule
        /// again; and it lets an onRender -> needRender inside drawFrameWhileLiveResizing re-arm the next frame,
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
        jmethodID drawFrameWhileLiveResizing = getDrawFrameWhileLiveResizingMethodID(jniEnv, liveResizeRef);
        jniEnv->CallVoidMethod(liveResizeRef, drawFrameWhileLiveResizing, (jint)pixelWidth, (jint)pixelHeight);
    });
}

/// Runs `runnable` on the AWT event dispatch thread and blocks the caller until it completes, via
/// `sun.lwawt.macosx.LWCToolkit.invokeAndWait(Runnable, Component)`. That method spins the AppKit run loop in
/// the special mode while waiting, so a synchronous Java->AppKit call made from `runnable` is serviced instead
/// of deadlocking against the (parked) AppKit main thread — the reason we use it here rather than parking the
/// thread on a coroutine. LWCToolkit lives in a non-exported JDK package, but JNI does not perform module
/// access checks, so no `--add-opens` is required. The class (global ref) and method id are stable for the
/// JVM lifetime and cached; any exception thrown by invokeAndWait is left pending so it propagates to Kotlin.
JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalLiveResize_invokeOnEventThreadAndWait(
    JNIEnv *env, jobject liveResize, jobject runnable, jobject component)
{
    static jclass lwcToolkitClass = NULL;
    static jmethodID invokeAndWaitMethodID = NULL;
    if (invokeAndWaitMethodID == NULL) {
        jclass localClass = env->FindClass("sun/lwawt/macosx/LWCToolkit");
        if (localClass == NULL) {
            return; // NoClassDefFoundError left pending -> propagates to Kotlin, which logs and skips the frame
        }
        jmethodID mid = env->GetStaticMethodID(
            localClass, "invokeAndWait", "(Ljava/lang/Runnable;Ljava/awt/Component;)V");
        if (mid == NULL) {
            env->DeleteLocalRef(localClass);
            return; // NoSuchMethodError left pending
        }
        lwcToolkitClass = (jclass) env->NewGlobalRef(localClass);
        env->DeleteLocalRef(localClass);
        invokeAndWaitMethodID = mid;
    }
    env->CallStaticVoidMethod(lwcToolkitClass, invokeAndWaitMethodID, runnable, component);
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
        if (device.layer.liveResizeRef) {
            env->DeleteGlobalRef(device.layer.liveResizeRef);
        }
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
