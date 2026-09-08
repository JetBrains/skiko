#ifdef __linux__
// For pthread_timedjoin_np; must precede every glibc header.
#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif
#endif

#include <jni.h>

/*
 * Skiko-owned per-CRTC vblank pacing clock for
 * org.jetbrains.skiko.swing.LinuxDrmVBlankClock, built directly on the kernel
 * DRM uAPI (no libdrm dependency): the clock thread blocks in
 * DRM_IOCTL_WAIT_VBLANK on one CRTC and delivers the kernel's vblank timestamp
 * to onNativeTick. Unlike the compositor-tied mechanisms (wp_presentation
 * feedback, XWayland Present), the vblank wait free-runs whether or not
 * anything on screen is changing, which is what a subscription clock needs.
 *
 * The CRTC is bound by matching each active CRTC's mode period against the
 * display's advertised refresh period, across all DRM card nodes. This is
 * approximate in two known ways, both of which only cost accuracy, never
 * correctness.
 *
 * Two displays running at the same refresh rate share the first CRTC that
 * matches, so one of them is paced by the other's scanout. Measured on a
 * 2560x1440 and a 1920x1080 panel both set to 120 Hz: the rate stays right to
 * 0.00205 Hz (0.0017%) and only the phase is wrong, sliding through a full
 * frame every 58529 frames, about 8 minutes. Frames are neither dropped nor
 * doubled; what is lost is phase alignment to the intended display.
 *
 * The refresh rate the toolkit reports is an integer, so any period within
 * about 5e8/rate^2 ns of the target is indistinguishable from it — +-35 us at
 * 120 Hz, against the 143 ns that separated the two panels above. No tolerance
 * can tell same-rate CRTCs apart while still accepting an honest 119.98 Hz
 * panel that the toolkit rounds to 120, so period alone is exhausted here.
 * Two better keys were tried and rejected on measurement: display resolution
 * aliases under fractional scaling (a scaled 2560x1440 panel reports as
 * exactly 3840x2160, a real 4K mode), and CRTC scanout position is (0,0) for
 * every CRTC under a Wayland compositor while the toolkit reports layout
 * coordinates. Binding by compositor connector name would be exact, but needs
 * toolkit-internal data Skiko cannot reach; the connectorName parameter is
 * reserved for that and is currently always null.
 *
 * When the toolkit reports no refresh rate at all there is nothing to match
 * on, so the first active CRTC is used — right on a single-display desktop, a
 * guess on a mixed-refresh one. Device access relies on the
 * logind seat ACL every local desktop session has; remote and headless
 * environments have no accessible display device and the probe reports the
 * backend unavailable.
 */

#include <errno.h>
#include <fcntl.h>
#include <pthread.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/ioctl.h>
#include <time.h>
#include <unistd.h>

#include <drm/drm.h>
#include <drm/drm_mode.h>

typedef struct {
    pthread_t thread;
    int hasThread;
    volatile int stop;
    int fd;
    int crtcIndex;
    int monotonicTimestamps;
    jlong fallbackPeriodNanos;
    jobject clockRef;
} FramePacingDrmClock;

static JavaVM *framePacingJvm = NULL;
static jmethodID framePacingOnNativeTickMID = NULL;

static int stopRequested(FramePacingDrmClock *clock)
{
    return __atomic_load_n(&clock->stop, __ATOMIC_ACQUIRE);
}

static int64_t nowNanos(void)
{
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return (int64_t)ts.tv_sec * 1000000000LL + ts.tv_nsec;
}

/* Sleeps, or returns 0 early when the clock was stopped. */
static int sleepOrStop(FramePacingDrmClock *clock, int64_t nanos)
{
    if (nanos > 0) {
        struct timespec ts = { nanos / 1000000000LL, nanos % 1000000000LL };
        while (nanosleep(&ts, &ts) == -1 && errno == EINTR) {
            if (stopRequested(clock)) {
                return 0;
            }
        }
    }
    return !stopRequested(clock);
}

/*
 * Finds the active CRTC whose mode period best matches wantPeriodNanos,
 * scanning every card node. wantPeriodNanos <= 0 accepts the first active CRTC
 * (the availability probe). On success the card stays open and ownership of
 * the descriptor passes to the caller.
 */
static int findBestCrtc(int64_t wantPeriodNanos, int *outFd, int *outCrtcIndex)
{
    int bestFd = -1;
    int bestIndex = -1;
    int64_t bestScore = INT64_MAX;

    for (int card = 0; card < 16; card++) {
        char path[32];
        snprintf(path, sizeof(path), "/dev/dri/card%d", card);
        int fd = open(path, O_RDWR | O_CLOEXEC);
        if (fd < 0) {
            continue;
        }

        struct drm_mode_card_res res;
        uint32_t crtcs[64];
        memset(&res, 0, sizeof(res));
        if (ioctl(fd, DRM_IOCTL_MODE_GETRESOURCES, &res) == 0 && res.count_crtcs > 0) {
            uint32_t count = res.count_crtcs > 64 ? 64 : res.count_crtcs;
            memset(&res, 0, sizeof(res));
            res.crtc_id_ptr = (uint64_t)(uintptr_t)crtcs;
            res.count_crtcs = count;
            if (ioctl(fd, DRM_IOCTL_MODE_GETRESOURCES, &res) == 0) {
                /*
                 * The kernel writes as many ids as the count it was handed
                 * allows, then reports the true total. That total can be
                 * smaller than the first call said, if a CRTC went away in
                 * between, so clamp to whichever is lower: the loop must only
                 * read ids the kernel actually wrote.
                 */
                if (res.count_crtcs < count) {
                    count = res.count_crtcs;
                }
                for (uint32_t i = 0; i < count; i++) {
                    struct drm_mode_crtc crtc;
                    memset(&crtc, 0, sizeof(crtc));
                    crtc.crtc_id = crtcs[i];
                    if (ioctl(fd, DRM_IOCTL_MODE_GETCRTC, &crtc) != 0 ||
                            !crtc.mode_valid || crtc.mode.clock == 0) {
                        continue;
                    }
                    // Pixel clock is in kHz: period = htotal * vtotal / clock.
                    int64_t period = (int64_t)crtc.mode.htotal * crtc.mode.vtotal
                            * 1000000LL / crtc.mode.clock;
                    int64_t score = wantPeriodNanos > 0
                            ? llabs(period - wantPeriodNanos) : 0;
                    if (score < bestScore) {
                        bestScore = score;
                        bestIndex = (int)i;
                        if (bestFd != fd) {
                            if (bestFd >= 0) {
                                close(bestFd);
                            }
                            bestFd = fd;
                        }
                    }
                }
            }
        }

        if (bestFd != fd) {
            close(fd);
        }
        if (wantPeriodNanos <= 0 && bestFd >= 0) {
            break;
        }
    }

    if (bestFd < 0) {
        return 0;
    }
    *outFd = bestFd;
    *outCrtcIndex = bestIndex;
    return 1;
}

extern "C" {

JNIEXPORT jboolean JNICALL
Java_org_jetbrains_skiko_swing_LinuxDrmVBlankClock_nativeProbe(JNIEnv *env, jclass cls)
{
    (void)env;
    (void)cls;
    int fd = -1;
    int index = -1;
    if (!findBestCrtc(0, &fd, &index)) {
        return JNI_FALSE;
    }
    close(fd);
    return JNI_TRUE;
}


} // extern "C"
