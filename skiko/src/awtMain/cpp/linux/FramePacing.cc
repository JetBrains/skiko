#include <jni.h>

/*
 * The vblank source behind LinuxDrmVBlankClock: DRM_IOCTL_WAIT_VBLANK on one
 * CRTC, through the kernel's DRM interface. The Kotlin clock thread opens it,
 * waits on it in a loop and closes it; nothing here runs on a thread of its
 * own. Device access relies on the logind seat ACL every local desktop session
 * has; a remote or headless session has no accessible device and the probe
 * reports the backend as unavailable.
 *
 * The CRTC is chosen by matching each active CRTC's mode period against the
 * display's refresh period, across all DRM card nodes, because the toolkit
 * gives Skiko no connector name to bind to. Two displays running at the same
 * refresh rate therefore share the first CRTC that matches, and one of them is
 * paced by the other's scanout. Measured in practice: the rate is correct, but
 * phase is wrong and drifts over a long enough period. While the drift has the
 * tick close to the end of the intended display's period, a repaint that
 * starts on it finishes after that display's vblank and is shown one frame too
 * late, so the window sees periods of dropped and doubled frames around each
 * crossing.
 *
 * Nothing available to Skiko lets us distinguish such CRTCs. The refresh rate
 * the toolkit reports is an integer, so any period within ~5e8/rate^2 ns of the
 * target is indistinguishable from it — +-35 us at 120 Hz for example — and a
 * tolerance tight enough to split them would reject an honest 119.98 Hz panel
 * that the toolkit rounds to 120.
 *
 * Display resolution aliases under fractional scaling (a scaled 2560x1440 panel
 * reports as exactly 3840x2160, a real 4K mode), and CRTC scanout position is
 * (0,0) for every CRTC under a Wayland compositor. Binding by connector name
 * would be exact but needs toolkit-internal data and additional JVM-side APIs.
 *
 * A display whose refresh rate the toolkit does not report is not given this
 * clock at all: with nothing to match on, any CRTC would be a guess on a desktop
 * with more than one display. (findBestCrtc still accepts 0 — that is the
 * availability probe, which only asks whether any active CRTC is reachable.)
 */

#include <errno.h>
#include <fcntl.h>
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
    int fd;
    int crtcIndex;
    int monotonicTimestamps;
} DrmVBlankSource;

static int64_t nowNanos(void) {
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return (int64_t)ts.tv_sec * 1000000000LL + ts.tv_nsec;
}

/*
 * Finds the active CRTC whose mode period best matches wantPeriodNanos,
 * scanning every card node. wantPeriodNanos <= 0 accepts the first active CRTC
 * (the availability probe). On success the card stays open and ownership of
 * the descriptor passes to the caller.
 */
static int findBestCrtc(int64_t wantPeriodNanos, int *outFd, int *outCrtcIndex) {
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
                    if (ioctl(fd, DRM_IOCTL_MODE_GETCRTC, &crtc) != 0 || !crtc.mode_valid
                            || crtc.mode.clock == 0) {
                        continue;
                    }
                    // Pixel clock is in kHz: period = htotal * vtotal / clock.
                    int64_t period
                            = (int64_t)crtc.mode.htotal * crtc.mode.vtotal * 1000000LL / crtc.mode.clock;
                    int64_t score = wantPeriodNanos > 0 ? llabs(period - wantPeriodNanos) : 0;
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

JNIEXPORT jlong JNICALL
Java_org_jetbrains_skiko_swing_LinuxDrmVBlankClock_nativeOpen(JNIEnv *env, jclass cls,
        jlong displayPeriodNanos)
{
    (void)env;
    (void)cls;
    int fd = -1;
    int crtcIndex = -1;
    if (!findBestCrtc(displayPeriodNanos, &fd, &crtcIndex)) {
        return 0;
    }

    DrmVBlankSource *source = (DrmVBlankSource *)calloc(1, sizeof(DrmVBlankSource));
    if (source == NULL) {
        close(fd);
        return 0;
    }
    source->fd = fd;
    source->crtcIndex = crtcIndex;

    struct drm_get_cap cap;
    memset(&cap, 0, sizeof(cap));
    cap.capability = DRM_CAP_TIMESTAMP_MONOTONIC;
    source->monotonicTimestamps = ioctl(fd, DRM_IOCTL_GET_CAP, &cap) == 0 && cap.value != 0;

    return (jlong)(intptr_t)source;
}

/*
 * Returns the kernel's vblank timestamp on the CLOCK_MONOTONIC scale, which is
 * what System.nanoTime() reads on Linux, or -1 when the CRTC is gone.
 */
JNIEXPORT jlong JNICALL
Java_org_jetbrains_skiko_swing_LinuxDrmVBlankClock_nativeWaitTick(JNIEnv *env, jclass cls, jlong handle)
{
    (void)env;
    (void)cls;
    DrmVBlankSource *source = (DrmVBlankSource *)(intptr_t)handle;

    union drm_wait_vblank vbl;
    memset(&vbl, 0, sizeof(vbl));
    const unsigned int highCrtc
            = ((unsigned int)source->crtcIndex << _DRM_VBLANK_HIGH_CRTC_SHIFT) & _DRM_VBLANK_HIGH_CRTC_MASK;
    vbl.request.type = (enum drm_vblank_seq_type)(_DRM_VBLANK_RELATIVE | highCrtc);
    vbl.request.sequence = 1;

    int rc;
    do {
        rc = ioctl(source->fd, DRM_IOCTL_WAIT_VBLANK, &vbl);
    } while (rc == -1 && errno == EINTR);
    if (rc == -1) {
        return -1;
    }

    return source->monotonicTimestamps
            ? (int64_t)vbl.reply.tval_sec * 1000000000LL + (int64_t)vbl.reply.tval_usec * 1000LL
            : nowNanos();
}

JNIEXPORT void JNICALL
Java_org_jetbrains_skiko_swing_LinuxDrmVBlankClock_nativeClose(JNIEnv *env, jclass cls, jlong handle)
{
    (void)env;
    (void)cls;
    DrmVBlankSource *source = (DrmVBlankSource *)(intptr_t)handle;
    close(source->fd);
    free(source);
}

} // extern "C"
