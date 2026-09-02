#import <AppKit/AppKit.h>
#import <QuartzCore/QuartzCore.h>

#include <jni.h>

/*
 * Skiko-owned CADisplayLink for frame pacing (NSScreen.displayLink, macOS 14+):
 * one link per subscribed display, driving
 * org.jetbrains.skiko.swing.MacDisplayLinkClock.onNativeTick from a dedicated
 * per-clock runloop thread (attached to the VM as a daemon). CVDisplayLink
 * would offer the same shape on older systems but is deprecated since
 * macOS 15; on macOS 13 and older the probe reports unavailable and the
 * Kotlin side paces with the shared timer instead.
 *
 * Timestamps: CADisplayLink.timestamp is CACurrentMediaTime()-based, which is
 * mach_absolute_time-derived — the same monotonic base as System.nanoTime() —
 * so seconds * 1e9 converts directly.
 *
 * The link's preferredFrameRateRange is left at its default, which follows the
 * display's current refresh behavior — including adaptive rates on ProMotion
 * panels — making this backend VRR-aware.
 *
 * Written in ARC style, matching this build.
 */

static JavaVM *framePacingJvm = NULL;
static jmethodID framePacingOnNativeTickMID = NULL;

/*
 * NSScreen for a CGDirectDisplayID. NSScreen.screens is class-level state and
 * is read here from the calling (non-AppKit) thread deliberately: resolving it
 * via the AppKit thread from inside the pacing service lock would invite an
 * AppKit/EDT lock inversion for a value that is only used to create the link.
 */
static NSScreen *skikoScreenForDisplayID(CGDirectDisplayID displayID)
{
    for (NSScreen *screen in [NSScreen screens]) {
        NSNumber *screenNumber = [[screen deviceDescription] objectForKey:@"NSScreenNumber"];
        if (screenNumber != nil && (CGDirectDisplayID)[screenNumber unsignedIntValue] == displayID) {
            return screen;
        }
    }
    return nil;
}

@interface SkikoFramePacingClock : NSObject
- (instancetype)initWithDisplayID:(CGDirectDisplayID)displayID clockRef:(jobject)clockRef;
- (void)start;
- (void)stop;
- (void)joinAndReleaseRef:(JNIEnv *)env;
@end

@implementation SkikoFramePacingClock {
    CGDirectDisplayID _displayID;
    jobject _clockRef; // global ref, deleted in joinAndReleaseRef
    NSCondition *_doneCondition;
    CFRunLoopRef _runLoop; // owned by the clock thread; guarded by _doneCondition
    BOOL _stopped;         // guarded by _doneCondition
    BOOL _threadExited;    // guarded by _doneCondition
}

- (instancetype)initWithDisplayID:(CGDirectDisplayID)displayID clockRef:(jobject)clockRef {
    self = [super init];
    if (self) {
        _displayID = displayID;
        _clockRef = clockRef;
        _doneCondition = [NSCondition new];
        _runLoop = NULL;
        _stopped = NO;
        _threadExited = NO;
    }
    return self;
}

- (void)start {
    NSThread *thread = [[NSThread alloc] initWithTarget:self
                                               selector:@selector(threadMain)
                                                 object:nil];
    thread.name = [NSString stringWithFormat:@"Skiko-FramePacing-CADisplayLink-%u", _displayID];
    [thread start];
}

- (void)signalThreadExited {
    [_doneCondition lock];
    _threadExited = YES;
    [_doneCondition signal];
    [_doneCondition unlock];
}

- (void)threadMain {
    @autoreleasepool {
        CADisplayLink *link = nil;
        if (@available(macOS 14.0, *)) {
            NSScreen *screen = skikoScreenForDisplayID(_displayID);
            if (screen != nil) {
                link = [screen displayLinkWithTarget:self selector:@selector(onTick:)];
            }
        }

        [_doneCondition lock];
        if (_stopped || link == nil) {
            [_doneCondition unlock];
            [link invalidate];
            [self signalThreadExited];
            return;
        }
        _runLoop = CFRunLoopGetCurrent();
        [_doneCondition unlock];

        [link addToRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
        CFRunLoopRun(); // exits when stop() calls CFRunLoopStop

        // Invalidation must happen on the runloop thread; it also breaks the
        // link's retain of this target.
        [link invalidate];
        [self signalThreadExited];
    }
}

- (void)onTick:(CADisplayLink *)link {
    JNIEnv *env;
    if (framePacingJvm->AttachCurrentThreadAsDaemon((void **)&env, NULL) != JNI_OK) {
        return;
    }
    jlong nanos = (jlong)(link.timestamp * 1000000000.0);
    env->CallVoidMethod(_clockRef, framePacingOnNativeTickMID, nanos);
    if (env->ExceptionCheck()) {
        env->ExceptionClear();
    }
}

- (void)stop {
    [_doneCondition lock];
    _stopped = YES;
    if (_runLoop != NULL) {
        CFRunLoopStop(_runLoop);
    }
    [_doneCondition unlock];
}

- (void)joinAndReleaseRef:(JNIEnv *)env {
    // The thread exits promptly once stopped (at most one frame callback);
    // bound the wait so release stays effectively brief even if it wedges.
    NSDate *deadline = [NSDate dateWithTimeIntervalSinceNow:1.0];
    [_doneCondition lock];
    while (!_threadExited && [_doneCondition waitUntilDate:deadline]) {
        // Re-check _threadExited; waitUntilDate returning NO means timeout.
    }
    BOOL exited = _threadExited;
    [_doneCondition unlock];
    if (!exited) {
        // The runloop thread is wedged and may still be inside onTick:. Leak the
        // global reference rather than delete it under a live JNI call, which
        // would crash the VM. The clock is already stopped, so the leak is one
        // Kotlin object per wedged display and nothing else.
        return;
    }
    env->DeleteGlobalRef(_clockRef);
    _clockRef = NULL;
}

@end

extern "C" {

JNIEXPORT jboolean JNICALL
Java_org_jetbrains_skiko_swing_MacDisplayLinkClock_nativeProbe(JNIEnv *env, jclass cls, jint displayID)
{
    if (@available(macOS 14.0, *)) {
        @autoreleasepool {
            return skikoScreenForDisplayID((CGDirectDisplayID)displayID) != nil ? JNI_TRUE : JNI_FALSE;
        }
    }
    return JNI_FALSE;
}

JNIEXPORT jlong JNICALL
Java_org_jetbrains_skiko_swing_MacDisplayLinkClock_nativeCreate(JNIEnv *env, jclass cls,
                                                                jint displayID, jobject clock)
{
    if (framePacingJvm == NULL) {
        if (env->GetJavaVM(&framePacingJvm) != JNI_OK) {
            return 0;
        }
    }
    if (framePacingOnNativeTickMID == NULL) {
        jclass clockClass = env->GetObjectClass(clock);
        framePacingOnNativeTickMID = env->GetMethodID(clockClass, "onNativeTick", "(J)V");
        if (framePacingOnNativeTickMID == NULL) {
            env->ExceptionClear();
            return 0;
        }
    }

    if (!Java_org_jetbrains_skiko_swing_MacDisplayLinkClock_nativeProbe(env, cls, displayID)) {
        return 0;
    }

    jobject clockRef = env->NewGlobalRef(clock);
    if (clockRef == NULL) {
        return 0;
    }
    SkikoFramePacingClock *pacingClock =
            [[SkikoFramePacingClock alloc] initWithDisplayID:(CGDirectDisplayID)displayID
                                                    clockRef:clockRef];
    // The bridge-retain is the reference the jlong carries; nativeRelease drops it.
    return (jlong)(intptr_t)CFBridgingRetain(pacingClock);
}

JNIEXPORT void JNICALL
Java_org_jetbrains_skiko_swing_MacDisplayLinkClock_nativeStart(JNIEnv *env, jclass cls, jlong ptr)
{
    SkikoFramePacingClock *pacingClock = (__bridge SkikoFramePacingClock *)(void *)(intptr_t)ptr;
    [pacingClock start];
}

JNIEXPORT void JNICALL
Java_org_jetbrains_skiko_swing_MacDisplayLinkClock_nativeStop(JNIEnv *env, jclass cls, jlong ptr)
{
    SkikoFramePacingClock *pacingClock = (__bridge SkikoFramePacingClock *)(void *)(intptr_t)ptr;
    [pacingClock stop];
}

JNIEXPORT void JNICALL
Java_org_jetbrains_skiko_swing_MacDisplayLinkClock_nativeRelease(JNIEnv *env, jclass cls, jlong ptr)
{
    SkikoFramePacingClock *pacingClock =
            (SkikoFramePacingClock *)CFBridgingRelease((void *)(intptr_t)ptr);
    [pacingClock joinAndReleaseRef:env];
}

} // extern "C"
