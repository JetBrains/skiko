#if SK_BUILD_FOR_WIN

#include <windows.h>
#include <dwmapi.h>
#include <atomic>

#include "jni_helpers.h"

// Defined in skiko/src/jvmMain/cpp/common/impl/Library.cc's JNI_OnLoad. All the cpp/objc
// source sets (jvmMain/common, awtMain/common, awtMain/<os>) are linked into a single
// "skiko-<hostId>" shared library (see Library.kt), so this extern resolves at link time even
// though it is defined in a different source set. Same pattern as
// awtMain/objectiveC/macos/MetalRedrawer.mm's `extern JavaVM *jvm;`.
extern "C" JavaVM *jvm;

namespace {

/*
 * Windows has no EDT-deliverable vsync callback: today vsync is fused into the dwmFlush/D3D
 * swap() calls that happen as part of rendering a frame (see Java_..._Direct3DRedrawer_swap in
 * directXRedrawer.cc). VsyncTicker is a *separate*, render-path-independent tick source: one
 * dedicated background thread per window that predicts the next compositor vblank and reports
 * the predicted present timestamp back to Kotlin. It never touches the DirectXDevice/swap chain
 * machinery and must stay that way.
 */
struct VsyncTicker {
    HWND hwnd = nullptr;
    jobject receiver = nullptr; // global ref, owned by this ticker; released in nativeStop
    HANDLE thread = nullptr;
    HANDLE stopEvent = nullptr;
    std::atomic<bool> stopRequested { false };
};

// DwmGetCompositionTimingInfo can fail to produce timing (e.g. DWM composition is off, or a
// window is in exclusive fullscreen). Fall back to a fixed-interval poll in that case so the
// tick source keeps making forward progress instead of stalling forever.
constexpr DWORD kFallbackIntervalMs = 16; // ~60Hz

inline jlong qpcToNanos(LONGLONG ticks, LONGLONG frequency) {
    // Split the multiply to avoid overflowing a 64-bit intermediate for large tick counts
    // (ticks can be ~1e13+ after weeks of uptime on a >=10MHz QPC counter).
    LONGLONG wholeSeconds = ticks / frequency;
    LONGLONG remainderTicks = ticks % frequency;
    LONGLONG nanos = wholeSeconds * 1000000000LL + (remainderTicks * 1000000000LL) / frequency;
    return static_cast<jlong>(nanos);
}

// Predicts the QPC timestamp of the next vblank strictly after `nowQpc`. Returns false if DWM
// composition timing info could not be obtained.
bool predictNextVBlank(HWND hwnd, LONGLONG nowQpc, LONGLONG &outPredictedQpc) {
    DWM_TIMING_INFO info = {};
    info.cbSize = sizeof(info);
    // hwnd is passed through for forward-compatibility; as of current Windows versions DWM
    // reports a single global composition timer regardless of which window is passed in.
    if (FAILED(DwmGetCompositionTimingInfo(hwnd, &info)) || info.qpcRefreshPeriod == 0) {
        return false;
    }
    LONGLONG predicted = static_cast<LONGLONG>(info.qpcVBlank);
    const LONGLONG period = static_cast<LONGLONG>(info.qpcRefreshPeriod);
    while (predicted <= nowQpc) {
        predicted += period;
    }
    outPredictedQpc = predicted;
    return true;
}

DWORD WINAPI vsyncThreadProc(LPVOID param) {
    VsyncTicker *ticker = static_cast<VsyncTicker *>(param);

    JNIEnv *env = nullptr;
    if (jvm->AttachCurrentThreadAsDaemon(reinterpret_cast<void **>(&env), nullptr) != JNI_OK || env == nullptr) {
        return 0;
    }

    // Deliberately NOT env->FindClass("org/jetbrains/skiko/windows/WindowsVsyncTicker") cached
    // in a static: FindClass with a hardcoded class name aborts the JVM if that name ever drifts
    // out of sync with the loaded class. GetObjectClass on the live receiver instance always resolves
    // the class that is actually loaded, the same way MetalRedrawer.mm's
    // getOnOcclusionStateChangedMethodID does for the analogous macOS callback.
    jclass receiverClass = env->GetObjectClass(ticker->receiver);
    jmethodID onTick = env->GetMethodID(receiverClass, "onVsyncTick", "(J)V");
    env->DeleteLocalRef(receiverClass);

    if (onTick == nullptr) {
        env->ExceptionClear();
        jvm->DetachCurrentThread();
        return 0;
    }

    LARGE_INTEGER freq;
    QueryPerformanceFrequency(&freq);

    while (!ticker->stopRequested.load(std::memory_order_relaxed)) {
        LARGE_INTEGER now;
        QueryPerformanceCounter(&now);

        LONGLONG predictedQpc;
        DWORD waitMillis;
        if (predictNextVBlank(ticker->hwnd, now.QuadPart, predictedQpc)) {
            const LONGLONG waitTicks = predictedQpc - now.QuadPart;
            const double waitMs = (double) waitTicks * 1000.0 / (double) freq.QuadPart;
            waitMillis = waitMs > 1.0 ? static_cast<DWORD>(waitMs + 0.5) : 1;
        } else {
            predictedQpc = now.QuadPart + (freq.QuadPart * kFallbackIntervalMs) / 1000;
            waitMillis = kFallbackIntervalMs;
        }

        // Waitable-object wait: doubles as the sleep-until-next-vblank and as the stop signal,
        // so stop() returns promptly instead of waiting out a full refresh period.
        DWORD waitResult = WaitForSingleObject(ticker->stopEvent, waitMillis);
        if (waitResult == WAIT_OBJECT_0) {
            break; // nativeStop() signaled stopEvent
        }

        jlong predictedNanos = qpcToNanos(predictedQpc, freq.QuadPart);
        env->CallVoidMethod(ticker->receiver, onTick, predictedNanos);
        if (env->ExceptionCheck()) {
            // Don't let an exception thrown from the Kotlin callback tear down this thread's
            // JNI state; just drop it and keep ticking.
            env->ExceptionClear();
        }
    }

    jvm->DetachCurrentThread();
    return 0;
}

} // namespace

extern "C"
{
    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_windows_WindowsVsyncTicker_nativeStart(
        JNIEnv *env, jobject self, jlong windowHandle)
    {
        HWND hwnd = fromJavaPointer<HWND>(windowHandle);

        HANDLE stopEvent = CreateEventW(nullptr, /* bManualReset */ TRUE, /* bInitialState */ FALSE, nullptr);
        if (stopEvent == nullptr) {
            return 0;
        }

        VsyncTicker *ticker = new VsyncTicker();
        ticker->hwnd = hwnd;
        ticker->receiver = env->NewGlobalRef(self);
        ticker->stopEvent = stopEvent;

        HANDLE thread = CreateThread(nullptr, 0, vsyncThreadProc, ticker, 0, nullptr);
        if (thread == nullptr) {
            env->DeleteGlobalRef(ticker->receiver);
            CloseHandle(stopEvent);
            delete ticker;
            return 0;
        }
        ticker->thread = thread;

        return toJavaPointer(ticker);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_windows_WindowsVsyncTicker_nativeStop(
        JNIEnv *env, jobject self, jlong tickerHandle)
    {
        if (tickerHandle == 0) {
            return;
        }
        VsyncTicker *ticker = fromJavaPointer<VsyncTicker *>(tickerHandle);

        ticker->stopRequested.store(true, std::memory_order_relaxed);
        SetEvent(ticker->stopEvent);
        WaitForSingleObject(ticker->thread, INFINITE);

        CloseHandle(ticker->thread);
        CloseHandle(ticker->stopEvent);
        env->DeleteGlobalRef(ticker->receiver);
        delete ticker;
    }
}

#endif
