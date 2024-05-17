#ifdef SK_METAL

#import <jawt.h>
#import <jawt_md.h>
#import <QuartzCore/QuartzCore.h>
#import <AppKit/AppKit.h>
#import <stdatomic.h>

@interface DisplayLinkThrottler : NSObject
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
    BOOL _isSleeping;
}

- (instancetype)init {
    self = [super init];

    if (self) {
        _displayLinkScreen = nil;
        _displayLink = nil;
        _vsyncConditionLock = [[NSConditionLock alloc] initWithCondition: 1];
        _isSleeping = NO;

        NSNotificationCenter *notificationCenter = [[NSWorkspace sharedWorkspace] notificationCenter];

        [notificationCenter addObserver:self
                            selector:@selector(systemWillSleep:)
                            name:NSWorkspaceWillSleepNotification
                            object:nil];

        [notificationCenter addObserver:self
                            selector:@selector(systemDidWake:)
                            name:NSWorkspaceDidWakeNotification
                            object:nil];
    }

    return self;
}

- (void)systemWillSleep:(NSNotification *)notification {
    _isSleeping = YES;
    [self invalidateDisplayLink];
    [self onVSync];
}

- (void)systemDidWake:(NSNotification *)notification {
    _isSleeping = NO;
}

- (void)onVSync {
    /// Lock condition lock and immediately unlock setting condition variable to 1 (can render now)
    [_vsyncConditionLock lock];
    [_vsyncConditionLock unlockWithCondition:1];
}

- (void)waitVSync {
    /// If display link is not constructed (due to failure or explicit opt-out), don't perform any waiting
    if (!_displayLink) {
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
        _displayLinkScreen = nil;
    }
}

- (void)setupDisplayLinkForWindow:(NSWindow *)window {
    if (_isSleeping) {
        /// System is sleeping, don't setup display link
        return;
    }

    NSScreen *screen = window.screen;

    if (!screen) {
        /// window is not yet attached to screen (or window is nil)
        return;
    }

    if ([screen isEqualTo: _displayLinkScreen]) {
        /// display link is already active for this screen, do nothing
        return;
    }

    [self invalidateDisplayLink];

    NSDictionary* screenDescription = [screen deviceDescription];
    NSNumber* screenID = [screenDescription objectForKey:@"NSScreenNumber"];

    _displayLink = [self createDisplayLinkForScreen:screenID];
    _displayLinkScreen = screen;
}

- (CVDisplayLinkRef)createDisplayLinkForScreen:(NSNumber *)screenID {
    CVReturn result;
    CVDisplayLinkRef displayLink;

    result = CVDisplayLinkCreateWithCGDisplay([screenID unsignedIntValue], &displayLink);

    if (result != kCVReturnSuccess) {
        return nil;
    }

    result = CVDisplayLinkSetOutputCallback(displayLink, &displayLinkCallback, (__bridge void *)(self));

    if (result != kCVReturnSuccess) {
        CVDisplayLinkRelease(displayLink);
        return nil;
    }

    result = CVDisplayLinkStart(displayLink);

    if (result != kCVReturnSuccess) {
        CVDisplayLinkRelease(displayLink);
        return nil;
    }

    return displayLink;
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