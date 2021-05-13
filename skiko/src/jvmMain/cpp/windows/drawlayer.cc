#define WIN32_LEAN_AND_MEAN
#include <jawt_md.h>
#include <Windows.h>
#include <cassert>
#include "jni_helpers.h"

extern "C"
{
    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_nativeInit(JNIEnv *env, jobject canvas, jlong platformInfoPtr)
    {
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_nativeDispose(JNIEnv *env, jobject canvas)
    {
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_HardwareLayer_getWindowHandle(JNIEnv *env, jobject canvas, jlong platformInfoPtr)
    {
        JAWT_Win32DrawingSurfaceInfo* dsi_win = fromJavaPointer<JAWT_Win32DrawingSurfaceInfo *>(platformInfoPtr);
        HWND ancestor = GetAncestor(dsi_win->hwnd, GA_PARENT);
        assert(ancestor != NULL);
        return (jlong) ancestor;
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_HardwareLayer_getContentHandle(JNIEnv *env, jobject canvas, jlong platformInfoPtr)
    {
        JAWT_Win32DrawingSurfaceInfo* dsi_win = fromJavaPointer<JAWT_Win32DrawingSurfaceInfo *>(platformInfoPtr);
        return (jlong) dsi_win->hwnd;
    }
} // extern "C"