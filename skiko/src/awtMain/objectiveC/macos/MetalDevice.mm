#import "MetalDevice.h"

#include <assert.h>

/// TODO: extract SKIKO_JNI_VERSION to a separate header?
#include "../../../jvmMain/cpp/common/interop.hh"

/// Linked from skiko/src/jvmMain/cpp/common/impl/Library.cc
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

static CVReturn MetalDeviceDisplayLinkCallback(CVDisplayLinkRef displayLink, const CVTimeStamp *now, const CVTimeStamp *outputTime, CVOptionFlags flagsIn, CVOptionFlags *flagsOut, void *displayLinkContext) {
    MetalDevice *device = (__bridge MetalDevice *)displayLinkContext;

    [device handleDisplayLinkFired];

    return kCVReturnSuccess;
}

@implementation MetalDevice {
    NSScreen *_displayLinkScreen;
    CVDisplayLinkRef _displayLink;

    BOOL _vsyncBarrierEnabled;

    jobject _callbacks;

    /// Corresponding to FrameDispatcherBarriersCallbacks
    jmethodID _signalVsync;
    jmethodID _signalFrameCompletion;
    jmethodID _enableVsyncBarrier;
    jmethodID _disableVsyncBarrier;
}

- (instancetype)initWithContainer:(CALayer *)container adapter:(id<MTLDevice>)adapter window:(NSWindow *)window env:(JNIEnv *)env redrawer:(jobject)redrawer callbacks:(jobject)callbacks {
    self = [super init];

    if (self) {
        self.container = container;
        self.adapter = adapter;
        self.queue = [adapter newCommandQueue];
        self.window = window;
        self.layer = [AWTMetalLayer new];
        _vsyncBarrierEnabled = NO;

        _displayLinkScreen = nil;
        _callbacks = env->NewGlobalRef(callbacks);

        [container removeAllAnimations];
        [container setAutoresizingMask: (kCALayerWidthSizable|kCALayerHeightSizable)];
        [container setNeedsDisplayOnBoundsChange: YES];

        self.layer.device = adapter;
        self.layer.pixelFormat = MTLPixelFormatBGRA8Unorm;
        self.layer.contentsGravity = kCAGravityTopLeft;
        CGFloat transparent[] = { 0.0f, 0.0f, 0.0f, 0.0f };
        self.layer.backgroundColor = CGColorCreate(CGColorSpaceCreateDeviceRGB(), transparent);
        self.layer.opaque = NO;
        self.layer.javaRef = env->NewGlobalRef(redrawer);

        [container addSublayer: self.layer];

        jclass callbacksClass = env->GetObjectClass(_callbacks);

        _signalVsync = env->GetMethodID(callbacksClass, "signalVsync", "()V");
        _signalFrameCompletion = env->GetMethodID(callbacksClass, "signalFrameCompletion", "()V");
        _enableVsyncBarrier = env->GetMethodID(callbacksClass, "enableVsyncBarrier", "()V");
        _disableVsyncBarrier = env->GetMethodID(callbacksClass, "disableVsyncBarrier", "()V");
    }

    return self;
}

- (void)dealloc {
    [self invalidateDisplayLink];
}

- (void)disposeWithEnv:(JNIEnv *)env {
    env->DeleteGlobalRef(_callbacks);
    env->DeleteGlobalRef(self.layer.javaRef);

    [self.layer removeFromSuperlayer];
}

- (void)setVsyncBarrierEnabled:(BOOL)enabled env:(JNIEnv *)env {
    if (_vsyncBarrierEnabled == enabled) {
        return;
    }

    _vsyncBarrierEnabled = enabled;

    jmethodID invokedMethodID = enabled ? _enableVsyncBarrier : _disableVsyncBarrier;

    env->CallVoidMethod(_callbacks, invokedMethodID);
}

- (void)recreateDisplayLinkIfNeededWithEnv:(JNIEnv *)env {
    if ([self.window.screen isEqualTo: _displayLinkScreen]) {
        return;
    }

    [self invalidateDisplayLink];

    _displayLinkScreen = self.window.screen;

    NSDictionary* screenDescription = [_displayLinkScreen deviceDescription];
    NSNumber* screenID = [screenDescription objectForKey:@"NSScreenNumber"];

    CVReturn result;

    result = CVDisplayLinkCreateWithCGDisplay([screenID unsignedIntValue], &_displayLink);

    if (result != kCVReturnSuccess) {
        _displayLink = nil;
        [self setVsyncBarrierEnabled:NO env:env];
        return;
    }

    result = CVDisplayLinkSetOutputCallback(_displayLink, &MetalDeviceDisplayLinkCallback, (__bridge void *)(self));

    if (result != kCVReturnSuccess) {
        CVDisplayLinkRelease(_displayLink);
        _displayLink = nil;
        [self setVsyncBarrierEnabled:NO env:env];
        return;
    }

    result = CVDisplayLinkStart(_displayLink);

    if (result != kCVReturnSuccess) {
        CVDisplayLinkRelease(_displayLink);
        _displayLink = nil;
        [self setVsyncBarrierEnabled:NO env:env];
        return;
    }

    [self setVsyncBarrierEnabled:YES env:env];
}

- (void)handleDisplayLinkFired {
    JNIEnv *env = resolveJNIEnvForCurrentThread();

    env->CallVoidMethod(_callbacks, _signalVsync);
}

- (void)signalFrameCompletionWithEnv:(JNIEnv *)env {
    /// When dispatched from `MTLCommandBuffer` completion handler
    /// the thread is managed by Metal, so we need to resolve it dynamically
    if (!env) {
        env = resolveJNIEnvForCurrentThread();
    }

    env->CallVoidMethod(_callbacks, _signalFrameCompletion);
}

- (void)invalidateDisplayLink {
    if (_displayLink) {
        CVDisplayLinkStop(_displayLink);
        CVDisplayLinkRelease(_displayLink);
        _displayLink = nil;
    }
}

@end
