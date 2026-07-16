#ifdef SK_METAL

#import <jawt.h>
#import <jawt_md.h>

#import <Cocoa/Cocoa.h>
#import <QuartzCore/QuartzCore.h>
#import <Metal/Metal.h>
#import <QuartzCore/CAMetalLayer.h>

#import "ganesh/GrBackendSurface.h"
#import "ganesh/GrDirectContext.h"
#import "ganesh/mtl/GrMtlBackendContext.h"
#import "ganesh/mtl/GrMtlDirectContext.h"
#import "ganesh/mtl/GrMtlTypes.h"

#import "MetalDevice.h"

#include "common/interop.hh"

// Forward-declared so AWTMetalLayer (below) can use them.
static jmethodID getDrawFrameWhileLiveResizingMethodID(JNIEnv *env, jobject redrawer);

extern "C"
{

/// Runs `runnable` on the AWT event dispatch thread and blocks the caller until it completes, via
/// `sun.lwawt.macosx.LWCToolkit.invokeAndWait(Runnable, Component)`. That method spins the AppKit run loop in
/// the special mode while waiting, so a synchronous Java->AppKit call made from `runnable` is serviced instead
/// of deadlocking against the (parked) AppKit main thread — the reason we use it here rather than parking the
/// thread on a coroutine. LWCToolkit lives in a non-exported JDK package, but JNI does not perform module
/// access checks, so no `--add-opens` is required. The class (global ref) and method id are stable for the
/// JVM lifetime and cached; any exception thrown by invokeAndWait is left pending so it propagates to Kotlin.
JNIEXPORT void JNICALL Java_org_jetbrains_skiko_AWTMacOsKt_macOsInvokeOnEventThreadAndWait(
    JNIEnv *env, jobject redrawer, jobject component, jobject runnable)
{
    static jclass lwcToolkitClass = NULL;
    static jmethodID invokeAndWaitMethodID = NULL;
    if (invokeAndWaitMethodID == NULL) {
        jclass localClass = env->FindClass("sun/lwawt/macosx/LWCToolkit");
        if (localClass == NULL) {
            return; // NoClassDefFoundError left pending -> propagates to Kotlin, which logs and skips the frame
        }
        jmethodID mid = env->GetStaticMethodID(
            localClass, "invokeAndWait", "(Ljava/lang/Runnable;Ljava/awt/Component;)V");
        if (mid == NULL) {
            env->DeleteLocalRef(localClass);
            return; // NoSuchMethodError left pending
        }
        lwcToolkitClass = (jclass) env->NewGlobalRef(localClass);
        env->DeleteLocalRef(localClass);
        invokeAndWaitMethodID = mid;
    }
    env->CallStaticVoidMethod(lwcToolkitClass, invokeAndWaitMethodID, runnable, component);
}

} // extern C
#endif
