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

    JNIEXPORT jint JNICALL Java_org_jetbrains_skiko_SystemThemeKt_getCurrentSystemTheme(JNIEnv *env, jobject topLevel)
    {
        auto subkey = L"Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";
        auto name = L"AppsUseLightTheme";
        DWORD result;
        DWORD result_length = sizeof(result);
        auto status = RegGetValueW(
                HKEY_CURRENT_USER,
                subkey.c_str(),
                name.c_str(),
                RRF_RT_DWORD,
                NULL,
                &result,
                &result_length
            );
        switch (status) {
            case ERROR_SUCCESS:
                if (result) {
                    // Light.
                    return 0;
                } else {
                    // Dark.
                    return 1;
                }
             default:
                // Unknown.
                return 2;
         }
    }
} // extern "C"