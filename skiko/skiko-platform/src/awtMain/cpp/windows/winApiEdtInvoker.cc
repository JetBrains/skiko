// Native backing for org.jetbrains.skiko.renderer.WinApiEdtInvoker.

#include <Windows.h>
#include "jni_helpers.h"
#include "winApiEdtInvoker.h"

static thread_local bool tlsPumpingEdt = false;

bool isPumpingEdt() { return tlsPumpingEdt; }

// Far above any real resize frame, so only a pathological block on the EDT reaches it — render code raising a modal
// dialog, say, whose own event loop won't return until dismissed. Backing out lets the toolkit thread resume its
// normal full-input loop and service that dialog, degrading to a stutter rather than a hang.
static const DWORD kPumpTimeoutMs = 1000;

extern "C"
{
    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_renderer_WinApiEdtInvoker_preparePumping(
        JNIEnv *env, jobject invoker)
    {
        return toJavaPointer(CreateEventW(nullptr, /*manualReset*/ FALSE, /*initialState*/ FALSE, nullptr));
    }

    // The Win32 analog of the nested CFRunLoop in the macOS LWCToolkit.invokeAndWait.
    //
    // Handle SENT and POSTED messages until `doneEventPtr` is signalled to stop.
    // The EDT can send POSTED messages (focus/IME) back here and wait for them.
    // Do not handle input messages as they are handled by the parent event loop. Handling them here would, for example,
    // stop the window following the cursor in a live-resize session.
    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_renderer_WinApiEdtInvoker_pumpUntilDone(
        JNIEnv *env, jobject invoker, jlong doneEventPtr)
    {
        HANDLE doneEvent = fromJavaPointer<HANDLE>(doneEventPtr);
        tlsPumpingEdt = true;
        bool completed = false;
        ULONGLONG deadline = GetTickCount64() + kPumpTimeoutMs;
        for (;;)
        {
            ULONGLONG now = GetTickCount64();
            DWORD waitMs = now >= deadline ? 0 : (DWORD)(deadline - now);
            DWORD r = MsgWaitForMultipleObjectsEx(1, &doneEvent, waitMs, QS_SENDMESSAGE | QS_POSTMESSAGE, MWMO_INPUTAVAILABLE);
            if (r == WAIT_OBJECT_0) { completed = true; break; }
            if (r == WAIT_TIMEOUT) break;
            MSG msg;
            bool quitting = false;
            // PM_QS_SENDMESSAGE is REQUIRED, not redundant: naming any PM_QS_* value switches PeekMessage from
            // "process every class" to "process only these", so PM_QS_POSTMESSAGE alone never dispatches a pending
            // SENT message. It just leaves QS_SENDMESSAGE set, which re-wakes the wait above immediately
            // (MWMO_INPUTAVAILABLE) — a 100%-CPU spin with the sender blocked until the timeout.
            while (PeekMessageW(&msg, nullptr, 0, 0, PM_REMOVE | PM_QS_POSTMESSAGE | PM_QS_SENDMESSAGE))
            {
                if (msg.message == WM_QUIT)
                {
                    // Re-post so the outer modal loop still tears down, and stop waiting: the EDT may be shutting
                    // down too, in which case the task never completes.
                    PostQuitMessage((int)msg.wParam);
                    quitting = true;
                    break;
                }
                TranslateMessage(&msg);
                DispatchMessageW(&msg);
            }
            if (quitting) break;
        }
        tlsPumpingEdt = false;
        // After bailing out the task is still pending and will SetEvent when it runs, so leak the handle rather than
        // risk signaling a recycled one.
        if (completed) CloseHandle(doneEvent);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_renderer_WinApiEdtInvoker_signalDone(
        JNIEnv *env, jobject invoker, jlong doneEvent)
    {
        HANDLE ev = fromJavaPointer<HANDLE>(doneEvent);
        if (ev) SetEvent(ev);
    }
}
