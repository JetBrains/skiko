#define WIN32_LEAN_AND_MEAN
#include <jawt_md.h>
#include <Windows.h>
#include <shellscalingapi.h>
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

    JNIEXPORT jint JNICALL Java_org_jetbrains_skiko_SystemTheme_1awtKt_getCurrentSystemTheme(JNIEnv *env, jobject topLevel)
    {
        auto subkey = L"Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";
        auto name = L"AppsUseLightTheme";
        DWORD result;
        DWORD result_length = sizeof(result);
        auto status = RegGetValueW(
                HKEY_CURRENT_USER,
                subkey,
                name,
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

    JNIEXPORT jint JNICALL Java_org_jetbrains_skiko_HardwareLayer_getCurrentDPI(JNIEnv *env, jobject canvas, jlong platformInfoPtr)
    {
        typedef HRESULT (STDAPICALLTYPE *GDFM)(HMONITOR, MONITOR_DPI_TYPE, UINT*, UINT*);
        static GDFM getDpiForMonitor = nullptr;
        typedef UINT (WINAPI *GDFW)(HWND);
        static GDFW getDpiForWindow = nullptr;

        // Try to dynamically load GetDpiForWindow and GetDpiForMonitor - they are only supported from Windows 10 and 8.1 respectively
        static bool dynamicFunctionsLoaded = false;
        if(!dynamicFunctionsLoaded) {
            HINSTANCE shcoreDll = LoadLibrary("Shcore.dll");
            if(shcoreDll) {
                getDpiForMonitor = reinterpret_cast<GDFM>(GetProcAddress(shcoreDll, "GetDpiForMonitor"));
            }
            
            HINSTANCE user32Dll = LoadLibrary("User32");
            if(user32Dll) {
                getDpiForWindow = reinterpret_cast<GDFW>(GetProcAddress(user32Dll, "GetDpiForWindow"));
            }
            
            dynamicFunctionsLoaded = true;
        }

        HWND hwnd = (HWND)Java_org_jetbrains_skiko_HardwareLayer_getWindowHandle(env, canvas, platformInfoPtr);
        int dpi = 0;
        if(getDpiForMonitor) {
            HMONITOR display = MonitorFromWindow(hwnd, MONITOR_DEFAULTTONEAREST);
            UINT xDpi = 0, yDpi = 0;
            getDpiForMonitor(display, MDT_RAW_DPI, &xDpi, &yDpi);
            dpi = (int)xDpi;
        }
        
        // We can get dpi:0 if we set up multiple displays for content duplication (mirror). 
        if (dpi == 0) {            
            if(getDpiForWindow) {
                // get default system dpi
                dpi = getDpiForWindow(hwnd);
            }
        }
        
        if(dpi == 0) {
            // If failed to get DPI, assume standard 96
            dpi = 96;
        }
        
        return dpi;
    }
} // extern "C"