#import <AppKit/AppKit.h>
#import <QuartzCore/QuartzCore.h>

#include <jni.h>

/*
 * The vblank source behind MacDisplayLinkClock: a CADisplayLink for the
 * display (NSScreen.displayLink, macOS 14+). CADisplayLink delivers through a
 * run loop, so the Kotlin clock thread runs its own run loop in short slices
 * from nativeWaitTick; the callback records the timestamp and stops the slice.
 * Everything runs on that one thread, including the link's creation and
 * invalidation, which CADisplayLink requires to happen on the run loop thread.
 * On macOS 13 and older the probe reports the backend unavailable.
 *
 * CADisplayLink.timestamp is CACurrentMediaTime()-based, which is
 * mach_absolute_time-derived — the same monotonic base as System.nanoTime() —
 * so seconds * 1e9 converts directly. The link's preferredFrameRateRange is
 * left at its default, so it follows adaptive refresh rates.
 */

/*
 * NSScreen.screens is class-level state and is read from the calling thread on
 * purpose: going through the AppKit thread from inside the pacing service lock
 * would invite a lock inversion with the EDT.
 */
static NSScreen *screenForDisplayID(CGDirectDisplayID displayID) {
    for (NSScreen *screen in [NSScreen screens]) {
        NSNumber *screenNumber = [[screen deviceDescription] objectForKey:@"NSScreenNumber"];
        if (screenNumber != nil && (CGDirectDisplayID)[screenNumber unsignedIntValue] == displayID) {
            return screen;
        }
    }
    return nil;
}

@interface SkikoDisplayLinkSource : NSObject
@property (nonatomic) CGDirectDisplayID displayID;
@property (nonatomic, strong) CADisplayLink *link;
@property (nonatomic) BOOL ticked;
@property (nonatomic) int64_t tickNanos;
- (void)onTick:(CADisplayLink *)link;
@end

@implementation SkikoDisplayLinkSource

- (void)onTick:(CADisplayLink *)link {
    self.tickNanos = (int64_t)(link.timestamp * 1000000000.0);
    self.ticked = YES;
    CFRunLoopStop(CFRunLoopGetCurrent());
}

@end

extern "C" {

JNIEXPORT jboolean JNICALL
Java_org_jetbrains_skiko_swing_MacDisplayLinkClock_nativeProbe(JNIEnv *env, jclass cls, jint displayID)
{
    if (@available(macOS 14.0, *)) {
        @autoreleasepool {
            return screenForDisplayID((CGDirectDisplayID)displayID) != nil ? JNI_TRUE : JNI_FALSE;
        }
    }
    return JNI_FALSE;
}

JNIEXPORT jlong JNICALL
Java_org_jetbrains_skiko_swing_MacDisplayLinkClock_nativeOpen(JNIEnv *env, jclass cls, jint displayID)
{
    if (@available(macOS 14.0, *)) {
        @autoreleasepool {
            NSScreen *screen = screenForDisplayID((CGDirectDisplayID)displayID);
            if (screen == nil) {
                return 0;
            }
            SkikoDisplayLinkSource *source = [SkikoDisplayLinkSource new];
            source.displayID = (CGDirectDisplayID)displayID;
            // The link itself is created on the first wait, on the thread whose run loop it joins.
            return (jlong)(intptr_t)CFBridgingRetain(source);
        }
    }
    return 0;
}

/*
 * Runs this thread's run loop until the link fires or a short slice elapses.
 * Returns the vblank time on the System.nanoTime() scale, 0 when the slice
 * elapsed without a tick, or -1 when the display is gone.
 */
JNIEXPORT jlong JNICALL
Java_org_jetbrains_skiko_swing_MacDisplayLinkClock_nativeWaitTick(JNIEnv *env, jclass cls, jlong handle)
{
    SkikoDisplayLinkSource *source = (__bridge SkikoDisplayLinkSource *)(void *)(intptr_t)handle;
    @autoreleasepool {
        if (source.link == nil) {
            if (@available(macOS 14.0, *)) {
                NSScreen *screen = screenForDisplayID(source.displayID);
                if (screen == nil) {
                    return -1;
                }
                source.link = [screen displayLinkWithTarget:source selector:@selector(onTick:)];
            }
            if (source.link == nil) {
                return -1;
            }
            [source.link addToRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
        }

        source.ticked = NO;
        CFRunLoopRunInMode(kCFRunLoopDefaultMode, 0.1, false);
        return source.ticked ? (jlong)source.tickNanos : 0;
    }
}

JNIEXPORT void JNICALL
Java_org_jetbrains_skiko_swing_MacDisplayLinkClock_nativeClose(JNIEnv *env, jclass cls, jlong handle)
{
    @autoreleasepool {
        SkikoDisplayLinkSource *source = CFBridgingRelease((void *)(intptr_t)handle);
        [source.link invalidate];
        source.link = nil;
    }
}

} // extern "C"
