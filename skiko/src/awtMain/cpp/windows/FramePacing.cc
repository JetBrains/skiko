#include <windows.h>

#include <dwmapi.h>
#include <dxgi.h>
#include <stdlib.h>
#include <wchar.h>

#include <jni.h>

/*
 * Skiko-owned pacing clocks, delivering ticks to
 * org.jetbrains.skiko.swing.WinNativeClock.onNativeTick from a daemon-attached
 * native thread. QPC timestamps are converted to the System.nanoTime() time
 * base (also QPC derived).
 *
 * Preferred source is IDXGIOutput::WaitForVBlank on the output belonging to the
 * clock's display: a genuine per-display hardware vblank, so each display is
 * paced at its own true refresh rate.
 *
 * Fallback source is DWM composition timing, which carries a single
 * desktop-wide cadence (DWM composes at the fastest connected display's rate),
 * waited out with a high-resolution waitable timer. Deliberately never calls
 * DwmFlush: in-process it can block for hundreds of milliseconds when the
 * client gates presents on the same tick.
 *
 * Displays are looked up by EnumDisplayMonitors index: AWT's Windows screen
 * indices follow the same enumeration order, so a screen index is enough to
 * find the matching output.
 */

typedef HRESULT (WINAPI *DwmGetCompositionTimingInfoType)(HWND, DWM_TIMING_INFO *);
typedef HRESULT (WINAPI *CreateDXGIFactory1Type)(REFIID, void **);

static DwmGetCompositionTimingInfoType pDwmGetCompositionTimingInfo = NULL;
static CreateDXGIFactory1Type pCreateDXGIFactory1 = NULL;
static JavaVM *framePacingJvm = NULL;
static jmethodID framePacingOnNativeTickMID = NULL;
static LONGLONG framePacingQpcFrequency = 0;

typedef struct {
    HANDLE thread;
    HANDLE stopEvent;
    jobject clockRef;
    jlong fallbackPeriodNanos;
    IDXGIOutput *output; // NULL for the DWM composition clock
} FramePacingClock;

static BOOL loadDwm()
{
    if (pDwmGetCompositionTimingInfo != NULL) {
        return TRUE;
    }
    HMODULE dwm = LoadLibraryW(L"dwmapi.dll");
    if (dwm == NULL) {
        return FALSE;
    }
    pDwmGetCompositionTimingInfo =
            (DwmGetCompositionTimingInfoType)GetProcAddress(dwm, "DwmGetCompositionTimingInfo");
    return pDwmGetCompositionTimingInfo != NULL;
}

static BOOL loadDxgi()
{
    if (pCreateDXGIFactory1 != NULL) {
        return TRUE;
    }
    HMODULE dxgi = LoadLibraryW(L"dxgi.dll");
    if (dxgi == NULL) {
        return FALSE;
    }
    pCreateDXGIFactory1 = (CreateDXGIFactory1Type)GetProcAddress(dxgi, "CreateDXGIFactory1");
    return pCreateDXGIFactory1 != NULL;
}

typedef struct {
    int index;
    int target;
    HMONITOR found;
} MonitorSearch;

static BOOL CALLBACK monitorEnumProc(HMONITOR monitor, HDC hdc, LPRECT rect, LPARAM param)
{
    MonitorSearch *search = (MonitorSearch *)param;
    if (search->index++ == search->target) {
        search->found = monitor;
        return FALSE;
    }
    return TRUE;
}

/*
 * Win32 display device name ("\\.\DISPLAY1") for an AWT screen index. This is
 * what DXGI_OUTPUT_DESC.DeviceName carries, and matching on it avoids
 * comparing desktop rectangles, which would have to account for per-monitor
 * DPI scaling to be correct. AWT's screen indices follow EnumDisplayMonitors
 * order, so the same enumeration recovers the HMONITOR.
 */
static BOOL monitorDeviceName(int screen, WCHAR *name, size_t nameChars)
{
    MonitorSearch search;
    search.index = 0;
    search.target = screen;
    search.found = NULL;
    ::EnumDisplayMonitors(NULL, NULL, monitorEnumProc, (LPARAM)&search);
    if (search.found == NULL) {
        return FALSE;
    }

    MONITORINFOEXW info;
    ZeroMemory(&info, sizeof(info));
    info.cbSize = sizeof(info);
    if (!::GetMonitorInfoW(search.found, (LPMONITORINFO)&info)) {
        return FALSE;
    }

    return wcscpy_s(name, nameChars, info.szDevice) == 0;
}

/* Returns the output with a reference held, or NULL. */
static IDXGIOutput *findOutput(const WCHAR *deviceName)
{
    if (!loadDxgi()) {
        return NULL;
    }

    IDXGIFactory1 *factory = NULL;
    if (FAILED(pCreateDXGIFactory1(__uuidof(IDXGIFactory1), (void **)&factory))) {
        return NULL;
    }

    IDXGIOutput *found = NULL;
    IDXGIAdapter1 *adapter = NULL;
    for (UINT ai = 0;
            found == NULL && factory->EnumAdapters1(ai, &adapter) != DXGI_ERROR_NOT_FOUND;
            ai++) {
        IDXGIOutput *output = NULL;
        for (UINT oi = 0; adapter->EnumOutputs(oi, &output) != DXGI_ERROR_NOT_FOUND; oi++) {
            DXGI_OUTPUT_DESC desc;
            if (SUCCEEDED(output->GetDesc(&desc)) && desc.AttachedToDesktop &&
                    wcscmp(desc.DeviceName, deviceName) == 0) {
                found = output; // keep this one's reference
                break;
            }
            output->Release();
        }
        adapter->Release();
    }

    factory->Release();
    return found;
}

static HANDLE createHighResolutionTimer()
{
    HANDLE timer = CreateWaitableTimerExW(NULL, NULL,
            CREATE_WAITABLE_TIMER_HIGH_RESOLUTION, TIMER_ALL_ACCESS);
    if (timer == NULL) {
        timer = CreateWaitableTimerExW(NULL, NULL, 0, TIMER_ALL_ACCESS);
    }
    return timer;
}

static void deliverTick(JNIEnv *env, FramePacingClock *clock, LONGLONG tickQpc)
{
    jlong nanos = (jlong)((double)tickQpc * 1000000000.0 / (double)framePacingQpcFrequency);
    env->CallVoidMethod(clock->clockRef, framePacingOnNativeTickMID, nanos);
    if (env->ExceptionCheck()) {
        env->ExceptionClear();
    }
}

/* Waits waitQpc counts, or until stopped. Returns FALSE when stopped. */
static BOOL waitQpcOrStop(FramePacingClock *clock, HANDLE timer, LONGLONG waitQpc)
{
    LARGE_INTEGER due;
    due.QuadPart = -(waitQpc * 10000000LL / framePacingQpcFrequency); // relative, 100 ns
    if (due.QuadPart >= 0) {
        due.QuadPart = -1;
    }
    if (!SetWaitableTimer(timer, &due, 0, NULL, NULL, FALSE)) {
        // With no armed timer the wait below would park until the clock stops.
        // Fall back to a millisecond-resolution wait on the stop event, which
        // keeps roughly the right cadence.
        DWORD millis = (DWORD)(-due.QuadPart / 10000LL);
        return WaitForSingleObject(clock->stopEvent, millis > 0 ? millis : 1) == WAIT_TIMEOUT;
    }

    HANDLE handles[2] = { clock->stopEvent, timer };
    return WaitForMultipleObjects(2, handles, FALSE, INFINITE) == WAIT_OBJECT_0 + 1;
}

static DWORD WINAPI vblankThreadProc(LPVOID param)
{
    FramePacingClock *clock = (FramePacingClock *)param;
    JNIEnv *env;
    if (framePacingJvm->AttachCurrentThreadAsDaemon((void **)&env, NULL) != JNI_OK) {
        return 0;
    }

    HANDLE timer = createHighResolutionTimer();
    if (timer == NULL) {
        return 0;
    }

    LONGLONG fallbackQpc = clock->fallbackPeriodNanos * framePacingQpcFrequency / 1000000000LL;
    if (fallbackQpc <= 0) {
        fallbackQpc = framePacingQpcFrequency / 60;
    }
    /*
     * A display in power save stays attached to the desktop but stops scanning
     * out, and WaitForVBlank then returns immediately and successfully rather
     * than failing — left alone the loop spins at over a million ticks a
     * second, burning a core to save GPU watts. No real display ticks at twice
     * its nominal rate, so anything faster is not a vblank; pace off the
     * nominal period until real ones resume. The threshold has to be this
     * loose because a true refresh rate legitimately runs a little faster than
     * the nominal one (a "59 Hz" panel scans out at 59.95 Hz).
     */
    const LONGLONG minIntervalQpc = fallbackQpc / 2;

    LARGE_INTEGER lastTick;
    QueryPerformanceCounter(&lastTick);

    /*
     * The reference stays owned by the clock struct and is released in
     * nativeRelease, after this thread has been joined. Releasing it here
     * instead would double-release whenever that join times out.
     */
    IDXGIOutput *output = clock->output;
    BOOL vblankUsable = output != NULL;

    for (;;) {
        if (WaitForSingleObject(clock->stopEvent, 0) == WAIT_OBJECT_0) {
            break;
        }

        if (vblankUsable) {
            if (FAILED(output->WaitForVBlank())) {
                /*
                 * The output is gone (display removed, adapter reset). Drop to
                 * the nominal period rather than stopping: a clock that stops
                 * ticking would starve its subscribers until the pacer's
                 * timeout notices; a nominal-rate clock keeps them paced.
                 */
                vblankUsable = FALSE;
                continue;
            }
            // WaitForVBlank is not interruptible, so re-check before delivering.
            if (WaitForSingleObject(clock->stopEvent, 0) == WAIT_OBJECT_0) {
                break;
            }

            LARGE_INTEGER now;
            QueryPerformanceCounter(&now);
            LONGLONG elapsed = now.QuadPart - lastTick.QuadPart;
            if (elapsed < minIntervalQpc &&
                    !waitQpcOrStop(clock, timer, fallbackQpc - elapsed)) {
                break;
            }
        } else if (!waitQpcOrStop(clock, timer, fallbackQpc)) {
            break;
        }

        QueryPerformanceCounter(&lastTick);
        deliverTick(env, clock, lastTick.QuadPart);
    }

    CloseHandle(timer);
    return 0;
}

static DWORD WINAPI compositionThreadProc(LPVOID param)
{
    FramePacingClock *clock = (FramePacingClock *)param;
    JNIEnv *env;
    if (framePacingJvm->AttachCurrentThreadAsDaemon((void **)&env, NULL) != JNI_OK) {
        return 0;
    }

    HANDLE timer = createHighResolutionTimer();
    if (timer == NULL) {
        return 0;
    }

    LONGLONG fallbackQpc = clock->fallbackPeriodNanos * framePacingQpcFrequency / 1000000000LL;
    if (fallbackQpc <= 0) {
        fallbackQpc = framePacingQpcFrequency / 60;
    }

    for (;;) {
        LARGE_INTEGER now;
        QueryPerformanceCounter(&now);

        LONGLONG waitQpc;
        DWM_TIMING_INFO timing;
        ZeroMemory(&timing, sizeof(timing));
        timing.cbSize = sizeof(timing);
        if (pDwmGetCompositionTimingInfo != NULL &&
                pDwmGetCompositionTimingInfo(NULL, &timing) == S_OK &&
                timing.qpcRefreshPeriod > 0) {
            // Wait to the next vblank boundary after "now".
            LONGLONG period = (LONGLONG)timing.qpcRefreshPeriod;
            LONGLONG sinceVBlank = now.QuadPart - (LONGLONG)timing.qpcVBlank;
            LONGLONG intoPeriod = sinceVBlank % period;
            if (intoPeriod < 0) {
                intoPeriod += period;
            }
            waitQpc = period - intoPeriod;
        } else {
            // DWM unavailable mid-run: keep an estimated cadence.
            waitQpc = fallbackQpc;
        }

        if (!waitQpcOrStop(clock, timer, waitQpc)) {
            break; // stop event or failure
        }

        LARGE_INTEGER tickTime;
        QueryPerformanceCounter(&tickTime);
        deliverTick(env, clock, tickTime.QuadPart);
    }

    CloseHandle(timer);
    return 0;
}

static BOOL initShared(JNIEnv *env, jobject clockObj)
{
    if (framePacingJvm == NULL) {
        if (env->GetJavaVM(&framePacingJvm) != JNI_OK) {
            return FALSE;
        }
        LARGE_INTEGER freq;
        QueryPerformanceFrequency(&freq);
        framePacingQpcFrequency = freq.QuadPart;
    }
    if (framePacingOnNativeTickMID == NULL) {
        // Both clock flavors are org.jetbrains.skiko.swing.WinNativeClock, so
        // one cached id stays valid for every clock kind.
        jclass clockClass = env->GetObjectClass(clockObj);
        framePacingOnNativeTickMID = env->GetMethodID(clockClass, "onNativeTick", "(J)V");
        if (framePacingOnNativeTickMID == NULL) {
            env->ExceptionClear();
            return FALSE;
        }
    }
    return TRUE;
}

static FramePacingClock *allocClock(JNIEnv *env, jobject clockObj,
                                    jlong fallbackPeriodNanos, IDXGIOutput *output)
{
    FramePacingClock *clock = (FramePacingClock *)malloc(sizeof(FramePacingClock));
    if (clock == NULL) {
        return NULL;
    }
    clock->thread = NULL;
    clock->output = output;
    clock->fallbackPeriodNanos = fallbackPeriodNanos;
    clock->stopEvent = CreateEventW(NULL, TRUE, FALSE, NULL);
    if (clock->stopEvent == NULL) {
        free(clock);
        return NULL;
    }
    clock->clockRef = env->NewGlobalRef(clockObj);
    if (clock->clockRef == NULL) {
        CloseHandle(clock->stopEvent);
        free(clock);
        return NULL;
    }
    return clock;
}

extern "C" {

JNIEXPORT jboolean JNICALL
Java_org_jetbrains_skiko_swing_WinNativeClock_nativeProbeVBlank(JNIEnv *env, jclass cls)
{
    if (!loadDxgi()) {
        return JNI_FALSE;
    }

    IDXGIFactory1 *factory = NULL;
    if (FAILED(pCreateDXGIFactory1(__uuidof(IDXGIFactory1), (void **)&factory))) {
        return JNI_FALSE;
    }

    // Available means at least one output is attached to the desktop; a remote
    // session enumerates adapters but no attached outputs.
    jboolean available = JNI_FALSE;
    IDXGIAdapter1 *adapter = NULL;
    for (UINT ai = 0;
            !available && factory->EnumAdapters1(ai, &adapter) != DXGI_ERROR_NOT_FOUND;
            ai++) {
        IDXGIOutput *output = NULL;
        for (UINT oi = 0; adapter->EnumOutputs(oi, &output) != DXGI_ERROR_NOT_FOUND; oi++) {
            DXGI_OUTPUT_DESC desc;
            if (SUCCEEDED(output->GetDesc(&desc)) && desc.AttachedToDesktop) {
                available = JNI_TRUE;
            }
            output->Release();
            if (available) {
                break;
            }
        }
        adapter->Release();
    }

    factory->Release();
    return available;
}

JNIEXPORT jlong JNICALL
Java_org_jetbrains_skiko_swing_WinNativeClock_nativeCreate(JNIEnv *env, jclass cls,
                                                           jobject clockObj, jlong fallbackPeriodNanos)
{
    // DWM is optional here: without it the clock thread paces at the nominal
    // period on the high-resolution waitable timer, which is still far better
    // than a JVM-side wait (stock JVMs quantize to the ~16 ms system timer).
    loadDwm();
    if (!initShared(env, clockObj)) {
        return 0;
    }
    return (jlong)(intptr_t)allocClock(env, clockObj, fallbackPeriodNanos, NULL);
}

JNIEXPORT jlong JNICALL
Java_org_jetbrains_skiko_swing_WinNativeClock_nativeCreateVBlank(JNIEnv *env, jclass cls, jobject clockObj,
                                                                 jint screen, jlong fallbackPeriodNanos)
{
    if (!initShared(env, clockObj)) {
        return 0;
    }

    WCHAR deviceName[CCHDEVICENAME];
    if (!monitorDeviceName((int)screen, deviceName, CCHDEVICENAME)) {
        return 0;
    }

    // Allocated before the output is acquired so that a failed allocation
    // cannot strand the output's reference.
    FramePacingClock *clock = allocClock(env, clockObj, fallbackPeriodNanos, NULL);
    if (clock == NULL) {
        return 0;
    }

    clock->output = findOutput(deviceName);
    if (clock->output == NULL) {
        CloseHandle(clock->stopEvent);
        env->DeleteGlobalRef(clock->clockRef);
        free(clock);
        return 0;
    }

    return (jlong)(intptr_t)clock;
}

JNIEXPORT void JNICALL
Java_org_jetbrains_skiko_swing_WinNativeClock_nativeStart(JNIEnv *env, jclass cls, jlong ptr)
{
    FramePacingClock *clock = (FramePacingClock *)(intptr_t)ptr;
    clock->thread = CreateThread(NULL, 0,
            clock->output != NULL ? vblankThreadProc : compositionThreadProc,
            clock, 0, NULL);
}

JNIEXPORT void JNICALL
Java_org_jetbrains_skiko_swing_WinNativeClock_nativeStop(JNIEnv *env, jclass cls, jlong ptr)
{
    FramePacingClock *clock = (FramePacingClock *)(intptr_t)ptr;
    SetEvent(clock->stopEvent);
}

JNIEXPORT void JNICALL
Java_org_jetbrains_skiko_swing_WinNativeClock_nativeRelease(JNIEnv *env, jclass cls, jlong ptr)
{
    FramePacingClock *clock = (FramePacingClock *)(intptr_t)ptr;
    if (clock->thread != NULL) {
        // The thread exits promptly once the stop event is set (at most one
        // frame wait); bound the join so release stays effectively brief.
        DWORD joined = WaitForSingleObject(clock->thread, 1000);
        CloseHandle(clock->thread);
        if (joined != WAIT_OBJECT_0) {
            /*
             * The thread is still inside WaitForVBlank, which is not
             * interruptible and can hold for as long as the driver takes to
             * recover from a GPU reset. Leak the clock rather than free it:
             * closing the stop event under a waiting thread lets the handle
             * value be reused for something else, and releasing the output or
             * freeing the struct would be a use-after-free. The clock is
             * already stopped, so the leaked thread delivers nothing once its
             * wait returns.
             */
            return;
        }
    }
    if (clock->output != NULL) {
        clock->output->Release();
    }
    CloseHandle(clock->stopEvent);
    env->DeleteGlobalRef(clock->clockRef);
    free(clock);
}

} // extern "C"
