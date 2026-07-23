// Native backing for org.jetbrains.skiko.redrawer.EdtInvoker: run a Runnable on the AWT event dispatch thread
// (EDT) from another thread (the Windows toolkit thread that drives a live resize) and block until it
// completes, pumping this thread's sent + posted messages meanwhile (but not its hardware input) so the EDT's
// cross-thread window ops don't deadlock against the wait. The Windows analog of macOS LWCToolkit.invokeAndWait,
// which has no JDK equivalent. See invokeAndWaitWhilePumping below for exactly which messages and why.

#include <Windows.h>
#include "jni_helpers.h"
#include "edtInvoker.h"

// Reentrancy flag for invokeAndWaitWhilePumping: true only while THIS thread is spinning the nested loop below.
// thread_local, so it means "this thread is pump-waiting" regardless of which thread drives the invocation — the
// generic form of the guard a message-dispatching caller needs (see isPumpingEdt in the header).
static thread_local bool tlsPumpingEdt = false;

bool isPumpingEdt() { return tlsPumpingEdt; }

// Safety net for the pump-wait below: the posted EDT task (a live-resize render) finishes in well under this in every
// normal case. Only a pathological block on the EDT — e.g. render code raising a modal dialog, whose own event loop
// won't return until dismissed — exceeds it. On expiry we stop waiting and let the toolkit thread return to its normal
// (full) message loop, which then services that dialog; the abandoned task completes harmlessly later. Set far above
// any real resize frame (tens to low hundreds of ms) so it never trips normal resizing.
static const DWORD kPumpTimeoutMs = 1000;

extern "C"
{
    // Posts `runnable` to the EDT via the static java.awt.EventQueue.invokeLater(Runnable). Caches the class as a
    // global ref alongside the method ID (single lookup), calls it, and clears any pending exception.
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

    // Constructs a new EdtInvocationTask(runnable, doneEvent) — the small Java shim that runs `runnable` on the EDT
    // and then signals `doneEvent`. Class + ctor cached on first use; clears any pending exception (returns null on
    // failure, which the caller already handles).
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

    // Runs `runnable` on the EDT and blocks this thread until it finishes, spinning a nested message loop meanwhile —
    // the Win32 counterpart of the nested CFRunLoop that macOS LWCToolkit.invokeAndWait spins (via doAWTRunLoop). It
    // must service more than SENT messages: EDT work can make synchronous cross-thread calls back to this toolkit
    // thread that travel as POSTED messages — e.g. showing/disposing a window from a Compose recomposition posts
    // focus/IME setup to this thread and blocks the EDT on the reply. A sent-only pump services SetWindowPos
    // marshaling but starves those, and the EDT deadlocks against us.
    //
    // But it must NOT drain hardware input (mouse/keyboard). We block here nested inside Windows' modal move/size
    // loop, which owns the drag's input; removing that input stalls the drag (the window stops following the cursor).
    // Input is a distinct message class (QS_INPUT) from the round-trips we need (QS_POSTMESSAGE / QS_SENDMESSAGE), so
    // we wait on and PeekMessage only the latter two (PM_QS_POSTMESSAGE), leaving the drag's input for the modal loop.
    // SENT messages are dispatched by the PeekMessage call itself regardless of the class filter. While we pump,
    // isPumpingEdt() makes the live-resize WndProc inert, so a re-dispatched SetWindowPos (which SENDs WM_NCCALCSIZE
    // back here) can't re-enter its synchronous render.
    //
    // Backstop: the wait is capped by kPumpTimeoutMs. Nothing normal reaches it, but if the EDT task blocks forever
    // (render code raising a modal dialog, whose loop won't return until dismissed) we back out and let the toolkit
    // thread return to its normal full-input loop, which then services that dialog — so the app degrades to a brief
    // stutter instead of a hard hang. The abandoned task finishes harmlessly later (see the CloseHandle note below).
    //
    // Fully self-contained: creates its own per-call auto-reset event (so independent invocations never
    // cross-signal), wraps `runnable` to signal that event on completion, posts it (EventQueue.invokeLater),
    // pump-waits, and closes the event. The only Java involved is the EdtInvocationTask shim, because JNI
    // cannot fabricate a java.lang.Runnable to post.
    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_EdtInvoker_invokeAndWaitWhilePumping(
        JNIEnv *env, jobject invoker, jobject runnable)
    {
        HANDLE done = CreateEventW(nullptr, /*manualReset*/ FALSE, /*initialState*/ FALSE, nullptr);
        if (!done) return;

        jobject task = javaNewEdtInvocationTask(env, runnable, done);
        if (!task) { CloseHandle(done); return; }

        // Set before posting so that even if the EDT runs the task and posts/sends a message back before we reach the
        // loop, a re-entrant WndProc dispatched from it sees us as busy. Cleared once the round-trip completes.
        tlsPumpingEdt = true;
        javaEventQueueInvokeLater(env, task);
        env->DeleteLocalRef(task);
        bool completed = false;
        ULONGLONG deadline = GetTickCount64() + kPumpTimeoutMs;
        for (;;)
        {
            ULONGLONG now = GetTickCount64();
            DWORD waitMs = now >= deadline ? 0 : (DWORD)(deadline - now);
            // Wake on the task finishing, a cross-thread message to service, or the safety-net deadline. Deliberately
            // no QS_INPUT — see above.
            DWORD r = MsgWaitForMultipleObjectsEx(1, &done, waitMs, QS_SENDMESSAGE | QS_POSTMESSAGE, MWMO_INPUTAVAILABLE);
            if (r == WAIT_OBJECT_0) { completed = true; break; } // task signaled done
            if (r == WAIT_TIMEOUT) break; // safety net expired: back out (see kPumpTimeoutMs); leave `done` for the task
            MSG msg;
            bool quitting = false;
            // PM_QS_POSTMESSAGE restricts removal to posted (app) messages — the drag's hardware input stays in the
            // queue for the modal loop. Any pending SENT messages are still dispatched by this PeekMessage.
            while (PeekMessageW(&msg, nullptr, 0, 0, PM_REMOVE | PM_QS_POSTMESSAGE))
            {
                if (msg.message == WM_QUIT)
                {
                    // Quit arriving mid-pump: re-post so the outer (modal) loop still tears the app down, and stop
                    // pumping — the task may never finish if the EDT is itself shutting down, so we must not wait on it.
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
        // Close the event only on a normal finish. If we bailed early — WM_QUIT, or the safety-net timeout with the
        // EDT still blocked — the task may still be pending and will SetEvent(done) when it eventually runs; leave the
        // handle open (a one-off leak) rather than risk signaling a freed (possibly recycled) handle.
        if (completed) CloseHandle(done);
    }

    // Called by EdtInvocationTask.run() (on the EDT) once the wrapped runnable finished, to release the
    // pump-waiting thread. `doneEvent` is the HANDLE created in invokeAndWaitWhilePumping above.
    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_EdtInvocationTask_signalDone(
        JNIEnv *env, jobject task, jlong doneEvent)
    {
        HANDLE ev = fromJavaPointer<HANDLE>(doneEvent);
        if (ev) SetEvent(ev);
    }
}
