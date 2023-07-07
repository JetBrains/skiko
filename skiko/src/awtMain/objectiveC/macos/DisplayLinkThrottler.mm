#ifdef SK_METAL

#import <jawt.h>
#import <jawt_md.h>
#import <QuartzCore/QuartzCore.h>
#import <AppKit/AppKit.h>
#import <stdatomic.h>

@interface DisplayLinkThrottler : NSObject

- (void)onVSync;

@end

static CVReturn displayLinkCallback(CVDisplayLinkRef displayLink, const CVTimeStamp *now, const CVTimeStamp *outputTime, CVOptionFlags flagsIn, CVOptionFlags *flagsOut, void *displayLinkContext) {
    DisplayLinkThrottler *throttler = (__bridge DisplayLinkThrottler *)displayLinkContext;

    [throttler onVSync];

    return kCVReturnSuccess;
}

@implementation DisplayLinkThrottler {
    NSScreen *_displayLinkScreen;
    CVDisplayLinkRef _displayLink;
    NSConditionLock *_vsyncConditionLock;
    volatile atomic_bool _displayLinkOk;
}

- (instancetype)init {
    self = [super init];

    if (self) {
        _displayLinkScreen = nil;
        _displayLink = nil;
        _vsyncConditionLock = [[NSConditionLock alloc] initWithCondition: 1];
        atomic_store(&_displayLinkOk, true);
    }

    return self;
}

- (void)onVSync {
    /// Lock condition lock and immediately unlock setting condition variable to 1 (can render now)
    [_vsyncConditionLock lock];
    [_vsyncConditionLock unlockWithCondition:1];
}

- (void)waitVSync {
    bool displayLinkOk = atomic_load(&_displayLinkOk);

    /// If display link construction was corrupted, don't perform any waiting
    if (!displayLinkOk) {
        return;
    }

    /// Wait until `onVSync` signals 1, then immediately lock, set it to 0 and unlock again.
    [_vsyncConditionLock lockWhenCondition:1];
    [_vsyncConditionLock unlockWithCondition:0];
}

- (void)invalidateDisplayLink {
    if (_displayLink) {
        CVDisplayLinkStop(_displayLink);
        CVDisplayLinkRelease(_displayLink);
        _displayLink = nil;
    }
}

- (void)handleDisplayLinkSetupFailure {
    atomic_store(&_displayLinkOk, false);

    /// Next line is here to tackle edge case where displayLink reconstruction failed
    /// but someone is already waiting at `waitUntilVsync`
    /// It's quite improbable scenario anyway
    [self onVSync];
}

- (void)setupDisplayLinkForWindow:(NSWindow *)window {
    NSScreen *screen = window.screen;

    if (!screen) {
        /// window is not yet attached to screen (or window is nil)
        return;
    }

    if ([screen isEqualTo: _displayLinkScreen]) {
        /// display link is already active for current window, do nothing
        return;
    }

    [self invalidateDisplayLink];

    NSDictionary* screenDescription = [_displayLinkScreen deviceDescription];
    NSNumber* screenID = [screenDescription objectForKey:@"NSScreenNumber"];

    CVReturn result;

    CVDisplayLinkRef displayLink;
    result = CVDisplayLinkCreateWithCGDisplay([screenID unsignedIntValue], &displayLink);

    if (result != kCVReturnSuccess) {
        [self handleDisplayLinkSetupFailure];
        return;
    }

    result = CVDisplayLinkSetOutputCallback(displayLink, &displayLinkCallback, (__bridge void *)(self));

    if (result != kCVReturnSuccess) {
        [self handleDisplayLinkSetupFailure];
        return;
    }

    result = CVDisplayLinkStart(displayLink);

    if (result != kCVReturnSuccess) {
        [self handleDisplayLinkSetupFailure];
        return;
    }

    _displayLink = displayLink;
    _displayLinkScreen = screen;

    atomic_store(&_displayLinkOk, true);
}

- (void)dealloc {
    [self invalidateDisplayLink];
}

@end

extern "C" {

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_DisplayLinkThrottler_create(JNIEnv *env, jobject obj) {
    DisplayLinkThrottler *throttler = [[DisplayLinkThrottler alloc] init];

    return (jlong) (__bridge_retained void *) throttler;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_DisplayLinkThrottler_dispose(JNIEnv *env, jobject obj, jlong throttlerPtr) {
    DisplayLinkThrottler *throttler = (__bridge_transfer DisplayLinkThrottler *) (void *) throttlerPtr;
    /// throttler will be released by ARC and deallocated in the end of this scope.
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_DisplayLinkThrottler_waitVSync(JNIEnv *env, jobject obj, jlong throttlerPtr, jlong windowPtr) {
    DisplayLinkThrottler *throttler = (__bridge DisplayLinkThrottler *) (void *) throttlerPtr;
    NSWindow *window = (__bridge NSWindow *) (void *) windowPtr;

    [throttler setupDisplayLinkForWindow:window];
    [throttler waitVSync];
}

}

#endif // SK_METAL