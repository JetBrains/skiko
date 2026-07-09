// Native backing for org.jetbrains.skiko.redrawer.EdtInvoker: run a Runnable on the AWT event dispatch thread
// (EDT) from another thread (the Windows toolkit thread that drives a live resize) and block until it
// completes, pumping Windows SENT messages meanwhile so the EDT's cross-thread window ops don't deadlock
// against the wait. The Windows analog of macOS LWCToolkit.invokeAndWait, which has no JDK equivalent.

#include <Windows.h>
#include "jni_helpers.h"

extern "C"
{
    // java.awt.EventQueue.invokeLater(runnable) — posts the runnable to the EDT. A static JDK method, so it
    // caches the class as a global ref alongside the method ID (single lookup).
    static void invokeLaterOnAwtEdtThread(JNIEnv *env, jobject runnable)
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

    // Constructs a new EdtInvocationTask(runnable, doneEvent) — the small Java shim that runs `runnable` on the
    // EDT and then signals `doneEvent`. Class + ctor cached on first use.
    static jobject newEdtInvocationTask(JNIEnv *env, jobject runnable, HANDLE doneEvent)
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
        return env->NewObject(cls, ctor, runnable, toJavaPointer(doneEvent));
    }

    // Runs `runnable` on the EDT and blocks this thread until it finishes, servicing cross-thread SENT messages
    // meanwhile so the EDT's window ops marshaled back here (beginValidate/SetWindowPos) complete instead of
    // deadlocking against us.
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

        jobject task = newEdtInvocationTask(env, runnable, done);
        if (task)
        {
            invokeLaterOnAwtEdtThread(env, task);
            env->DeleteLocalRef(task);
            for (;;)
            {
                DWORD r = MsgWaitForMultipleObjectsEx(1, &done, INFINITE, QS_SENDMESSAGE, MWMO_INPUTAVAILABLE);
                if (r == WAIT_OBJECT_0) break; // task signaled done
                MSG msg;
                PeekMessageW(&msg, nullptr, 0, 0, PM_NOREMOVE); // deliver pending sent messages (does not consume input)
            }
        }
        CloseHandle(done);
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
