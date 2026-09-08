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

} // extern "C"
