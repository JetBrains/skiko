#include <GL/gl.h>
#include <GL/glx.h>
#include <X11/Xlib.h>
#include <atomic>
#include <cstdint>
#include <cstring>
#include <ctime>
#include <pthread.h>

#include "jni_helpers.h"

// Defined in skiko/src/jvmMain/cpp/common/impl/Library.cc's JNI_OnLoad. All the cpp source sets
// (jvmMain/common, awtMain/common, awtMain/<os>) are linked into a single "skiko-<hostId>" shared
// library (see Library.kt), so this extern resolves at link time even though it is defined in a
// different source set. Same pattern used by awtMain/windows/VsyncTicker.cc.
extern "C" JavaVM *jvm;

namespace {

/*
 * LinuxVsyncTicker is a *separate*, render-path-independent tick source: one dedicated background
 * thread per window that blocks on the driver's real vblank counter and reports the observed
 * vsync time back to Kotlin. It never touches the GLX context / drawing-surface-locking machinery
 * that LinuxOpenGLRenderContext's swapBuffers/makeCurrent use (see redrawer.cc) and must stay that way.
 *
 * Both glXWaitForMscOML (GLX_OML_sync_control) and glXGetVideoSyncSGI/glXWaitVideoSyncSGI
 * (GLX_SGI_video_sync) REQUIRE a GLXContext to be current on the calling thread - per their
 * respective Khronos specs, each generates GLX_BAD_CONTEXT if there is no current context, and
 * glXWaitForMscOML/glXWaitVideoSyncSGI additionally require that current context to be *direct*:
 *
 *   "Each of the functions defined by this extension will generate a GLX_BAD_CONTEXT error if
 *   there is no current GLXContext" and "glXWaitForMscOML ... will ... generate a GLX_BAD_CONTEXT
 *   error if the current context is not direct." (GLX_OML_sync_control)
 *
 *   glXGetVideoSyncSGI/glXWaitVideoSyncSGI likewise return GLX_BAD_CONTEXT if there is no current
 *   context (and glXWaitVideoSyncSGI additionally if it isn't direct). Notably these two take NO
 *   Display/GLXDrawable parameters at all - they operate purely on whichever context is current on
 *   the calling thread. (GLX_SGI_video_sync)
 *
 * Without such a current, direct context, every OML/SGI call on the wait thread fails with
 * GLX_BAD_CONTEXT and the loop returns immediately - and does so silently, since nativeStart()
 * reports a "successfully started" non-zero handle to Kotlin before the thread ever runs a single
 * iteration. That is why createDedicatedContext() must obtain and validate one up front.
 *
 * That does NOT mean the wait thread needs (or should have) any involvement with
 * LinuxOpenGLRenderContext's real render/present context: it needs *a* current, direct GLXContext, not
 * *that* context. So this thread creates and makes current its own minimal, dedicated, offscreen
 * GLXContext backed by a 1x1 GLXPbuffer (see createDedicatedContext()) - a context that never
 * renders anything and is never shared with (no share-context) or made current outside this one
 * thread. That keeps this ticker fully decoupled from the render path while still satisfying the
 * spec's "a direct context must be current" requirement.
 *
 * That dedicated context needs its own X11 Display* connection, current on the wait thread for its
 * entire lifetime. It is tempting to reuse the same Display* the render thread and EDT already use
 * (obtained by the Kotlin caller via the JAWT DrawingSurface lock, see AWTLinuxDrawingSurface.kt) -
 * but doing so from a second thread, concurrently with and without coordinating with that thread, is
 * only safe if Xlib's own thread-safety has been enabled via XInitThreads(), which does not happen
 * here: the JDK's own X11 AWT native implementation
 * (src/java.desktop/unix/native/libawt_xawt in the OpenJDK sources) does not call XInitThreads()
 * anywhere - it instead serializes all of its own Xlib access through a private, internal AWT_LOCK
 * mutex that this native code has no access to. Calling XInitThreads() ourselves at this point would
 * not fix that either: Xlib requires XInitThreads() to be the very first Xlib call a process makes,
 * completing before any other Xlib call - and by the time this JNI method runs, AWT has already
 * opened and used its Display connection without it. Calling XInitThreads() this late is exactly the
 * scenario JDK-6293929 (a real JDK bug report) describes as causing hangs, because the already-open
 * Display is left with Xlib locking "on" while its pre-existing internal structures were never
 * initialized for it.
 *
 * Instead, this ticker opens its OWN, second, thread-exclusive Xlib connection to the same X server
 * (same DisplayString) and never lets any thread other than its own dedicated wait thread touch it -
 * this is what satisfies the OML/SGI "a current, direct context" requirement above without adding any
 * traffic to the shared AWT/JAWT connection. That connection is opened once, right before the wait
 * thread starts, and stays open and current on that thread for its entire lifetime, rather than being
 * reopened per tick (see createDedicatedContext() and vsyncThreadProc()) - which keeps how often it
 * calls XOpenDisplay/XCloseDisplay as low as this ticker's own start()/stop() lifecycle allows.
 *
 * That does NOT make this provably safe against Xlib's process-global state, and this file does not
 * claim otherwise: XOpenDisplay() - both the shared AWT connection's original call and this one -
 * touches process-global Xlib bookkeeping guarded by a lock that is a documented no-op unless
 * XInitThreads() has run, and (per the reasoning above) XInitThreads() is never called anywhere in
 * this process. So this second connection does not sidestep or eliminate that shared, unsynchronized
 * state; it is the same already-accepted risk class as this codebase's other unsynchronized
 * second-connection XOpenDisplay uses (drawlayer.cc's getDpiScale(), swingRedrawer.cc's
 * OffScreenContext) - just held open for the wait thread's whole lifetime instead of one-shot, which
 * reduces how often that unsynchronized path is exercised (once per ticker start/stop, not once per
 * tick) without eliminating the exposure. A fully safe design is not achievable from within this file,
 * or this codebase, alone: neither this code nor OpenJDK's own AWT Xlib implementation calls
 * XInitThreads(), and calling it here, this late, would itself be unsafe (see JDK-6293929 above). This
 * is accepted, best-effort scope for Linux vsync precision, not a claim of full correctness. If a
 * truly robust fix is ever needed, it
 * would mean rethinking this whole approach (e.g. not using raw Xlib here at all), not adding more
 * Xlib/GLX surface area on top of it, given the codebase's possible future move away from X11.
 *
 * The only touch this file makes on the *shared* Display connection is a momentary, read-only
 * XDisplayString() call (to learn which server to reconnect to) and the extension-string probe in
 * resolveMode() - both non-blocking, low-risk patterns, unlike the blocking OML/SGI
 * calls this bug is actually about.
 */

typedef Bool (*GetSyncValuesOMLProc)(Display *, GLXDrawable, int64_t *, int64_t *, int64_t *);
typedef Bool (*WaitForMscOMLProc)(Display *, GLXDrawable, int64_t, int64_t, int64_t, int64_t *, int64_t *, int64_t *);
typedef int (*GetVideoSyncSGIProc)(unsigned int *);
typedef int (*WaitVideoSyncSGIProc)(int, int, unsigned int *);

enum class Mode {
    kUnsupported,
    kOml,
    kSgi,
};

struct VsyncTicker {
    // The dedicated, second X11 connection opened by createDedicatedContext() below - used ONLY by
    // this ticker's own wait thread, for its entire lifetime (opened just before the thread starts,
    // closed by the thread itself just before it exits). Never the shared JAWT/AWT display.
    Display *display = nullptr;
    // The *original* window's XID, as passed in from Kotlin (ultimately from the JAWT drawing
    // surface). This is a plain server-side identifier, not a pointer into any Xlib connection
    // state, so - unlike a Display* - it is safe to read from any thread. It is still passed as the
    // `drawable` argument to glXGetSyncValuesOML/glXWaitForMscOML (querying/waiting on the real
    // on-screen window's MSC/UST/SBC), even though those calls are issued on `display` (the
    // dedicated connection) with `context` (the dedicated context) current - the GLX_OML_sync_control
    // spec does not require the current context to be associated with `drawable` in any way, only
    // that *some* current, direct context exists on the calling thread.
    GLXDrawable window = 0;
    Mode mode = Mode::kUnsupported;

    // The dedicated, offscreen, direct GLXContext/GLXPbuffer created by createDedicatedContext() and
    // made current on the wait thread by vsyncThreadProc() before the OML/SGI wait loop starts. See
    // the file-level comment for why these exist and why they are never shared with the render path.
    GLXPbuffer pbuffer = 0;
    GLXContext context = nullptr;

    GetSyncValuesOMLProc getSyncValuesOML = nullptr;
    WaitForMscOMLProc waitForMscOML = nullptr;
    GetVideoSyncSGIProc getVideoSyncSGI = nullptr;
    WaitVideoSyncSGIProc waitVideoSyncSGI = nullptr;

    jobject receiver = nullptr; // global ref, owned by this ticker; released in nativeStop
    pthread_t thread = {};
    std::atomic<bool> stopRequested { false };
};

// Checks the GLX server/client extension string for `name` as a whitespace-delimited token, not
// just a substring match (e.g. "GLX_SGI_video_sync" must not match a hypothetical
// "GLX_SGI_video_sync_v2" some other driver could advertise).
bool hasGlxExtension(Display *dpy, int screen, const char *name) {
    const char *extensions = glXQueryExtensionsString(dpy, screen);
    if (extensions == nullptr) {
        return false;
    }
    const size_t nameLen = strlen(name);
    const char *cursor = extensions;
    while ((cursor = strstr(cursor, name)) != nullptr) {
        const char *end = cursor + nameLen;
        const bool boundedStart = (cursor == extensions) || (*(cursor - 1) == ' ');
        const bool boundedEnd = (*end == ' ') || (*end == '\0');
        if (boundedStart && boundedEnd) {
            return true;
        }
        cursor = end;
    }
    return false;
}

// Resolves the best available tick mechanism for `display`/`screen` (the *shared* display - this is
// purely a read-only capability probe, not one of the blocking wait calls, see the file-level
// comment). Requires both the function pointers to resolve (via glXGetProcAddress, which is a
// client-side capability check only) *and* the corresponding extension to be advertised by the
// server (via glXQueryExtensionsString) - GLX_ARB_get_proc_address is documented to return non-null
// for known entry points regardless of whether the server actually implements them, so the
// proc-address check alone is not sufficient.
//
// This only narrows down *which* extension is advertised. Actually being able to use it also
// requires a dedicated direct GLXContext to be obtainable (see createDedicatedContext(), called
// separately from nativeStart before a thread is started or a handle returned) - both OML and SGI's
// Wait variant require a *direct* current context, which is not guaranteed just because the
// extension string is present (e.g. over an indirect/remote X connection).
void resolveMode(Display *display, int screen, VsyncTicker *ticker) {
    auto getSyncValuesOML = (GetSyncValuesOMLProc) glXGetProcAddress((const GLubyte *) "glXGetSyncValuesOML");
    auto waitForMscOML = (WaitForMscOMLProc) glXGetProcAddress((const GLubyte *) "glXWaitForMscOML");
    if (getSyncValuesOML != nullptr && waitForMscOML != nullptr &&
        hasGlxExtension(display, screen, "GLX_OML_sync_control")) {
        ticker->mode = Mode::kOml;
        ticker->getSyncValuesOML = getSyncValuesOML;
        ticker->waitForMscOML = waitForMscOML;
        return;
    }

    auto getVideoSyncSGI = (GetVideoSyncSGIProc) glXGetProcAddress((const GLubyte *) "glXGetVideoSyncSGI");
    auto waitVideoSyncSGI = (WaitVideoSyncSGIProc) glXGetProcAddress((const GLubyte *) "glXWaitVideoSyncSGI");
    if (getVideoSyncSGI != nullptr && waitVideoSyncSGI != nullptr &&
        hasGlxExtension(display, screen, "GLX_SGI_video_sync")) {
        ticker->mode = Mode::kSgi;
        ticker->getVideoSyncSGI = getVideoSyncSGI;
        ticker->waitVideoSyncSGI = waitVideoSyncSGI;
        return;
    }

    ticker->mode = Mode::kUnsupported;
}

// Opens the dedicated, second X11 connection (see the file-level comment for why) and creates a
// minimal, dedicated, *direct* GLXContext + 1x1 GLXPbuffer on it: this is what both OML and SGI's
// Wait variant require to be current on the calling thread, decoupled from any real window or from
// LinuxOpenGLRenderContext's render/present context (no share-context; never made current anywhere else).
//
// Populates ticker->display/pbuffer/context and returns true only if a genuinely *direct* context
// was obtained. On any failure (connection failure, no pbuffer-capable FBConfig, pbuffer/context
// creation failure, or a non-direct context - e.g. over an indirect/remote X connection) this cleans
// up any partial state it created and returns false with ticker->display/pbuffer/context left at
// their default (null/zero) values, so nativeStart() can report "unsupported" (a 0 handle) instead
// of starting a thread whose very first OML/SGI call is guaranteed to fail with GLX_BAD_CONTEXT.
bool createDedicatedContext(Display *sharedDisplay, VsyncTicker *ticker) {
    // XDisplayString() only reads an immutable field set once at XOpenDisplay() time (the connection
    // name never changes for the lifetime of a Display*), so this is safe to read here even though
    // `sharedDisplay` is otherwise owned by AWT/the render thread - see the file-level comment.
    const char *connectionName = XDisplayString(sharedDisplay);
    Display *dpy = XOpenDisplay(connectionName);
    if (dpy == nullptr) {
        return false;
    }
    const int screen = DefaultScreen(dpy);

    static const int fbAttribs[] = {
        GLX_DRAWABLE_TYPE, GLX_PBUFFER_BIT,
        GLX_RENDER_TYPE, GLX_RGBA_BIT,
        None
    };
    int numConfigs = 0;
    GLXFBConfig *configs = glXChooseFBConfig(dpy, screen, fbAttribs, &numConfigs);
    if (configs == nullptr || numConfigs == 0) {
        if (configs != nullptr) {
            XFree(configs);
        }
        XCloseDisplay(dpy);
        return false;
    }
    GLXFBConfig fbConfig = configs[0];
    XFree(configs);

    static const int pbufferAttribs[] = {
        GLX_PBUFFER_WIDTH, 1,
        GLX_PBUFFER_HEIGHT, 1,
        None
    };
    GLXPbuffer pbuffer = glXCreatePbuffer(dpy, fbConfig, pbufferAttribs);
    if (pbuffer == 0) {
        XCloseDisplay(dpy);
        return false;
    }

    // No share context (nullptr): deliberately decoupled from any render context.
    // direct = True: required by both glXWaitForMscOML and glXWaitVideoSyncSGI (see the spec quotes
    // in the file-level comment above).
    GLXContext context = glXCreateNewContext(dpy, fbConfig, GLX_RGBA_TYPE, nullptr, /* direct */ True);
    if (context == nullptr) {
        glXDestroyPbuffer(dpy, pbuffer);
        XCloseDisplay(dpy);
        return false;
    }

    if (!glXIsDirect(dpy, context)) {
        // Asked for a direct context but didn't get one (e.g. an indirect/remote X connection, or a
        // driver/setup that cannot provide direct rendering at all). Both glXWaitForMscOML and
        // glXWaitVideoSyncSGI require the current context to be direct, so this setup cannot support
        // either mode - report unsupported instead of a context guaranteed to GLX_BAD_CONTEXT.
        glXDestroyContext(dpy, context);
        glXDestroyPbuffer(dpy, pbuffer);
        XCloseDisplay(dpy);
        return false;
    }

    ticker->display = dpy;
    ticker->pbuffer = pbuffer;
    ticker->context = context;
    return true;
}

inline jlong monotonicNanosNow() {
    struct timespec ts = {};
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return static_cast<jlong>(ts.tv_sec) * 1000000000LL + static_cast<jlong>(ts.tv_nsec);
}

void runOmlLoop(VsyncTicker *ticker, JNIEnv *env, jmethodID onTick) {
    int64_t ust, msc, sbc;
    if (!ticker->getSyncValuesOML(ticker->display, ticker->window, &ust, &msc, &sbc)) {
        return; // Driver claimed OML support but the initial (non-blocking) read failed.
    }

    while (!ticker->stopRequested.load(std::memory_order_relaxed)) {
        const int64_t targetMsc = msc + 1;
        // Blocks the calling thread (this thread only) until the vblank with count `targetMsc`.
        // Requires - and relies on - the dedicated, direct GLXContext already made current on this
        // thread by vsyncThreadProc() before this loop starts (see the file-level comment for why).
        // There is no cross-thread cancellation for this call, so nativeStop() can only observe
        // stopRequested once this returns - see its doc.
        const Bool ok = ticker->waitForMscOML(
            ticker->display, ticker->window, targetMsc, /* divisor */ 0, /* remainder */ 0,
            &ust, &msc, &sbc
        );
        if (ticker->stopRequested.load(std::memory_order_relaxed)) {
            break;
        }
        if (!ok) {
            break; // Driver failure; stop ticking rather than spin-retry indefinitely.
        }

        // UST is a 64-bit, monotonically non-decreasing microsecond timestamp (GLX_OML_sync_control spec).
        const jlong vsyncTimeNanos = static_cast<jlong>(ust) * 1000LL;
        env->CallVoidMethod(ticker->receiver, onTick, vsyncTimeNanos);
        if (env->ExceptionCheck()) {
            // Don't let an exception thrown from the Kotlin callback tear down this thread's JNI
            // state; just drop it and keep ticking.
            env->ExceptionClear();
        }
    }
}

void runSgiLoop(VsyncTicker *ticker, JNIEnv *env, jmethodID onTick) {
    unsigned int count;
    if (ticker->getVideoSyncSGI(&count) != 0) {
        return; // Driver claimed SGI support but the initial (non-blocking) read failed.
    }

    while (!ticker->stopRequested.load(std::memory_order_relaxed)) {
        // divisor=2 with the complementary parity of `count` guarantees this waits for a genuinely
        // new vblank (rather than e.g. divisor=1/remainder=0, which some drivers treat as
        // trivially satisfied by the current count and so may return immediately).
        //
        // Like glXWaitForMscOML above, this requires - and relies on - the dedicated, direct
        // GLXContext already made current on this thread by vsyncThreadProc(); unlike OML, this call
        // takes no Display/GLXDrawable arguments at all, operating purely on whatever context is
        // current on the calling thread.
        const int target = static_cast<int>((count + 1) % 2);
        const int rc = ticker->waitVideoSyncSGI(2, target, &count);
        if (ticker->stopRequested.load(std::memory_order_relaxed)) {
            break;
        }
        if (rc != 0) {
            break; // Driver failure; stop ticking rather than spin-retry indefinitely.
        }

        // GLX_SGI_video_sync has no UST-equivalent timestamp output; approximate the vsync time
        // with the wall clock at the moment the blocking wait returned.
        env->CallVoidMethod(ticker->receiver, onTick, monotonicNanosNow());
        if (env->ExceptionCheck()) {
            env->ExceptionClear();
        }
    }
}

void *vsyncThreadProc(void *param) {
    VsyncTicker *ticker = static_cast<VsyncTicker *>(param);

    // Make this thread's dedicated, direct, offscreen GLXContext current *on this thread only*,
    // before doing anything else GLX-related. This is what glXWaitForMscOML/glXGetVideoSyncSGI/
    // glXWaitVideoSyncSGI require - see the file-level comment and createDedicatedContext(), which
    // already validated that this exact context/pbuffer pair is usable and direct before this
    // thread was ever started.
    if (!glXMakeContextCurrent(ticker->display, ticker->pbuffer, ticker->pbuffer, ticker->context)) {
        // Should not happen given createDedicatedContext() already validated this pair; if it
        // somehow does, fail closed (never tick) rather than issuing OML/SGI calls that are
        // guaranteed to GLX_BAD_CONTEXT without a current context. Nothing was made current, so
        // there is no context to release, but the context/pbuffer/connection this thread owns
        // still need tearing down - mirror the normal-exit teardown below rather than leaking them.
        glXDestroyContext(ticker->display, ticker->context);
        glXDestroyPbuffer(ticker->display, ticker->pbuffer);
        XCloseDisplay(ticker->display);
        return nullptr;
    }

    JNIEnv *env = nullptr;
    if (jvm->AttachCurrentThreadAsDaemon(reinterpret_cast<void **>(&env), nullptr) != JNI_OK || env == nullptr) {
        // Same as above: release the context that was just made current, then tear down the
        // context/pbuffer/connection this thread owns - mirror the normal-exit teardown below
        // rather than leaking them.
        glXMakeContextCurrent(ticker->display, None, None, nullptr);
        glXDestroyContext(ticker->display, ticker->context);
        glXDestroyPbuffer(ticker->display, ticker->pbuffer);
        XCloseDisplay(ticker->display);
        return nullptr;
    }

    // Deliberately NOT a cached FindClass with a hardcoded class name: a stale cached jclass whose
    // class name disagrees with the loaded class aborts the JVM. GetObjectClass on the
    // live receiver instance always resolves the class that is actually loaded. Same pattern as
    // awtMain/windows/VsyncTicker.cc's vsyncThreadProc.
    jclass receiverClass = env->GetObjectClass(ticker->receiver);
    jmethodID onTick = env->GetMethodID(receiverClass, "onVsyncTick", "(J)V");
    env->DeleteLocalRef(receiverClass);

    if (onTick != nullptr) {
        if (ticker->mode == Mode::kOml) {
            runOmlLoop(ticker, env, onTick);
        } else if (ticker->mode == Mode::kSgi) {
            runSgiLoop(ticker, env, onTick);
        }
    } else {
        env->ExceptionClear();
    }

    jvm->DetachCurrentThread();

    // Release and tear down this thread's dedicated context/pbuffer/connection. All of it is
    // exclusively owned by this thread (see the file-level comment), so it is safe to do here,
    // without any lock, right before the thread exits.
    glXMakeContextCurrent(ticker->display, None, None, nullptr);
    glXDestroyContext(ticker->display, ticker->context);
    glXDestroyPbuffer(ticker->display, ticker->pbuffer);
    XCloseDisplay(ticker->display);

    return nullptr;
}

} // namespace

extern "C"
{
    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_linux_LinuxVsyncTicker_nativeStart(
        JNIEnv *env, jobject self, jlong displayPtr, jlong windowPtr)
    {
        Display *sharedDisplay = fromJavaPointer<Display *>(displayPtr);
        if (sharedDisplay == nullptr) {
            return 0;
        }
        GLXDrawable window = fromJavaPointer<GLXDrawable>(windowPtr);

        VsyncTicker *ticker = new VsyncTicker();
        ticker->window = window;
        resolveMode(sharedDisplay, DefaultScreen(sharedDisplay), ticker);

        if (ticker->mode == Mode::kUnsupported) {
            // Fail gracefully: neither GLX_OML_sync_control nor GLX_SGI_video_sync is advertised.
            // Report "unsupported" via a 0 handle instead of starting a thread that would never tick.
            delete ticker;
            return 0;
        }

        if (!createDedicatedContext(sharedDisplay, ticker)) {
            // Extensions are advertised, but a dedicated *direct* GLXContext could not be obtained
            // (e.g. no pbuffer-capable FBConfig, or an indirect/remote X connection that cannot
            // provide direct rendering). Both OML and SGI's Wait variant require a direct current
            // context, so neither mode can actually work here - report "unsupported" rather than a
            // handle that will silently tick zero times forever. See createDedicatedContext().
            delete ticker;
            return 0;
        }

        ticker->receiver = env->NewGlobalRef(self);

        int rc = pthread_create(&ticker->thread, nullptr, vsyncThreadProc, ticker);
        if (rc != 0) {
            env->DeleteGlobalRef(ticker->receiver);
            // No thread was ever started, so this (the calling) thread is still the sole owner of
            // ticker->display/pbuffer/context at this point - safe to tear them down here directly.
            glXDestroyContext(ticker->display, ticker->context);
            glXDestroyPbuffer(ticker->display, ticker->pbuffer);
            XCloseDisplay(ticker->display);
            delete ticker;
            return 0;
        }

        return toJavaPointer(ticker);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_linux_LinuxVsyncTicker_nativeStop(
        JNIEnv *env, jobject self, jlong tickerHandle)
    {
        if (tickerHandle == 0) {
            return;
        }
        VsyncTicker *ticker = fromJavaPointer<VsyncTicker *>(tickerHandle);

        ticker->stopRequested.store(true, std::memory_order_relaxed);
        // Unlike the Windows sibling (which has a waitable stopEvent to cancel the wait early),
        // neither glXWaitForMscOML nor glXWaitVideoSyncSGI has a cross-thread cancellation
        // primitive, so this can only observe stopRequested once the in-flight blocking wait call
        // returns (at most one vblank period later, absent a driver stall). The wait thread tears
        // down its own dedicated GLX context/pbuffer/display connection itself, right before it
        // exits (see vsyncThreadProc) - there is nothing GLX-related left for this call to clean up.
        pthread_join(ticker->thread, nullptr);

        env->DeleteGlobalRef(ticker->receiver);
        delete ticker;
    }
}
