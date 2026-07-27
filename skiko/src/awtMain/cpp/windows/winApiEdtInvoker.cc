// Native backing for org.jetbrains.skiko.redrawer.WinApiEdtInvoker.

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
    static void javaEventQueueInvokeLater(JNIEnv *env, jobject runnable)
    {
        static jclass cls = nullptr;
        static jmethodID mid = nullptr;
        if (!mid)
        {
            jclass local = env->FindClass("java/awt/EventQueue");
            cls = (jclass)env->NewGlobalRef(local);
            env->DeleteLocalRef(local);
            mid = env->GetStaticMethodID(cls, "invokeLater", "(Ljava/lang/Runnable;)V");
        }
        if (mid) env->CallStaticVoidMethod(cls, mid, runnable);
        if (env->ExceptionCheck()) { env->ExceptionDescribe(); env->ExceptionClear(); }
    }

    // A Java shim is unavoidable here: JNI cannot fabricate a java.lang.Runnable to post.
    static jobject javaNewEdtInvocationTask(JNIEnv *env, jobject runnable, HANDLE doneEvent)
    {
        static jclass cls = nullptr;
        static jmethodID ctor = nullptr;
        if (!ctor)
        {
            jclass local = env->FindClass("org/jetbrains/skiko/redrawer/EdtInvocationTask");
            cls = (jclass)env->NewGlobalRef(local);
            env->DeleteLocalRef(local);
            ctor = env->GetMethodID(cls, "<init>", "(Ljava/lang/Runnable;J)V");
        }
        jobject task = env->NewObject(cls, ctor, runnable, toJavaPointer(doneEvent));
        if (env->ExceptionCheck()) { env->ExceptionDescribe(); env->ExceptionClear(); }
        return task;
    }

    // The Win32 counterpart of the nested CFRunLoop macOS LWCToolkit.invokeAndWait spins. Which message classes it
    // pumps is the whole design:
    //
    // POSTED as well as SENT, because EDT work makes synchronous cross-thread calls back to this thread that travel
    // as posted messages — showing a window from a Compose recomposition posts focus/IME setup here and blocks the
    // EDT on the reply. A sent-only pump starves those and the EDT deadlocks against us.
    //
    // But never input: we are nested inside the modal move/size loop, which owns the drag's input, and draining it
    // stops the window following the cursor.
    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_WinApiEdtInvoker_invokeAndWaitWhilePumping(
        JNIEnv *env, jobject invoker, jobject runnable)
    {
        HANDLE doneEvent = CreateEventW(nullptr, /*manualReset*/ FALSE, /*initialState*/ FALSE, nullptr);
        if (!doneEvent) return;

        jobject task = javaNewEdtInvocationTask(env, runnable, doneEvent);
        if (!task) { CloseHandle(doneEvent); return; }

        tlsPumpingEdt = true; // before posting: the EDT may send a message back before we reach the loop
        javaEventQueueInvokeLater(env, task);
        env->DeleteLocalRef(task);
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

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_EdtInvocationTask_signalDone(
        JNIEnv *env, jobject task, jlong doneEvent)
    {
        HANDLE ev = fromJavaPointer<HANDLE>(doneEvent);
        if (ev) SetEvent(ev);
    }
}
